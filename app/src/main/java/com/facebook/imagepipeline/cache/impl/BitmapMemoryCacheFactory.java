package com.facebook.imagepipeline.cache.impl;

import com.facebook.cache.commom.CacheKey;
import com.facebook.imagepipeline.cache.ImageCacheStatsTracker;
import com.facebook.imagepipeline.cache.MemoryCache;
import com.facebook.imagepipeline.cache.MemoryCacheTracker;
import com.facebook.imagepipeline.image.impl.CloseableImage;

/**
 * Created by heshixiyang on 2017/3/16.
 */
//一个BitmapMemoryCache的工厂
public class BitmapMemoryCacheFactory {

    public static MemoryCache<CacheKey, CloseableImage> get(
            final CountingMemoryCache<CacheKey, CloseableImage> bitmapCountingMemoryCache,
            final ImageCacheStatsTracker imageCacheStatsTracker) {

        imageCacheStatsTracker.registerBitmapMemoryCache(bitmapCountingMemoryCache);

        MemoryCacheTracker memoryCacheTracker = new MemoryCacheTracker<CacheKey>() {
            @Override
            public void onCacheHit(CacheKey cacheKey) {
                imageCacheStatsTracker.onBitmapCacheHit(cacheKey);
            }

            @Override
            public void onCacheMiss() {
                imageCacheStatsTracker.onBitmapCacheMiss();
            }

            @Override
            public void onCachePut() {
                imageCacheStatsTracker.onBitmapCachePut();
            }
        };

        return new InstrumentedMemoryCache<>(bitmapCountingMemoryCache, memoryCacheTracker);
    }
}

