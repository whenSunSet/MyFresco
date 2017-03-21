package com.facebook.imagepipeline.memory.impl;

/**
 * Created by heshixiyang on 2017/3/17.
 */

import android.graphics.Bitmap;
import android.os.Build;

import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.internal.Throwables;
import com.facebook.commom.references.CloseableReference;
import com.facebook.commom.references.ResourceReleaser;
import com.facebook.imagepipeline.common.TooManyBitmapsException;
import com.facebook.imagepipeline.nativecode.Bitmaps;
import com.facebook.imageutils.BitmapUtil;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;

/**
 * 计数bitmap，保持跟踪总的byte的数量，和bitmap数量
 * Counts bitmaps - keeps track of both, count and total size in bytes.
 */
public class BitmapCounter {

    //bitmap的总数
    @GuardedBy("this")
    private int mCount;

    //bitmap占byte的总量
    @GuardedBy("this")
    private long mSize;

    private final int mMaxCount;
    private final int mMaxSize;
    private final ResourceReleaser<Bitmap> mUnpooledBitmapsReleaser;

    public BitmapCounter(int maxCount, int maxSize) {
        Preconditions.checkArgument(maxCount > 0);
        Preconditions.checkArgument(maxSize > 0);
        mMaxCount = maxCount;
        mMaxSize = maxSize;
        mUnpooledBitmapsReleaser = new ResourceReleaser<Bitmap>() {
            @Override
            public void release(Bitmap value) {
                try {
                    decrease(value);
                } finally {
                    value.recycle();
                }
            }
        };
    }

    /**
     * Includes given bitmap in the bitmap count. The bitmap is included only if doing so
     * does not violate configured limit
     *
     * @param bitmap to include in the count
     * @return true if and only if bitmap is successfully included in the count
     */
    public synchronized boolean increase(Bitmap bitmap) {
        final int bitmapSize = BitmapUtil.getSizeInBytes(bitmap);
        if (mCount >= mMaxCount || mSize + bitmapSize > mMaxSize) {
            return false;
        }
        mCount++;
        mSize += bitmapSize;
        return true;
    }

    /**
     * Excludes given bitmap from the count.
     *
     * @param bitmap to be excluded from the count
     */
    public synchronized void decrease(Bitmap bitmap) {
        final int bitmapSize = BitmapUtil.getSizeInBytes(bitmap);
        Preconditions.checkArgument(mCount > 0, "No bitmaps registered.");
        Preconditions.checkArgument(
                bitmapSize <= mSize,
                "Bitmap size bigger than the total registered size: %d, %d",
                bitmapSize,
                mSize);
        mSize -= bitmapSize;
        mCount--;
    }

    /**
     * @return number of counted bitmaps
     */
    public synchronized int getCount() {
        return mCount;
    }

    /**
     * @return total size in bytes of counted bitmaps
     */
    public synchronized long getSize() {
        return mSize;
    }

    public ResourceReleaser<Bitmap> getReleaser() {
        return mUnpooledBitmapsReleaser;
    }

    /**
     * 将bitmaps和bitmap counter关联起来，如果抛出了TooManyBitmapsException，那么需要调用{@link Bitmap#recycle}
     * Associates bitmaps with the bitmap counter. <p/> <p>If this method throws
     * TooManyBitmapsException, the code will have called {@link Bitmap#recycle} on the
     * bitmaps.</p>
     *
     * @param bitmaps the bitmaps to associate
     * @return the references to the bitmaps that are now tied to the bitmap pool
     * @throws TooManyBitmapsException if the pool is full
     */
    public List<CloseableReference<Bitmap>> associateBitmapsWithBitmapCounter(
            final List<Bitmap> bitmaps) {
        int countedBitmaps = 0;
        try {
            for (; countedBitmaps < bitmaps.size(); ++countedBitmaps) {
                final Bitmap bitmap = bitmaps.get(countedBitmaps);
                // 'Pin' the bytes of the purgeable bitmap, so it is now not purgeable
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    Bitmaps.pinBitmap(bitmap);
                }
                if (!increase(bitmap)) {
                    throw new TooManyBitmapsException();
                }
            }
            List<CloseableReference<Bitmap>> ret = new ArrayList<>(bitmaps.size());
            for (Bitmap bitmap : bitmaps) {
                ret.add(CloseableReference.of(bitmap, mUnpooledBitmapsReleaser));
            }
            return ret;
        } catch (Exception exception) {
            if (bitmaps != null) {
                for (Bitmap bitmap : bitmaps) {
                    if (countedBitmaps-- > 0) {
                        decrease(bitmap);
                    }
                    bitmap.recycle();
                }
            }
            throw Throwables.propagate(exception);
        }
    }
}
