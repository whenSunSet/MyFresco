package com.facebook.imagepipeline.animated.base;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import android.graphics.Bitmap;

/**
 * 一个动画image的帧的公共接口
 */
public interface AnimatedImageFrame {

    /**
     * 配置实例。这个实例将释放本地资源。一旦调用，其他该实例上的方法可能抛出异常
     * 注意,底层的本机资源可能不被真正释放,直到所有的相关实例{@link AnimatedImage}都被处理
     */
    void dispose();

    /**
     * 渲染帧到指定的bitmap，bitmap必须有一个宽度和高度*至少指定的宽度和高度,必须在8888年RGBA颜色格式。
     *
     * @param width the width to render to (the image is scaled to this width)
     * @param height the height to render to (the image is scaled to this height)
     * @param bitmap the bitmap to render into
     */
    void renderFrame(int width, int height, Bitmap bitmap);

    /**
     * 获取每一帧的持续时间
     * @return 返回毫秒
     */
    int getDurationMs();

    /**
     * Gets the width of the frame.
     *
     * @return the width of the frame
     */
    int getWidth();

    /**
     * Gets the height of the frame.
     *
     * @return the height of the frame
     */
    int getHeight();

    /**
     * 获取frame基于canvas的位移x
     * @return the x-offset of the frame
     */
    int getXOffset();

    /**
     * 获取frame基于canvas的位移y
     * @return the y-offset of the frame
     */
    int getYOffset();
}
