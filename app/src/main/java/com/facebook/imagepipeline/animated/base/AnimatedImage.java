package com.facebook.imagepipeline.animated.base;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import com.facebook.imagepipeline.animated.base.impl.AnimatedDrawableFrameInfo;

/**
 * 一个对于动画image的公共接口
 */
public interface AnimatedImage {

    int LOOP_COUNT_INFINITE = 0;

    /**
     * 配置实例。这个实例将释放本地资源。一旦调用，其他该实例上的方法可能抛出异常
     * 注意,底层的本机资源可能不被真正释放,直到所有的相关实例{@link AnimatedImageFrame}都被处理
     */
    void dispose();

    /**
     * 得到图像的宽度(也称为canvas在WebP上的术语)
     *
     * @return the width of the image
     */
    int getWidth();

    /**
     * Gets the height of the image (also known as the canvas in WebP nomenclature).
     *
     * @return the height of the image
     */
    int getHeight();

    /**
     * 获取image的帧数
     *
     * @return the number of frames in the image
     */
    int getFrameCount();

    /**
     * 获取动画image的播放时间
     *
     * @return the duration of the animated image in milliseconds
     */
    int getDuration();

    /**
     * 获取每一帧的的显示时间
     *
     * @return an array that is the size of the number of frames containing the duration of each frame
     *     in milliseconds
     */
    int[] getFrameDurations();

    /**
     * 得到的动画循环运行的数量
     *
     * @return the number of loops, or 0 to indicate infinite
     */
    int getLoopCount();

    /**
     * 创建一个{@link AnimatedImageFrame}，对于某个特定的帧
     *
     * @param frameNumber the index of the frame
     * @return a newly created {@link AnimatedImageFrame}
     */
    AnimatedImageFrame getFrame(int frameNumber);

    /**
     * 返回是否{@link AnimatedImageFrame#renderFrame}支持scaling对于任意大小或者是否scaling应该在外部完成
     *
     * @return whether rendering supports scaling
     */
    boolean doesRenderSupportScaling();

    /**
     * 获取编码的图像的字节数，该图像的数据应该已经在内存中
     *
     * @return the size in bytes of the encoded image data
     */
    int getSizeInBytes();

    /**
     * 获取某一帧的信息
     *
     * @param frameNumber the frame to get the info for
     * @return the frame info
     */
    AnimatedDrawableFrameInfo getFrameInfo(int frameNumber);
}
