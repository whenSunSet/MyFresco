package com.facebook.imagepipeline.bitmaps;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import android.graphics.Bitmap;

import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.platform.GingerbreadPurgeableDecoder;

/**
 * Android 2.4 bitmap的制造工厂使用了{@link GingerbreadPurgeableDecoder}
 * Bitmap factory for Gingerbread.
 */
public class GingerbreadBitmapFactory extends PlatformBitmapFactory {

    /**
     * 创建一个bitmap以一个固定的大小
     * Creates a bitmap of the specified width and height.
     *
     * @param width the width of the bitmap
     * @param height the height of the bitmap
     * @param bitmapConfig the {@link Bitmap.Config}
     * used to create the decoded Bitmap
     * @return a reference to the bitmap
     * @throws OutOfMemoryError if the Bitmap cannot be allocated
     */
    @Override
    public CloseableReference<Bitmap> createBitmapInternal(
            int width,
            int height,
            Bitmap.Config bitmapConfig) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, bitmapConfig);
        return CloseableReference.of(bitmap, SimpleBitmapReleaser.getInstance());
    }
}
