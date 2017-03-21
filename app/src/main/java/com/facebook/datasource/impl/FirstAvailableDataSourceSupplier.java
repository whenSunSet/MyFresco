package com.facebook.datasource.impl;

/**
 * Created by Administrator on 2017/3/13 0013.
 */

import com.facebook.commom.executors.impl.CallerThreadExecutor;
import com.facebook.commom.internal.Objects;
import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.internal.Supplier;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSubscriber;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * 提供一个实现了{@link DataSource}的data source ,该data source 会提取出第一个可以使用的data source
 * 当使用MultiImageRequest,可以使用当前的提供器，所有的DataSource都是由mDataSourceSuppliers提供的
 * mDataSourceSuppliers是匿名内部类，其中实现类get()方法返回ImagePipeline.fetchDecodedImage()方法返回的data source
 * {@link DataSource} supplier that provides a data source which will forward results of the first
 * available data source.
 *
 * <p>Data sources are obtained in order. Only if the current data source fails, or if it finishes
 * without result, the next one will be tried.
 */
@ThreadSafe
public class FirstAvailableDataSourceSupplier<T> implements Supplier<DataSource<T>> {

    private final List<Supplier<DataSource<T>>> mDataSourceSuppliers;

    private FirstAvailableDataSourceSupplier(List<Supplier<DataSource<T>>> dataSourceSuppliers) {
        Preconditions.checkArgument(!dataSourceSuppliers.isEmpty(), "List of suppliers is empty!");
        mDataSourceSuppliers = dataSourceSuppliers;
    }

    public static <T> FirstAvailableDataSourceSupplier<T> create(
            List<Supplier<DataSource<T>>> dataSourceSuppliers) {
        return new FirstAvailableDataSourceSupplier<T>(dataSourceSuppliers);
    }

    @Override
    public DataSource<T> get() {
        return new FirstAvailableDataSource();
    }

    @Override
    public int hashCode() {
        return mDataSourceSuppliers.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof FirstAvailableDataSourceSupplier)) {
            return false;
        }
        FirstAvailableDataSourceSupplier that = (FirstAvailableDataSourceSupplier) other;
        return Objects.equal(this.mDataSourceSuppliers, that.mDataSourceSuppliers);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("list", mDataSourceSuppliers)
                .toString();
    }

    @ThreadSafe
    private class FirstAvailableDataSource extends AbstractDataSource<T> {

        private int mIndex = 0;
        private DataSource<T> mCurrentDataSource = null;
        private DataSource<T> mDataSourceWithResult = null;

        public FirstAvailableDataSource() {
            if (!startNextDataSource()) {
                setFailure(new RuntimeException("No data source supplier or supplier returned null."));
            }
        }

        @Override
        @Nullable
        public synchronized T getResult() {
            DataSource<T> dataSourceWithResult = getDataSourceWithResult();
            return (dataSourceWithResult != null) ? dataSourceWithResult.getResult() : null;
        }

        @Override
        public synchronized boolean hasResult() {
            DataSource<T> dataSourceWithResult = getDataSourceWithResult();
            return (dataSourceWithResult != null) && dataSourceWithResult.hasResult();
        }

        @Override
        public boolean close() {
            DataSource<T> currentDataSource;
            DataSource<T> dataSourceWithResult;
            synchronized (FirstAvailableDataSource.this) {
                // it's fine to call {@code super.close()} within a synchronized block because we don't
                // implement {@link #closeResult()}, but perform result closing ourselves.
                if (!super.close()) {
                    return false;
                }
                currentDataSource = mCurrentDataSource;
                mCurrentDataSource = null;
                dataSourceWithResult = mDataSourceWithResult;
                mDataSourceWithResult = null;
            }
            closeSafely(dataSourceWithResult);
            closeSafely(currentDataSource);
            return true;
        }

        //dataSourceSupplier.get()这里异步获取了dataSource
        private boolean startNextDataSource() {
            Supplier<DataSource<T>> dataSourceSupplier = getNextSupplier();
            DataSource<T> dataSource = (dataSourceSupplier != null) ? dataSourceSupplier.get() : null;
            if (setCurrentDataSource(dataSource) && dataSource != null) {
                dataSource.subscribe(new InternalDataSubscriber(), CallerThreadExecutor.getInstance());
                return true;
            } else {
                closeSafely(dataSource);
                return false;
            }
        }

        @Nullable
        private synchronized Supplier<DataSource<T>> getNextSupplier() {
            if (!isClosed() && mIndex < mDataSourceSuppliers.size()) {
                return mDataSourceSuppliers.get(mIndex++);
            }
            return null;
        }

        private synchronized boolean setCurrentDataSource(DataSource<T> dataSource) {
            if (isClosed()) {
                return false;
            }
            mCurrentDataSource = dataSource;
            return true;
        }

        private synchronized boolean clearCurrentDataSource(DataSource<T> dataSource) {
            if (isClosed() || dataSource != mCurrentDataSource) {
                return false;
            }
            mCurrentDataSource = null;
            return true;
        }

        @Nullable
        private synchronized DataSource<T> getDataSourceWithResult() {
            return mDataSourceWithResult;
        }

        private void maybeSetDataSourceWithResult(
                DataSource<T> dataSource,
                boolean isFinished) {
            DataSource<T> oldDataSource = null;
            synchronized (FirstAvailableDataSource.this) {
                if (dataSource != mCurrentDataSource || dataSource == mDataSourceWithResult) {
                    return;
                }
                // If we didn't have any result so far, we got one now, so we'll set
                // {@code mDataSourceWithResult} to point to the current data source.
                // If we did have a result which came from another data source,
                // we'll only set {@code mDataSourceWithResult} to point to the current
                // data source if it has finished (i.e. the new result is final).
                if (mDataSourceWithResult == null || isFinished) {
                    oldDataSource = mDataSourceWithResult;
                    mDataSourceWithResult = dataSource;
                }
            }
            closeSafely(oldDataSource);
        }

        private void onDataSourceFailed(DataSource<T> dataSource) {
            if (!clearCurrentDataSource(dataSource)) {
                return;
            }
            if (dataSource != getDataSourceWithResult()) {
                closeSafely(dataSource);
            }
            if (!startNextDataSource()) {
                setFailure(dataSource.getFailureCause());
            }
        }

        private void onDataSourceNewResult(DataSource<T> dataSource) {
            maybeSetDataSourceWithResult(dataSource, dataSource.isFinished());
            // If the data source with the new result is our {@code mDataSourceWithResult},
            // we have to notify our subscribers about the new result.
            if (dataSource == getDataSourceWithResult()) {
                setResult(null, dataSource.isFinished());
            }
        }

        private void closeSafely(DataSource<T> dataSource) {
            if (dataSource != null) {
                dataSource.close();
            }
        }

        //内部的观察者，这里会调用FirstAvailableDataSource的相应方法，
        // 然后调用AbstractDataSource的相应方法，最终调用外部的观察者的相应方法
        private class InternalDataSubscriber implements DataSubscriber<T> {

            @Override
            public void onFailure(DataSource<T> dataSource) {
                FirstAvailableDataSource.this.onDataSourceFailed(dataSource);
            }

            @Override
            public void onCancellation(DataSource<T> dataSource) {
            }

            @Override
            public void onNewResult(DataSource<T> dataSource) {
                if (dataSource.hasResult()) {
                    FirstAvailableDataSource.this.onDataSourceNewResult(dataSource);
                } else if (dataSource.isFinished()) {
                    FirstAvailableDataSource.this.onDataSourceFailed(dataSource);
                }
            }

            @Override
            public void onProgressUpdate(DataSource<T> dataSource) {
                float oldProgress = FirstAvailableDataSource.this.getProgress();
                FirstAvailableDataSource.this.setProgress(Math.max(oldProgress, dataSource.getProgress()));
            }
        }
    }
}
