package com.facebook.imagepipeline.producers.memoryCacheProducer;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.cache.commom.CacheKey;
import com.facebook.commom.internal.ImmutableMap;
import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.cache.CacheKeyFactory;
import com.facebook.imagepipeline.cache.MemoryCache;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.image.impl.CloseableImage;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.facebook.imagepipeline.producers.ProducerListener;
import com.facebook.imagepipeline.producers.base.DelegatingConsumer;
import com.facebook.imagepipeline.producers.util.ProducerConstants;
import com.facebook.imagepipeline.request.impl.ImageRequest;

/**
 * 内存缓存，在ui线程进行，如果找到图片缓存就直接传给consumer返回，否则传给下一个Producer
 * Memory cache producer for the bitmap memory cache.
 */
public class BitmapMemoryCacheProducer implements Producer<CloseableReference<CloseableImage>> {

    public static final String PRODUCER_NAME = "BitmapMemoryCacheProducer";
    public static final String EXTRA_CACHED_VALUE_FOUND = ProducerConstants.EXTRA_CACHED_VALUE_FOUND;

    private final MemoryCache<CacheKey, CloseableImage> mMemoryCache;
    private final CacheKeyFactory mCacheKeyFactory;
    private final Producer<CloseableReference<CloseableImage>> mInputProducer;

    public BitmapMemoryCacheProducer(
            MemoryCache<CacheKey, CloseableImage> memoryCache,
            CacheKeyFactory cacheKeyFactory,
            Producer<CloseableReference<CloseableImage>> inputProducer) {
        mMemoryCache = memoryCache;
        mCacheKeyFactory = cacheKeyFactory;
        mInputProducer = inputProducer;
    }

    @Override
    public void produceResults(
            final Consumer<CloseableReference<CloseableImage>> consumer,
            final ProducerContext producerContext) {

        final ProducerListener listener = producerContext.getListener();
        final String requestId = producerContext.getId();
        listener.onProducerStart(requestId, getProducerName());
        final ImageRequest imageRequest = producerContext.getImageRequest();
        final Object callerContext = producerContext.getCallerContext();
        final CacheKey cacheKey = mCacheKeyFactory.getBitmapCacheKey(imageRequest, callerContext);

        CloseableReference<CloseableImage> cachedReference = mMemoryCache.get(cacheKey);

        if (cachedReference != null) {
            boolean isFinal = cachedReference.get().getQualityInfo().isOfFullQuality();
            if (isFinal) {
                listener.onProducerFinishWithSuccess(
                        requestId,
                        getProducerName(),
                        listener.requiresExtraMap(requestId)
                                ? ImmutableMap.of(EXTRA_CACHED_VALUE_FOUND, "true")
                                : null);
                consumer.onProgressUpdate(1f);
            }
            consumer.onNewResult(cachedReference, isFinal);
            cachedReference.close();
            if (isFinal) {
                return;
            }
        }

        if (producerContext.getLowestPermittedRequestLevel().getValue() >=
                ImageRequest.RequestLevel.BITMAP_MEMORY_CACHE.getValue()) {
            listener.onProducerFinishWithSuccess(
                    requestId,
                    getProducerName(),
                    listener.requiresExtraMap(requestId)
                            ? ImmutableMap.of(EXTRA_CACHED_VALUE_FOUND, "false")
                            : null);
            consumer.onNewResult(null, true);
            return;
        }

        Consumer<CloseableReference<CloseableImage>> wrappedConsumer = wrapConsumer(consumer, cacheKey);
        listener.onProducerFinishWithSuccess(
                requestId,
                getProducerName(),
                listener.requiresExtraMap(requestId)
                        ? ImmutableMap.of(EXTRA_CACHED_VALUE_FOUND, "false")
                        : null);
        mInputProducer.produceResults(wrappedConsumer, producerContext);
    }

    protected Consumer<CloseableReference<CloseableImage>> wrapConsumer(
            final Consumer<CloseableReference<CloseableImage>> consumer,
            final CacheKey cacheKey) {
        return new DelegatingConsumer<
                        CloseableReference<CloseableImage>,
                        CloseableReference<CloseableImage>>(consumer) {
            @Override
            public void onNewResultImpl(CloseableReference<CloseableImage> newResult, boolean isLast) {
                // ignore invalid intermediate results and forward the null result if last
                if (newResult == null) {
                    if (isLast) {
                        getConsumer().onNewResult(null, true);
                    }
                    return;
                }
                // stateful results cannot be cached and are just forwarded
                if (newResult.get().isStateful()) {
                    getConsumer().onNewResult(newResult, isLast);
                    return;
                }
                // if the intermediate result is not of a better quality than the cached result,
                // forward the already cached result and don't cache the new result.
                if (!isLast) {
                    CloseableReference<CloseableImage> currentCachedResult = mMemoryCache.get(cacheKey);
                    if (currentCachedResult != null) {
                        try {
                            QualityInfo newInfo = newResult.get().getQualityInfo();
                            QualityInfo cachedInfo = currentCachedResult.get().getQualityInfo();
                            if (cachedInfo.isOfFullQuality() || cachedInfo.getQuality() >= newInfo.getQuality()) {
                                getConsumer().onNewResult(currentCachedResult, false);
                                return;
                            }
                        } finally {
                            CloseableReference.closeSafely(currentCachedResult);
                        }
                    }
                }
                // cache and forward the new result
                CloseableReference<CloseableImage> newCachedResult =
                        mMemoryCache.cache(cacheKey, newResult);
                try {
                    if (isLast) {
                        getConsumer().onProgressUpdate(1f);
                    }
                    getConsumer().onNewResult(
                            (newCachedResult != null) ? newCachedResult : newResult, isLast);
                } finally {
                    CloseableReference.closeSafely(newCachedResult);
                }
            }
        };
    }

    protected String getProducerName() {
        return PRODUCER_NAME;
    }
}
