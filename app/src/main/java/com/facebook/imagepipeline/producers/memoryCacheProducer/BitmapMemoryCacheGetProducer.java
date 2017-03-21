package com.facebook.imagepipeline.producers.memoryCacheProducer;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.cache.commom.CacheKey;
import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.cache.CacheKeyFactory;
import com.facebook.imagepipeline.cache.MemoryCache;
import com.facebook.imagepipeline.image.impl.CloseableImage;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.Producer;

/**
 * Bitmap memory cache producer that is read-only.
 */
public class BitmapMemoryCacheGetProducer extends BitmapMemoryCacheProducer {

    public static final String PRODUCER_NAME = "BitmapMemoryCacheGetProducer";

    public BitmapMemoryCacheGetProducer(
            MemoryCache<CacheKey, CloseableImage> memoryCache,
            CacheKeyFactory cacheKeyFactory,
            Producer<CloseableReference<CloseableImage>> inputProducer) {
        super(memoryCache, cacheKeyFactory, inputProducer);
    }

    @Override
    protected Consumer<CloseableReference<CloseableImage>> wrapConsumer(
            final Consumer<CloseableReference<CloseableImage>> consumer,
            final CacheKey cacheKey) {
        // since this cache is read-only, we can pass our consumer directly to the next producer
        return consumer;
    }

    @Override
    protected String getProducerName() {
        return PRODUCER_NAME;
    }
}
