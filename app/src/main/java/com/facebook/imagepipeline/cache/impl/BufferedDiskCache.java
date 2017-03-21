package com.facebook.imagepipeline.cache.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import com.facebook.binaryresource.BinaryResource;
import com.facebook.cache.commom.CacheKey;
import com.facebook.cache.commom.WriterCallback;
import com.facebook.cache.disk.FileCache;
import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.logging.FLog;
import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.cache.ImageCacheStatsTracker;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.memory.PooledByteBufferFactory;
import com.facebook.imagepipeline.memory.impl.PooledByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import bolts.Task;

/**
 * BufferedDiskCache提供了获取和放置的操作对于调度 硬盘缓存 的读和写
 * BufferedDiskCache provides get and put operations to take care of scheduling disk-cache
 * read/writes.
 */
public class BufferedDiskCache {
    private static final Class<?> TAG = BufferedDiskCache.class;

    private final FileCache mFileCache;
    private final PooledByteBufferFactory mPooledByteBufferFactory;
    private final PooledByteStreams mPooledByteStreams;
    private final Executor mReadExecutor;
    private final Executor mWriteExecutor;
    private final StagingArea mStagingArea;
    private final ImageCacheStatsTracker mImageCacheStatsTracker;

    public BufferedDiskCache(
            FileCache fileCache,
            PooledByteBufferFactory pooledByteBufferFactory,
            PooledByteStreams pooledByteStreams,
            Executor readExecutor,
            Executor writeExecutor,
            ImageCacheStatsTracker imageCacheStatsTracker) {
        mFileCache = fileCache;
        mPooledByteBufferFactory = pooledByteBufferFactory;
        mPooledByteStreams = pooledByteStreams;
        mReadExecutor = readExecutor;
        mWriteExecutor = writeExecutor;
        mImageCacheStatsTracker = imageCacheStatsTracker;
        mStagingArea = StagingArea.getInstance();
    }

    /**
     * 返回该Cache Key是否在内存中
     * Returns true if the key is in the in-memory key index.
     *
     * 不能保证是正确的，因为可能会有缓存的导致返回false，但是如果返回true，那么就移动存在
     * Not guaranteed to be correct. The cache may yet have this key even if this returns false.
     * But if it returns true, it definitely has it.
     *
     * 避免磁盘读取
     * Avoids a disk read.
     */
    public boolean containsSync(CacheKey key) {
        return mStagingArea.containsKey(key) || mFileCache.hasKeySync(key);
    }

    /**
     * 执行一个键值对，在硬盘缓存中进行查找，如果在staging area中没有找到值，那么在后台线程中查看硬盘缓存
     * Performs a key-value look up in the disk cache. If no value is found in the staging area,
     * then disk cache checks are scheduled on a background thread. Any error manifests itself as a
     * cache miss, i.e. the returned Task resolves to false.
     * @param key
     * @return Task that resolves to true if an element is found, or false otherwise
     */
    public Task<Boolean> contains(final CacheKey key) {
        if (containsSync(key)) {
            return Task.forResult(true);
        }
        return containsAsync(key);
    }

    private Task<Boolean> containsAsync(final CacheKey key) {
        try {
            return Task.call(
                    new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return checkInStagingAreaAndFileCache(key);
                        }
                    },
                    mReadExecutor);
        } catch (Exception exception) {
            // Log failure
            // TODO: 3697790
            FLog.w(
                    TAG,
                    exception,
                    "Failed to schedule disk-cache read for %s",
                    key.getUriString());
            return Task.forError(exception);
        }
    }

    /**
     * 同步地执行硬盘缓存检查
     * Performs disk cache check synchronously.
     * @param key
     * @return true if the key is found in disk cache else false
     */
    public boolean diskCheckSync(final CacheKey key) {
        if (containsSync(key)) {
            return true;
        }
        return checkInStagingAreaAndFileCache(key);
    }

    /**
     * Performs key-value look up in disk cache. If value is not found in disk cache staging area
     * then disk cache read is scheduled on background thread. Any error manifests itself as
     * cache miss, i.e. the returned task resolves to null.
     * @param key
     * @return Task that resolves to cached element or null if one cannot be retrieved;
     *   returned task never rethrows any exception
     */
    public Task<EncodedImage> get(CacheKey key, AtomicBoolean isCancelled) {
        final EncodedImage pinnedImage = mStagingArea.get(key);
        if (pinnedImage != null) {
            return foundPinnedImage(key, pinnedImage);
        }
        return getAsync(key, isCancelled);
    }

    /**
     * 在文件缓存中查看key-value，如果没有就返回false
     * Performs key-value loop up in staging area and file cache.
     * Any error manifests itself as a miss, i.e. returns false.
     * @param key
     * @return true if the image is found in staging area or File cache, false if not found
     */
    private boolean checkInStagingAreaAndFileCache(final CacheKey key) {
        EncodedImage result = mStagingArea.get(key);
        if (result != null) {
            result.close();
            FLog.v(TAG, "Found image for %s in staging area", key.getUriString());
            mImageCacheStatsTracker.onStagingAreaHit(key);
            return true;
        } else {
            FLog.v(TAG, "Did not find image for %s in staging area", key.getUriString());
            mImageCacheStatsTracker.onStagingAreaMiss();
            try {
                return mFileCache.hasKey(key);
            } catch (Exception exception) {
                return false;
            }
        }
    }

    private Task<EncodedImage> getAsync(final CacheKey key, final AtomicBoolean isCancelled) {
        try {
            return Task.call(
                    new Callable<EncodedImage>() {
                        @Override
                        public EncodedImage call()
                                throws Exception {
                            if (isCancelled.get()) {
                                throw new CancellationException();
                            }
                            EncodedImage result = mStagingArea.get(key);
                            if (result != null) {
                                FLog.v(TAG, "Found image for %s in staging area", key.getUriString());
                                mImageCacheStatsTracker.onStagingAreaHit(key);
                                result.setEncodedCacheKey(key);
                            } else {
                                FLog.v(TAG, "Did not find image for %s in staging area", key.getUriString());
                                mImageCacheStatsTracker.onStagingAreaMiss();

                                try {
                                    final PooledByteBuffer buffer = readFromDiskCache(key);
                                    CloseableReference<PooledByteBuffer> ref = CloseableReference.of(buffer);
                                    try {
                                        result = new EncodedImage(ref);
                                        result.setEncodedCacheKey(key);
                                    } finally {
                                        CloseableReference.closeSafely(ref);
                                    }
                                } catch (Exception exception) {
                                    return null;
                                }
                            }

                            if (Thread.interrupted()) {
                                FLog.v(TAG, "Host thread was interrupted, decreasing reference count");
                                if (result != null) {
                                    result.close();
                                }
                                throw new InterruptedException();
                            } else {
                                return result;
                            }
                        }
                    },
                    mReadExecutor);
        } catch (Exception exception) {
            // Log failure
            // TODO: 3697790
            FLog.w(
                    TAG,
                    exception,
                    "Failed to schedule disk-cache read for %s",
                    key.getUriString());
            return Task.forError(exception);
        }
    }

    /**
     * 将已经编码的image在硬盘缓存中与被给予的key连接起来，硬盘写在后台线程中，所以这个线程的调用者不需要锁
     * Associates encodedImage with given key in disk cache. Disk write is performed on background
     * thread, so the caller of this method is not blocked
     */
    public void put(
            final CacheKey key,
            EncodedImage encodedImage) {
        Preconditions.checkNotNull(key);
        Preconditions.checkArgument(EncodedImage.isValid(encodedImage));

        //将encodedImage储存在staging area中
        // Store encodedImage in staging area
        mStagingArea.put(key, encodedImage);
        encodedImage.setEncodedCacheKey(key);

        //写入硬盘缓存。这将在后台线程中执行，所以增减引用计数，当这个写已经完成了，我们会将引用技术减少
        // Write to disk cache. This will be executed on background thread, so increment the ref count.
        // When this write completes (with success/failure), then we will bump down the ref count
        // again.
        final EncodedImage finalEncodedImage = EncodedImage.cloneOrNull(encodedImage);
        try {
            mWriteExecutor.execute(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                writeToDiskCache(key, finalEncodedImage);
                            } finally {
                                mStagingArea.remove(key, finalEncodedImage);
                                EncodedImage.closeSafely(finalEncodedImage);
                            }
                        }
                    });
        } catch (Exception exception) {
            //我们未能将缓存写入
            // We failed to enqueue cache write. Log failure and decrement ref count
            // TODO: 3697790
            FLog.w(
                    TAG,
                    exception,
                    "Failed to schedule disk-cache write for %s",
                    key.getUriString());
            mStagingArea.remove(key, encodedImage);
            EncodedImage.closeSafely(finalEncodedImage);
        }
    }

    /**
     * 将这个item从硬盘缓存和staging area删除
     * Removes the item from the disk cache and the staging area.
     */
    public Task<Void> remove(final CacheKey key) {
        Preconditions.checkNotNull(key);
        mStagingArea.remove(key);
        try {
            return Task.call(
                    new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            mStagingArea.remove(key);
                            mFileCache.remove(key);
                            return null;
                        }
                    },
                    mWriteExecutor);
        } catch (Exception exception) {
            // Log failure
            // TODO: 3697790
            FLog.w(TAG, exception, "Failed to schedule disk-cache remove for %s", key.getUriString());
            return Task.forError(exception);
        }
    }

    /**
     * 清除所有
     * Clears the disk cache and the staging area.
     */
    public Task<Void> clearAll() {
        mStagingArea.clearAll();
        try {
            return Task.call(
                    new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            mStagingArea.clearAll();
                            mFileCache.clearAll();
                            return null;
                        }
                    },
                    mWriteExecutor);
        } catch (Exception exception) {
            // Log failure
            // TODO: 3697790
            FLog.w(TAG, exception, "Failed to schedule disk-cache clear");
            return Task.forError(exception);
        }
    }

    private Task<EncodedImage> foundPinnedImage(CacheKey key, EncodedImage pinnedImage) {
        FLog.v(TAG, "Found image for %s in staging area", key.getUriString());
        mImageCacheStatsTracker.onStagingAreaHit(key);
        return Task.forResult(pinnedImage);
    }

    /**
     * 执行磁盘缓存读取，产生了任何错误就返回null
     * Performs disk cache read. In case of any exception null is returned.
     */
    private PooledByteBuffer readFromDiskCache(final CacheKey key) throws IOException {
        try {
            FLog.v(TAG, "Disk cache read for %s", key.getUriString());

            final BinaryResource diskCacheResource = mFileCache.getResource(key);
            if (diskCacheResource == null) {
                FLog.v(TAG, "Disk cache miss for %s", key.getUriString());
                mImageCacheStatsTracker.onDiskCacheMiss();
                return null;
            } else {
                FLog.v(TAG, "Found entry in disk cache for %s", key.getUriString());
                mImageCacheStatsTracker.onDiskCacheHit();
            }

            PooledByteBuffer byteBuffer;
            final InputStream is = diskCacheResource.openStream();
            try {
                byteBuffer = mPooledByteBufferFactory.newByteBuffer(is, (int) diskCacheResource.size());
            } finally {
                is.close();
            }

            FLog.v(TAG, "Successful read from disk cache for %s", key.getUriString());
            return byteBuffer;
        } catch (IOException ioe) {
            // TODO: 3697790 log failures
            // TODO: 5258772 - uncomment line below
            // mFileCache.remove(key);
            FLog.w(TAG, ioe, "Exception reading from cache for %s", key.getUriString());
            mImageCacheStatsTracker.onDiskCacheGetFail();
            throw ioe;
        }
    }

    /**
     * 将EncodedImage写入硬盘缓存
     * Writes to disk cache
     * @throws IOException
     */
    private void writeToDiskCache(
            final CacheKey key,
            final EncodedImage encodedImage) {
        FLog.v(TAG, "About to write to disk-cache for key %s", key.getUriString());
        try {
            mFileCache.insert(
                    key, new WriterCallback() {
                        @Override
                        public void write(OutputStream os) throws IOException {
                            mPooledByteStreams.copy(encodedImage.getInputStream(), os);
                        }
                    }
            );
            FLog.v(TAG, "Successful disk-cache write for key %s", key.getUriString());
        } catch (IOException ioe) {
            // Log failure
            // TODO: 3697790
            FLog.w(TAG, ioe, "Failed to write to disk-cache for key %s", key.getUriString());
        }
    }
}
