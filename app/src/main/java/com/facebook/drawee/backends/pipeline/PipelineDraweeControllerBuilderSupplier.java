package com.facebook.drawee.backends.pipeline;

import android.content.Context;

import com.facebook.commom.executors.impl.UiThreadImmediateExecutorService;
import com.facebook.commom.internal.Supplier;
import com.facebook.drawee.componnents.DeferredReleaser;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.imagepipeline.animated.factory.AnimatedDrawableFactory;
import com.facebook.imagepipeline.animated.factory.AnimatedFactory;
import com.facebook.imagepipeline.core.impl.ImagePipeline;
import com.facebook.imagepipeline.core.impl.ImagePipelineFactory;

import java.util.Set;

import javax.annotation.Nullable;

/**
 * Created by heshixiyang on 2017/3/19.
 */
//一个提供PipelineDraweeControllerBuilder的Supplier
public class PipelineDraweeControllerBuilderSupplier implements
        Supplier<PipelineDraweeControllerBuilder> {

    private final Context mContext;
    private final ImagePipeline mImagePipeline;
    private final PipelineDraweeControllerFactory mPipelineDraweeControllerFactory;
    private final Set<ControllerListener> mBoundControllerListeners;

    public PipelineDraweeControllerBuilderSupplier(Context context) {
        this(context, null);
    }

    public PipelineDraweeControllerBuilderSupplier(
            Context context,
            @Nullable DraweeConfig draweeConfig) {
        this(context, ImagePipelineFactory.getInstance(), draweeConfig);
    }

    public PipelineDraweeControllerBuilderSupplier(
            Context context,
            ImagePipelineFactory imagePipelineFactory,
            @Nullable DraweeConfig draweeConfig) {
        this(context, imagePipelineFactory, null, draweeConfig);
    }

    public PipelineDraweeControllerBuilderSupplier(
            Context context,
            ImagePipelineFactory imagePipelineFactory,
            Set<ControllerListener> boundControllerListeners,
            @Nullable DraweeConfig draweeConfig) {
        mContext = context;
        mImagePipeline = imagePipelineFactory.getImagePipeline();

        final AnimatedFactory animatedFactory = imagePipelineFactory.getAnimatedFactory();
        AnimatedDrawableFactory animatedDrawableFactory = null;
        if (animatedFactory != null) {
            animatedDrawableFactory = animatedFactory.getAnimatedDrawableFactory(context);
        }
        if (draweeConfig != null && draweeConfig.getPipelineDraweeControllerFactory() != null) {
            mPipelineDraweeControllerFactory = draweeConfig.getPipelineDraweeControllerFactory();
        } else {
            mPipelineDraweeControllerFactory = new PipelineDraweeControllerFactory();
        }
        mPipelineDraweeControllerFactory.init(
                context.getResources(),
                DeferredReleaser.getInstance(),
                animatedDrawableFactory,
                UiThreadImmediateExecutorService.getInstance(),
                mImagePipeline.getBitmapMemoryCache(),
                draweeConfig != null
                        ? draweeConfig.getCustomDrawableFactories()
                        : null,
                draweeConfig != null
                        ? draweeConfig.getDebugOverlayEnabledSupplier()
                        : null);
        mBoundControllerListeners = boundControllerListeners;
    }

    @Override
    public PipelineDraweeControllerBuilder get() {
        return new PipelineDraweeControllerBuilder(
                mContext,
                mPipelineDraweeControllerFactory,
                mImagePipeline,
                mBoundControllerListeners);
    }
}
