package com.facebook.commom.webp;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import android.graphics.Bitmap;

/**
 * 这是一个实用的class，我们使用这个是为了分配一个被CloseableReference包装的bitmap
 * This is a utility class we use in order to allocate a Bitmap that will be wrapped later
 * with a CloseableReference
 */
public interface BitmapCreator {

    /**
     * 这个返回一个被CloseableReference包装的bitmap
     * This creates a Bitmap with will be then wrapped with a CloseableReference
     *
     * @param width The width of the image
     * @param height The height of the image
     * @param bitmapConfig The Config object to use
     * @return The Bitmap
     */
    Bitmap createNakedBitmap(
            int width,
            int height,
            Bitmap.Config bitmapConfig);
}
