package com.facebook.animation.bitmap.cache;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import android.graphics.Bitmap;

import com.facebook.commom.references.CloseableReference;
import com.facebook.animation.bitmap.BitmapAnimationBackend;
import com.facebook.animation.bitmap.BitmapFrameCache;

import javax.annotation.Nullable;

/**
 * 不做任何事情的帧缓存
 * No-op bitmap cache that doesn't do anything.
 */
public class NoOpCache implements BitmapFrameCache {

    @Nullable
    @Override
    public CloseableReference<Bitmap> getCachedFrame(int frameNumber) {
        return null;
    }

    @Nullable
    @Override
    public CloseableReference<Bitmap> getFallbackFrame(int frameNumber) {
        return null;
    }

    @Nullable
    @Override
    public CloseableReference<Bitmap> getBitmapToReuseForFrame(
            int frameNumber,
            int width,
            int height) {
        return null;
    }

    @Override
    public int getSizeInBytes() {
        return 0;
    }

    @Override
    public void clear() {
        // no-op
    }

    @Override
    public void onFrameRendered(
            int frameNumber,
            CloseableReference<Bitmap> bitmap,
            @BitmapAnimationBackend.FrameType int frameType) {
        // no-op
    }

    @Override
    public void setFrameCacheListener(FrameCacheListener frameCacheListener) {
        // Does not cache anything
    }
}
