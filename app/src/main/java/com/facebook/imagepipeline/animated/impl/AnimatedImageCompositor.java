package com.facebook.imagepipeline.animated.impl;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableBackend;
import com.facebook.imagepipeline.animated.base.impl.AnimatedDrawableFrameInfo;
import com.facebook.imagepipeline.animated.base.impl.AnimatedDrawableFrameInfo.BlendOperation;
import com.facebook.imagepipeline.animated.base.impl.AnimatedDrawableFrameInfo.DisposalMethod;
import com.facebook.imagepipeline.animated.base.AnimatedImage;

/**
 * 包含在一个{@link AnimatedImage}中，其用来提供帧合成的逻辑
 * 动画格式像是GIF和WebP支持inter-frame压缩算法，这个算法将前后帧混合在一起实现
 * 这个class封装了所有操作图像帧的行为，旨在通过一个回调来处理缓存
 * Contains the logic for compositing the frames of an {@link AnimatedImage}. Animated image
 * formats like GIF and WebP support inter-frame compression where a subsequent frame may require
 * being blended on a previous frame in order to render the full frame. This class encapsulates
 * the behavior to be able to render any frame of the image. Designed to work with a cache
 * via a Callback.
 */
public class AnimatedImageCompositor {

    /**
     * 缓存的回调
     * Callback for caching.
     */
    public interface Callback {

        /**
         * 在{@link #renderFrame}中调用，让调用者知道当试图生成一个被请求的帧的时候，一个其他的帧也会生成。
         * 这允许调用者可选择地缓存中间结果。调用者必须拷贝bitmap，如果他希望缓存bitmap作为{@link #renderFrame}
         * 中继续使用生成请求帧
         * Called from within {@link #renderFrame} to let the caller know that while trying generate
         * the requested frame, an earlier frame was generated. This allows the caller to optionally
         * cache the intermediate result. The caller must copy the Bitmap if it wishes to cache it
         * as {@link #renderFrame} will continue using it generate the requested frame.
         *
         * @param frameNumber the frame number of the intermediate result
         * @param bitmap the bitmap which must not be modified or directly cached
         */
        void onIntermediateResult(int frameNumber, Bitmap bitmap);

        /**
         * 在{@link #renderFrame}中调用，来告诉调用者一个缓存的bitmap的帧number，
         * 如果调用者有bitmap的缓存，它可以大大减少呈现请求帧所需的工作。
         * Called from within {@link #renderFrame} to ask the caller for a cached bitmap for the
         * specified frame number. If the caller has the bitmap cached, it can greatly reduce the
         * work required to render the requested frame.
         *
         * @param frameNumber the frame number to get
         * @return a reference to the bitmap. The ownership of the reference is passed to the caller
         *    who must close it.
         */
        CloseableReference<Bitmap> getCachedBitmap(int frameNumber);
    }

    private final AnimatedDrawableBackend mAnimatedDrawableBackend;
    private final Callback mCallback;
    private final Paint mTransparentFillPaint;

    public AnimatedImageCompositor(
            AnimatedDrawableBackend animatedDrawableBackend,
            Callback callback) {
        mAnimatedDrawableBackend = animatedDrawableBackend;
        mCallback = callback;
        mTransparentFillPaint = new Paint();
        mTransparentFillPaint.setColor(Color.TRANSPARENT);
        mTransparentFillPaint.setStyle(Paint.Style.FILL);
        mTransparentFillPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));
    }

    /**
     * 绘制给定的特别的帧，只能被调用在rendering 线程
     * Renders the specified frame. Only should be called on the rendering thread.
     *
     * @param frameNumber the frame to render
     * @param bitmap the bitmap to render into
     */
    public void renderFrame(int frameNumber, Bitmap bitmap) {
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC);

        //如果需要混合，那么准最近的帧和canvas
        // If blending is required, prepare the canvas with the nearest cached frame.
        int nextIndex;
        if (!isKeyFrame(frameNumber)) {
            //混合是必须的，nextIndex的指针指向下一个
            // Blending is required. nextIndex points to the next index to render onto the canvas.
            nextIndex = prepareCanvasWithClosestCachedFrame(frameNumber - 1, canvas);
        } else {
            // Blending isn't required. Start at the frame we're trying to render.
            nextIndex = frameNumber;
        }

        // Iterate from nextIndex to the frame number just preceding the one we're trying to render
        // and composite them in order according to the Disposal Method.
        for (int index = nextIndex; index < frameNumber; index++) {
            AnimatedDrawableFrameInfo frameInfo = mAnimatedDrawableBackend.getFrameInfo(index);
            DisposalMethod disposalMethod = frameInfo.disposalMethod;
            if (disposalMethod == DisposalMethod.DISPOSE_TO_PREVIOUS) {
                continue;
            }
            if (frameInfo.blendOperation == BlendOperation.NO_BLEND) {
                disposeToBackground(canvas, frameInfo);
            }
            mAnimatedDrawableBackend.renderFrame(index, canvas);
            mCallback.onIntermediateResult(index, bitmap);
            if (disposalMethod == DisposalMethod.DISPOSE_TO_BACKGROUND) {
                disposeToBackground(canvas, frameInfo);
            }
        }

        AnimatedDrawableFrameInfo frameInfo = mAnimatedDrawableBackend.getFrameInfo(frameNumber);
        if (frameInfo.blendOperation == BlendOperation.NO_BLEND) {
            disposeToBackground(canvas, frameInfo);
        }
        // Finally, we render the current frame. We don't dispose it.
        mAnimatedDrawableBackend.renderFrame(frameNumber, canvas);
    }

    /**
     * 返回一个表示混合逻辑的值在{@link #isFrameNeededForRendering}中使用
     * Return value for {@link #isFrameNeededForRendering} used in the compositing logic.
     */
    private enum FrameNeededResult {
        //该帧请求渲染下一帧
        /** The frame is required to render the next frame */
        REQUIRED,

        //该帧请求不渲染下一帧
        /** The frame is not required to render the next frame. */
        NOT_REQUIRED,

        //跳过这一帧
        /** Skip this frame and keep going. Used for GIF's DISPOSE_TO_PREVIOUS */
        SKIP,

        //停止处理这一帧，这意味着该image 没有指定特殊的处理方法
        /** Stop processing at this frame. This means the image didn't specify the disposal method */
        ABORT
    }

    /**
     * 给定一个帧数,准备画布上呈现基于最近的缓存帧或之前的帧。
     * 返回画布上将准备作为最近的帧缓存被渲染和处理。返回的索引是下一个需要合成到画布上的帧。
     * Given a frame number, prepares the canvas to render based on the nearest cached frame
     * at or before the frame. On return the canvas will be prepared as if the nearest cached
     * frame had been rendered and disposed. The returned index is the next frame that needs to be
     * composited onto the canvas.
     *
     * @param previousFrameNumber the frame number that is ones less than the one we're rendering
     * @param canvas the canvas to prepare
     * @return the index of the the next frame to process
     */
    private int prepareCanvasWithClosestCachedFrame(int previousFrameNumber, Canvas canvas) {
        for (int index = previousFrameNumber; index >= 0; index--) {
            FrameNeededResult neededResult = isFrameNeededForRendering(index);
            switch (neededResult) {
                case REQUIRED:
                    AnimatedDrawableFrameInfo frameInfo = mAnimatedDrawableBackend.getFrameInfo(index);
                    CloseableReference<Bitmap> startBitmap = mCallback.getCachedBitmap(index);
                    if (startBitmap != null) {
                        try {
                            canvas.drawBitmap(startBitmap.get(), 0, 0, null);
                            if (frameInfo.disposalMethod == DisposalMethod.DISPOSE_TO_BACKGROUND) {
                                disposeToBackground(canvas, frameInfo);
                            }
                            return index + 1;
                        } finally {
                            startBitmap.close();
                        }
                    } else {
                        if (isKeyFrame(index)) {
                            return index;
                        } else {
                            // Keep going.
                            break;
                        }
                    }
                case NOT_REQUIRED:
                    return index + 1;
                case ABORT:
                    return index;
                case SKIP:
                default:
                    // Keep going.
            }
        }
        return 0;
    }

    private void disposeToBackground(Canvas canvas, AnimatedDrawableFrameInfo frameInfo) {
        canvas.drawRect(
                frameInfo.xOffset,
                frameInfo.yOffset,
                frameInfo.xOffset + frameInfo.width,
                frameInfo.yOffset + frameInfo.height,
                mTransparentFillPaint);
    }

    /**
     * 返回指定的帧是否需要呈现下一帧，这是混合逻辑的一部分，参考{@link FrameNeededResult}了解更多的信息
     * Returns whether the specified frame is needed for rendering the next frame. This is part of
     * the compositing logic. See {@link FrameNeededResult} for more info about the results.
     *
     * @param index the frame to check
     * @return whether the frame is required taking into account special conditions
     */
    private FrameNeededResult isFrameNeededForRendering(int index) {
        AnimatedDrawableFrameInfo frameInfo = mAnimatedDrawableBackend.getFrameInfo(index);
        DisposalMethod disposalMethod = frameInfo.disposalMethod;
        if (disposalMethod == DisposalMethod.DISPOSE_DO_NOT) {
            //需要这个帧继续显示
            // Need this frame so keep going.
            return FrameNeededResult.REQUIRED;
        } else if (disposalMethod == DisposalMethod.DISPOSE_TO_BACKGROUND) {
            if (isFullFrame(frameInfo)) {
                //
                // The frame covered the whole image and we're disposing to background,
                // so we don't even need to draw this frame.
                return FrameNeededResult.NOT_REQUIRED;
            } else {
                // We need to draw the image. Then erase the part the previous frame covered.
                // So keep going.
                return FrameNeededResult.REQUIRED;
            }
        } else if (disposalMethod == DisposalMethod.DISPOSE_TO_PREVIOUS) {
            return FrameNeededResult.SKIP;
        } else {
            return FrameNeededResult.ABORT;
        }
    }

    private boolean isKeyFrame(int index) {
        if (index == 0) {
            return true;
        }
        AnimatedDrawableFrameInfo currFrameInfo = mAnimatedDrawableBackend.getFrameInfo(index);
        AnimatedDrawableFrameInfo prevFrameInfo = mAnimatedDrawableBackend.getFrameInfo(index - 1);
        if (currFrameInfo.blendOperation == BlendOperation.NO_BLEND && isFullFrame(currFrameInfo)) {
            return true;
        } else
            return prevFrameInfo.disposalMethod == DisposalMethod.DISPOSE_TO_BACKGROUND
                    && isFullFrame(prevFrameInfo);
    }

    private boolean isFullFrame(AnimatedDrawableFrameInfo frameInfo) {
        return frameInfo.xOffset == 0 &&
                frameInfo.yOffset == 0 &&
                frameInfo.width == mAnimatedDrawableBackend.getRenderedWidth() &&
                frameInfo.height == mAnimatedDrawableBackend.getRenderedHeight();
    }
}
