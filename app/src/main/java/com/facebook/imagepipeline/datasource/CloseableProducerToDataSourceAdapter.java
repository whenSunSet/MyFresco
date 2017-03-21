package com.facebook.imagepipeline.datasource;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.commom.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.util.SettableProducerContext;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * DataSource<CloseableReference<T>> backed by a Producer<CloseableReference<T>>
 *
 * @param <T>
 */
@ThreadSafe
public class CloseableProducerToDataSourceAdapter<T>
        extends AbstractProducerToDataSourceAdapter<CloseableReference<T>> {

    public static <T> DataSource<CloseableReference<T>> create(
            Producer<CloseableReference<T>> producer,
            SettableProducerContext settableProducerContext,
            RequestListener listener) {
        return new CloseableProducerToDataSourceAdapter<T>(
                producer, settableProducerContext, listener);
    }

    private CloseableProducerToDataSourceAdapter(
            Producer<CloseableReference<T>> producer,
            SettableProducerContext settableProducerContext,
            RequestListener listener) {
        super(producer, settableProducerContext, listener);
    }

    @Override
    @Nullable
    public CloseableReference<T> getResult() {
        return CloseableReference.cloneOrNull(super.getResult());
    }

    @Override
    protected void closeResult(CloseableReference<T> result) {
        CloseableReference.closeSafely(result);
    }

    @Override
    protected void onNewResultImpl(CloseableReference<T> result, boolean isLast) {
        super.onNewResultImpl(CloseableReference.cloneOrNull(result), isLast);
    }
}
