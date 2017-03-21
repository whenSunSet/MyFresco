package com.facebook.imagepipeline.animated.factory;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import android.graphics.Bitmap;

import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.image.impl.CloseableImage;
import com.facebook.imagepipeline.image.impl.EncodedImage;

/**
 * 动画image的解码器
 * Decoder for animated images.
 */
public interface AnimatedImageFactory {

    /**
     * 将GIF解码到一个CloseableImage中
     * Decodes a GIF into a CloseableImage.
     * @param encodedImage encoded image (native byte array holding the encoded bytes and meta data)
     * @param options the options for the decode
     * @param bitmapConfig the Bitmap.Config used to generate the output bitmaps
     * @return a {@link CloseableImage} for the GIF image
     */
    CloseableImage decodeGif(
            final EncodedImage encodedImage,
            final ImageDecodeOptions options,
            final Bitmap.Config bitmapConfig);

    /**
     * 将WebP解码到一个CloseableImage中
     * Decode a WebP into a CloseableImage.
     * @param encodedImage encoded image (native byte array holding the encoded bytes and meta data)
     * @param options the options for the decode
     * @param bitmapConfig the Bitmap.Config used to generate the output bitmaps
     * @return a {@link CloseableImage} for the WebP image
     */
    CloseableImage decodeWebP(
            final EncodedImage encodedImage,
            final ImageDecodeOptions options,
            final Bitmap.Config bitmapConfig);

}
