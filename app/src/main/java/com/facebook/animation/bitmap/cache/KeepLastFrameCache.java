package com.facebook.animation.bitmap.cache;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import android.graphics.Bitmap;

import com.facebook.commom.references.CloseableReference;
import com.facebook.animation.bitmap.BitmapAnimationBackend;
import com.facebook.animation.bitmap.BitmapFrameCache;
import com.facebook.imageutils.BitmapUtil;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * 简单的缓存最后一帧图片的 帧缓存
 * Simple bitmap cache that keeps the last frame and reuses it if possible.
 */
public class KeepLastFrameCache implements BitmapFrameCache {

    private static final int FRAME_NUMBER_UNSET = -1;

    private int mLastFrameNumber = FRAME_NUMBER_UNSET;

    @Nullable
    private FrameCacheListener mFrameCacheListener;

    @GuardedBy("this")
    @Nullable
    private CloseableReference<Bitmap> mLastBitmapReference;

    @Nullable
    @Override
    public synchronized CloseableReference<Bitmap> getCachedFrame(int frameNumber) {
        if (mLastFrameNumber == frameNumber) {
            return CloseableReference.cloneOrNull(mLastBitmapReference);
        }
        return null;
    }

    @Nullable
    @Override
    public synchronized CloseableReference<Bitmap> getFallbackFrame(int frameNumber) {
        return CloseableReference.cloneOrNull(mLastBitmapReference);
    }

    @Override
    public synchronized CloseableReference<Bitmap> getBitmapToReuseForFrame(
            int frameNumber,
            int width,
            int height) {
        try {
            return CloseableReference.cloneOrNull(mLastBitmapReference);
        } finally {
            closeAndResetLastBitmapReference();
        }
    }

    @Override
    public synchronized int getSizeInBytes() {
        return mLastBitmapReference == null
                ? 0
                : BitmapUtil.getSizeInBytes(mLastBitmapReference.get());
    }

    @Override
    public synchronized void clear() {
        closeAndResetLastBitmapReference();
    }

    @Override
    public synchronized void onFrameRendered(
            int frameNumber,
            CloseableReference<Bitmap> bitmap,
            @BitmapAnimationBackend.FrameType int frameType) {
        if (bitmap != null
                && mLastBitmapReference != null
                && bitmap.get().equals(mLastBitmapReference.get())) {
            return;
        }
        CloseableReference.closeSafely(mLastBitmapReference);
        if (mFrameCacheListener != null && mLastFrameNumber != FRAME_NUMBER_UNSET) {
            mFrameCacheListener.onFrameEvicted(this, mLastFrameNumber);
        }
        mLastBitmapReference = CloseableReference.cloneOrNull(bitmap);
        if (mFrameCacheListener != null) {
            mFrameCacheListener.onFrameCached(this, frameNumber);
        }
        mLastFrameNumber = frameNumber;
    }

    @Override
    public void setFrameCacheListener(FrameCacheListener frameCacheListener) {
        mFrameCacheListener = frameCacheListener;
    }

    private synchronized void closeAndResetLastBitmapReference() {
        if (mFrameCacheListener != null && mLastFrameNumber != FRAME_NUMBER_UNSET) {
            mFrameCacheListener.onFrameEvicted(this, mLastFrameNumber);
        }
        CloseableReference.closeSafely(mLastBitmapReference);
        mLastBitmapReference = null;
        mLastFrameNumber = FRAME_NUMBER_UNSET;
    }
}
