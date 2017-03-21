package com.facebook.imagepipeline.datasource;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.datasource.DataSource;
import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.util.SettableProducerContext;

import javax.annotation.concurrent.ThreadSafe;

/**
 * DataSource<T> backed by a Producer<T>
 *
 * @param <T>
 */
@ThreadSafe
public class ProducerToDataSourceAdapter<T>
        extends AbstractProducerToDataSourceAdapter<T> {

    public static <T> DataSource<T> create(
            Producer<T> producer,
            SettableProducerContext settableProducerContext,
            RequestListener listener) {
        return new ProducerToDataSourceAdapter<T>(
                producer,
                settableProducerContext,
                listener);
    }

    private ProducerToDataSourceAdapter(
            Producer<T> producer,
            SettableProducerContext settableProducerContext,
            RequestListener listener) {
        super(producer, settableProducerContext, listener);
    }
}
