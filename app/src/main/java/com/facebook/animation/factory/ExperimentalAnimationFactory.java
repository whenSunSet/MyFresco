package com.facebook.animation.factory;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import android.graphics.Rect;

import com.facebook.commom.time.MonotonicClock;
import com.facebook.drawee.backends.pipeline.DrawableFactory;
import com.facebook.animation.backend.AnimationBackend;
import com.facebook.animation.backend.impl.AnimationBackendDelegateWithInactivityCheck;
import com.facebook.animation.drawable.AnimatedDrawable2;
import com.facebook.animation.wrapper.AnimatedDrawableCachingBackendWrapper;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableBackend;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableCachingBackend;
import com.facebook.imagepipeline.animated.base.impl.AnimatedDrawableOptions;
import com.facebook.imagepipeline.animated.base.AnimatedImage;
import com.facebook.imagepipeline.animated.base.impl.AnimatedImageResult;
import com.facebook.imagepipeline.animated.impl.AnimatedDrawableBackendProvider;
import com.facebook.imagepipeline.animated.impl.AnimatedDrawableCachingBackendImplProvider;
import com.facebook.imagepipeline.image.impl.CloseableAnimatedImage;
import com.facebook.imagepipeline.image.impl.CloseableImage;

import java.util.concurrent.ScheduledExecutorService;

/**
 * 一个{@link AnimatedDrawable2}的动画工厂
 * Animation factory for {@link AnimatedDrawable2}.
 *
 * 这基本上模仿了{@link AnimatedDrawableFactoryImpl}.
 * This basically mimics the backend creation of
 * {@link AnimatedDrawableFactoryImpl}.
 */
public class ExperimentalAnimationFactory implements DrawableFactory {

    private final AnimatedDrawableBackendProvider mAnimatedDrawableBackendProvider;
    private final AnimatedDrawableCachingBackendImplProvider mAnimatedDrawableCachingBackendProvider;
    private final ScheduledExecutorService mScheduledExecutorServiceForUiThread;
    private final MonotonicClock mMonotonicClock;

    public ExperimentalAnimationFactory(
            AnimatedDrawableBackendProvider animatedDrawableBackendProvider,
            AnimatedDrawableCachingBackendImplProvider animatedDrawableCachingBackendProvider,
            ScheduledExecutorService scheduledExecutorServiceForUiThread,
            MonotonicClock monotonicClock) {
        mAnimatedDrawableBackendProvider = animatedDrawableBackendProvider;
        mAnimatedDrawableCachingBackendProvider = animatedDrawableCachingBackendProvider;
        mScheduledExecutorServiceForUiThread = scheduledExecutorServiceForUiThread;
        mMonotonicClock = monotonicClock;
    }

    @Override
    public boolean supportsImageType(CloseableImage image) {
        return image instanceof CloseableAnimatedImage;
    }

    @Override
    public AnimatedDrawable2 createDrawable(CloseableImage image) {
        return new AnimatedDrawable2(
                createAnimationBackend(
                        ((CloseableAnimatedImage) image).getImageResult()));
    }

    private AnimationBackend createAnimationBackend(AnimatedImageResult animatedImageResult) {
        //创建一个animated drawable backend
        // Create the animated drawable backend
        AnimatedImage animatedImage = animatedImageResult.getImage();
        Rect initialBounds = new Rect(0, 0, animatedImage.getWidth(), animatedImage.getHeight());
        AnimatedDrawableBackend animatedDrawableBackend =
                mAnimatedDrawableBackendProvider.get(animatedImageResult, initialBounds);

        //添加caching backend
        // Add caching backend
        AnimatedDrawableCachingBackend animatedDrawableCachingBackend =
                mAnimatedDrawableCachingBackendProvider.get(
                        animatedDrawableBackend,
                        AnimatedDrawableOptions.DEFAULTS);
        AnimatedDrawableCachingBackendWrapper animatedDrawableCachingBackendWrapper =
                new AnimatedDrawableCachingBackendWrapper(animatedDrawableCachingBackend);

        //谭家inactivity check
        // Add inactivity check
        return AnimationBackendDelegateWithInactivityCheck.createForBackend(
                animatedDrawableCachingBackendWrapper,
                mMonotonicClock,
                mScheduledExecutorServiceForUiThread);
    }
}
