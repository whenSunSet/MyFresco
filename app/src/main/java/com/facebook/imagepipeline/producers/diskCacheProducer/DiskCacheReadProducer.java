package com.facebook.imagepipeline.producers.diskCacheProducer;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.commom.internal.ImmutableMap;
import com.facebook.commom.internal.VisibleForTesting;
import com.facebook.imagepipeline.cache.DiskCachePolicy;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.facebook.imagepipeline.producers.ProducerListener;
import com.facebook.imagepipeline.producers.base.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.util.ProducerConstants;
import com.facebook.imagepipeline.request.impl.ImageRequest;

import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import bolts.Continuation;
import bolts.Task;

/**
 * 一个提供硬盘读取的producer
 * Disk cache read producer.
 *
 * 这个producer在硬盘缓存中查询被请求的image，如果找到了，那么就将其传入consumer。如果没有找到
 * 那么就调用下一个producer。只要有任何结果就返回相应的consumer。
 *
 * 这个是一个代理硬盘缓存，实际操作的类是{@link DiskCachePolicy}
 *
 * 这个producer只有media variations experiment 内打开的时候才被使用
 * 让其他producer处于read and write之间
 * <p>This producer looks in the disk cache for the requested image. If the image is found, then it
 * is passed to the consumer. If the image is not found, then the request is passed to the next
 * producer in the sequence. Any results that the producer returns are passed to the consumer.
 *
 * <p>This implementation delegates disk cache interactions to a provided {@link DiskCachePolicy}.
 *
 * <p>This producer is currently used only if the media variations experiment is turned on, to
 * enable another producer to sit between cache read and write.
 */
public class DiskCacheReadProducer implements Producer<EncodedImage> {
    public static final String PRODUCER_NAME = "DiskCacheProducer";
    public static final String EXTRA_CACHED_VALUE_FOUND = ProducerConstants.EXTRA_CACHED_VALUE_FOUND;

    private final Producer<EncodedImage> mInputProducer;
    private final DiskCachePolicy mDiskCachePolicy;

    public DiskCacheReadProducer(
            Producer<EncodedImage> inputProducer,
            DiskCachePolicy diskCachePolicy) {
        mInputProducer = inputProducer;
        mDiskCachePolicy = diskCachePolicy;
    }

    public void produceResults(
            final Consumer<EncodedImage> consumer,
            final ProducerContext producerContext) {
        final ImageRequest imageRequest = producerContext.getImageRequest();
        if (!imageRequest.isDiskCacheEnabled()) {
            maybeStartInputProducer(consumer, producerContext);
            return;
        }

        producerContext.getListener().onProducerStart(producerContext.getId(), PRODUCER_NAME);

        final AtomicBoolean isCancelled = new AtomicBoolean(false);
        Task<EncodedImage> diskLookupTask = mDiskCachePolicy
                .createAndStartCacheReadTask(imageRequest, producerContext.getCallerContext(), isCancelled);
        Continuation<EncodedImage, Void> continuation = onFinishDiskReads(consumer, producerContext);
        diskLookupTask.continueWith(continuation);
        subscribeTaskForRequestCancellation(isCancelled, producerContext);
    }

    private Continuation<EncodedImage, Void> onFinishDiskReads(
            final Consumer<EncodedImage> consumer,
            final ProducerContext producerContext) {
        final String requestId = producerContext.getId();
        final ProducerListener listener = producerContext.getListener();
        return new Continuation<EncodedImage, Void>() {
            @Override
            public Void then(Task<EncodedImage> task)
                    throws Exception {
                if (isTaskCancelled(task)) {
                    listener.onProducerFinishWithCancellation(requestId, PRODUCER_NAME, null);
                    consumer.onCancellation();
                } else if (task.isFaulted()) {
                    listener.onProducerFinishWithFailure(requestId, PRODUCER_NAME, task.getError(), null);
                    mInputProducer.produceResults(consumer, producerContext);
                } else {
                    EncodedImage cachedReference = task.getResult();
                    if (cachedReference != null) {
                        listener.onProducerFinishWithSuccess(
                                requestId,
                                PRODUCER_NAME,
                                getExtraMap(listener, requestId, true));
                        consumer.onProgressUpdate(1);
                        consumer.onNewResult(cachedReference, true);
                        cachedReference.close();
                    } else {
                        listener.onProducerFinishWithSuccess(
                                requestId,
                                PRODUCER_NAME,
                                getExtraMap(listener, requestId, false));
                        mInputProducer.produceResults(consumer, producerContext);
                    }
                }
                return null;
            }
        };
    }

    private static boolean isTaskCancelled(Task<?> task) {
        return task.isCancelled() ||
                (task.isFaulted() && task.getError() instanceof CancellationException);
    }

    private void maybeStartInputProducer(
            Consumer<EncodedImage> consumer,
            ProducerContext producerContext) {
        if (producerContext.getLowestPermittedRequestLevel().getValue() >=
                ImageRequest.RequestLevel.DISK_CACHE.getValue()) {
            consumer.onNewResult(null, true);
            return;
        }

        mInputProducer.produceResults(consumer, producerContext);
    }

    @VisibleForTesting
    static Map<String, String> getExtraMap(
            final ProducerListener listener,
            final String requestId,
            final boolean valueFound) {
        if (!listener.requiresExtraMap(requestId)) {
            return null;
        }
        return ImmutableMap.of(EXTRA_CACHED_VALUE_FOUND, String.valueOf(valueFound));
    }

    private void subscribeTaskForRequestCancellation(
            final AtomicBoolean isCancelled,
            ProducerContext producerContext) {
        producerContext.addCallbacks(
                new BaseProducerContextCallbacks() {
                    @Override
                    public void onCancellationRequested() {
                        isCancelled.set(true);
                    }
                });
    }
}
