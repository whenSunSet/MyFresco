package com.facebook.imagepipeline.animated.impl.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;

import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableBackend;
import com.facebook.imagepipeline.animated.base.AnimatedImage;
import com.facebook.imagepipeline.animated.base.AnimatedImageFrame;
import com.facebook.imagepipeline.animated.base.impl.AnimatedDrawableFrameInfo;
import com.facebook.imagepipeline.animated.base.impl.AnimatedImageResult;
import com.facebook.imagepipeline.animated.factory.impl.AnimatedDrawableFactoryImpl;
import com.facebook.imagepipeline.animated.util.AnimatedDrawableUtil;

import javax.annotation.concurrent.GuardedBy;

/**
 * 一个渲染{@link AnimatedImage}的{@link AnimatedDrawableBackend}，该对象在{@link AnimatedDrawableFactoryImpl#create}中被完整创建。
 * An {@link AnimatedDrawableBackend} that renders {@link AnimatedImage}.
 */
public class AnimatedDrawableBackendImpl implements AnimatedDrawableBackend {

    //外部传入
    private final AnimatedDrawableUtil mAnimatedDrawableUtil;
    //外部传入
    private final AnimatedImageResult mAnimatedImageResult;
    //外部传入
    private final Rect mRenderedBounds;
    /**
     * {@link AnimatedImageResult#getImage()}获取
     */
    private final AnimatedImage mAnimatedImage;
    /**
     * {@link AnimatedDrawableUtil#fixFrameDurations}获取
     */
    private final int[] mFrameDurationsMs;
    /**
     * {@link AnimatedDrawableUtil#getFrameTimeStampsFromDurations}获取
     */
    private final int[] mFrameTimestampsMs;
    /**
     * {@link AnimatedDrawableUtil#getTotalDurationFromFrameDurations}获取
     */
    private final int mDurationMs;
    /**
     * {@link AnimatedImage#getFrameInfo}获取
     */
    private final AnimatedDrawableFrameInfo[] mFrameInfos;

    @GuardedBy("this")
    private Bitmap mTempBitmap;

    public AnimatedDrawableBackendImpl(
            AnimatedDrawableUtil animatedDrawableUtil,
            AnimatedImageResult animatedImageResult,
            Rect bounds) {
        mAnimatedDrawableUtil = animatedDrawableUtil;
        mAnimatedImageResult = animatedImageResult;
        mAnimatedImage = animatedImageResult.getImage();
        mFrameDurationsMs = mAnimatedImage.getFrameDurations();
        mAnimatedDrawableUtil.fixFrameDurations(mFrameDurationsMs);
        mDurationMs = mAnimatedDrawableUtil.getTotalDurationFromFrameDurations(mFrameDurationsMs);
        mFrameTimestampsMs = mAnimatedDrawableUtil.getFrameTimeStampsFromDurations(mFrameDurationsMs);
        mRenderedBounds = getBoundsToUse(mAnimatedImage, bounds);
        mFrameInfos = new AnimatedDrawableFrameInfo[mAnimatedImage.getFrameCount()];
        for (int i = 0; i < mAnimatedImage.getFrameCount(); i++) {
            mFrameInfos[i] = mAnimatedImage.getFrameInfo(i);
        }
    }

    private static Rect getBoundsToUse(AnimatedImage image, Rect targetBounds) {
        if (targetBounds == null) {
            return new Rect(0, 0, image.getWidth(), image.getHeight());
        }
        return new Rect(
                0,
                0,
                Math.min(targetBounds.width(), image.getWidth()),
                Math.min(targetBounds.height(), image.getHeight()));
    }

    @Override
    public AnimatedImageResult getAnimatedImageResult() {
        return mAnimatedImageResult;
    }

    @Override
    public int getDurationMs() {
        return mDurationMs;
    }

    @Override
    public int getFrameCount() {
        return mAnimatedImage.getFrameCount();
    }

    @Override
    public int getLoopCount() {
        return mAnimatedImage.getLoopCount();
    }

    @Override
    public int getWidth() {
        return mAnimatedImage.getWidth();
    }

    @Override
    public int getHeight() {
        return mAnimatedImage.getHeight();
    }

    @Override
    public int getRenderedWidth() {
        return mRenderedBounds.width();
    }

    @Override
    public int getRenderedHeight() {
        return mRenderedBounds.height();
    }

    @Override
    public AnimatedDrawableFrameInfo getFrameInfo(int frameNumber) {
        return mFrameInfos[frameNumber];
    }

    @Override
    public int getFrameForTimestampMs(int timestampMs) {
        return mAnimatedDrawableUtil.getFrameForTimestampMs(mFrameTimestampsMs, timestampMs);
    }

    @Override
    public int getTimestampMsForFrame(int frameNumber) {
        Preconditions.checkElementIndex(frameNumber, mFrameTimestampsMs.length);
        return mFrameTimestampsMs[frameNumber];
    }

    @Override
    public int getDurationMsForFrame(int frameNumber) {
        return mFrameDurationsMs[frameNumber];
    }

    @Override
    public int getFrameForPreview() {
        return mAnimatedImageResult.getFrameForPreview();
    }

    @Override
    public AnimatedDrawableBackend forNewBounds(Rect bounds) {
        Rect boundsToUse = getBoundsToUse(mAnimatedImage, bounds);
        if (boundsToUse.equals(mRenderedBounds)) {
            // Actual bounds aren't changed.
            return this;
        }
        return new AnimatedDrawableBackendImpl(
                mAnimatedDrawableUtil,
                mAnimatedImageResult,
                bounds);
    }

    @Override
    public synchronized int getMemoryUsage() {
        int bytes = 0;
        if (mTempBitmap != null) {
            bytes += mAnimatedDrawableUtil.getSizeOfBitmap(mTempBitmap);
        }
        bytes += mAnimatedImage.getSizeInBytes();
        return bytes;
    }

    @Override
    public CloseableReference<Bitmap> getPreDecodedFrame(int frameNumber) {
        return mAnimatedImageResult.getDecodedFrame(frameNumber);
    }

    @Override
    public boolean hasPreDecodedFrame(int index) {
        return mAnimatedImageResult.hasDecodedFrame(index);
    }

    /**
     * 将某一帧绘制到传入的Canvas上
     * 1.通过{@link #mAnimatedImage}(A)获取{@link AnimatedImageFrame}
     * 2.通过{@link AnimatedImage#doesRenderSupportScaling()}判断是否支持缩放
     * 3.如果支持那么调用{@link #renderImageSupportsScaling},否则调用{@link #renderImageDoesNotSupportScaling}
     * 4.最后将该帧资源释放
     * @param frameNumber the frame number (0-based)
     * @param canvas the canvas to render onto
     */
    @Override
    public void renderFrame(int frameNumber, Canvas canvas) {
        AnimatedImageFrame frame  = mAnimatedImage.getFrame(frameNumber);
        try {
            if (mAnimatedImage.doesRenderSupportScaling()) {
                renderImageSupportsScaling(canvas, frame);
            } else {
                renderImageDoesNotSupportScaling(canvas, frame);
            }
        } finally {
            frame.dispose();
        }
    }

    private void renderImageSupportsScaling(Canvas canvas, AnimatedImageFrame frame) {
        double xScale = (double) mRenderedBounds.width() / (double) mAnimatedImage.getWidth();
        double yScale = (double) mRenderedBounds.height() / (double) mAnimatedImage.getHeight();

        int frameWidth = (int) Math.round(frame.getWidth() * xScale);
        int frameHeight = (int) Math.round(frame.getHeight() * yScale);
        int xOffset = (int) (frame.getXOffset() * xScale);
        int yOffset = (int) (frame.getYOffset() * yScale);

        synchronized (this) {
            if (mTempBitmap == null) {
                mTempBitmap = Bitmap.createBitmap(
                        mRenderedBounds.width(),
                        mRenderedBounds.height(),
                        Bitmap.Config.ARGB_8888);
            }
            mTempBitmap.eraseColor(Color.TRANSPARENT);
            frame.renderFrame(frameWidth, frameHeight, mTempBitmap);
            canvas.drawBitmap(mTempBitmap, xOffset, yOffset, null);
        }
    }

    public void renderImageDoesNotSupportScaling(Canvas canvas, AnimatedImageFrame frame) {
        int frameWidth = frame.getWidth();
        int frameHeight = frame.getHeight();
        int xOffset = frame.getXOffset();
        int yOffset = frame.getYOffset();
        synchronized (this) {
            if (mTempBitmap == null) {
                mTempBitmap = Bitmap.createBitmap(
                        mAnimatedImage.getWidth(),
                        mAnimatedImage.getHeight(),
                        Bitmap.Config.ARGB_8888);
            }
            mTempBitmap.eraseColor(Color.TRANSPARENT);
            frame.renderFrame(frameWidth, frameHeight, mTempBitmap);

            float xScale = (float) mRenderedBounds.width() / (float) mAnimatedImage.getWidth();
            float yScale = (float) mRenderedBounds.height() / (float) mAnimatedImage.getHeight();
            canvas.save();
            canvas.scale(xScale, yScale);
            canvas.translate(xOffset, yOffset);
            canvas.drawBitmap(mTempBitmap, 0, 0, null);
            canvas.restore();
        }
    }

    @Override
    public synchronized void dropCaches() {
        if (mTempBitmap != null) {
            mTempBitmap.recycle();
            mTempBitmap = null;
        }
    }
}
