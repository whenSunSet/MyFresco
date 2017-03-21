package com.facebook.drawee.backends.pipeline;

/**
 * Created by heshixiyang on 2017/3/19.
 */

import android.content.Context;
import android.net.Uri;

import com.facebook.cache.commom.CacheKey;
import com.facebook.commom.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.controller.AbstractDraweeControllerBuilder;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.imagepipeline.cache.CacheKeyFactory;
import com.facebook.imagepipeline.common.RotationOptions;
import com.facebook.imagepipeline.core.impl.ImagePipeline;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.impl.CloseableImage;
import com.facebook.imagepipeline.request.impl.ImageRequest;
import com.facebook.imagepipeline.request.impl.ImageRequestBuilder;

import java.util.Set;

import javax.annotation.Nullable;

/**
 * 一个提供ImagePipeline Drawee controller 的builder
 * Concrete implementation of ImagePipeline Drawee controller builder.
 * <p/> See {@link AbstractDraweeControllerBuilder} for more details.
 */
public class PipelineDraweeControllerBuilder extends AbstractDraweeControllerBuilder<
        PipelineDraweeControllerBuilder,
        ImageRequest,
        CloseableReference<CloseableImage>,
        ImageInfo> {

    private final ImagePipeline mImagePipeline;
    private final PipelineDraweeControllerFactory mPipelineDraweeControllerFactory;

    public PipelineDraweeControllerBuilder(
            Context context,
            PipelineDraweeControllerFactory pipelineDraweeControllerFactory,
            ImagePipeline imagePipeline,
            Set<ControllerListener> boundControllerListeners) {
        super(context, boundControllerListeners);
        mImagePipeline = imagePipeline;
        mPipelineDraweeControllerFactory = pipelineDraweeControllerFactory;
    }

    @Override
    public PipelineDraweeControllerBuilder setUri(@Nullable Uri uri) {
        if (uri == null) {
            return super.setImageRequest(null);
        }
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                .setRotationOptions(RotationOptions.autoRotateAtRenderTime())
                .build();
        return super.setImageRequest(imageRequest);
    }

    @Override
    public PipelineDraweeControllerBuilder setUri(@Nullable String uriString) {
        if (uriString == null || uriString.isEmpty()) {
            return super.setImageRequest(ImageRequest.fromUri(uriString));
        }
        return setUri(Uri.parse(uriString));
    }

    //实现了父类的抽象方法，通过obtainDataSourceSupplier()、generateUniqueControllerId()、
    // getCacheKey()和PipelineDraweeControllerFactory这几个方法初始化了PipelineDraweeController
    @Override
    protected PipelineDraweeController obtainController() {
        DraweeController oldController = getOldController();
        PipelineDraweeController controller;
        if (oldController instanceof PipelineDraweeController) {
            controller = (PipelineDraweeController) oldController;
            controller.initialize(
                    obtainDataSourceSupplier(),
                    generateUniqueControllerId(),
                    getCacheKey(),
                    getCallerContext());
        } else {
            controller = mPipelineDraweeControllerFactory.newController(
                    obtainDataSourceSupplier(),
                    generateUniqueControllerId(),
                    getCacheKey(),
                    getCallerContext());
        }
        return controller;
    }

    private CacheKey getCacheKey() {
        final ImageRequest imageRequest = getImageRequest();
        final CacheKeyFactory cacheKeyFactory = mImagePipeline.getCacheKeyFactory();
        CacheKey cacheKey = null;
        if (cacheKeyFactory != null && imageRequest != null) {
            if (imageRequest.getPostprocessor() != null) {
                cacheKey = cacheKeyFactory.getPostprocessedBitmapCacheKey(
                        imageRequest,
                        getCallerContext());
            } else {
                cacheKey = cacheKeyFactory.getBitmapCacheKey(
                        imageRequest,
                        getCallerContext());
            }
        }
        return cacheKey;
    }

    //通过ImagePipeline.fetchDecodedImage(ImageRequest ,CacheLevel )获取DataSource。
    @Override
    protected DataSource<CloseableReference<CloseableImage>> getDataSourceForRequest(
            ImageRequest imageRequest,
            Object callerContext,
            CacheLevel cacheLevel) {
        return mImagePipeline.fetchDecodedImage(
                imageRequest,
                callerContext,
                convertCacheLevelToRequestLevel(cacheLevel));
    }

    @Override
    protected PipelineDraweeControllerBuilder getThis() {
        return this;
    }

    public static ImageRequest.RequestLevel convertCacheLevelToRequestLevel(
            AbstractDraweeControllerBuilder.CacheLevel cacheLevel) {
        switch (cacheLevel) {
            case FULL_FETCH:
                return ImageRequest.RequestLevel.FULL_FETCH;
            case DISK_CACHE:
                return ImageRequest.RequestLevel.DISK_CACHE;
            case BITMAP_MEMORY_CACHE:
                return ImageRequest.RequestLevel.BITMAP_MEMORY_CACHE;
            default:
                throw new RuntimeException("Cache level" + cacheLevel + "is not supported. ");
        }
    }
}
