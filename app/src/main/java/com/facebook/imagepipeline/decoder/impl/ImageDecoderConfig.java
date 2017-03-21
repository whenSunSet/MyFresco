package com.facebook.imagepipeline.decoder.impl;

/**
 * Created by heshixiyang on 2017/3/17.
 */

import com.facebook.imageformat.DefaultImageFormats;
import com.facebook.imageformat.ImageFormat;
import com.facebook.imagepipeline.decoder.ImageDecoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link ImageDecoder}的配置
 * Configuration for {@link ImageDecoder}.
 */
public class ImageDecoderConfig {

    private final Map<ImageFormat, ImageDecoder> mCustomImageDecoders;

    private final List<ImageFormat.FormatChecker> mCustomImageFormats;

    private ImageDecoderConfig(Builder builder) {
        mCustomImageDecoders = builder.mCustomImageDecoders;
        mCustomImageFormats = builder.mCustomImageFormats;
    }

    public Map<ImageFormat, ImageDecoder> getCustomImageDecoders() {
        return mCustomImageDecoders;
    }

    public List<ImageFormat.FormatChecker> getCustomImageFormats() {
        return mCustomImageFormats;
    }

    public static Builder newBuilder() {
        return new Builder();
    }
    public static class Builder {
        private Map<ImageFormat, ImageDecoder> mCustomImageDecoders;
        private List<ImageFormat.FormatChecker> mCustomImageFormats;

        /**
         * 添加一个新的解码器给一个image格式
         * Add a new decoding cabability for a new image format.
         *
         * @param imageFormat the new image format
         * @param imageFormatChecker the format checker that can determine the new image format
         * @param decoder the decoder that can decode the new image format
         * @return the builder
         */
        public Builder addDecodingCapability(
                ImageFormat imageFormat,
                ImageFormat.FormatChecker imageFormatChecker,
                ImageDecoder decoder) {
            if (mCustomImageFormats == null) {
                mCustomImageFormats = new ArrayList<>();
            }
            mCustomImageFormats.add(imageFormatChecker);
            overrideDecoder(imageFormat, decoder);
            return this;
        }

        /**
         * 设置覆盖一个解码器
         * Use a different decoder for an existing image format.
         * This can be used for example to set a custom decoder for any of the
         * {@link DefaultImageFormats}
         *
         * @param imageFormat the existing image format
         * @param decoder the decoder to use
         * @return the builder
         */
        public Builder overrideDecoder(ImageFormat imageFormat, ImageDecoder decoder) {
            if (mCustomImageDecoders == null) {
                mCustomImageDecoders  = new HashMap<>();
            }
            mCustomImageDecoders.put(imageFormat, decoder);
            return this;
        }

        public ImageDecoderConfig build() {
            return new ImageDecoderConfig(this);
        }
    }
}
