package com.facebook.imagepipeline.memory.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import android.annotation.TargetApi;
import android.graphics.Bitmap;

import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.memory.MemoryTrimmableRegistry;
import com.facebook.imagepipeline.memory.PoolStatsTracker;
import com.facebook.imageutils.BitmapUtil;

import javax.annotation.concurrent.ThreadSafe;

/**
 * 管理一个bitmaps的pool。这允许我们重复使用bitmaps以代替不停的分配空间
 * Manages a pool of bitmaps. This allows us to reuse bitmaps instead of constantly allocating
 * them (and pressuring the Java GC to garbage collect unused bitmaps).
 * <p>
 * 这个池子支持get/release的范式
 * The pool supports a get/release paradigm.
 * get()允许一个bitmap从pool中提出重用。如果没有这样的bitmap，那么就返回一个
 * get() allows for a bitmap in the pool to be reused if it matches the desired
 * dimensions; if no such bitmap is found in the pool, a new one is allocated.
 * release() returns a bitmap to the pool.
 */
@ThreadSafe
@TargetApi(21)
public class BitmapPool extends BasePool<Bitmap> {

    /**
     * Creates an instance of a bitmap pool.
     * @param memoryTrimmableRegistry the memory manager to register with
     * @param poolParams pool parameters
     */
    public BitmapPool(
            MemoryTrimmableRegistry memoryTrimmableRegistry,
            PoolParams poolParams,
            PoolStatsTracker poolStatsTracker) {
        super(memoryTrimmableRegistry, poolParams, poolStatsTracker);
        initialize();
    }

    /**
     * 分配一个bitmap，该bitmap为size大小
     * 因为配置的大小事不可知的，所以大小事实际的大小
     * Allocate a bitmap that has a backing memory allocacation of 'size' bytes.
     * This is configuration agnostic so the size is the actual size in bytes of the bitmap.
     * @param size the 'size' in bytes of the bitmap
     * @return a new bitmap with the specified size in memory
     */
    @Override
    protected Bitmap alloc(int size) {
        return Bitmap.createBitmap(
                1,
                (int) Math.ceil(size / (double) BitmapUtil.RGB_565_BYTES_PER_PIXEL),
                Bitmap.Config.RGB_565);
    }

    /**
     * 将一个bitmap回收
     * Frees the bitmap
     * @param value the bitmap to free
     */
    @Override
    protected void free(Bitmap value) {
        Preconditions.checkNotNull(value);
        value.recycle();
    }

    /**
     *
     * Gets the bucketed size (typically something the same or larger than the requested size)
     * @param requestSize the logical request size
     * @return the 'bucketed' size
     */
    @Override
    protected int getBucketedSize(int requestSize) {
        return requestSize;
    }

    /**
     * Gets the bucketed size of the value.
     * We don't check the 'validity' of the value (beyond the not-null check). That's handled
     * in {@link #isReusable(Bitmap)}
     * @param value the value
     * @return bucketed size of the value
     */
    @Override
    protected int getBucketedSizeForValue(Bitmap value) {
        Preconditions.checkNotNull(value);
        return value.getAllocationByteCount();
    }

    /**
     * Gets the size in bytes for the given bucketed size
     * @param bucketedSize the bucketed size
     * @return size in bytes
     */
    @Override
    protected int getSizeInBytes(int bucketedSize) {
        return  bucketedSize;
    }

    /**
     * Determine if this bitmap is reusable (i.e.) if subsequent {@link #get(int)} requests can
     * use this value.
     * The bitmap is reusable if
     *  - it has not already been recycled AND
     *  - it is mutable
     * @param value the value to test for reusability
     * @return true, if the bitmap can be reused
     */
    @Override
    protected boolean isReusable(Bitmap value) {
        Preconditions.checkNotNull(value);
        return !value.isRecycled() &&
                value.isMutable();
    }
}
