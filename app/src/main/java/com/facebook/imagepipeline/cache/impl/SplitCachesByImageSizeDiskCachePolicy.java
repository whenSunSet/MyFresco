package com.facebook.imagepipeline.cache.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import com.facebook.cache.commom.CacheKey;
import com.facebook.imagepipeline.cache.CacheKeyFactory;
import com.facebook.imagepipeline.cache.DiskCachePolicy;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.request.impl.ImageRequest;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicBoolean;

import bolts.Continuation;
import bolts.Task;

/**
 * 这是一个试图加载main或者small硬盘缓存的Task工厂，如果该任务失败了，那么就回滚到前面一个任务
 * Task factory to attempt to load an image from either the main or small disk cache and then
 * fallback to the other if the first attempt was unsuccessful.
 */
public class SplitCachesByImageSizeDiskCachePolicy implements DiskCachePolicy {

    private final BufferedDiskCache mDefaultBufferedDiskCache;
    private final BufferedDiskCache mSmallImageBufferedDiskCache;
    private final CacheKeyFactory mCacheKeyFactory;
    private final int mForceSmallCacheThresholdBytes;

    public SplitCachesByImageSizeDiskCachePolicy(
            BufferedDiskCache defaultBufferedDiskCache,
            BufferedDiskCache smallImageBufferedDiskCache,
            CacheKeyFactory cacheKeyFactory,
            int forceSmallCacheThresholdBytes) {
        mDefaultBufferedDiskCache = defaultBufferedDiskCache;
        mSmallImageBufferedDiskCache = smallImageBufferedDiskCache;
        mCacheKeyFactory = cacheKeyFactory;
        mForceSmallCacheThresholdBytes = forceSmallCacheThresholdBytes;
    }

    @Override
    public Task<EncodedImage> createAndStartCacheReadTask(
            ImageRequest imageRequest,
            Object callerContext,
            final AtomicBoolean isCancelled) {
        final CacheKey cacheKey = mCacheKeyFactory.getEncodedCacheKey(imageRequest, callerContext);
        final boolean alreadyInSmall = mSmallImageBufferedDiskCache.containsSync(cacheKey);
        final boolean alreadyInMain = mDefaultBufferedDiskCache.containsSync(cacheKey);
        final BufferedDiskCache firstCache;
        final BufferedDiskCache secondCache;
        if (alreadyInSmall || !alreadyInMain) {
            firstCache = mSmallImageBufferedDiskCache;
            secondCache = mDefaultBufferedDiskCache;
        } else {
            firstCache = mDefaultBufferedDiskCache;
            secondCache = mSmallImageBufferedDiskCache;
        }
        return firstCache.get(cacheKey, isCancelled)
                .continueWithTask(
                        new Continuation<EncodedImage, Task<EncodedImage>>() {
                            @Override
                            public Task<EncodedImage> then(Task<EncodedImage> task) throws Exception {
                                if (isTaskCancelled(task) || (!task.isFaulted() && task.getResult() != null)) {
                                    return task;
                                }
                                return secondCache.get(cacheKey, isCancelled);
                            }
                        });
    }

    @Override
    public void writeToCache(
            EncodedImage newResult,
            ImageRequest imageRequest,
            Object callerContext) {
        final CacheKey cacheKey = mCacheKeyFactory.getEncodedCacheKey(imageRequest, callerContext);

        int size = newResult.getSize();
        if (size > 0 && size < mForceSmallCacheThresholdBytes) {
            mSmallImageBufferedDiskCache.put(cacheKey, newResult);
        } else {
            mDefaultBufferedDiskCache.put(cacheKey, newResult);
        }
    }

    private static boolean isTaskCancelled(Task<?> task) {
        return task.isCancelled() ||
                (task.isFaulted() && task.getError() instanceof CancellationException);
    }
}
