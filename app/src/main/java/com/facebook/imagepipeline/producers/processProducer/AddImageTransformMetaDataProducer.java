package com.facebook.imagepipeline.producers.processProducer;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.facebook.imagepipeline.producers.base.DelegatingConsumer;

/**
 * 添加一个转化image元数据的producer
 * Add image transform meta data producer
 * 从results中提取元数据，并传递给下一个producer，并将其添加到从consumer中返回的结果中
 * <p>Extracts meta data from the results passed down from the next producer, and adds it to the
 * result that it returns to the consumer.
 */

public class AddImageTransformMetaDataProducer implements Producer<EncodedImage> {
    private final Producer<EncodedImage> mInputProducer;

    public AddImageTransformMetaDataProducer(Producer<EncodedImage> inputProducer) {
        mInputProducer = inputProducer;
    }

    @Override
    public void produceResults(Consumer<EncodedImage> consumer, ProducerContext context) {
        mInputProducer.produceResults(new AddImageTransformMetaDataConsumer(consumer), context);
    }

    private static class AddImageTransformMetaDataConsumer extends DelegatingConsumer<
                EncodedImage, EncodedImage> {

        private AddImageTransformMetaDataConsumer(Consumer<EncodedImage> consumer) {
            super(consumer);
        }

        @Override
        protected void onNewResultImpl(EncodedImage newResult, boolean isLast) {
            if (newResult == null) {
                getConsumer().onNewResult(null, isLast);
                return;
            }
            if (!EncodedImage.isMetaDataAvailable(newResult)) {
                newResult.parseMetaData();
            }
            getConsumer().onNewResult(newResult, isLast);
        }
    }
}

