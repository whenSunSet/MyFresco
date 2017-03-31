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
import com.facebook.imagepipeline.animated.base.impl.AnimatedDrawable;
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
import com.facebook.imagepipeline.core.impl.DefaultExecutorSupplier;

import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Created by heshixiyang on 2017/3/16.
 */

/**
 * 该类是单例，所以在初始化之后，就会一直保存在内存中
 */
@NotThreadSafe
@DoNotStrip
public class AnimatedFactoryImpl implements AnimatedFactory {

    /**
     * 内部产生的提供{@link AnimatedDrawableBackend}的provider，一般提供的是{@link AnimatedDrawableBackendImpl}
     */
    private AnimatedDrawableBackendProvider mAnimatedDrawableBackendProvider;
    /**
     * 内部产生的{@link AnimatedDrawable}的实用方法类
     */
    private AnimatedDrawableUtil mAnimatedDrawableUtil;
    /**
     * 内部产生的{@link AnimatedDrawableFactory}的默认实现{@link AnimatedDrawableFactoryImpl}
     */
    private AnimatedDrawableFactory mAnimatedDrawableFactory;
    /**
     * 内部产生的{@link AnimatedImageFactory}的默认实现{@link AnimatedImageFactoryImpl}
     */
    private AnimatedImageFactory mAnimatedImageFactory;
    /**
     * {@link AnimatedFactoryProvider}提供的
     */
    private ExecutorSupplier mExecutorSupplier;

    /**
     * {@link AnimatedFactoryProvider}提供的
     */
    private PlatformBitmapFactory mPlatformBitmapFactory;

    @DoNotStrip
    public AnimatedFactoryImpl(
            PlatformBitmapFactory platformBitmapFactory,
            ExecutorSupplier executorSupplier) {
        this.mPlatformBitmapFactory = platformBitmapFactory;
        this.mExecutorSupplier = executorSupplier;
    }

    /**
     * 1.如果{@link AnimatedDrawableFactory}(A)没有初始化过，那么就创建它.
     * 2.通过{@link DefaultExecutorSupplier#forDecode()}(B)提供的一个图片解码线程池，
     * 创建一个{@link SerialExecutorService}(C),C是用来串行执行任务的ExecutorService。
     * 3.创建一个{@link ActivityManager}(D)
     * 4.调用传入C和D{@link #buildAnimatedDrawableFactory}
     * @param context
     * @return
     */
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

    /**
     * 如果{@link #mAnimatedImageFactory}没创建就调用{@link #buildAnimatedImageFactory}
     * @return
     */
    @Override
    public AnimatedImageFactory getAnimatedImageFactory() {
        if (mAnimatedImageFactory == null) {
            mAnimatedImageFactory = buildAnimatedImageFactory();
        }
        return mAnimatedImageFactory;
    }

    /**
     * 1.先实现{@link AnimatedDrawableCachingBackendImplProvider#get},
     * 其会返回一个{@link AnimatedDrawableCachingBackendImpl}(A)。
     * 2.调用{@link #createAnimatedDrawableFactory}
     *
     * @param serialExecutorService {@link #getAnimatedDrawableFactory}传入
     * @param activityManager {@link #getAnimatedDrawableFactory}传入
     * @param animatedDrawableUtil 本类构造
     * @param animatedDrawableBackendProvider 本类构造
     * @param scheduledExecutorService {@link UiThreadImmediateExecutorService#getInstance} 将事件传回主线程的ExecutorService
     * @param monotonicClock {@link RealtimeSinceBootClock#get()}一个计时器
     * @param resources
     * @return
     */
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

    private AnimatedDrawableUtil getAnimatedDrawableUtil() {
        if (mAnimatedDrawableUtil == null) {
            mAnimatedDrawableUtil = new AnimatedDrawableUtil();
        }
        return mAnimatedDrawableUtil;
    }

    /**
     * 实现{@link AnimatedDrawableBackendProvider#get},其提供了{@link AnimatedDrawableBackendImpl}
     * @return {@link AnimatedImageFactoryImpl}
     */
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

    /**
     * 创建一个{@link AnimatedDrawableFactoryImpl}
     * @param animatedDrawableBackendProvider 是 {@link #mAnimatedDrawableBackendProvider}
     * @param animatedDrawableCachingBackendImplProvider 提供{@link AnimatedDrawableCachingBackendImpl}
     * @param animatedDrawableUtil 是 {@link #mAnimatedDrawableUtil}
     * @param scheduledExecutorService {@link UiThreadImmediateExecutorService#getInstance} 将事件传回主线程的ExecutorService
     * @param resources
     * @return
     */
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
