package com.facebook.imagepipeline.cache.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import com.facebook.cache.commom.CacheKey;
import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.logging.FLog;
import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.memory.PooledByteBuffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.concurrent.GuardedBy;

/**
 * 这个类封装了一个Map，这个map的键值对为ImageCacheKeys 和 EncodedImages，这里的EncodedImages
 * 指向了PooledByteBuffers。这里通过SimpleImageCache，来储存缓存，最后被写在硬盘缓存中
 * 这样他们可以返回的平行缓存操作
 * This is class encapsulates Map that maps ImageCacheKeys to EncodedImages pointing to
 * PooledByteBuffers. It is used by SimpleImageCache to store values that are being written
 * to disk cache, so that they can be returned by parallel cache get operations.
 */
public class StagingArea {
    private static final Class<?> TAG = StagingArea.class;

    @GuardedBy("this")
    private Map<CacheKey, EncodedImage> mMap;

    private StagingArea() {
        mMap = new HashMap<>();
    }

    public static StagingArea getInstance() {
        return new StagingArea();
    }

    /**
     * 在这个StagingArea储存key-value，调用这个方法将覆盖前面已经存储的值
     * Stores key-value in this StagingArea. This call overrides previous value
     * of stored reference if
     * @param key
     * @param encodedImage EncodedImage to be associated with key
     */
    public synchronized void put(final CacheKey key, final EncodedImage encodedImage) {
        Preconditions.checkNotNull(key);
        Preconditions.checkArgument(EncodedImage.isValid(encodedImage));

        // we're making a 'copy' of this reference - so duplicate it
        final EncodedImage oldEntry = mMap.put(key, EncodedImage.cloneOrNull(encodedImage));
        EncodedImage.closeSafely(oldEntry);
        logStats();
    }

    /**
     * Removes all items from the StagingArea.
     */
    public void clearAll() {
        final List<EncodedImage> old;
        synchronized (this) {
            old = new ArrayList<>(mMap.values());
            mMap.clear();
        }
        for (int i = 0; i < old.size(); i++) {
            EncodedImage encodedImage = old.get(i);
            if (encodedImage != null) {
                encodedImage.close();
            }
        }
    }

    /**
     * Removes item from the StagingArea.
     * @param key
     * @return true if item was removed
     */
    public boolean remove(final CacheKey key) {
        Preconditions.checkNotNull(key);
        final EncodedImage encodedImage;
        synchronized (this) {
            encodedImage = mMap.remove(key);
        }
        if (encodedImage == null) {
            return false;
        }
        try {
            return encodedImage.isValid();
        } finally {
            encodedImage.close();
        }
    }

    /**
     * 移除键值对
     * Removes key-value from the StagingArea. Both key and value must match.
     * @param key
     * @param encodedImage value corresponding to key
     * @return true if item was removed
     */
    public synchronized boolean remove(final CacheKey key, final EncodedImage encodedImage) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(encodedImage);
        Preconditions.checkArgument(EncodedImage.isValid(encodedImage));

        final EncodedImage oldValue = mMap.get(key);

        if (oldValue == null) {
            return false;
        }

        CloseableReference<PooledByteBuffer> oldRef = oldValue.getByteBufferRef();
        CloseableReference<PooledByteBuffer> ref = encodedImage.getByteBufferRef();
        try {
            if (oldRef == null || ref == null || oldRef.get() != ref.get()) {
                return false;
            }
            mMap.remove(key);
        } finally {
            CloseableReference.closeSafely(ref);
            CloseableReference.closeSafely(oldRef);
            EncodedImage.closeSafely(oldValue);
        }

        logStats();
        return true;
    }

    /**
     * @param key
     * @return value associated with given key or null if no value is associated
     */
    public synchronized EncodedImage get(final CacheKey key) {
        Preconditions.checkNotNull(key);
        EncodedImage storedEncodedImage = mMap.get(key);
        if (storedEncodedImage != null) {
            synchronized (storedEncodedImage) {
                if (!EncodedImage.isValid(storedEncodedImage)) {
                    // Reference is not valid, this means that someone cleared reference while it was still in
                    // use. Log error
                    // TODO: 3697790
                    mMap.remove(key);
                    FLog.w(
                            TAG,
                            "Found closed reference %d for key %s (%d)",
                            System.identityHashCode(storedEncodedImage),
                            key.getUriString(),
                            System.identityHashCode(key));
                    return null;
                }
                storedEncodedImage = EncodedImage.cloneOrNull(storedEncodedImage);
            }
        }
        return storedEncodedImage;
    }

    /**
     * 判断某个Key是否在staging area中存在
     * Determine if an valid entry for the key exists in the staging area.
     */
    public synchronized boolean containsKey(CacheKey key) {
        Preconditions.checkNotNull(key);
        if (!mMap.containsKey(key)) {
            return false;
        }
        EncodedImage storedEncodedImage = mMap.get(key);
        synchronized (storedEncodedImage) {
            if (!EncodedImage.isValid(storedEncodedImage)) {
                // Reference is not valid, this means that someone cleared reference while it was still in
                // use. Log error
                // TODO: 3697790
                mMap.remove(key);
                FLog.w(
                        TAG,
                        "Found closed reference %d for key %s (%d)",
                        System.identityHashCode(storedEncodedImage),
                        key.getUriString(),
                        System.identityHashCode(key));
                return false;
            }
            return true;
        }
    }

    /**
     *
     * Simple 'debug' logging of stats.
     */
    private synchronized void logStats() {
        FLog.v(TAG, "Count = %d", mMap.size());
    }

}
