package com.facebook.imagepipeline.animated.factory.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;

import com.facebook.animated.gif.GifImage;
import com.facebook.animated.webp.WebPImage;
import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableBackend;
import com.facebook.imagepipeline.animated.base.AnimatedImage;
import com.facebook.imagepipeline.animated.base.impl.AnimatedImageResult;
import com.facebook.imagepipeline.animated.factory.AnimatedImageDecoder;
import com.facebook.imagepipeline.animated.factory.AnimatedImageFactory;
import com.facebook.imagepipeline.animated.impl.AnimatedDrawableBackendProvider;
import com.facebook.imagepipeline.animated.impl.AnimatedImageCompositor;
import com.facebook.imagepipeline.animated.impl.impl.AnimatedDrawableBackendImpl;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.image.impl.CloseableAnimatedImage;
import com.facebook.imagepipeline.image.impl.CloseableImage;
import com.facebook.imagepipeline.image.impl.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.image.impl.ImmutableQualityInfo;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.memory.impl.NativePooledByteBuffer;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于将一个EncodedImage解码的工厂，从{@link AnimatedFactoryImpl}中产生
 * Decoder for animated images.
 */
public class AnimatedImageFactoryImpl implements AnimatedImageFactory {

    /**
     * 外部传入,用于提供{@link AnimatedDrawableBackendImpl}
     */
    private final AnimatedDrawableBackendProvider mAnimatedDrawableBackendProvider;

    /**
     * 外部传入,{@link AnimatedFactoryImpl}提供的
     */
    private final PlatformBitmapFactory mBitmapFactory;

    /**
     * 分别为{@link GifImage}和{@link WebPImage}，它们内部用jni代码解码gif和webp的数据，
     * 又由于它们都实现了AnimatedImage，所以解码完毕的数据又创建一个新的WebPImage或GifImage来储存
     */
    static AnimatedImageDecoder sGifAnimatedImageDecoder = null;
    static AnimatedImageDecoder sWebpAnimatedImageDecoder = null;

    private static AnimatedImageDecoder loadIfPresent(final String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return (AnimatedImageDecoder) clazz.newInstance();
        } catch (Throwable e) {
            return null;
        }
    }

    static {
        sGifAnimatedImageDecoder = loadIfPresent("com.facebook.animated.gif.GifImage");
        sWebpAnimatedImageDecoder = loadIfPresent("com.facebook.animated.webp.WebPImage");
    }

    public AnimatedImageFactoryImpl(
            AnimatedDrawableBackendProvider animatedDrawableBackendProvider,
            PlatformBitmapFactory bitmapFactory) {
        mAnimatedDrawableBackendProvider = animatedDrawableBackendProvider;
        mBitmapFactory = bitmapFactory;
    }

    /**
     * 将一个GIF的{@link EncodedImage}解码进入CloseableImage
     * Decodes a GIF into a CloseableImage.
     *
     * 1.如果sGifAnimatedImageDecoder没初始化就抛出异常{@link UnsupportedOperationException},
     * 2.用sGifAnimatedImageDecoder解码{@link EncodedImage}提供的{@link PooledByteBuffer}默认实现是{@link NativePooledByteBuffer}
     * 3.将2中产生的{@link AnimatedImage}即{@link GifImage}，传入{@link #getCloseableImage}
     *
     * 参数解释和接口中一致
     * @param encodedImage encoded image (native byte array holding the encoded bytes and meta data)
     * @param options the options for the decode
     * @param bitmapConfig the Bitmap.Config used to generate the output bitmaps
     * @return a {@link CloseableImage} for the GIF image
     */
    public CloseableImage decodeGif(
            final EncodedImage encodedImage,
            final ImageDecodeOptions options,
            final Bitmap.Config bitmapConfig) {
        if (sGifAnimatedImageDecoder == null) {
            throw new UnsupportedOperationException("To encode animated gif please add the dependency " +
                    "to the animated-gif module");
        }
        final CloseableReference<PooledByteBuffer> bytesRef = encodedImage.getByteBufferRef();
        Preconditions.checkNotNull(bytesRef);
        try {
            final PooledByteBuffer input = bytesRef.get();
            AnimatedImage gifImage = sGifAnimatedImageDecoder.decode(input.getNativePtr(), input.size());

            return getCloseableImage(options, gifImage, bitmapConfig);
        } finally {
            CloseableReference.closeSafely(bytesRef);
        }
    }

    /**
     * 解码WebP进入CloseableImage
     * Decode a WebP into a CloseableImage.
     *
     * 方法进行的流程和{@link #decodeGif}类似
     *
     * 参数解释和接口中一致
     * @param encodedImage encoded image (native byte array holding the encoded bytes and meta data)
     * @param options the options for the decode
     * @param bitmapConfig the Bitmap.Config used to generate the output bitmaps
     * @return a {@link CloseableImage} for the WebP image
     */
    public CloseableImage decodeWebP(
            final EncodedImage encodedImage,
            final ImageDecodeOptions options,
            final Bitmap.Config bitmapConfig) {
        if (sWebpAnimatedImageDecoder == null) {
            throw new UnsupportedOperationException("To encode animated webp please add the dependency " +
                    "to the animated-webp module");
        }
        final CloseableReference<PooledByteBuffer> bytesRef = encodedImage.getByteBufferRef();
        Preconditions.checkNotNull(bytesRef);
        try {
            final PooledByteBuffer input = bytesRef.get();
            AnimatedImage webPImage = sWebpAnimatedImageDecoder.decode(
                    input.getNativePtr(),
                    input.size());
            return getCloseableImage(options, webPImage, bitmapConfig);
        } finally {
            CloseableReference.closeSafely(bytesRef);
        }
    }

    /**
     * 1.判断是否将动画当成静态图片，如果是调用{@link #createPreviewBitmap}然后return一个{@link CloseableStaticBitmap}
     * 2.根据{@link ImageDecodeOptions#decodeAllFrames}判断释是否解码动画的所有帧，如果是调用{@link #decodeAllFrames}创建一个List<CloseableReference<Bitmap>>
     * 3.根据传入的{@link AnimatedImage}、创建的List<CloseableReference<Bitmap>>和创建的previewBitmap，创建一个{@link AnimatedImageResult}
     * 4.根据{@link AnimatedImageResult}创建一个{@link CloseableAnimatedImage}
     * @param options 图片解码的选项
     * @param image git或者webp的整体数据
     * @param bitmapConfig git和webp每一帧图片的Config
     * @return
     */
    private CloseableImage getCloseableImage(
            ImageDecodeOptions options,
            AnimatedImage image,
            Bitmap.Config bitmapConfig) {
        List<CloseableReference<Bitmap>> decodedFrames = null;
        CloseableReference<Bitmap> previewBitmap = null;
        try {
            final int frameForPreview = options.useLastFrameForPreview ? image.getFrameCount() - 1 : 0;
            if (options.forceStaticImage) {
                return new CloseableStaticBitmap(
                        createPreviewBitmap(image, bitmapConfig, frameForPreview),
                        ImmutableQualityInfo.FULL_QUALITY,
                        0);
            }

            if (options.decodeAllFrames) {
                decodedFrames = decodeAllFrames(image, bitmapConfig);
                previewBitmap = CloseableReference.cloneOrNull(decodedFrames.get(frameForPreview));
            }

            if (options.decodePreviewFrame && previewBitmap == null) {
                previewBitmap = createPreviewBitmap(image, bitmapConfig, frameForPreview);
            }
            AnimatedImageResult animatedImageResult = AnimatedImageResult.newBuilder(image)
                    .setPreviewBitmap(previewBitmap)
                    .setFrameForPreview(frameForPreview)
                    .setDecodedFrames(decodedFrames)
                    .build();
            return new CloseableAnimatedImage(animatedImageResult);
        } finally {
            CloseableReference.closeSafely(previewBitmap);
            CloseableReference.closeSafely(decodedFrames);
        }
    }

    /**
     * 创建一个动画的预览帧
     * 1.调用{@link #createBitmap}创建一个空的Bitmap(A)
     * 2.根据传入的AnimatedImage，创建一个{@link AnimatedImageResult}(B)
     * 3.根据B配合{@link #mAnimatedDrawableBackendProvider}创建一个{@link AnimatedDrawableBackend}(C)
     * 4.根据C创建一个{@link AnimatedImageCompositor}
     * 5.调用{@link AnimatedImageCompositor#renderFrame}渲染A,最后返回A
     *
     * 参数解释同上一个方法
     * @param image
     * @param bitmapConfig
     * @param frameForPreview
     * @return
     */
    private CloseableReference<Bitmap> createPreviewBitmap(
            AnimatedImage image,
            Bitmap.Config bitmapConfig,
            int frameForPreview) {
        CloseableReference<Bitmap> bitmap = createBitmap(
                image.getWidth(),
                image.getHeight(),
                bitmapConfig);
        AnimatedImageResult tempResult = AnimatedImageResult.forAnimatedImage(image);
        AnimatedDrawableBackend drawableBackend =
                mAnimatedDrawableBackendProvider.get(tempResult, null);
        AnimatedImageCompositor animatedImageCompositor = new AnimatedImageCompositor(
                drawableBackend,
                new AnimatedImageCompositor.Callback() {
                    @Override
                    public void onIntermediateResult(int frameNumber, Bitmap bitmap) {
                        // Don't care.
                    }

                    @Override
                    public CloseableReference<Bitmap> getCachedBitmap(int frameNumber) {
                        return null;
                    }
                });
        animatedImageCompositor.renderFrame(frameForPreview, bitmap.get());
        return bitmap;
    }

    /**
     * 解码所有动画的帧
     * 1.前几个步骤类似{@link #createPreviewBitmap}(A)的2、3、4、5
     * 2.在5的时候A中是只渲染动画的第一帧或者最后一帧，然后直接返回
     * 3.而在本方法中是渲染所有的帧然后返回一个list
     *
     * 参数解释同上一个方法
     * @param image
     * @param bitmapConfig
     * @return
     */
    private List<CloseableReference<Bitmap>> decodeAllFrames(
            AnimatedImage image,
            Bitmap.Config bitmapConfig) {
        AnimatedImageResult tempResult = AnimatedImageResult.forAnimatedImage(image);
        AnimatedDrawableBackend drawableBackend =
                mAnimatedDrawableBackendProvider.get(tempResult, null);
        final List<CloseableReference<Bitmap>> bitmaps =
                new ArrayList<>(drawableBackend.getFrameCount());
        AnimatedImageCompositor animatedImageCompositor = new AnimatedImageCompositor(
                drawableBackend,
                new AnimatedImageCompositor.Callback() {
                    @Override
                    public void onIntermediateResult(int frameNumber, Bitmap bitmap) {
                        // Don't care.
                    }

                    @Override
                    public CloseableReference<Bitmap> getCachedBitmap(int frameNumber) {
                        return CloseableReference.cloneOrNull(bitmaps.get(frameNumber));
                    }
                });
        for (int i = 0; i < drawableBackend.getFrameCount(); i++) {
            CloseableReference<Bitmap> bitmap = createBitmap(
                    drawableBackend.getWidth(),
                    drawableBackend.getHeight(),
                    bitmapConfig);
            animatedImageCompositor.renderFrame(i, bitmap.get());
            bitmaps.add(bitmap);
        }
        return bitmaps;
    }

    /**
     * 通过{@link #mBitmapFactory}创建空白的Bitmap
     * @param width
     * @param height
     * @param bitmapConfig
     * @return
     */
    @SuppressLint("NewApi")
    private CloseableReference<Bitmap> createBitmap(
            int width,
            int height,
            Bitmap.Config bitmapConfig) {
        CloseableReference<Bitmap> bitmap =
                mBitmapFactory.createBitmapInternal(width, height, bitmapConfig);
        bitmap.get().eraseColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            bitmap.get().setHasAlpha(true);
        }
        return bitmap;
    }
}
