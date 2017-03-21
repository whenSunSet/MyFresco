package com.facebook.imagepipeline.animated.factory.impl;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;

import com.facebook.commom.executors.SerialExecutorService;
import com.facebook.commom.executors.impl.DefaultSerialExecutorService;
import com.facebook.commom.executors.impl.UiThreadImmediateExecutorService;
import com.facebook.commom.internal.DoNotStrip;
import com.facebook.commom.time.MonotonicClock;
import com.facebook.commom.time.impl.RealtimeSinceBootClock;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableBackend;
import com.facebook.imagepipeline.animated.base.impl.AnimatedDrawableOptions;
import com.facebook.imagepipeline.animated.base.impl.AnimatedImageResult;
import com.facebook.imagepipeline.animated.factory.AnimatedDrawableFactory;
import com.facebook.imagepipeline.animated.factory.AnimatedFactory;
import com.facebook.imagepipeline.animated.factory.AnimatedImageFactory;
import com.facebook.imagepipeline.animated.impl.impl.AnimatedDrawableBackendImpl;
import com.facebook.imagepipeline.animated.impl.AnimatedDrawableBackendProvider;
import com.facebook.imagepipeline.animated.impl.impl.AnimatedDrawableCachingBackendImpl;
import com.facebook.imagepipeline.animated.impl.AnimatedDrawableCachingBackendImplProvider;
import com.facebook.imagepipeline.animated.util.AnimatedDrawableUtil;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.core.ExecutorSupplier;

import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Created by heshixiyang on 2017/3/16.
 */

@NotThreadSafe
@DoNotStrip
public class AnimatedFactoryImpl implements AnimatedFactory {

    private AnimatedDrawableBackendProvider mAnimatedDrawableBackendProvider;
    private AnimatedDrawableUtil mAnimatedDrawableUtil;
    private AnimatedDrawableFactory mAnimatedDrawableFactory;
    private AnimatedImageFactory mAnimatedImageFactory;

    private ExecutorSupplier mExecutorSupplier;

    private PlatformBitmapFactory mPlatformBitmapFactory;

    @DoNotStrip
    public AnimatedFactoryImpl(
            PlatformBitmapFactory platformBitmapFactory,
            ExecutorSupplier executorSupplier) {
        this.mPlatformBitmapFactory = platformBitmapFactory;
        this.mExecutorSupplier = executorSupplier;
    }

    private AnimatedDrawableFactory buildAnimatedDrawableFactory(
            final SerialExecutorService serialExecutorService,
            final ActivityManager activityManager,
            final AnimatedDrawableUtil animatedDrawableUtil,
            AnimatedDrawableBackendProvider animatedDrawableBackendProvider,
            ScheduledExecutorService scheduledExecutorService,
            final MonotonicClock monotonicClock,
            Resources resources) {
        AnimatedDrawableCachingBackendImplProvider animatedDrawableCachingBackendImplProvider =
                new AnimatedDrawableCachingBackendImplProvider() {
                    @Override
                    public AnimatedDrawableCachingBackendImpl get(
                            AnimatedDrawableBackend animatedDrawableBackend,
                            AnimatedDrawableOptions options) {
                        return new AnimatedDrawableCachingBackendImpl(
                                serialExecutorService,
                                activityManager,
                                animatedDrawableUtil,
                                monotonicClock,
                                animatedDrawableBackend,
                                options);
                    }
                };

        return createAnimatedDrawableFactory(
                animatedDrawableBackendProvider,
                animatedDrawableCachingBackendImplProvider,
                animatedDrawableUtil,
                scheduledExecutorService,
                resources);
    }

    private AnimatedDrawableBackendProvider getAnimatedDrawableBackendProvider() {
        if (mAnimatedDrawableBackendProvider == null) {
            mAnimatedDrawableBackendProvider = new AnimatedDrawableBackendProvider() {
                @Override
                public AnimatedDrawableBackend get(AnimatedImageResult animatedImageResult, Rect bounds) {
                    return new AnimatedDrawableBackendImpl(
                            getAnimatedDrawableUtil(),
                            animatedImageResult,
                            bounds);
                }
            };
        }
        return mAnimatedDrawableBackendProvider;
    }

    @Override
    public AnimatedDrawableFactory getAnimatedDrawableFactory(Context context) {
        if (mAnimatedDrawableFactory == null) {
            SerialExecutorService serialExecutorService =
                    new DefaultSerialExecutorService(mExecutorSupplier.forDecode());
            ActivityManager activityManager =
                    (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            mAnimatedDrawableFactory = buildAnimatedDrawableFactory(
                    serialExecutorService,
                    activityManager,
                    getAnimatedDrawableUtil(),
                    getAnimatedDrawableBackendProvider(),
                    UiThreadImmediateExecutorService.getInstance(),
                    RealtimeSinceBootClock.get(),
                    context.getResources());
        }
        return mAnimatedDrawableFactory;
    }

    // We need some of these methods public for now so internal code can use them.
    private AnimatedDrawableUtil getAnimatedDrawableUtil() {
        if (mAnimatedDrawableUtil == null) {
            mAnimatedDrawableUtil = new AnimatedDrawableUtil();
        }
        return mAnimatedDrawableUtil;
    }

    private AnimatedImageFactory buildAnimatedImageFactory() {
        AnimatedDrawableBackendProvider animatedDrawableBackendProvider =
                new AnimatedDrawableBackendProvider() {
                    @Override
                    public AnimatedDrawableBackend get(AnimatedImageResult imageResult, Rect bounds) {
                        return new AnimatedDrawableBackendImpl(getAnimatedDrawableUtil(), imageResult, bounds);
                    }
                };
        return new AnimatedImageFactoryImpl(animatedDrawableBackendProvider, mPlatformBitmapFactory);
    }

    @Override
    public AnimatedImageFactory getAnimatedImageFactory() {
        if (mAnimatedImageFactory == null) {
            mAnimatedImageFactory = buildAnimatedImageFactory();
        }
        return mAnimatedImageFactory;
    }

    protected AnimatedDrawableFactory createAnimatedDrawableFactory(
            AnimatedDrawableBackendProvider animatedDrawableBackendProvider,
            AnimatedDrawableCachingBackendImplProvider animatedDrawableCachingBackendImplProvider,
            AnimatedDrawableUtil animatedDrawableUtil,
            ScheduledExecutorService scheduledExecutorService,
            Resources resources) {
        return new AnimatedDrawableFactoryImpl(
                animatedDrawableBackendProvider,
                animatedDrawableCachingBackendImplProvider,
                animatedDrawableUtil,
                scheduledExecutorService,
                resources);
    }
}
