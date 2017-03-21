package com.facebook.drawee.generic;

/**
 * Created by heshixiyang on 2017/3/19.
 */

import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import com.facebook.commom.internal.Preconditions;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import static com.facebook.drawee.drawable.impl.ScalingUtils.ScaleType;

/**
 * 一个{@link GenericDraweeHierarchy}的构造器
 * Class to construct a {@link GenericDraweeHierarchy}.
 * Drawable们一定是一个不能被重用的混合层次。每一个层次需要有一个他们自己的drawable实例
 * 因为这个builder 不做深拷贝对于输入的参数，所以调用者有责任传入不同的drawable实例让hierarchy构造
 * 同样的，hierarchies也不能被重用在复数个view之中。每一个view需要有自己的hierarchy实例
 * 调用者有责任为每一个view构造一个新的hierarchy
 * <p/> Drawables must not be reused by multiple hierarchies. Each hierarchy needs to have its own
 * drawable instances. Since this builder does not do deep copies of the input parameters, it is
 * the caller's responsibility to pass a different drawable instances for each hierarchy built.
 * Likewise, hierarchies must not be reused by multiple views. Each view needs to have its own
 * instance of a hierarchy. The caller is responsible for building a new hierarchy for each view.
 */
public class GenericDraweeHierarchyBuilder {

    public static final int DEFAULT_FADE_DURATION = 300;
    public static final ScaleType DEFAULT_SCALE_TYPE = ScaleType.CENTER_INSIDE;
    public static final ScaleType DEFAULT_ACTUAL_IMAGE_SCALE_TYPE = ScaleType.CENTER_CROP;

    private Resources mResources;

    private int mFadeDuration;

    private float mDesiredAspectRatio;

    private Drawable mPlaceholderImage;
    private @Nullable
    ScaleType mPlaceholderImageScaleType;

    private Drawable mRetryImage;
    private ScaleType mRetryImageScaleType;

    private Drawable mFailureImage;
    private ScaleType mFailureImageScaleType;

    private Drawable mProgressBarImage;
    private ScaleType mProgressBarImageScaleType;

    private ScaleType mActualImageScaleType;
    private Matrix mActualImageMatrix;
    private PointF mActualImageFocusPoint;
    private ColorFilter mActualImageColorFilter;

    private Drawable mBackground;
    private List<Drawable> mOverlays;
    private Drawable mPressedStateOverlay;

    private RoundingParams mRoundingParams;

    public GenericDraweeHierarchyBuilder(Resources resources) {
        mResources = resources;
        init();
    }

    public static GenericDraweeHierarchyBuilder newInstance(Resources resources) {
        return new GenericDraweeHierarchyBuilder(resources);
    }

    /**
     * Initializes this builder to its defaults.
     */
    private void init() {
        mFadeDuration = DEFAULT_FADE_DURATION;

        mDesiredAspectRatio = 0;

        mPlaceholderImage = null;
        mPlaceholderImageScaleType = DEFAULT_SCALE_TYPE;

        mRetryImage = null;
        mRetryImageScaleType = DEFAULT_SCALE_TYPE;

        mFailureImage = null;
        mFailureImageScaleType = DEFAULT_SCALE_TYPE;

        mProgressBarImage = null;
        mProgressBarImageScaleType = DEFAULT_SCALE_TYPE;

        mActualImageScaleType = DEFAULT_ACTUAL_IMAGE_SCALE_TYPE;
        mActualImageMatrix = null;
        mActualImageFocusPoint = null;
        mActualImageColorFilter = null;

        mBackground = null;
        mOverlays = null;
        mPressedStateOverlay = null;

        mRoundingParams = null;
    }

    /**
     * Resets this builder to its initial values making it reusable.
     *
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder reset() {
        init();
        return this;
    }

    /**
     * Gets resources.
     *
     * @return resources
     */
    public Resources getResources() {
        return mResources;
    }

    /**
     * 设置隐藏持续的时间
     * Sets the duration of the fade animation.
     * 如果没有设置，那么默认的参数是300ms
     * If not set, the default value of 300ms will be used.
     *
     * @param fadeDuration duration in milliseconds
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setFadeDuration(int fadeDuration) {
        mFadeDuration = fadeDuration;
        return this;
    }

    /**
     * Gets the duration of the fade animation.
     */
    public int getFadeDuration() {
        return mFadeDuration;
    }

    /**
     * 设置想要的长宽比
     * Sets the desired aspect ratio.
     *
     * 注意hierarchy自己是不能设置长宽比的，这只是一个建议，如果view支持的话
     * Note, the hierarchy itself cannot enforce the aspect ratio.
     * This is merely a suggestion to the view if it supports it.
     *
     * @param desiredAspectRatio the desired aspect ratio
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setDesiredAspectRatio(float desiredAspectRatio) {
        mDesiredAspectRatio = desiredAspectRatio;
        return this;
    }

    /**
     * Gets the desired aspect ratio.
     */
    public float getDesiredAspectRatio() {
        return mDesiredAspectRatio;
    }

    /**
     * Sets the placeholder image.
     *
     * @param placeholderDrawable drawable to be used as placeholder image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setPlaceholderImage(@Nullable Drawable placeholderDrawable) {
        mPlaceholderImage = placeholderDrawable;
        return this;
    }

    /**
     * Sets the placeholder image.
     *
     * @param resourceId an identifier of an Android drawable or color resource
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setPlaceholderImage(int resourceId) {
        mPlaceholderImage = mResources.getDrawable(resourceId);
        return this;
    }

    /**
     * Gets the placeholder image.
     */
    public @Nullable Drawable getPlaceholderImage() {
        return mPlaceholderImage;
    }

    /**
     * Sets the placeholder image scale type.
     *
     * If not set, the default value CENTER_INSIDE will be used.
     *
     * @param placeholderImageScaleType scale type for the placeholder image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setPlaceholderImageScaleType(
            @Nullable ScaleType placeholderImageScaleType) {
        mPlaceholderImageScaleType = placeholderImageScaleType;
        return this;
    }

    /**
     * Gets the placeholder image scale type.
     */
    public @Nullable ScaleType getPlaceholderImageScaleType() {
        return mPlaceholderImageScaleType;
    }

    /**
     * Sets the placeholder image and its scale type.
     *
     * @param placeholderDrawable drawable to be used as placeholder image
     * @param placeholderImageScaleType scale type for the placeholder image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setPlaceholderImage(
            Drawable placeholderDrawable,
            @Nullable ScaleType placeholderImageScaleType) {
        mPlaceholderImage = placeholderDrawable;
        mPlaceholderImageScaleType = placeholderImageScaleType;
        return this;
    }

    /**
     * Sets the placeholder image and its scale type.
     *
     * @param resourceId an identifier of an Android drawable or color resource
     * @param placeholderImageScaleType scale type for the placeholder image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setPlaceholderImage(
            int resourceId,
            @Nullable ScaleType placeholderImageScaleType) {
        mPlaceholderImage = mResources.getDrawable(resourceId);
        mPlaceholderImageScaleType = placeholderImageScaleType;
        return this;
    }

    /**
     * Sets the retry image.
     *
     * @param retryDrawable drawable to be used as retry image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setRetryImage(@Nullable Drawable retryDrawable) {
        mRetryImage = retryDrawable;
        return this;
    }

    /**
     * Sets the retry image.
     *
     * @param resourceId an identifier of an Android drawable or color resource
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setRetryImage(int resourceId) {
        mRetryImage = mResources.getDrawable(resourceId);
        return this;
    }

    /**
     * Gets the retry image.
     */
    public @Nullable Drawable getRetryImage() {
        return mRetryImage;
    }

    /**
     * Sets the retry image scale type.
     *
     * If not set, the default value CENTER_INSIDE will be used.
     *
     * @param retryImageScaleType scale type for the retry image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setRetryImageScaleType(
            @Nullable ScaleType retryImageScaleType) {
        mRetryImageScaleType = retryImageScaleType;
        return this;
    }

    /**
     * Gets the retry image scale type.
     */
    public @Nullable ScaleType getRetryImageScaleType() {
        return mRetryImageScaleType;
    }

    /**
     * Sets the retry image and its scale type.
     *
     * @param retryDrawable drawable to be used as retry image
     * @param retryImageScaleType scale type for the retry image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setRetryImage(
            Drawable retryDrawable,
            @Nullable ScaleType retryImageScaleType) {
        mRetryImage = retryDrawable;
        mRetryImageScaleType = retryImageScaleType;
        return this;
    }

    /**
     * Sets the retry image and its scale type.
     *
     * @param resourceId an identifier of an Android drawable or color resource
     * @param retryImageScaleType scale type for the retry image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setRetryImage(
            int resourceId,
            @Nullable ScaleType retryImageScaleType) {
        mRetryImage = mResources.getDrawable(resourceId);
        mRetryImageScaleType = retryImageScaleType;
        return this;
    }

    /**
     * Sets the failure image.
     *
     * @param failureDrawable drawable to be used as failure image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setFailureImage(@Nullable Drawable failureDrawable) {
        mFailureImage = failureDrawable;
        return this;
    }

    /**
     * Sets the failure image.
     *
     * @param resourceId an identifier of an Android drawable or color resource
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setFailureImage(int resourceId) {
        mFailureImage = mResources.getDrawable(resourceId);
        return this;
    }

    /**
     * Gets the failure image.
     */
    public @Nullable Drawable getFailureImage() {
        return mFailureImage;
    }

    /**
     * Sets the failure image scale type.
     *
     * If not set, the default value CENTER_INSIDE will be used.
     *
     * @param failureImageScaleType scale type for the failure image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setFailureImageScaleType(
            @Nullable ScaleType failureImageScaleType) {
        mFailureImageScaleType = failureImageScaleType;
        return this;
    }

    /**
     * Gets the failure image scale type.
     */
    public @Nullable ScaleType getFailureImageScaleType() {
        return mFailureImageScaleType;
    }

    /**
     * Sets the failure image and its scale type.
     *
     * @param failureDrawable drawable to be used as failure image
     * @param failureImageScaleType scale type for the failure image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setFailureImage(
            Drawable failureDrawable,
            @Nullable ScaleType failureImageScaleType) {
        mFailureImage = failureDrawable;
        mFailureImageScaleType = failureImageScaleType;
        return this;
    }

    /**
     * Sets the failure image and its scale type.
     *
     * @param resourceId an identifier of an Android drawable or color resource
     * @param failureImageScaleType scale type for the failure image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setFailureImage(
            int resourceId,
            @Nullable ScaleType failureImageScaleType) {
        mFailureImage = mResources.getDrawable(resourceId);
        mFailureImageScaleType = failureImageScaleType;
        return this;
    }

    /**
     * Sets the progress bar image.
     *
     * @param progressBarDrawable drawable to be used as progress bar image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setProgressBarImage(@Nullable Drawable progressBarDrawable) {
        mProgressBarImage = progressBarDrawable;
        return this;
    }

    /**
     * Sets the progress bar image.
     *
     * @param resourceId an identifier of an Android drawable or color resource
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setProgressBarImage(int resourceId) {
        mProgressBarImage = mResources.getDrawable(resourceId);
        return this;
    }

    /**
     * Gets the progress bar image.
     */
    public @Nullable Drawable getProgressBarImage() {
        return mProgressBarImage;
    }

    /**
     * Sets the progress bar image scale type.
     *
     * If not set, the default value CENTER_INSIDE will be used.
     *
     * @param progressBarImageScaleType scale type for the progress bar image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setProgressBarImageScaleType(
            @Nullable ScaleType progressBarImageScaleType) {
        mProgressBarImageScaleType = progressBarImageScaleType;
        return this;
    }

    /**
     * Gets the progress bar image scale type.
     */
    public @Nullable ScaleType getProgressBarImageScaleType() {
        return mProgressBarImageScaleType;
    }

    /**
     * Sets the progress bar image and its scale type.
     *
     * @param progressBarDrawable drawable to be used as progress bar image
     * @param progressBarImageScaleType scale type for the progress bar image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setProgressBarImage(
            Drawable progressBarDrawable,
            @Nullable ScaleType progressBarImageScaleType) {
        mProgressBarImage = progressBarDrawable;
        mProgressBarImageScaleType = progressBarImageScaleType;
        return this;
    }

    /**
     * Sets the progress bar image and its scale type.
     *
     * @param resourceId an identifier of an Android drawable or color resource
     * @param progressBarImageScaleType scale type for the progress bar image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setProgressBarImage(
            int resourceId,
            @Nullable ScaleType progressBarImageScaleType) {
        mProgressBarImage = mResources.getDrawable(resourceId);
        mProgressBarImageScaleType = progressBarImageScaleType;
        return this;
    }

    /**
     * Sets the scale type for the actual image.
     *
     * If not set, the default value CENTER_CROP will be used.
     *
     * @param actualImageScaleType scale type for the actual image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setActualImageScaleType(
            @Nullable ScaleType actualImageScaleType) {
        mActualImageScaleType = actualImageScaleType;
        mActualImageMatrix = null;
        return this;
    }

    /**
     * Gets the scale type for the actual image.
     */
    public @Nullable ScaleType getActualImageScaleType() {
        return mActualImageScaleType;
    }

    /**
     * Sets the transformation matrix, and removes the scale type, for the actual image.
     *
     * @param actualImageMatrix matrix for the actual image
     * @return modified instance of this builder
     *
     * @deprecated implement and set a custom {@link ScaleType} instead.
     */
    @Deprecated
    public GenericDraweeHierarchyBuilder setActualImageMatrix(@Nullable Matrix actualImageMatrix) {
        mActualImageMatrix = actualImageMatrix;
        mActualImageScaleType = null;
        return this;
    }

    /**
     * Gets the matrix for the actual image.
     */
    public @Nullable Matrix getActualImageMatrix() {
        return mActualImageMatrix;
    }

    /**
     * Sets the focus point for the actual image.
     *
     * If a focus point aware scale type is used (e.g. FOCUS_CROP), the focus point of the image
     * will be attempted to be centered within a view.
     * Each coordinate is a real number in [0, 1] range, in the coordinate system where top-left
     * corner of the image corresponds to (0, 0) and the bottom-right corner corresponds to (1, 1).
     *
     * @param focusPoint focus point of the image
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setActualImageFocusPoint(@Nullable PointF focusPoint) {
        mActualImageFocusPoint = focusPoint;
        return this;
    }

    /**
     * Gets the focus point for the actual image.
     */
    public @Nullable PointF getActualImageFocusPoint() {
        return mActualImageFocusPoint;
    }

    /**
     * Sets the color filter for the actual image.
     *
     * @param colorFilter color filter to be set
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setActualImageColorFilter(@Nullable ColorFilter colorFilter) {
        mActualImageColorFilter = colorFilter;
        return this;
    }

    /**
     * Gets the color filter for the actual image.
     */
    public @Nullable ColorFilter getActualImageColorFilter() {
        return mActualImageColorFilter;
    }

    /**
     * Sets a background.
     *
     * @param background background drawable
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setBackground(@Nullable Drawable background) {
        mBackground = background;
        return this;
    }

    /**
     * Gets the background.
     */
    public @Nullable Drawable getBackground() {
        return mBackground;
    }

    /**
     * Sets the overlays.
     *
     * Overlays are drawn in list order after the backgrounds and the rest of the hierarchy. The
     * last overlay will be drawn at the top.
     *
     * @param overlays overlay drawables
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setOverlays(@Nullable List<Drawable> overlays) {
        mOverlays = overlays;
        return this;
    }

    /**
     * Sets a single overlay.
     *
     * @param overlay overlay drawable
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setOverlay(@Nullable Drawable overlay) {
        if (overlay == null) {
            mOverlays = null;
        } else {
            mOverlays = Arrays.asList(overlay);
        }
        return this;
    }

    /**
     * Gets the overlays.
     */
    public @Nullable List<Drawable> getOverlays() {
        return mOverlays;
    }

    /**
     * Sets the overlay for pressed state.
     *
     * @param drawable for pressed state
     * @return
     */
    public GenericDraweeHierarchyBuilder setPressedStateOverlay(@Nullable Drawable drawable) {
        if (drawable == null) {
            mPressedStateOverlay = null;
        } else {
            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, drawable);
            mPressedStateOverlay = stateListDrawable;
        }
        return this;
    }

    /**
     * Gets the overlay for pressed state.
     */
    public @Nullable Drawable getPressedStateOverlay() {
        return mPressedStateOverlay;
    }

    /**
     * Sets the rounding params.
     *
     * @param roundingParams rounding params to be set
     * @return modified instance of this builder
     */
    public GenericDraweeHierarchyBuilder setRoundingParams(@Nullable RoundingParams roundingParams) {
        mRoundingParams = roundingParams;
        return this;
    }

    /**
     * Gets the rounding params.
     */
    @Nullable
    public RoundingParams getRoundingParams() {
        return mRoundingParams;
    }

    private void validate() {
        if (mOverlays != null) {
            for (Drawable overlay : mOverlays) {
                Preconditions.checkNotNull(overlay);
            }
        }
    }

    /**
     * Builds the hierarchy.
     */
    public GenericDraweeHierarchy build() {
        validate();
        return new GenericDraweeHierarchy(this);
    }
}
