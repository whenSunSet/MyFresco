package com.facebook.imagepipeline.animated.factory;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import android.graphics.Bitmap;

import com.facebook.imagepipeline.animated.factory.impl.AnimatedImageFactoryImpl;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.image.impl.CloseableImage;
import com.facebook.imagepipeline.image.impl.EncodedImage;

/**
 * 用于将一个EncodedImage解码的接口工厂，默认实现是{@link AnimatedImageFactoryImpl}
 * Decoder for animated images.
 */
public interface AnimatedImageFactory {

    /**
     * 将GIF解码到一个CloseableImage中
     * Decodes a GIF into a CloseableImage.
     *
     * @param encodedImage encoded image (native byte array holding the encoded bytes and meta data)
     *                     这个EncodedImage内部是用本地内存储存byte数据和图片的元数据。
     * @param options the options for the decode
     *                解码的options
     * @param bitmapConfig the Bitmap.Config used to generate the output bitmaps
     *                     产生输出Bitmap的Config
     * @return a {@link CloseableImage} for the GIF image
     */
    CloseableImage decodeGif(
            final EncodedImage encodedImage,
            final ImageDecodeOptions options,
            final Bitmap.Config bitmapConfig);

    /**
     * 将WebP解码到一个CloseableImage中
     * Decode a WebP into a CloseableImage.
     *
     * 参数的解释同上一方法
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
