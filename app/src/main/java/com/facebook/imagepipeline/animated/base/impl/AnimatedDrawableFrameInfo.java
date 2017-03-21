package com.facebook.imagepipeline.animated.base.impl;

/**
 * Created by heshixiyang on 2017/3/9.
 */
public class AnimatedDrawableFrameInfo {

    /**
     * 应该如何处理当前帧在渲染下一帧图像之前
     */
    public enum DisposalMethod {

        /** Do not dipose the frame. Leave as-is. */
        DISPOSE_DO_NOT,

        /** Dispose to the background color */
        DISPOSE_TO_BACKGROUND,

        /** Dispose to the previous frame */
        DISPOSE_TO_PREVIOUS
    }

    /**
     * 表明当前帧的透明像素与当前的canvas是如何混合的，
     */
    public enum BlendOperation {
        /** Blend **/
        BLEND_WITH_PREVIOUS,
        /** Do not blend **/
        NO_BLEND,
    }

    public final int frameNumber;
    public final int xOffset;
    public final int yOffset;
    public final int width;
    public final int height;
    public final BlendOperation blendOperation;
    public final DisposalMethod disposalMethod;

    public AnimatedDrawableFrameInfo(
            int frameNumber,
            int xOffset,
            int yOffset,
            int width,
            int height,
            BlendOperation blendOperation,
            DisposalMethod disposalMethod) {
        this.frameNumber = frameNumber;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.width = width;
        this.height = height;
        this.blendOperation = blendOperation;
        this.disposalMethod = disposalMethod;
    }
}
