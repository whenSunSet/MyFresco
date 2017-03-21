package com.facebook.imagepipeline.common;

/**
 * Created by heshixiyang on 2017/3/17.
 */

import android.graphics.Bitmap;

import com.facebook.imagepipeline.decoder.ImageDecoder;

import java.util.Locale;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * 改变{@code ImageDecoder}行为的选项
 * Options for changing the behavior of the {@code ImageDecoder}.
 */
@Immutable
public class ImageDecodeOptions {

    private static final ImageDecodeOptions DEFAULTS = ImageDecodeOptions.newBuilder().build();

    /**
     * 最小的解码中间结果的间隔
     * Decoding of intermediate results for an image won't happen more often that minDecodeIntervalMs.
     */
    public final int minDecodeIntervalMs;

    /**
     * 是否解码预览的帧，对于animated images
     * Whether to decode a preview frame for animated images.
     */
    public final boolean decodePreviewFrame;

    /**
     * 使用最后一帧，作为预览
     * Indicates that the last frame should be used as the preview frame instead of the first.
     */
    public final boolean useLastFrameForPreview;

    /**
     * 是否解码所有帧并且储存在内存之中，这个只能用于小的动画，在内存中缓存几十帧的GIF或者WEBP是不适合的
     * Whether to decode all the frames and store them in memory. This should only ever be used
     * for animations that are known to be small (e.g. stickers). Caching dozens of large Bitmaps
     * in memory for general GIFs or WebP's will not fit in memory.
     */
    public final boolean decodeAllFrames;

    /**
     * 是否绘制成一个静态的image，即时它是动画
     * Force image to be rendered as a static image, even if it is an animated format.
     *
     * 这个flag将迫使GIF被绘制成静态image
     * This flag will force animated GIFs to be rendered as static images
     */
    public final boolean forceStaticImage;

    /**
     * 静态image和JPEG将用这个解码
     * StaticImage and JPEG will decode with this config;
     */
    public final Bitmap.Config bitmapConfig;

    /**
     * 自定义的解码器
     * Custom image decoder override.
     */
    public final @Nullable
    ImageDecoder customImageDecoder;

    public ImageDecodeOptions(ImageDecodeOptionsBuilder b) {
        this.minDecodeIntervalMs = b.getMinDecodeIntervalMs();
        this.decodePreviewFrame = b.getDecodePreviewFrame();
        this.useLastFrameForPreview = b.getUseLastFrameForPreview();
        this.decodeAllFrames = b.getDecodeAllFrames();
        this.forceStaticImage = b.getForceStaticImage();
        this.bitmapConfig = b.getBitmapConfig();
        this.customImageDecoder = b.getCustomImageDecoder();
    }

    /**
     * 获取默认的解码器的选择
     * Gets the default options.
     *
     * @return the default options
     */
    public static ImageDecodeOptions defaults() {
        return DEFAULTS;
    }

    /**
     * Creates a new builder.
     *
     * @return a new builder
     */
    public static ImageDecodeOptionsBuilder newBuilder() {
        return new ImageDecodeOptionsBuilder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageDecodeOptions that = (ImageDecodeOptions) o;

        if (decodePreviewFrame != that.decodePreviewFrame) return false;
        if (useLastFrameForPreview != that.useLastFrameForPreview) return false;
        if (decodeAllFrames != that.decodeAllFrames) return false;
        if (forceStaticImage != that.forceStaticImage) return false;
        if (bitmapConfig != that.bitmapConfig) return false;
        if (customImageDecoder != that.customImageDecoder) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = minDecodeIntervalMs;
        result = 31 * result + (decodePreviewFrame ? 1 : 0);
        result = 31 * result + (useLastFrameForPreview ? 1 : 0);
        result = 31 * result + (decodeAllFrames ? 1 : 0);
        result = 31 * result + (forceStaticImage ? 1 : 0);
        result = 31 * result + bitmapConfig.ordinal();
        result = 31 * result + (customImageDecoder != null ? customImageDecoder.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format(
                (Locale) null,
                "%d-%b-%b-%b-%b-%s-%s",
                minDecodeIntervalMs,
                decodePreviewFrame,
                useLastFrameForPreview,
                decodeAllFrames,
                forceStaticImage,
                bitmapConfig.name(),
                customImageDecoder);
    }
}
