package com.facebook.imagepipeline.producers.util;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.facebook.imagepipeline.producers.base.DelegatingConsumer;

/**
 * Swallow result producer.
 *
 * <p>This producer just inserts a consumer that swallows results into the stack of consumers.
 */
public class SwallowResultProducer<T> implements Producer<Void> {
    private final Producer<T> mInputProducer;

    public SwallowResultProducer(Producer<T> inputProducer) {
        mInputProducer = inputProducer;
    }

    @Override
    public void produceResults(Consumer<Void> consumer, ProducerContext producerContext) {
        DelegatingConsumer<T, Void> swallowResultConsumer = new DelegatingConsumer<T, Void>(consumer) {
            @Override
            protected void onNewResultImpl(T newResult, boolean isLast) {
                if (isLast) {
                    getConsumer().onNewResult(null, isLast);
                }
            }
        };
        mInputProducer.produceResults(swallowResultConsumer, producerContext);
    }
}

