package com.facebook.imagepipeline.decoder.impl;

/**
 * Created by heshixiyang on 2017/3/17.
 */

import android.graphics.Bitmap;

import com.facebook.commom.internal.Closeables;
import com.facebook.commom.references.CloseableReference;
import com.facebook.imageformat.DefaultImageFormats;
import com.facebook.imageformat.ImageFormat;
import com.facebook.imageformat.ImageFormatChecker;
import com.facebook.imagepipeline.animated.factory.AnimatedImageFactory;
import com.facebook.imagepipeline.animated.factory.impl.AnimatedImageFactoryImpl;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.decoder.ImageDecoder;
import com.facebook.imagepipeline.image.impl.CloseableImage;
import com.facebook.imagepipeline.image.impl.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.image.impl.ImmutableQualityInfo;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imagepipeline.platform.PlatformDecoder;

import java.io.InputStream;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * 解码image
 * Decodes images.
 *
 * mDefaultDecoder实现了image类型的识别，然后将image传入相应的方法进行解码
 * <p> ImageDecoder implements image type recognition and passes decode requests to
 * specialized methods implemented by subclasses.
 *
 * On dalvik, it produces 'pinned' purgeable bitmaps.
 *
 * <p> Pinned purgeables behave as specified in
 * {@link android.graphics.BitmapFactory.Options#inPurgeable} with one modification. The bitmap is
 * 'pinned' so is never purged.
 *
 * <p> For API 21 and higher, this class produces standard Bitmaps, as purgeability is not supported
 * on the most recent versions of Android.
 */
public class DefaultImageDecoder implements ImageDecoder {

    /**
     * 解码Gif和Webp的对象，默认的实现是{@link AnimatedImageFactoryImpl}
     */
    private final AnimatedImageFactory mAnimatedImageFactory;
    private final Bitmap.Config mBitmapConfig;
    /**
     * 解码静态图片的对象，不同的系统版本的实现不同
     */
    private final PlatformDecoder mPlatformDecoder;
    /**
     * 在{@link #decode}中最后被调用，用于根据图片类型判断到底使用哪种解码器
     */
    private final ImageDecoder mDefaultDecoder = new ImageDecoder() {
        @Override
        public CloseableImage decode(
                EncodedImage encodedImage,
                int length,
                QualityInfo qualityInfo,
                ImageDecodeOptions options) {
            ImageFormat imageFormat = encodedImage.getImageFormat();
            if (imageFormat == DefaultImageFormats.JPEG) {
                return decodeJpeg(encodedImage, length, qualityInfo, options);
            } else if (imageFormat == DefaultImageFormats.GIF) {
                return decodeGif(encodedImage, options);
            } else if (imageFormat == DefaultImageFormats.WEBP_ANIMATED) {
                return decodeAnimatedWebp(encodedImage, options);
            } else if (imageFormat == ImageFormat.UNKNOWN) {
                throw new IllegalArgumentException("unknown image format");
            }
            return decodeStaticImage(encodedImage, options);
        }
    };

    @Nullable
    private final Map<ImageFormat, ImageDecoder> mCustomDecoders;

    public DefaultImageDecoder(
            final AnimatedImageFactory animatedImageFactory,
            final PlatformDecoder platformDecoder,
            final Bitmap.Config bitmapConfig) {
        this(animatedImageFactory, platformDecoder, bitmapConfig, null);
    }

    public DefaultImageDecoder(
            final AnimatedImageFactory animatedImageFactory,
            final PlatformDecoder platformDecoder,
            final Bitmap.Config bitmapConfig,
            @Nullable Map<ImageFormat, ImageDecoder> customDecoders) {
        mAnimatedImageFactory = animatedImageFactory;
        mBitmapConfig = bitmapConfig;
        mPlatformDecoder = platformDecoder;
        mCustomDecoders = customDecoders;
    }

    /**
     * 一般是调用了{@link ImageDecoder#decode}之后，这个方法就被调用了。
     * 1.如果传入参数options，中有使用者定义的解码器，那么就使用用户定义的解码器，然后直接返回
     * 2.使用ImageFormatChecker检测传入的EncodedImage的资源格式
     * 3.如果本类中的{@link #mCustomDecoders}之前就被使用者设置了，那么就用其进行解码
     * 4.最后调用{@link #mDefaultDecoder}的decode()方法。
     *
     * @param encodedImage input image (encoded bytes plus meta data)
     * @param length 如果图片类型支持残缺解码，那么这个参数就代表到底解码多少图片
     * @param qualityInfo quality information for the image
     *                    图片质量信息
     * @param options options that cange decode behavior
     *                解码时候可以选择的参数
     */
    @Override
    public CloseableImage decode(
            final EncodedImage encodedImage,
            final int length,
            final QualityInfo qualityInfo,
            final ImageDecodeOptions options) {
        if (options.customImageDecoder != null) {
            return options.customImageDecoder.decode(encodedImage, length, qualityInfo, options);
        }
        ImageFormat imageFormat = encodedImage.getImageFormat();
        if (imageFormat == null || imageFormat == ImageFormat.UNKNOWN) {
            imageFormat = ImageFormatChecker.getImageFormat_WrapIOException(
                    encodedImage.getInputStream());
            encodedImage.setImageFormat(imageFormat);
        }
        if (mCustomDecoders != null) {
            ImageDecoder decoder = mCustomDecoders.get(imageFormat);
            if (decoder != null) {
                return decoder.decode(encodedImage, length, qualityInfo, options);
            }
        }
        return mDefaultDecoder.decode(encodedImage, length, qualityInfo, options);
    }

    /**
     * 解码Gif动画
     * Decodes gif into CloseableImage.
     *
     * 1.判断传入参数options，是否只是需要静态图片，如果是那么调用{@link #decodeStaticImage}
     * 2.如果不是那么调用{@link AnimatedImageFactoryImpl#decodeGif}来解码Gif动画
     *
     * @param encodedImage input image (encoded bytes plus meta data)
     * @return a CloseableImage
     */
    public CloseableImage decodeGif(
            EncodedImage encodedImage,
            ImageDecodeOptions options) {
        InputStream is = encodedImage.getInputStream();
        if (is == null) {
            return null;
        }
        try {
            if (!options.forceStaticImage
                    && mAnimatedImageFactory != null) {
                return mAnimatedImageFactory.decodeGif(encodedImage, options, mBitmapConfig);
            }
            return decodeStaticImage(encodedImage, options);
        } finally {
            Closeables.closeQuietly(is);
        }
    }

    /**
     *
     * 使用{@link #mPlatformDecoder}解码静态图片,不同的系统版本实现不同
     * @param encodedImage input image (encoded bytes plus meta data)
     * @return a CloseableStaticBitmap
     */
    public CloseableStaticBitmap decodeStaticImage(
            final EncodedImage encodedImage,
            ImageDecodeOptions options) {
        CloseableReference<Bitmap> bitmapReference =
                mPlatformDecoder.decodeFromEncodedImage(encodedImage, options.bitmapConfig);
        try {
            return new CloseableStaticBitmap(
                    bitmapReference,
                    ImmutableQualityInfo.FULL_QUALITY,
                    encodedImage.getRotationAngle());
        } finally {
            bitmapReference.close();
        }
    }

    /**
     * 使用{@link #mPlatformDecoder}解码JPEG图片,不同系统版本的实现不同
     * Decodes a partial jpeg.
     *
     * @param encodedImage input image (encoded bytes plus meta data)
     * @param length amount of currently available data in bytes
     * @param qualityInfo quality info for the image
     * @return a CloseableStaticBitmap
     */
    public CloseableStaticBitmap decodeJpeg(
            final EncodedImage encodedImage,
            int length,
            QualityInfo qualityInfo,
            ImageDecodeOptions options) {
        CloseableReference<Bitmap> bitmapReference =
                mPlatformDecoder.decodeJPEGFromEncodedImage(encodedImage, options.bitmapConfig, length);
        try {
            return new CloseableStaticBitmap(
                    bitmapReference,
                    qualityInfo,
                    encodedImage.getRotationAngle());
        } finally {
            bitmapReference.close();
        }
    }

    /**
     * 解码Webp的动画文件，注意Webp也有静态的，若Webp为静态，那么就是调用{@link #decodeStaticImage}来解析
     * Decode a webp animated image into a CloseableImage.
     * 这里调用了{@link AnimatedImageFactoryImpl#decodeWebP}来解码Webp动画
     * <p> The image is decoded into a 'pinned' purgeable bitmap.
     *
     * @param encodedImage input image (encoded bytes plus meta data)
     * @param options
     * @return a {@link CloseableImage}
     */
    public CloseableImage decodeAnimatedWebp(
            final EncodedImage encodedImage,
            final ImageDecodeOptions options) {
        return mAnimatedImageFactory.decodeWebP(encodedImage, options, mBitmapConfig);
    }
}
