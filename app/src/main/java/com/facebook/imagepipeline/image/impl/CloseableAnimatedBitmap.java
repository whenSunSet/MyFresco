package com.facebook.imagepipeline.image.impl;

/**
 * Created by heshixiyang on 2017/3/17.
 */

import android.graphics.Bitmap;

import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.references.CloseableReference;
import com.facebook.commom.references.ResourceReleaser;
import com.facebook.imageutils.BitmapUtil;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * 一个包含了一个动画的所有bitmap和其帧停留事件的CloseableImage
 * CloseableImage that contains array of Bitmaps and frame durations.
 */
@ThreadSafe
public class CloseableAnimatedBitmap extends CloseableBitmap {

    // bitmap frames
    @GuardedBy("this")
    private List<CloseableReference<Bitmap>> mBitmapReferences;
    private volatile List<Bitmap> mBitmaps;

    // frame durations
    private volatile List<Integer> mDurations;

    public CloseableAnimatedBitmap(
            List<CloseableReference<Bitmap>> bitmapReferences,
            List<Integer> durations) {
        Preconditions.checkNotNull(bitmapReferences);
        Preconditions.checkState(bitmapReferences.size() >= 1, "Need at least 1 frame!");
        mBitmapReferences = new ArrayList<>(bitmapReferences.size());
        mBitmaps = new ArrayList<>(bitmapReferences.size());
        for (CloseableReference<Bitmap> bitmapReference : bitmapReferences) {
            mBitmapReferences.add(bitmapReference.clone());
            mBitmaps.add(bitmapReference.get());
        }
        mDurations = Preconditions.checkNotNull(durations);
        Preconditions.checkState(mDurations.size() == mBitmaps.size(), "Arrays length mismatch!");
    }

    /**
     * Creates a new instance of a CloseableStaticBitmap.
     *
     * @param bitmaps the bitmap frames. This list must be immutable.
     * @param durations the frame durations, This list must be immutable.
     * @param resourceReleaser ResourceReleaser to release the bitmaps to
     */
    public CloseableAnimatedBitmap(
            List<Bitmap> bitmaps,
            List<Integer> durations,
            ResourceReleaser<Bitmap> resourceReleaser) {
        Preconditions.checkNotNull(bitmaps);
        Preconditions.checkState(bitmaps.size() >= 1, "Need at least 1 frame!");
        mBitmaps = new ArrayList<>(bitmaps.size());
        mBitmapReferences = new ArrayList<>(bitmaps.size());
        for (Bitmap bitmap : bitmaps) {
            mBitmapReferences.add(CloseableReference.of(bitmap, resourceReleaser));
            mBitmaps.add(bitmap);
        }
        mDurations = Preconditions.checkNotNull(durations);
        Preconditions.checkState(mDurations.size() == mBitmaps.size(), "Arrays length mismatch!");
    }

    /**
     * 释放bitmap的pool
     * Releases the bitmaps to the pool.
     */
    @Override
    public void close() {
        List<CloseableReference<Bitmap>> bitmapReferences;
        synchronized (this) {
            if (mBitmapReferences == null) {
                return;
            }
            bitmapReferences = mBitmapReferences;
            mBitmapReferences = null;
            mBitmaps = null;
            mDurations = null;
        }
        CloseableReference.closeSafely(bitmapReferences);
    }

    /**
     * Returns whether this instance is closed.
     */
    @Override
    public synchronized boolean isClosed() {
        return mBitmaps == null;
    }

    /**
     * Gets the bitmap frames.
     *
     * @return bitmap frames
     */
    public List<Bitmap> getBitmaps() {
        return mBitmaps;
    }

    /**
     * Gets the frame durations.
     *
     * @return frame durations
     */
    public List<Integer> getDurations() {
        return mDurations;
    }

    /**
     * 获取第一个帧
     * Gets the first frame.
     *
     * @return the first frame
     */
    @Override
    public Bitmap getUnderlyingBitmap() {
        List<Bitmap> bitmaps = mBitmaps;
        return (bitmaps != null) ? bitmaps.get(0) : null;
    }

    /**
     * @return size in bytes all bitmaps in sum
     */
    @Override
    public int getSizeInBytes() {
        List<Bitmap> bitmaps = mBitmaps;
        if (bitmaps == null || bitmaps.size() == 0) {
            return 0;
        } else {
            return BitmapUtil.getSizeInBytes(bitmaps.get(0)) * bitmaps.size();
        }
    }

    /**
     * @return width of the image
     */
    @Override
    public int getWidth() {
        List<Bitmap> bitmaps = mBitmaps;
        return (bitmaps == null) ? 0 : bitmaps.get(0).getWidth();
    }

    /**
     * @return height of the image
     */
    @Override
    public int getHeight() {
        List<Bitmap> bitmaps = mBitmaps;
        return (bitmaps == null) ? 0 : bitmaps.get(0).getHeight();
    }

}
