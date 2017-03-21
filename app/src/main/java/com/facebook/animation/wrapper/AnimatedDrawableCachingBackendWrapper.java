package com.facebook.animation.wrapper;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.facebook.commom.references.CloseableReference;
import com.facebook.animation.backend.AnimationBackend;
import com.facebook.animation.backend.impl.AnimationBackendDelegateWithInactivityCheck;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableCachingBackend;

import javax.annotation.Nullable;

/**
 * 一个{@link AnimatedDrawableCachingBackend}的包装器
 * Animation backend that wraps around {@link AnimatedDrawableCachingBackend}.
 */
public class AnimatedDrawableCachingBackendWrapper implements AnimationBackend,
        AnimationBackendDelegateWithInactivityCheck.InactivityListener {

    private final Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);

    private AnimatedDrawableCachingBackend mAnimatedDrawableCachingBackend;

    @Nullable
    private volatile CloseableReference<Bitmap> mLastDrawnFrame;

    @Nullable
    private Rect mBounds;

    public AnimatedDrawableCachingBackendWrapper(
            AnimatedDrawableCachingBackend animatedDrawableCachingBackend) {
        mAnimatedDrawableCachingBackend = animatedDrawableCachingBackend;
    }

    @Override
    public int getFrameCount() {
        return mAnimatedDrawableCachingBackend.getFrameCount();
    }

    @Override
    public int getFrameDurationMs(int frameNumber) {
        return mAnimatedDrawableCachingBackend.getDurationMsForFrame(frameNumber);
    }

    @Override
    public int getLoopCount() {
        return mAnimatedDrawableCachingBackend.getLoopCount();
    }

    @Override
    public boolean drawFrame(Drawable parent, Canvas canvas, int frameNumber) {
        // Render order (first available will be drawn): correct frame, last drawn frame, preview frame
        if (drawBitmap(
                mAnimatedDrawableCachingBackend.getBitmapForFrame(frameNumber),
                canvas,
                mBounds)) {
            return true;
        } else if (mLastDrawnFrame != null) {
            drawBitmap(mLastDrawnFrame, canvas, mBounds);
        } else if (drawBitmap(
                mAnimatedDrawableCachingBackend.getPreviewBitmap(),
                canvas,
                mBounds)) {
            return true;
        }
        return false;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public void setBounds(@Nullable Rect bounds) {
        if (bounds != null && bounds.equals(mBounds)) {
            return;
        }
        mBounds = bounds;
        AnimatedDrawableCachingBackend newBackend =
                mAnimatedDrawableCachingBackend.forNewBounds(bounds);
        if (newBackend != mAnimatedDrawableCachingBackend) {
            mAnimatedDrawableCachingBackend.dropCaches();
        }
        mAnimatedDrawableCachingBackend = newBackend;
        CloseableReference.closeSafely(mLastDrawnFrame);
        mLastDrawnFrame = null;
    }

    @Override
    public int getSizeInBytes() {
        return mAnimatedDrawableCachingBackend.getMemoryUsage();
    }

    @Override
    public void clear() {
        if (mAnimatedDrawableCachingBackend != null) {
            mAnimatedDrawableCachingBackend.dropCaches();
        }
        CloseableReference.closeSafely(mLastDrawnFrame);
        mLastDrawnFrame = null;
    }

    @Override
    public int getIntrinsicWidth() {
        return mAnimatedDrawableCachingBackend.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mAnimatedDrawableCachingBackend.getHeight();
    }

    @Override
    public void onInactive() {
        clear();
    }

    private boolean drawBitmap(
            CloseableReference<Bitmap> bitmapReference,
            Canvas canvas,
            @Nullable Rect bounds) {
        if (!CloseableReference.isValid(bitmapReference)) {
            return false;
        }
        if (bounds == null) {
            canvas.drawBitmap(bitmapReference.get(), 0, 0, mPaint);
        } else {
            canvas.drawBitmap(bitmapReference.get(), null, bounds, mPaint);
        }

        // Close the previous frame
        if (bitmapReference != mLastDrawnFrame) {
            CloseableReference.closeSafely(mLastDrawnFrame);
            mLastDrawnFrame = bitmapReference;
        }
        return true;
    }
}
