package com.facebook.animation.bitmap.wrapper;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.facebook.commom.references.CloseableReference;
import com.facebook.animation.bitmap.BitmapFrameCache;
import com.facebook.animation.bitmap.BitmapFrameRenderer;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableBackend;
import com.facebook.imagepipeline.animated.impl.AnimatedImageCompositor;

import javax.annotation.Nullable;

/**
 * 包装一个{@link AnimatedDrawableBackend}的{@link BitmapFrameRenderer}
 * {@link BitmapFrameRenderer} that wraps around an {@link AnimatedDrawableBackend}.
 */
public class AnimatedDrawableBackendFrameRenderer implements BitmapFrameRenderer {

    private final BitmapFrameCache mBitmapFrameCache;

    private AnimatedDrawableBackend mAnimatedDrawableBackend;
    private AnimatedImageCompositor mAnimatedImageCompositor;

    private final AnimatedImageCompositor.Callback mCallback =
            new AnimatedImageCompositor.Callback() {
                @Override
                public void onIntermediateResult(int frameNumber, Bitmap bitmap) {
                    // We currently don't cache intermediate bitmaps here
                }

                @Nullable
                @Override
                public CloseableReference<Bitmap> getCachedBitmap(int frameNumber) {
                    return mBitmapFrameCache.getCachedFrame(frameNumber);
                }
            };

    public AnimatedDrawableBackendFrameRenderer(
            BitmapFrameCache bitmapFrameCache,
            AnimatedDrawableBackend animatedDrawableBackend) {
        mBitmapFrameCache = bitmapFrameCache;
        mAnimatedDrawableBackend = animatedDrawableBackend;

        mAnimatedImageCompositor = new AnimatedImageCompositor(mAnimatedDrawableBackend, mCallback);
    }

    @Override
    public void setBounds(@Nullable Rect bounds) {
        AnimatedDrawableBackend newBackend = mAnimatedDrawableBackend.forNewBounds(bounds);
        if (newBackend != mAnimatedDrawableBackend) {
            mAnimatedDrawableBackend = newBackend;
            mAnimatedImageCompositor = new AnimatedImageCompositor(mAnimatedDrawableBackend, mCallback);
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return mAnimatedDrawableBackend.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mAnimatedDrawableBackend.getHeight();
    }

    @Override
    public boolean renderFrame(int frameNumber, Bitmap targetBitmap) {
        mAnimatedImageCompositor.renderFrame(frameNumber, targetBitmap);
        return true;
    }
}
