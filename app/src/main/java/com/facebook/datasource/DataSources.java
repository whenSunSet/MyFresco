package com.facebook.datasource;

/**
 * Created by Administrator on 2017/3/13 0013.
 */

import com.facebook.commom.internal.Supplier;
import com.facebook.datasource.impl.SimpleDataSource;

/**
 * 静态的实用方法对于{@link DataSource}
 * Static utility methods pertaining to the {@link DataSource} interface.
 */
public class DataSources {

    private DataSources() {
    }

    public static <T> DataSource<T> immediateFailedDataSource(Throwable failure) {
        SimpleDataSource<T> simpleDataSource = SimpleDataSource.create();
        simpleDataSource.setFailure(failure);
        return simpleDataSource;
    }

    public static <T> DataSource<T> immediateDataSource(T result) {
        SimpleDataSource<T> simpleDataSource = SimpleDataSource.create();
        simpleDataSource.setResult(result);
        return simpleDataSource;
    }

    public static <T> Supplier<DataSource<T>> getFailedDataSourceSupplier(final Throwable failure) {
        return new Supplier<DataSource<T>>() {
            @Override
            public DataSource<T> get() {
                return DataSources.immediateFailedDataSource(failure);
            }
        };
    }
}

