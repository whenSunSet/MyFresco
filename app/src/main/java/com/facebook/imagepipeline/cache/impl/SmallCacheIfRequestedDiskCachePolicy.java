package com.facebook.imagepipeline.cache.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import com.facebook.cache.commom.CacheKey;
import com.facebook.imagepipeline.cache.CacheKeyFactory;
import com.facebook.imagepipeline.cache.DiskCachePolicy;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.request.impl.ImageRequest;

import java.util.concurrent.atomic.AtomicBoolean;

import bolts.Task;

/**
 * 一个Task的工厂，这个Task对应一个简单的硬盘缓存，这个缓存试图加载image，该image已经请求了image Request
 * Task factory for the simple disk cache case of attempting to load the image from whichever cache
 * is requested by the image request.
 */
public class SmallCacheIfRequestedDiskCachePolicy implements DiskCachePolicy {

    private final BufferedDiskCache mDefaultBufferedDiskCache;
    private final BufferedDiskCache mSmallImageBufferedDiskCache;
    private final CacheKeyFactory mCacheKeyFactory;

    public SmallCacheIfRequestedDiskCachePolicy(
            BufferedDiskCache defaultBufferedDiskCache,
            BufferedDiskCache smallImageBufferedDiskCache, CacheKeyFactory cacheKeyFactory) {
        mDefaultBufferedDiskCache = defaultBufferedDiskCache;
        mSmallImageBufferedDiskCache = smallImageBufferedDiskCache;
        mCacheKeyFactory = cacheKeyFactory;
    }

    @Override
    public Task<EncodedImage> createAndStartCacheReadTask(
            ImageRequest imageRequest,
            Object callerContext,
            AtomicBoolean isCancelled) {
        final CacheKey cacheKey = mCacheKeyFactory.getEncodedCacheKey(imageRequest, callerContext);
        if (imageRequest.getCacheChoice() == ImageRequest.CacheChoice.SMALL) {
            return mSmallImageBufferedDiskCache.get(cacheKey, isCancelled);
        } else {
            return mDefaultBufferedDiskCache.get(cacheKey, isCancelled);
        }
    }

    @Override
    public void writeToCache(
            EncodedImage newResult,
            ImageRequest imageRequest,
            Object callerContext) {
        final CacheKey cacheKey = mCacheKeyFactory.getEncodedCacheKey(imageRequest, callerContext);

        if (imageRequest.getCacheChoice() == ImageRequest.CacheChoice.SMALL) {
            mSmallImageBufferedDiskCache.put(cacheKey, newResult);
        } else {
            mDefaultBufferedDiskCache.put(cacheKey, newResult);
        }
    }
}
