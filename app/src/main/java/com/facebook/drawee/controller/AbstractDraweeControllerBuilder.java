package com.facebook.drawee.controller;

/**
 * Created by heshixiyang on 2017/3/19.
 */

import android.content.Context;
import android.graphics.drawable.Animatable;

import com.facebook.commom.internal.Objects;
import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.internal.Supplier;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.DataSources;
import com.facebook.datasource.impl.FirstAvailableDataSourceSupplier;
import com.facebook.datasource.impl.IncreasingQualityDataSourceSupplier;
import com.facebook.drawee.componnents.RetryManager;
import com.facebook.drawee.gestures.GestureDetector;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.interfaces.SimpleDraweeControllerBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;

/**
 * 一个基本的Drawee controller 建造者
 * Base implementation for Drawee controller builders.
 */
public abstract class AbstractDraweeControllerBuilder <
        BUILDER extends AbstractDraweeControllerBuilder<BUILDER, REQUEST, IMAGE, INFO>,
        REQUEST,
        IMAGE,
        INFO>
        implements SimpleDraweeControllerBuilder {

    private static final ControllerListener<Object> sAutoPlayAnimationsListener =
            new BaseControllerListener<Object>() {
                @Override
                public void onFinalImageSet(String id, @Nullable Object info, @Nullable Animatable anim) {
                    if (anim != null) {
                        anim.start();
                    }
                }
            };

    private static final NullPointerException NO_REQUEST_EXCEPTION =
            new NullPointerException("No image request was specified!");

    // components
    private final Context mContext;
    private final Set<ControllerListener> mBoundControllerListeners;

    // builder parameters
    private @Nullable Object mCallerContext;
    //普通请求
    private @Nullable REQUEST mImageRequest;
    //低精度请求
    private @Nullable REQUEST mLowResImageRequest;
    //多请求
    private @Nullable REQUEST[] mMultiImageRequests;
    private boolean mTryCacheOnlyFirst;
    private @Nullable
    Supplier<DataSource<IMAGE>> mDataSourceSupplier;
    private @Nullable ControllerListener<? super INFO> mControllerListener;
    private @Nullable ControllerViewportVisibilityListener mControllerViewportVisibilityListener;
    private boolean mTapToRetryEnabled;
    private boolean mAutoPlayAnimations;
    private boolean mRetainImageOnFailure;
    private String mContentDescription;
    // 上一次的DraweeController可以被重用
    private @Nullable DraweeController mOldController;

    private static final AtomicLong sIdCounter = new AtomicLong();

    protected AbstractDraweeControllerBuilder(
            Context context,
            Set<ControllerListener> boundControllerListeners) {
        mContext = context;
        mBoundControllerListeners = boundControllerListeners;
        init();
    }

    /** Initializes this builder. */
    private void init() {
        mCallerContext = null;
        mImageRequest = null;
        mLowResImageRequest = null;
        mMultiImageRequests = null;
        mTryCacheOnlyFirst = true;
        mControllerListener = null;
        mControllerViewportVisibilityListener = null;
        mTapToRetryEnabled = false;
        mAutoPlayAnimations = false;
        mOldController = null;
        mContentDescription = null;
    }

    /** Resets this builder to its initial values making it reusable. */
    public BUILDER reset() {
        init();
        return getThis();
    }

    /** Sets the caller context. */
    @Override
    public BUILDER setCallerContext(Object callerContext) {
        mCallerContext = callerContext;
        return getThis();
    }

    /** Gets the caller context. */
    @Nullable
    public Object getCallerContext() {
        return mCallerContext;
    }

    /** Sets the image request. */
    public BUILDER setImageRequest(REQUEST imageRequest) {
        mImageRequest = imageRequest;
        return getThis();
    }

    /** Gets the image request. */
    @Nullable
    public REQUEST getImageRequest() {
        return mImageRequest;
    }

    /** Sets the low-res image request. */
    public BUILDER setLowResImageRequest(REQUEST lowResImageRequest) {
        mLowResImageRequest = lowResImageRequest;
        return getThis();
    }

    /** Gets the low-res image request. */
    @Nullable
    public REQUEST getLowResImageRequest() {
        return mLowResImageRequest;
    }

    /**
     * Sets the array of first-available image requests that will be probed in order.
     * <p> For performance reasons, the array is not deep-copied, but only stored by reference.
     * Please don't modify once submitted.
     */
    public BUILDER setFirstAvailableImageRequests(REQUEST[] firstAvailableImageRequests) {
        return setFirstAvailableImageRequests(firstAvailableImageRequests, true);
    }

    /**
     * Sets the array of first-available image requests that will be probed in order.
     * <p> For performance reasons, the array is not deep-copied, but only stored by reference.
     * Please don't modify once submitted.
     *
     * @param tryCacheOnlyFirst if set, bitmap cache only requests will be tried in order before
     *    the supplied requests.
     */
    public BUILDER setFirstAvailableImageRequests(
            REQUEST[] firstAvailableImageRequests,
            boolean tryCacheOnlyFirst) {
        mMultiImageRequests = firstAvailableImageRequests;
        mTryCacheOnlyFirst = tryCacheOnlyFirst;
        return getThis();
    }

    /**
     * Gets the array of first-available image requests.
     * <p> For performance reasons, the array is not deep-copied, but only stored by reference.
     * Please don't modify.
     */
    @Nullable
    public REQUEST[] getFirstAvailableImageRequests() {
        return mMultiImageRequests;
    }

    /**
     *  Sets the data source supplier to be used.
     *
     *  <p/> Note: This is mutually exclusive with other image request setters.
     */
    public void setDataSourceSupplier(@Nullable Supplier<DataSource<IMAGE>> dataSourceSupplier) {
        mDataSourceSupplier = dataSourceSupplier;
    }

    /**
     * Gets the data source supplier if set.
     *
     * <p/>Important: this only returns the externally set data source (if any). Subclasses should
     * use {#code obtainDataSourceSupplier()} to obtain a data source to be passed to the controller.
     */
    @Nullable
    public Supplier<DataSource<IMAGE>> getDataSourceSupplier() {
        return mDataSourceSupplier;
    }

    /** Sets whether tap-to-retry is enabled. */
    public BUILDER setTapToRetryEnabled(boolean enabled) {
        mTapToRetryEnabled = enabled;
        return getThis();
    }

    /** Gets whether tap-to-retry is enabled. */
    public boolean getTapToRetryEnabled() {
        return mTapToRetryEnabled;
    }

    /** Sets whether to display last available image in case of failure. */
    public BUILDER setRetainImageOnFailure(boolean enabled) {
        mRetainImageOnFailure = enabled;
        return getThis();
    }

    /** Gets whether to retain image on failure. */
    public boolean getRetainImageOnFailure() {
        return mRetainImageOnFailure;
    }

    /** Sets whether to auto play animations. */
    public BUILDER setAutoPlayAnimations(boolean enabled) {
        mAutoPlayAnimations = enabled;
        return getThis();
    }

    /** Gets whether to auto play animations. */
    public boolean getAutoPlayAnimations() {
        return mAutoPlayAnimations;
    }

    /** Sets the controller listener. */
    public BUILDER setControllerListener(ControllerListener<? super INFO> controllerListener) {
        mControllerListener = controllerListener;
        return getThis();
    }

    /** Gets the controller listener */
    @Nullable
    public ControllerListener<? super INFO> getControllerListener() {
        return mControllerListener;
    }

    /** Sets the controller viewport visibility listener. */
    public BUILDER setControllerViewportVisibilityListener(
            @Nullable ControllerViewportVisibilityListener controllerViewportVisibilityListener) {
        mControllerViewportVisibilityListener = controllerViewportVisibilityListener;
        return getThis();
    }

    /** Gets the controller viewport visibility listener. */
    @Nullable
    public ControllerViewportVisibilityListener getControllerViewportVisibilityListener() {
        return mControllerViewportVisibilityListener;
    }

    /** Sets the accessibility content description. */
    public BUILDER setContentDescription(String contentDescription) {
        mContentDescription = contentDescription;
        return getThis();
    }

    /** Gets the accessibility content description. */
    @Nullable
    public String getContentDescription() {
        return mContentDescription;
    }

    /** Sets the old controller to be reused if possible. */
    @Override
    public BUILDER setOldController(@Nullable DraweeController oldController) {
        mOldController = oldController;
        return getThis();
    }

    /** Gets the old controller to be reused. */
    @Nullable
    public DraweeController getOldController() {
        return mOldController;
    }

    /**
     * 在创建之前调用validate()，如果只有一个低级请求，
     * 那么把它当做最后的请求，然后调用 buildController()返回。
     * ied controller. */
    @Override
    public AbstractDraweeController build() {
        validate();

        if (mImageRequest == null && mMultiImageRequests == null && mLowResImageRequest != null) {
            mImageRequest = mLowResImageRequest;
            mLowResImageRequest = null;
        }

        return buildController();
    }

    /**
     * 判断设置的请求是否正确，若错误就抛出异常
     * Validates the parameters before building a controller. */
    protected void validate() {
        Preconditions.checkState(
                (mMultiImageRequests == null) || (mImageRequest == null),
                "Cannot specify both ImageRequest and FirstAvailableImageRequests!");
        Preconditions.checkState(
                (mDataSourceSupplier == null) ||
                        (mMultiImageRequests == null && mImageRequest == null && mLowResImageRequest == null),
                "Cannot specify DataSourceSupplier with other ImageRequests! Use one or the other.");
    }

    /**
     * 调用obtainController()获取AbstractDraweeController，设置AbstractDraweeController的一些值，
     * maybeBuildAndSetRetryManager(controller)、maybeAttachListeners(controller)。
     * Builds a regular controller. */
    protected AbstractDraweeController buildController() {
        AbstractDraweeController controller = obtainController();
        controller.setRetainImageOnFailure(getRetainImageOnFailure());
        controller.setContentDescription(getContentDescription());
        controller.setControllerViewportVisibilityListener(getControllerViewportVisibilityListener());
        maybeBuildAndSetRetryManager(controller);
        maybeAttachListeners(controller);
        return controller;
    }

    /** Generates unique controller id. */
    protected static String generateUniqueControllerId() {
        return String.valueOf(sIdCounter.getAndIncrement());
    }

    /**
     * 如果已经有就返回，如果low、hight、Multi三种请求中有一种不为null，
     * 就通过getDataSourceSupplierForRequest(ImageRequest)来获取DataSourceSupplier，
     * 如果请求都为null就返回null。
     * Gets the top-level data source supplier to be used by a controller. */
    protected Supplier<DataSource<IMAGE>> obtainDataSourceSupplier() {
        if (mDataSourceSupplier != null) {
            return mDataSourceSupplier;
        }

        Supplier<DataSource<IMAGE>> supplier = null;

        // final image supplier;
        if (mImageRequest != null) {
            supplier = getDataSourceSupplierForRequest(mImageRequest);
        } else if (mMultiImageRequests != null) {
            supplier = getFirstAvailableDataSourceSupplier(mMultiImageRequests, mTryCacheOnlyFirst);
        }

        // increasing-quality supplier; highest-quality supplier goes first
        if (supplier != null && mLowResImageRequest != null) {
            List<Supplier<DataSource<IMAGE>>> suppliers = new ArrayList<>(2);
            suppliers.add(supplier);
            suppliers.add(getDataSourceSupplierForRequest(mLowResImageRequest));
            supplier = IncreasingQualityDataSourceSupplier.create(suppliers);
        }

        // no image requests; use null data source supplier
        if (supplier == null) {
            supplier = DataSources.getFailedDataSourceSupplier(NO_REQUEST_EXCEPTION);
        }

        return supplier;
    }

    //多请求处理。
    protected Supplier<DataSource<IMAGE>> getFirstAvailableDataSourceSupplier(
            REQUEST[] imageRequests,
            boolean tryBitmapCacheOnlyFirst) {
        List<Supplier<DataSource<IMAGE>>> suppliers = new ArrayList<>(imageRequests.length * 2);
        if (tryBitmapCacheOnlyFirst) {
            // we first add bitmap-cache-only suppliers, then the full-fetch ones
            for (int i = 0; i < imageRequests.length; i++) {
                suppliers.add(
                        getDataSourceSupplierForRequest(imageRequests[i], CacheLevel.BITMAP_MEMORY_CACHE));
            }
        }
        for (int i = 0; i < imageRequests.length; i++) {
            suppliers.add(getDataSourceSupplierForRequest(imageRequests[i]));
        }
        return FirstAvailableDataSourceSupplier.create(suppliers);
    }

    /** Creates a data source supplier for the given image request. */
    protected Supplier<DataSource<IMAGE>> getDataSourceSupplierForRequest(REQUEST imageRequest) {
        return getDataSourceSupplierForRequest(imageRequest, CacheLevel.FULL_FETCH);
    }

    /**
     * 创建一个Supplier<DataSource<IMAGE>>()，内部重写了get()，
     * get内部调用了getDataSourceForRequest(imageRequest, callerContext, cacheLevel)为抽象方法，
     * 在子类中实现。
     * Creates a data source supplier for the given image request. */
    protected Supplier<DataSource<IMAGE>> getDataSourceSupplierForRequest(
            final REQUEST imageRequest,
            final CacheLevel cacheLevel) {
        final Object callerContext = getCallerContext();
        return new Supplier<DataSource<IMAGE>>() {
            @Override
            public DataSource<IMAGE> get() {
                return getDataSourceForRequest(imageRequest, callerContext, cacheLevel);
            }
            @Override
            public String toString() {
                return Objects.toStringHelper(this)
                        .add("request", imageRequest.toString())
                        .toString();
            }
        };
    }

    /**
     * 为AbstractDraweeController添加本对象中所有Listenner
     * Attaches listeners (if specified) to the given controller. */
    protected void maybeAttachListeners(AbstractDraweeController controller) {
        if (mBoundControllerListeners != null) {
            for (ControllerListener<? super INFO> listener : mBoundControllerListeners) {
                controller.addControllerListener(listener);
            }
        }
        if (mControllerListener != null) {
            controller.addControllerListener(mControllerListener);
        }
        if (mAutoPlayAnimations) {
            controller.addControllerListener(sAutoPlayAnimationsListener);
        }
    }

    /**
     * 为AbstractDraweeController添加点击重试，并调用maybeBuildAndSetGestureDetector()。
     * Installs a retry manager (if specified) to the given controller. */
    protected void maybeBuildAndSetRetryManager(AbstractDraweeController controller) {
        if (!mTapToRetryEnabled) {
            return;
        }
        RetryManager retryManager = controller.getRetryManager();
        if (retryManager == null) {
            retryManager = new RetryManager();
            controller.setRetryManager(retryManager);
        }
        retryManager.setTapToRetryEnabled(mTapToRetryEnabled);
        maybeBuildAndSetGestureDetector(controller);
    }

    /**
     * 为AbstractDraweeController添加手势代理
     * Installs a gesture detector to the given controller. */
    protected void maybeBuildAndSetGestureDetector(AbstractDraweeController controller) {
        GestureDetector gestureDetector = controller.getGestureDetector();
        if (gestureDetector == null) {
            gestureDetector = GestureDetector.newInstance(mContext);
            controller.setGestureDetector(gestureDetector);
        }
    }

    /* Gets the context. */
    protected Context getContext() {
        return mContext;
    }

    /** Concrete builder classes should override this method to return a new controller. */
    protected abstract AbstractDraweeController obtainController();

    /**
     * Concrete builder classes should override this method to return a data source for the request.
     *
     * <p/>IMPORTANT: Do NOT ever call this method directly. This method is only to be called from
     * a supplier created in {#code getDataSourceSupplierForRequest(REQUEST, boolean)}.
     *
     * <p/>IMPORTANT: Make sure that you do NOT use any non-final field from this method, as the field
     * may change if the instance of this builder gets reused. If any such field is required, override
     * {#code getDataSourceSupplierForRequest(REQUEST, boolean)}, and store the field in a final
     * variable (same as it is done for callerContext).
     */
    protected abstract DataSource<IMAGE> getDataSourceForRequest(
            final REQUEST imageRequest,
            final Object callerContext,
            final CacheLevel cacheLevel);

    /** Concrete builder classes should override this method to return {#code this}. */
    protected abstract BUILDER getThis();

    public enum CacheLevel {
        /* Fetch (from the network or local storage) */
        FULL_FETCH,

        /* Disk caching */
        DISK_CACHE,

        /* Bitmap caching */
        BITMAP_MEMORY_CACHE;
    }
}
