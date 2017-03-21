package com.facebook.animation.bitmap;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.support.annotation.Nullable;

import com.facebook.animation.backend.AnimationBackend;

/**
 * 这个被使用在{@link BitmapAnimationBackend}来绘制ige动画化的image
 * 例如GIF和动画化的WebPs
 * Bitmap frame renderer used by {@link BitmapAnimationBackend} to render
 * animated images (e.g. GIFs or animated WebPs).
 */
public interface BitmapFrameRenderer {

    /**
     * 渲染指定的帧
     * Render the frame for the given frame number to the target bitmap.
     *
     * @param frameNumber the frame number to render
     * @param targetBitmap the bitmap to render the frame in
     * @return true if successful
     */
    boolean renderFrame(int frameNumber, Bitmap targetBitmap);

    /**
     * 设置父drawable的bounds，以用在帧的绘制上
     * Set the parent drawable bounds to be used for frame rendering.
     *
     * @param bounds the bounds to use
     */
    void setBounds(@Nullable Rect bounds);

    /**
     * Return the intrinsic width of bitmap frames.
     * Return {@link AnimationBackend#INTRINSIC_DIMENSION_UNSET} if no specific width is set.
     *
     * @return the intrinsic width
     */
    int getIntrinsicWidth();

    /**
     * Return the intrinsic height of bitmap frames.
     * Return {@link AnimationBackend#INTRINSIC_DIMENSION_UNSET} if no specific height is set.
     *
     * @return the intrinsic height
     */
    int getIntrinsicHeight();
}
