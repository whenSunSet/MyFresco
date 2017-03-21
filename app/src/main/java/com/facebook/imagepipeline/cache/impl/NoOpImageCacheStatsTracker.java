package com.facebook.imagepipeline.cache.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import com.facebook.cache.commom.CacheKey;
import com.facebook.imagepipeline.cache.ImageCacheStatsTracker;

/**
 *
 * Class that does no stats tracking at all
 */
public class NoOpImageCacheStatsTracker implements ImageCacheStatsTracker {
    private static NoOpImageCacheStatsTracker sInstance = null;

    private NoOpImageCacheStatsTracker() {
    }

    public static synchronized NoOpImageCacheStatsTracker getInstance() {
        if (sInstance == null) {
            sInstance = new NoOpImageCacheStatsTracker();
        }
        return sInstance;
    }

    @Override
    public void onBitmapCachePut() {
    }

    @Override
    public void onBitmapCacheHit(CacheKey cacheKey) {
    }

    @Override
    public void onBitmapCacheMiss() {
    }

    @Override
    public void onMemoryCachePut() {
    }

    @Override
    public void onMemoryCacheHit(CacheKey cacheKey) {
    }

    @Override
    public void onMemoryCacheMiss() {
    }

    @Override
    public void onStagingAreaHit(CacheKey cacheKey) {
    }

    @Override
    public void onStagingAreaMiss() {
    }

    @Override
    public void onDiskCacheHit() {
    }

    @Override
    public void onDiskCacheMiss() {
    }

    @Override
    public void onDiskCacheGetFail() {
    }

    @Override
    public void registerBitmapMemoryCache(CountingMemoryCache<?, ?> bitmapMemoryCache) {
    }

    @Override
    public void registerEncodedMemoryCache(CountingMemoryCache<?, ?> encodedMemoryCache) {
    }
}

