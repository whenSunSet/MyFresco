package com.facebook.imagepipeline.producers.processProducer;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.facebook.imagepipeline.producers.base.DelegatingConsumer;

/**
 * Remove image transform meta data producer
 *
 * <p>Remove the {@link ImageTransformMetaData} object from the results passed down from the next
 * producer, and adds it to the result that it returns to the consumer.
 */
public class RemoveImageTransformMetaDataProducer
        implements Producer<CloseableReference<PooledByteBuffer>> {
    private final Producer<EncodedImage> mInputProducer;

    public RemoveImageTransformMetaDataProducer(
            Producer<EncodedImage> inputProducer) {
        mInputProducer = inputProducer;
    }

    @Override
    public void produceResults(
            Consumer<CloseableReference<PooledByteBuffer>> consumer,
            ProducerContext context) {
        mInputProducer.produceResults(new RemoveImageTransformMetaDataConsumer(consumer), context);
    }

    private class RemoveImageTransformMetaDataConsumer extends DelegatingConsumer<EncodedImage,
                CloseableReference<PooledByteBuffer>> {

        private RemoveImageTransformMetaDataConsumer(
                Consumer<CloseableReference<PooledByteBuffer>> consumer) {
            super(consumer);
        }

        @Override
        protected void onNewResultImpl(EncodedImage newResult, boolean isLast) {
            CloseableReference<PooledByteBuffer> ret = null;
            try {
                if (EncodedImage.isValid(newResult)) {
                    ret = newResult.getByteBufferRef();
                }
                getConsumer().onNewResult(ret, isLast);
            } finally {
                CloseableReference.closeSafely(ret);
            }
        }
    }
}

