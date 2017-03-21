package com.facebook.imagepipeline.animated.impl;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import android.net.Uri;

import com.facebook.cache.commom.CacheKey;
import com.facebook.commom.internal.Objects;
import com.facebook.commom.internal.VisibleForTesting;
import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.cache.impl.CountingMemoryCache;
import com.facebook.imagepipeline.image.impl.CloseableImage;

import java.util.Iterator;
import java.util.LinkedHashSet;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * 被一个image memory cache 包装为了缓存一个animated image
 * Facade to the image memory cache for frames of an animated image.
 * 每一个animated image应该有其自己的实
 * <p> Each animated image should have its own instance of this class.
 */
public class AnimatedFrameCache {

    @VisibleForTesting
    static class FrameKey implements CacheKey {

        private final CacheKey mImageCacheKey;
        private final int mFrameIndex;

        public FrameKey(CacheKey imageCacheKey, int frameIndex) {
            mImageCacheKey = imageCacheKey;
            mFrameIndex = frameIndex;
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this)
                    .add("imageCacheKey", mImageCacheKey)
                    .add("frameIndex", mFrameIndex)
                    .toString();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof FrameKey) {
                FrameKey that = (FrameKey) o;
                return this.mImageCacheKey == that.mImageCacheKey &&
                        this.mFrameIndex == that.mFrameIndex;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return mImageCacheKey.hashCode() * 1013 + mFrameIndex;
        }

        @Override
        public boolean containsUri(Uri uri) {
            return mImageCacheKey.containsUri(uri);
        }

        @Override
        public String getUriString() {
            return null;
        }
    }

    private final CacheKey mImageCacheKey;
    private final CountingMemoryCache<CacheKey, CloseableImage> mBackingCache;
    private final CountingMemoryCache.EntryStateObserver<CacheKey> mEntryStateObserver;
    //这个是用来储存可以重用的CacheKey
    @GuardedBy("this")
    private final LinkedHashSet<CacheKey> mFreeItemsPool;

    public AnimatedFrameCache(
            CacheKey imageCacheKey,
            final CountingMemoryCache<CacheKey, CloseableImage> backingCache) {
        mImageCacheKey = imageCacheKey;
        mBackingCache = backingCache;
        mFreeItemsPool = new LinkedHashSet<>();
        mEntryStateObserver = new CountingMemoryCache.EntryStateObserver<CacheKey>() {
            @Override
            public void onExclusivityChanged(CacheKey key, boolean isExclusive) {
                AnimatedFrameCache.this.onReusabilityChange(key, isExclusive);
            }
        };
    }

    public synchronized void onReusabilityChange(CacheKey key, boolean isReusable) {
        if (isReusable) {
            mFreeItemsPool.add(key);
        } else {
            mFreeItemsPool.remove(key);
        }
    }

    /**
     * 缓存某个给定的帧
     * Caches the image for the given frame index.
     *
     * 重要：客户端需要使用返回的reference，代替起源的reference，调用者有责任关闭返回的reference，
     * 一旦该reference没有用了
     * <p> Important: the client should use the returned reference instead of the original one.
     * It is the caller's responsibility to close the returned reference once not needed anymore.
     *
     * @return the new reference to be used, null if the value cannot be cached
     */
    @Nullable
    public CloseableReference<CloseableImage> cache(
            int frameIndex,
            CloseableReference<CloseableImage> imageRef) {
        return mBackingCache.cache(keyFor(frameIndex), imageRef, mEntryStateObserver);
    }

    /**
     * 获取image通过给定的帧数
     * Gets the image for the given frame index.
     *调用者有责任关闭返回的reference，一旦该reference没有用了
     * <p> It is the caller's responsibility to close the returned reference once not needed anymore.
     */
    @Nullable
    public CloseableReference<CloseableImage> get(int frameIndex) {
        return mBackingCache.get(keyFor(frameIndex));
    }

    /**
     * 获取重用的image，如果没有这个image就返回null
     * Gets the image to be reused, or null if there is no such image.
     * 返回的image是最近最少使用的image,没有更多的客户引用,它还没有被赶出缓存。
     * <p> The returned image is the least recently used image that has no more clients referencing
     * it, and it has not yet been evicted from the cache.
     * 客户端可以自由修改返回的image的bitmap,可以重新缓存一遍，并且没有任何限制。
     * <p> The client can freely modify the bitmap of the returned image and can cache it again
     * without any restrictions.
     */
    @Nullable
    public CloseableReference<CloseableImage> getForReuse() {
        while (true) {
            CacheKey key = popFirstFreeItemKey();
            if (key == null)  {
                return null;
            }
            CloseableReference<CloseableImage> imageRef = mBackingCache.reuse(key);
            if (imageRef != null) {
                return imageRef;
            }
        }
    }

    @Nullable
    private synchronized CacheKey popFirstFreeItemKey() {
        CacheKey cacheKey = null;
        Iterator<CacheKey> iterator = mFreeItemsPool.iterator();
        if (iterator.hasNext()) {
            cacheKey = iterator.next();
            iterator.remove();
        }
        return cacheKey;
    }

    private FrameKey keyFor(int frameIndex)   {
        return new FrameKey(mImageCacheKey, frameIndex);
    }
}
