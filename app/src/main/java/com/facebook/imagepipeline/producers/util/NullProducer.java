package com.facebook.imagepipeline.producers.util;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.ProducerContext;

/**
 * Producer that never produces anything, but just returns null.
 *
 * <p>This producer can be used to terminate a sequence, e.g. for a bitmap cache get only sequence,
 * just use BitmapMemoryCacheGetProducer followed by NullProducer.
 */
public class NullProducer<T> implements Producer<T> {

    /**
     * Start producing results for given context. Provided consumer is notified whenever progress is
     * made (new value is ready or error occurs).
     * @param consumer
     * @param context
     */
    public void produceResults(Consumer<T> consumer, ProducerContext context) {
        consumer.onNewResult((T) null, true);
    }
}
