package com.facebook.drawee.backends.pipeline;

/**
 * Created by heshixiyang on 2017/3/19.
 */

import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.facebook.cache.commom.CacheKey;
import com.facebook.commom.internal.ImmutableList;
import com.facebook.commom.internal.Objects;
import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.internal.Supplier;
import com.facebook.commom.logging.FLog;
import com.facebook.commom.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.base.DrawableWithCaches;
import com.facebook.drawee.componnents.DeferredReleaser;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.debug.DebugControllerOverlayDrawable;
import com.facebook.drawee.drawable.impl.OrientedDrawable;
import com.facebook.drawee.drawable.impl.ScaleTypeDrawable;
import com.facebook.drawee.drawable.impl.ScalingUtils;
import com.facebook.drawee.drawable.impl.ScalingUtils.ScaleType;
import com.facebook.drawee.interfaces.DraweeHierarchy;
import com.facebook.drawee.interfaces.SettableDraweeHierarchy;
import com.facebook.imagepipeline.animated.factory.AnimatedDrawableFactory;
import com.facebook.imagepipeline.cache.MemoryCache;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.impl.CloseableImage;
import com.facebook.imagepipeline.image.impl.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.impl.EncodedImage;

import java.util.concurrent.Executor;

import javax.annotation.Nullable;

/**
 *
 * Drawee controller 桥接 image pipeline 和 SettableDraweeHierarchy，在图像层次中真正的图像的数据来源于data source
 * data source 是自动获取和关闭的当attach / detach 被调用的时候
 *
 * Drawee controller that bridges the image pipeline with {@link SettableDraweeHierarchy}. <p> The
 * hierarchy's actual image is set to the image(s) obtained by the provided data source. The data
 * source is automatically obtained and closed based on attach / detach calls.
 */
public class PipelineDraweeController
        extends AbstractDraweeController<CloseableReference<CloseableImage>, ImageInfo> {

    private static final Class<?> TAG = PipelineDraweeController.class;

    // Components
    private final Resources mResources;
    private final AnimatedDrawableFactory mAnimatedDrawableFactory;
    @Nullable
    private final ImmutableList<DrawableFactory> mDrawableFactories;

    private @Nullable MemoryCache<CacheKey, CloseableImage> mMemoryCache;

    private CacheKey mCacheKey;

    // Constant state (non-final because controllers can be reused)
    private Supplier<DataSource<CloseableReference<CloseableImage>>> mDataSourceSupplier;

    private boolean mDrawDebugOverlay;

    private final DrawableFactory mDefaultDrawableFactory = new DrawableFactory() {

        @Override
        public boolean supportsImageType(CloseableImage image) {
            return true;
        }

        @Override
        public Drawable createDrawable(CloseableImage closeableImage) {
            if (closeableImage instanceof CloseableStaticBitmap) {
                CloseableStaticBitmap closeableStaticBitmap = (CloseableStaticBitmap) closeableImage;
                Drawable bitmapDrawable = new BitmapDrawable(
                        mResources,
                        closeableStaticBitmap.getUnderlyingBitmap());
                if (closeableStaticBitmap.getRotationAngle() == 0 ||
                        closeableStaticBitmap.getRotationAngle() == EncodedImage.UNKNOWN_ROTATION_ANGLE) {
                    return bitmapDrawable;
                } else {
                    return new OrientedDrawable(bitmapDrawable, closeableStaticBitmap.getRotationAngle());
                }
            } else if (mAnimatedDrawableFactory != null) {
                return mAnimatedDrawableFactory.create(closeableImage);
            }
            return null;
        }
    };

    public PipelineDraweeController(
            Resources resources,
            DeferredReleaser deferredReleaser,
            AnimatedDrawableFactory animatedDrawableFactory,
            Executor uiThreadExecutor,
            MemoryCache<CacheKey, CloseableImage> memoryCache,
            Supplier<DataSource<CloseableReference<CloseableImage>>> dataSourceSupplier,
            String id,
            CacheKey cacheKey,
            Object callerContext) {
        this(
                resources,
                deferredReleaser,
                animatedDrawableFactory,
                uiThreadExecutor,
                memoryCache,
                dataSourceSupplier,
                id,
                cacheKey,
                callerContext,
                null);
    }

    public PipelineDraweeController(
            Resources resources,
            DeferredReleaser deferredReleaser,
            AnimatedDrawableFactory animatedDrawableFactory,
            Executor uiThreadExecutor,
            MemoryCache<CacheKey, CloseableImage> memoryCache,
            Supplier<DataSource<CloseableReference<CloseableImage>>> dataSourceSupplier,
            String id,
            CacheKey cacheKey,
            Object callerContext,
            @Nullable ImmutableList<DrawableFactory> drawableFactories) {
        super(deferredReleaser, uiThreadExecutor, id, callerContext);
        mResources = resources;
        mAnimatedDrawableFactory = animatedDrawableFactory;
        mMemoryCache = memoryCache;
        mCacheKey = cacheKey;
        mDrawableFactories = drawableFactories;
        init(dataSourceSupplier);
    }

    /**
     * 初始化控制器和新的数据源，这个可以运行使用已经存在的控制器而不是使用新的，这个方法应该被调用在控制器处于detached的状态
     *
     * Initializes this controller with the new data source supplier, id and caller context. This
     * allows for reusing of the existing controller instead of instantiating a new one. This method
     * should be called when the controller is in detached state.
     *
     * @param dataSourceSupplier data source supplier
     * @param id unique id for this controller
     * @param callerContext tag and context for this controller
     */
    public void initialize(
            Supplier<DataSource<CloseableReference<CloseableImage>>> dataSourceSupplier,
            String id,
            CacheKey cacheKey,
            Object callerContext) {
        super.initialize(id, callerContext);
        init(dataSourceSupplier);
        mCacheKey = cacheKey;
    }

    public void setDrawDebugOverlay(boolean drawDebugOverlay) {
        mDrawDebugOverlay = drawDebugOverlay;
    }

    private void init(Supplier<DataSource<CloseableReference<CloseableImage>>> dataSourceSupplier) {
        mDataSourceSupplier = dataSourceSupplier;

        maybeUpdateDebugOverlay(null);
    }

    protected Resources getResources() {
        return mResources;
    }

    @Override
    protected DataSource<CloseableReference<CloseableImage>> getDataSource() {
        if (FLog.isLoggable(FLog.VERBOSE)) {
            FLog.v(TAG, "controller %x: getDataSource", System.identityHashCode(this));
        }
        return mDataSourceSupplier.get();
    }

    @Override
    protected Drawable createDrawable(CloseableReference<CloseableImage> image) {
        Preconditions.checkState(CloseableReference.isValid(image));
        CloseableImage closeableImage = image.get();

        maybeUpdateDebugOverlay(closeableImage);

        if (mDrawableFactories != null) {
            for (DrawableFactory factory : mDrawableFactories) {
                if (factory.supportsImageType(closeableImage)) {
                    Drawable drawable = factory.createDrawable(closeableImage);
                    if (drawable != null) {
                        return drawable;
                    }
                }
            }
        }

        Drawable defaultDrawable = mDefaultDrawableFactory.createDrawable(closeableImage);
        if (defaultDrawable != null) {
            return defaultDrawable;
        }
        throw new UnsupportedOperationException("Unrecognized image class: " + closeableImage);
    }

    @Override
    public void setHierarchy(@Nullable DraweeHierarchy hierarchy) {
        super.setHierarchy(hierarchy);
        maybeUpdateDebugOverlay(null);
    }

    private void maybeUpdateDebugOverlay(@Nullable CloseableImage image) {
        if (!mDrawDebugOverlay) {
            return;
        }
        Drawable controllerOverlay = getControllerOverlay();

        if (controllerOverlay == null) {
            controllerOverlay = new DebugControllerOverlayDrawable();
            setControllerOverlay(controllerOverlay);
        }

        if (controllerOverlay instanceof DebugControllerOverlayDrawable) {
            DebugControllerOverlayDrawable debugOverlay =
                    (DebugControllerOverlayDrawable) controllerOverlay;
            debugOverlay.setControllerId(getId());

            final DraweeHierarchy draweeHierarchy = getHierarchy();
            ScaleType scaleType = null;
            if (draweeHierarchy != null) {
                final ScaleTypeDrawable scaleTypeDrawable =
                        ScalingUtils.getActiveScaleTypeDrawable(draweeHierarchy.getTopLevelDrawable());
                scaleType = scaleTypeDrawable != null ? scaleTypeDrawable.getScaleType() : null;
            }
            debugOverlay.setScaleType(scaleType);

            if (image != null) {
                debugOverlay.setDimensions(image.getWidth(), image.getHeight());
                debugOverlay.setImageSize(image.getSizeInBytes());
            } else {
                debugOverlay.reset();
            }
        }
    }

    @Override
    protected ImageInfo getImageInfo(CloseableReference<CloseableImage> image) {
        Preconditions.checkState(CloseableReference.isValid(image));
        return image.get();
    }

    @Override
    protected int getImageHash(@Nullable CloseableReference<CloseableImage> image) {
        return (image != null) ? image.getValueHash() : 0;
    }

    @Override
    protected void releaseImage(@Nullable CloseableReference<CloseableImage> image) {
        CloseableReference.closeSafely(image);
    }

    @Override
    protected void releaseDrawable(@Nullable Drawable drawable) {
        if (drawable instanceof DrawableWithCaches) {
            ((DrawableWithCaches) drawable).dropCaches();
        }
    }

    @Override
    protected CloseableReference<CloseableImage> getCachedImage() {
        if (mMemoryCache == null || mCacheKey == null) {
            return null;
        }
        // We get the CacheKey
        CloseableReference<CloseableImage> closeableImage = mMemoryCache.get(mCacheKey);
        if (closeableImage != null && !closeableImage.get().getQualityInfo().isOfFullQuality()) {
            closeableImage.close();
            return null;
        }
        return closeableImage;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("super", super.toString())
                .add("dataSourceSupplier", mDataSourceSupplier)
                .toString();
    }
}

