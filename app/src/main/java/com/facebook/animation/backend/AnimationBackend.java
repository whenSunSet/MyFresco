package com.facebook.animation.backend;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;

/**
 * 动画后端的接口用于绘制帧
 * Animation backend interface that is used to draw frames.
 */
public interface AnimationBackend extends AnimationInformation {

    /**
     * 如果本身的长宽没有设置那么这就是默认值
     * Default value if the intrinsic dimensions are not set.
     *
     * @see #getIntrinsicWidth()
     * @see #getIntrinsicHeight()
     */
    int INTRINSIC_DIMENSION_UNSET = -1;

    /**
     * 在canvas上绘制被给定的帧
     * Draw the frame for the given frame number on the canvas.
     *
     * @param parent the parent that draws the frame
     * @param canvas the canvas to draw an
     * @param frameNumber the frame number of the frame to draw
     * @return true if successful, false if the frame could not be rendered
     */
    boolean drawFrame(Drawable parent, Canvas canvas, int frameNumber);

    /**
     * 如果支持的话，就设置alpha值给需要被绘制的帧对这个方法{@link #drawFrame(Drawable, Canvas, int)}
     * Set the alpha value to be used for drawing frames in {@link #drawFrame(Drawable, Canvas, int)}
     * if supported.
     *
     * @param alpha the alpha value between 0 and 255
     */
    void setAlpha(@IntRange(from=0,to=255) int alpha);

    /**
     * 如果支持的话，就设置色彩过滤给需要被绘制的帧，对这个方法{@link #drawFrame(Drawable, Canvas, int)}
     * The color filter to be used for drawing frames in {@link #drawFrame(Drawable, Canvas, int)}
     * if supported.
     *
     * @param colorFilter the color filter to use
     */
    void setColorFilter(@Nullable ColorFilter colorFilter);

    /**
     * 当父drawable的bounds被更新的时候调用
     * Called when the bounds of the parent drawable are updated.
     *
     * This can be used to perform some ahead-of-time computations if needed.
     *
     * The supplied bounds do not have to be stored. It is possible to just use
     * {@link Drawable#getBounds()} of the parent drawable of
     * {@link #drawFrame(Drawable, Canvas, int)} instead.
     *
     * @param bounds the bounds to be used for drawing frames
     */
    void setBounds(Rect bounds);

    /**
     * Get the intrinsic width of the underlying animation or
     * {@link #INTRINSIC_DIMENSION_UNSET} if not available.
     *
     * This value is used by the underlying drawable for aspect ratio computations,
     * similar to {@link Drawable#getIntrinsicWidth()}.
     *
     * @return the width or {@link #INTRINSIC_DIMENSION_UNSET} if unset
     */
    int getIntrinsicWidth();

    /**
     * 获取正在播放的动画的intrinsic height
     * Get the intrinsic height of the underlying animation or
     * {@link #INTRINSIC_DIMENSION_UNSET} if not available.
     *
     * This value is used by the underlying drawable for aspect ratio computations,
     * similar to {@link Drawable#getIntrinsicHeight()}.
     *
     * @return the height or {@link #INTRINSIC_DIMENSION_UNSET} if unset
     */
    int getIntrinsicHeight();

    /**
     * 对animation backend.获取byte数值
     * Get the size of the animation backend.
     * @return the size in bytes
     *
     */
    int getSizeInBytes();

    /**
     * 清理动画数据。这将被调用当drawable被清除
     * Clean up animation data. This will be called when the backing drawable is cleared as well.
     * 例如，删除所有的frame缓存
     * For example, drop all cached frames.
     */
    void clear();
}
