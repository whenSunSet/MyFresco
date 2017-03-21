package com.facebook.animation.bitmap;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.facebook.commom.references.CloseableReference;

/**
 * 位图帧缓存用于动画图像
 * Bitmap frame cache that is used for animated images.
 */
public interface BitmapFrameCache {

    interface FrameCacheListener {

        /**
         * 当被给定的帧已经被放入帧缓存中的时候调用
         * Called when the frame for the given frame number has been put in the frame cache.
         * @param bitmapFrameCache the frame cache that holds the frame
         * @param frameNumber the cached frame number
         */
        void onFrameCached(BitmapFrameCache bitmapFrameCache, int frameNumber);

        /**
         * 当指定帧被驱逐的时候被调用
         * Called when the frame for the given frame number has been evicted from the frame cache.
         *
         * @param bitmapFrameCache the frame cache that evicted the frame
         * @param frameNumber the frame number of the evicted frame
         */
        void onFrameEvicted(BitmapFrameCache bitmapFrameCache, int frameNumber);
    }

    /**
     * 从帧缓存中获取指定的帧
     * Get the cached frame for the given frame number.
     *
     * @param frameNumber the frame number to get the cached frame for
     * @return the cached frame or null if not cached
     */
    @Nullable
    CloseableReference<Bitmap> getCachedFrame(int frameNumber);

    /**
     * 获取一个退回帧通过指定的帧number，如果其他所有试图画一个帧的操作都失败了就调用这个方法
     * Get a fallback frame for the given frame number. This method is called if all other attempts
     * to draw a frame failed.
     * 例如最后一个帧被返回
     * The bitmap returned could for example be the last drawn frame (if any).
     *
     * @param frameNumber the frame number to get the fallback
     * @return the fallback frame or null if not cached
     */
    @Nullable
    CloseableReference<Bitmap> getFallbackFrame(int frameNumber);

    /**
     * 返回一个可重用的bitmap，这个应该被绘制在一个指定的帧上
     * Return a reusable bitmap that should be used to render the given frame.
     *
     * @param frameNumber the frame number to be rendered
     * @param width the width of the target bitmap
     * @param height the height of the target bitmap
     * @return the reusable bitmap or null if no reusable bitmaps available
     */
    @Nullable
    CloseableReference<Bitmap> getBitmapToReuseForFrame(int frameNumber, int width, int height);

    /**
     * @return the size in bytes of all cached data
     */
    int getSizeInBytes();

    /**
     * 清除缓存
     * Clear the cache.
     */
    void clear();

    /**
     * 在被给予的bitmap被画在canvas上的时候调用
     * 这个bitmap可以变成一个重用的bitmap在{@link #getBitmapToReuseForFrame(int, int, int)}中，
     * 或者变成一个新的bitmap
     * Callback when the given bitmap has been drawn to a canvas.
     * This bitmap can either be a reused bitmap returned by
     * {@link #getBitmapToReuseForFrame(int, int, int)} or a new bitmap.
     *
     * 注意：这个实现需要手动实现被给于的bitmap的克隆，如果其希望留下这个bitmap
     * Note: the implementation of this interface has to manually clone the given bitmap reference
     * if it wants to hold on to the bitmap.
     * 其起源的reference将会自动关闭，当这个方法被调用完毕之后
     * The original reference will be automatically closed after this call.
     *
     * @param frameNumber the frame number that has been rendered
     * @param bitmap the bitmap that has been rendered
     * @param frameType the frame type that has been rendered
     */
    void onFrameRendered(
            int frameNumber,
            CloseableReference<Bitmap> bitmap,
            int frameType);

    /**
     * Set a frame cache listener that gets notified about caching events.
     *
     * @param frameCacheListener the listener to use
     */
    void setFrameCacheListener(FrameCacheListener frameCacheListener);
}
