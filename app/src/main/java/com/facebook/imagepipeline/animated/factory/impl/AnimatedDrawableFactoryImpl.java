package com.facebook.imagepipeline.animated.factory.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.DisplayMetrics;

import com.facebook.commom.executors.impl.UiThreadImmediateExecutorService;
import com.facebook.commom.time.MonotonicClock;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableBackend;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableCachingBackend;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableDiagnostics;
import com.facebook.imagepipeline.animated.base.AnimatedImage;
import com.facebook.imagepipeline.animated.base.impl.AnimatedDrawable;
import com.facebook.imagepipeline.animated.base.impl.AnimatedDrawableOptions;
import com.facebook.imagepipeline.animated.base.impl.AnimatedImageResult;
import com.facebook.imagepipeline.animated.factory.AnimatedDrawableFactory;
import com.facebook.imagepipeline.animated.impl.AnimatedDrawableBackendProvider;
import com.facebook.imagepipeline.animated.impl.AnimatedDrawableCachingBackendImplProvider;
import com.facebook.imagepipeline.animated.impl.impl.AnimatedDrawableBackendImpl;
import com.facebook.imagepipeline.animated.impl.impl.AnimatedDrawableCachingBackendImpl;
import com.facebook.imagepipeline.animated.impl.impl.AnimatedDrawableDiagnosticsImpl;
import com.facebook.imagepipeline.animated.impl.impl.AnimatedDrawableDiagnosticsNoop;
import com.facebook.imagepipeline.animated.util.AnimatedDrawableUtil;
import com.facebook.imagepipeline.image.impl.CloseableAnimatedImage;
import com.facebook.imagepipeline.image.impl.CloseableImage;

import java.util.concurrent.ScheduledExecutorService;

/**
 * {@link AnimatedDrawable}.的工厂的实现，只由{@link AnimatedFactoryImpl}创建
 * Factory for instances of {@link AnimatedDrawable}.
 */
public class AnimatedDrawableFactoryImpl implements AnimatedDrawableFactory {

    /**
     * 一般产生的是{@link AnimatedDrawableBackendImpl}
     */
    private final AnimatedDrawableBackendProvider mAnimatedDrawableBackendProvider;
    /**
     * 一般产生的是{@link AnimatedDrawableCachingBackendImpl}用于包装{@link AnimatedDrawableBackendImpl}
     */
    private final AnimatedDrawableCachingBackendImplProvider mAnimatedDrawableCachingBackendProvider;
    private final AnimatedDrawableUtil mAnimatedDrawableUtil;
    /**
     *{@link UiThreadImmediateExecutorService#getInstance} 将事件传回主线程的ExecutorService
     */
    private final ScheduledExecutorService mScheduledExecutorServiceForUiThread;
    private final MonotonicClock mMonotonicClock;
    private final Resources mResources;

    public AnimatedDrawableFactoryImpl(
            AnimatedDrawableBackendProvider animatedDrawableBackendProvider,
            AnimatedDrawableCachingBackendImplProvider animatedDrawableCachingBackendProvider,
            AnimatedDrawableUtil animatedDrawableUtil,
            ScheduledExecutorService scheduledExecutorService,
            Resources resources) {
        mAnimatedDrawableBackendProvider = animatedDrawableBackendProvider;
        mAnimatedDrawableCachingBackendProvider = animatedDrawableCachingBackendProvider;
        mAnimatedDrawableUtil = animatedDrawableUtil;
        mScheduledExecutorServiceForUiThread = scheduledExecutorService;
        mMonotonicClock = new MonotonicClock() {
            @Override
            public long now() {
                return SystemClock.uptimeMillis();
            }
        };
        mResources = resources;
    }

    /**
     * 通过 {@link CloseableAnimatedImage}(A)，创建一个{@link AnimatedDrawable}，A由{@link AnimatedImageFactoryImpl#decodeGif}或{@link AnimatedImageFactoryImpl#decodeWebP}提供
     * Creates an {@link AnimatedDrawable} based on an {@link CloseableImage} which should be a CloseableAnimatedImage.
     * 然后再调用{@link #create(AnimatedImageResult, AnimatedDrawableOptions)}
     *
     * @param closeableImage The CloseableAnimatedImage to use for the AnimatedDrawable
     * @return a newly constructed {@link AnimatedDrawable}
     */
    @Override
    public Drawable create(CloseableImage closeableImage) {
        if (closeableImage instanceof CloseableAnimatedImage) {
            final AnimatedImageResult result = ((CloseableAnimatedImage) closeableImage).getImageResult();
            return create(result, AnimatedDrawableOptions.DEFAULTS);
        } else {
            throw new UnsupportedOperationException("Unrecognized image class: " + closeableImage);
        }
    }

    /**
     * 通过{@link AnimatedImageResult}(A)创建一个{@link AnimatedDrawable}
     * Creates an {@link AnimatedDrawable} based on an {@link AnimatedImage}.
     *
     * 1.通过{@link #mAnimatedDrawableBackendProvider}加上A，创建一个{@link AnimatedDrawableBackendImpl}(B)
     * 2.调用{@link #createAnimatedDrawable}传入B和 options
     *
     * @param animatedImageResult the result of the code
     * @param options additional options
     *                使用的是{@link AnimatedDrawableOptions#DEFAULTS}
     * @return a newly constructed {@link AnimatedDrawable}
     */
    private AnimatedDrawable create(
            AnimatedImageResult animatedImageResult,
            AnimatedDrawableOptions options) {
        AnimatedImage animatedImage = animatedImageResult.getImage();
        Rect initialBounds = new Rect(0, 0, animatedImage.getWidth(), animatedImage.getHeight());
        AnimatedDrawableBackend animatedDrawableBackend =
                mAnimatedDrawableBackendProvider.get(animatedImageResult, initialBounds);
        return createAnimatedDrawable(options, animatedDrawableBackend);
    }

    private AnimatedImageResult getImageIfCloseableAnimatedImage(CloseableImage image) {
        if (image instanceof CloseableAnimatedImage) {
            return ((CloseableAnimatedImage) image).getImageResult();
        }
        return null;
    }

    /**
     * 1.将传入的animatedDrawableBackend通过{@link #mAnimatedDrawableCachingBackendProvider}用{@link AnimatedDrawableCachingBackendImpl}(A)包装
     * 2.通过{@link AnimatedDrawableOptions#enableDebugging}判断是否为debug模式，如果是就创建一个{@link AnimatedDrawableDiagnostics}(B)用于在动画上绘制一层debug信息
     * 3.使用A、B、{@link #mScheduledExecutorServiceForUiThread}和{@link #mMonotonicClock}创建一个{@link AnimatedDrawable}
     *
     * @param options
     * @param animatedDrawableBackend
     * @return
     */
    private AnimatedDrawable createAnimatedDrawable(
            AnimatedDrawableOptions options,
            AnimatedDrawableBackend animatedDrawableBackend) {
        DisplayMetrics displayMetrics = mResources.getDisplayMetrics();
        AnimatedDrawableDiagnostics animatedDrawableDiagnostics;
        AnimatedDrawableCachingBackend animatedDrawableCachingBackend =
                mAnimatedDrawableCachingBackendProvider.get(
                        animatedDrawableBackend,
                        options);
        if (options.enableDebugging) {
            animatedDrawableDiagnostics =
                    new AnimatedDrawableDiagnosticsImpl(mAnimatedDrawableUtil, displayMetrics);
        } else {
            animatedDrawableDiagnostics = AnimatedDrawableDiagnosticsNoop.getInstance();
        }

        return new AnimatedDrawable(
                mScheduledExecutorServiceForUiThread,
                animatedDrawableCachingBackend,
                animatedDrawableDiagnostics,
                mMonotonicClock);
    }
}
