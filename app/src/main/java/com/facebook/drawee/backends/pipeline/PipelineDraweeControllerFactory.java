package com.facebook.drawee.backends.pipeline;

/**
 * Created by heshixiyang on 2017/3/19.
 */

import android.content.res.Resources;

import com.facebook.cache.commom.CacheKey;
import com.facebook.commom.internal.ImmutableList;
import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.internal.Supplier;
import com.facebook.commom.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.componnents.DeferredReleaser;
import com.facebook.imagepipeline.animated.factory.AnimatedDrawableFactory;
import com.facebook.imagepipeline.cache.MemoryCache;
import com.facebook.imagepipeline.image.impl.CloseableImage;

import java.util.concurrent.Executor;

import javax.annotation.Nullable;

/**
 * 一个PipelineDraweeController的默认实现工厂
 * Default implementation of {@link PipelineDraweeControllerFactory}.
 */
public class PipelineDraweeControllerFactory {

    private Resources mResources;
    private DeferredReleaser mDeferredReleaser;
    private AnimatedDrawableFactory mAnimatedDrawableFactory;
    private Executor mUiThreadExecutor;
    private MemoryCache<CacheKey, CloseableImage> mMemoryCache;
    @Nullable
    private ImmutableList<DrawableFactory> mDrawableFactories;
    @Nullable
    private Supplier<Boolean> mDebugOverlayEnabledSupplier;

    public void init(
            Resources resources,
            DeferredReleaser deferredReleaser,
            AnimatedDrawableFactory animatedDrawableFactory,
            Executor uiThreadExecutor,
            MemoryCache<CacheKey, CloseableImage> memoryCache,
            @Nullable ImmutableList<DrawableFactory> drawableFactories,
            @Nullable Supplier<Boolean> debugOverlayEnabledSupplier) {
        mResources = resources;
        mDeferredReleaser = deferredReleaser;
        mAnimatedDrawableFactory = animatedDrawableFactory;
        mUiThreadExecutor = uiThreadExecutor;
        mMemoryCache = memoryCache;
        mDrawableFactories = drawableFactories;
        mDebugOverlayEnabledSupplier = debugOverlayEnabledSupplier;
    }

    public PipelineDraweeController newController(
            Supplier<DataSource<CloseableReference<CloseableImage>>> dataSourceSupplier,
            String id,
            CacheKey cacheKey,
            Object callerContext) {
        Preconditions.checkState(mResources != null, "init() not called");
        // Field values passed as arguments so that any subclass of PipelineDraweeControllerFactory
        // can simply override internalCreateController() and return a custom Drawee controller
        PipelineDraweeController controller = internalCreateController(
                mResources,
                mDeferredReleaser,
                mAnimatedDrawableFactory,
                mUiThreadExecutor,
                mMemoryCache,
                mDrawableFactories,
                dataSourceSupplier,
                id,
                cacheKey,
                callerContext);
        if (mDebugOverlayEnabledSupplier != null) {
            controller.setDrawDebugOverlay(mDebugOverlayEnabledSupplier.get());
        }
        return controller;
    }

    protected PipelineDraweeController internalCreateController(
            Resources resources,
            DeferredReleaser deferredReleaser,
            AnimatedDrawableFactory animatedDrawableFactory,
            Executor uiThreadExecutor,
            MemoryCache<CacheKey, CloseableImage> memoryCache,
            @Nullable ImmutableList<DrawableFactory> drawableFactories,
            Supplier<DataSource<CloseableReference<CloseableImage>>> dataSourceSupplier,
            String id,
            CacheKey cacheKey,
            Object callerContext) {
        return new PipelineDraweeController(
                resources,
                deferredReleaser,
                animatedDrawableFactory,
                uiThreadExecutor,
                memoryCache,
                dataSourceSupplier,
                id,
                cacheKey,
                callerContext,
                drawableFactories);
    }
}
