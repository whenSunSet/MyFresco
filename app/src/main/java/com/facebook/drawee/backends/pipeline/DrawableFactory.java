package com.facebook.drawee.backends.pipeline;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.facebook.imagepipeline.image.impl.CloseableImage;

/**
 * 一个Drawable工程为了从指定的image中创建一个Drawable
 * Drawable factory to create Drawables for given images.
 */
public interface DrawableFactory {

    /**
     * 返回true如果工程可以从被给予的image中创建drawable
     * Returns true if the factory can create a Drawable for the given image.
     *
     * @param image the image to check
     * @return true if a Drawable can be created
     */
    boolean supportsImageType(CloseableImage image);

    /**
     * 创建igeDrawable从被给定的iamge中
     * Create a drawable for the given image.
     * 这个应该被保证的是只有{@link #supportsImageType(CloseableImage)}返回的是ture的时候才被调用
     * It is guaranteed that this method is only called if
     * {@link #supportsImageType(CloseableImage)} returned true.
     *
     * @param image the image to create the drawable for
     * @return the Drawable for the image or null if an error occurred
     */
    @Nullable
    Drawable createDrawable(CloseableImage image);
}
