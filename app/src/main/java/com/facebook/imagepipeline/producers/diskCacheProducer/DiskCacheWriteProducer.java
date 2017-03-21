package com.facebook.imagepipeline.producers.diskCacheProducer;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.imagepipeline.cache.DiskCachePolicy;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.facebook.imagepipeline.producers.base.DelegatingConsumer;
import com.facebook.imagepipeline.request.impl.ImageRequest;

/**
 * 一个硬盘写的producer
 * Disk cache write producer.
 *
 * 这个producer通过其他producer调用，允许硬盘缓存。
 * <p>This producer passes through to the next producer in the sequence, as long as the permitted
 * request level reaches beyond the disk cache. Otherwise this is a passive producer.
 *
 * 在Consumer中处理好之后，传递给下一个Consumer
 * <p>The final result passed to the consumer put into the disk cache as well as being passed on.
 *
 * 这个硬盘缓存producer代理了{@link DiskCachePolicy}
 * <p>Disk cache interactions are delegated to a provided {@link DiskCachePolicy}.
 * 这个producer只有在media variations experiment 开的时候使用，让其他producer处于read and write之间
 * <p>This producer is currently used only if the media variations experiment is turned on, to
 * enable another producer to sit between cache read and write.
 */
public class DiskCacheWriteProducer implements Producer<EncodedImage> {
    private final Producer<EncodedImage> mInputProducer;
    private final DiskCachePolicy mDiskCachePolicy;

    public DiskCacheWriteProducer(
            Producer<EncodedImage> inputProducer,
            DiskCachePolicy diskCachePolicy) {
        mInputProducer = inputProducer;
        mDiskCachePolicy = diskCachePolicy;
    }

    public void produceResults(
            final Consumer<EncodedImage> consumer,
            final ProducerContext producerContext) {
        maybeStartInputProducer(consumer, producerContext);
    }

    private void maybeStartInputProducer(
            Consumer<EncodedImage> consumerOfDiskCacheWriteProducer,
            ProducerContext producerContext) {
        if (producerContext.getLowestPermittedRequestLevel().getValue() >=
                ImageRequest.RequestLevel.DISK_CACHE.getValue()) {
            consumerOfDiskCacheWriteProducer.onNewResult(null, true);
        } else {
            Consumer<EncodedImage> consumer;
            if (producerContext.getImageRequest().isDiskCacheEnabled()) {
                consumer = new DiskCacheWriteConsumer(
                        consumerOfDiskCacheWriteProducer,
                        producerContext,
                        mDiskCachePolicy);
            } else {
                consumer = consumerOfDiskCacheWriteProducer;
            }

            mInputProducer.produceResults(consumer, producerContext);
        }
    }

    /**
     * Consumer that consumes results from next producer in the sequence.
     *
     * <p>The consumer puts the last result received into disk cache, and passes all results (success
     * or failure) down to the next consumer.
     */
    private static class DiskCacheWriteConsumer
            extends DelegatingConsumer<EncodedImage, EncodedImage> {

        private final ProducerContext mProducerContext;
        private final DiskCachePolicy mDiskCachePolicy;

        private DiskCacheWriteConsumer(
                final Consumer<EncodedImage> consumer,
                final ProducerContext producerContext,
                final DiskCachePolicy diskCachePolicy) {
            super(consumer);
            mProducerContext = producerContext;
            mDiskCachePolicy = diskCachePolicy;
        }

        @Override
        public void onNewResultImpl(EncodedImage newResult, boolean isLast) {
            if (newResult != null && isLast) {
                mDiskCachePolicy.writeToCache(
                        newResult,
                        mProducerContext.getImageRequest(),
                        mProducerContext.getCallerContext());
            }

            getConsumer().onNewResult(newResult, isLast);
        }
    }
}
