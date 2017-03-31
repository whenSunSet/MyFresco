package com.facebook.imagepipeline.decoder;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.image.impl.CloseableImage;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.image.QualityInfo;

/**
 * image解码接口，从一个{@link EncodedImage}中获取一个{@link CloseableImage}
 * 用于一张图片解码
 * Image decoder interface. Takes an {@link EncodedImage} and creates a {@link CloseableImage}.
 */
public interface ImageDecoder {

    CloseableImage decode(
            EncodedImage encodedImage,
            int length,
            QualityInfo qualityInfo,
            ImageDecodeOptions options);
}

