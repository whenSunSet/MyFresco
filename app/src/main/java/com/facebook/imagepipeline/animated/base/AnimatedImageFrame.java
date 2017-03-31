package com.facebook.imagepipeline.animated.base;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import android.graphics.Bitmap;

import com.facebook.animated.gif.GifFrame;
import com.facebook.animated.webp.WebPFrame;

/**
 * 动画的一个帧的接口，只有{@link GifFrame}和{@link WebPFrame}实现了这个接口
 */
public interface AnimatedImageFrame {

    /**
     * * 将本地内存byte数组资源释放掉，一旦调用，其他使用了同一本地内存的实例上调用该方法会抛出异常
     * 注意,底层的本机资源可能不被真正释放,直到所有的相关实例{@link AnimatedImage}都被处理
     */
    void dispose();

    /**
     * 渲染{@link GifFrame}和{@link WebPFrame}中帧数据到指定的bitmap，bitmap需要指定的宽度和高度,必须在8888年RGBA颜色格式。
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
     * 获取帧基于canvas的位移x
     * @return the x-offset of the frame
     */
    int getXOffset();

    /**
     * 获取帧基于canvas的位移y
     * @return the y-offset of the frame
     */
    int getYOffset();
}
