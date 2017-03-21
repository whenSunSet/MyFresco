package com.facebook.imagepipeline.platform;

import android.graphics.Bitmap;

import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.common.TooManyBitmapsException;
import com.facebook.imagepipeline.image.impl.EncodedImage;

/**
 * Created by heshixiyang on 2017/3/10.
 */
public interface PlatformDecoder {
    /**
     * 从编码的bytes中创建一个bitmap，支持JPEG但是调用者需要使用
     * {@link #decodeJPEGFromEncodedImage}来更好的解析JPEGs
     * Creates a bitmap from encoded bytes. Supports JPEG but callers should use {@link
     * #decodeJPEGFromEncodedImage} for partial JPEGs.
     *
     * @param encodedImage the reference to the encoded image with the reference to the encoded bytes
     * @param bitmapConfig the {@link android.graphics.Bitmap.Config} used to create the decoded
     * Bitmap
     * @return the bitmap
     * @throws TooManyBitmapsException if the pool is full
     * @throws java.lang.OutOfMemoryError if the Bitmap cannot be allocated
     */
    CloseableReference<Bitmap> decodeFromEncodedImage(
            final EncodedImage encodedImage,
            Bitmap.Config bitmapConfig);

    /**
     * 从编码的bytes中创建一个bitmap，更偏向于支持JPEG的图片
     * Creates a bitmap from encoded JPEG bytes. Supports a partial JPEG image.
     *
     * @param encodedImage the reference to the encoded image with the reference to the encoded bytes
     * @param bitmapConfig the {@link android.graphics.Bitmap.Config} used to create the decoded
     * Bitmap
     * @param length the number of encoded bytes in the buffer
     * @return the bitmap
     * @throws TooManyBitmapsException if the pool is full
     * @throws java.lang.OutOfMemoryError if the Bitmap cannot be allocated
     */
    CloseableReference<Bitmap> decodeJPEGFromEncodedImage(
            EncodedImage encodedImage,
            Bitmap.Config bitmapConfig,
            int length);
}
