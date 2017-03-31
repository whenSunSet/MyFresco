package com.facebook.imagepipeline.animated.base.impl;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import android.graphics.Bitmap;

import com.facebook.animated.gif.GifImage;
import com.facebook.animated.webp.WebPImage;
import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.animated.base.AnimatedImage;
import com.facebook.imagepipeline.animated.factory.impl.AnimatedImageFactoryImpl;

import java.util.List;

/**
 * {@link AnimatedImageResult}的build
 * Builder for {@link AnimatedImageResult}.
 */
public class AnimatedImageResultBuilder {

    /**
     * {@link GifImage}或{@link WebPImage}的实例，储存该动画的所有数据
     */
    private final AnimatedImage mImage;
    /**
     * 动画的预览帧，在{@link AnimatedImageFactoryImpl#createPreviewBitmap}中被创建
     */
    private CloseableReference<Bitmap> mPreviewBitmap;
    /**
     * 动画的所有帧，在{@link AnimatedImageFactoryImpl#decodeAllFrames}中被创建
     */
    private List<CloseableReference<Bitmap>> mDecodedFrames;
    /**
     * 预览帧是使用第一帧还是最后一帧，在{@link AnimatedImageFactoryImpl#getCloseableImage}中被设置
     */
    private int mFrameForPreview;

    AnimatedImageResultBuilder(AnimatedImage image) {
        mImage = image;
    }

    /**
     * Gets the image for the result.
     * @return the image
     */
    public AnimatedImage getImage() {
        return mImage;
    }

    /**
     * Gets the preview bitmap. This method returns a new reference. The caller must close it.
     *
     * @return the reference to the preview bitmap or null if none was set. This returns a reference
     *    that must be released by the caller
     */
    public CloseableReference<Bitmap> getPreviewBitmap() {
        return CloseableReference.cloneOrNull(mPreviewBitmap);
    }

    /**
     * Sets a preview bitmap.
     *
     * @param previewBitmap the preview. The method clones the reference.
     * @return this builder
     */
    public AnimatedImageResultBuilder setPreviewBitmap(CloseableReference<Bitmap> previewBitmap) {
        mPreviewBitmap = CloseableReference.cloneOrNull(previewBitmap);
        return this;
    }

    /**
     * Gets the frame that should be used for the preview image. If the preview bitmap was fetched,
     * this is the frame that it's for.
     *
     * @return the frame that should be used for the preview image
     */
    public int getFrameForPreview() {
        return mFrameForPreview;
    }

    /**
     * Sets the frame that should be used for the preview image. If the preview bitmap was fetched,
     * this is the frame that it's for.
     *
     * @return the frame that should be used for the preview image
     */
    public AnimatedImageResultBuilder setFrameForPreview(int frameForPreview) {
        mFrameForPreview = frameForPreview;
        return this;
    }

    /**
     * Gets the decoded frames. Only used if the {@code ImageDecodeOptions} were configured to
     * decode all frames at decode time.
     *
     * @return the references to the decoded frames or null if none was set. This returns references
     *    that must be released by the caller
     */
    public List<CloseableReference<Bitmap>> getDecodedFrames() {
        return CloseableReference.cloneOrNull(mDecodedFrames);
    }

    /**
     * Sets the decoded frames. Only used if the {@code ImageDecodeOptions} were configured to
     * decode all frames at decode time.
     *
     * @param decodedFrames the decoded frames. The method clones the references.
     */
    public AnimatedImageResultBuilder setDecodedFrames(
            List<CloseableReference<Bitmap>> decodedFrames) {
        mDecodedFrames = CloseableReference.cloneOrNull(decodedFrames);
        return this;
    }

    /**
     * Builds the {@link AnimatedImageResult}. The preview bitmap and the decoded frames are closed
     * after build is called, so this should not be called more than once or those fields will be lost
     * after the first call.
     *
     * @return the result
     */
    public AnimatedImageResult build() {
        try {
            return new AnimatedImageResult(this);
        } finally {
            CloseableReference.closeSafely(mPreviewBitmap);
            mPreviewBitmap = null;
            CloseableReference.closeSafely(mDecodedFrames);
            mDecodedFrames = null;
        }
    }
}
