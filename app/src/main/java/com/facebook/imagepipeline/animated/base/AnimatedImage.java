package com.facebook.imagepipeline.animated.base;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import com.facebook.animated.gif.GifImage;
import com.facebook.animated.webp.WebPImage;
import com.facebook.imagepipeline.animated.base.impl.AnimatedDrawableFrameInfo;
/**
 * 实现了这个接口的类只有{@link GifImage}和{@link WebPImage},
 * 这个接口的实现类用来储存Gif和Webp的数据，储存的方式是用本地内存byte数组。
 */
public interface AnimatedImage {

    int LOOP_COUNT_INFINITE = 0;

    /**
     * 将本地内存byte数组资源释放掉，一旦调用，其他使用了同一本地内存的实例上调用该方法会抛出异常
     * 注意,底层的本机资源可能不被真正释放,直到所有的相关实例{@link AnimatedImageFrame}都被处理
     */
    void dispose();

    /**
     * 得到动画的宽度
     * @return the width of the image
     */
    int getWidth();

    /**
     * 得到动画的高度
     * Gets the height of the image (also known as the canvas in WebP nomenclature).
     * @return the height of the image
     */
    int getHeight();

    /**
     * 获取动画的帧数
     *
     * @return the number of frames in the image
     */
    int getFrameCount();

    /**
     * 获取动画的播放时间
     *
     * @return the duration of the animated image in milliseconds
     */
    int getDuration();

    /**
     * 获取动画每一帧的的显示时间
     *
     * @return an array that is the size of the number of frames containing the duration of each frame
     *     in milliseconds
     */
    int[] getFrameDurations();

    /**
     * 得到的动画循环次数
     * @return the number of loops, or 0 to indicate infinite
     */
    int getLoopCount();

    /**
     * 创建一个{@link AnimatedImageFrame}，对于某个帧id
     * @param frameNumber the index of the frame
     * @return a newly created {@link AnimatedImageFrame}
     */
    AnimatedImageFrame getFrame(int frameNumber);

    /**
     * 返回是否{@link AnimatedImageFrame#renderFrame}支持缩放，因为有些时候动画的大小可能和被需要被绘制的区域的大小不符，此时就需要选择是否进行缩放
     *
     * @return whether rendering supports scaling
     */
    boolean doesRenderSupportScaling();

    /**
     * 获取该动画的字节数，该图像的数据应该已经在内存中
     * @return the size in bytes of the encoded image data
     */
    int getSizeInBytes();

    /**
     * 获取某一帧的信息
     * @param frameNumber the frame to get the info for
     * @return the frame info
     */
    AnimatedDrawableFrameInfo getFrameInfo(int frameNumber);
}

