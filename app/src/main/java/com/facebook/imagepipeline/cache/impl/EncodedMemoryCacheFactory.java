package com.facebook.imagepipeline.cache.impl;

import com.facebook.cache.commom.CacheKey;
import com.facebook.imagepipeline.cache.ImageCacheStatsTracker;
import com.facebook.imagepipeline.cache.MemoryCache;
import com.facebook.imagepipeline.cache.MemoryCacheTracker;
import com.facebook.imagepipeline.memory.PooledByteBuffer;

/**
 * Created by heshixiyang on 2017/3/16.
 */
//一个EncodedMemoryCache工厂
public class EncodedMemoryCacheFactory {

    public static MemoryCache<CacheKey, PooledByteBuffer> get(
            final CountingMemoryCache<CacheKey, PooledByteBuffer> encodedCountingMemoryCache,
            final ImageCacheStatsTracker imageCacheStatsTracker) {

        imageCacheStatsTracker.registerEncodedMemoryCache(encodedCountingMemoryCache);

        MemoryCacheTracker memoryCacheTracker = new MemoryCacheTracker<CacheKey>() {
            @Override
            public void onCacheHit(CacheKey cacheKey) {
                imageCacheStatsTracker.onMemoryCacheHit(cacheKey);
            }

            @Override
            public void onCacheMiss() {
                imageCacheStatsTracker.onMemoryCacheMiss();
            }

            @Override
            public void onCachePut() {
                imageCacheStatsTracker.onMemoryCachePut();
            }
        };

        return new InstrumentedMemoryCache<>(encodedCountingMemoryCache, memoryCacheTracker);
    }
}

