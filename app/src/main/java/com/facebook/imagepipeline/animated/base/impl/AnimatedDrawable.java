package com.facebook.imagepipeline.animated.base.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.animation.LinearInterpolator;

import com.facebook.commom.time.MonotonicClock;
import com.facebook.imagepipeline.animated.base.AnimatableDrawable;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableBackend;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableCachingBackend;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableDiagnostics;
import com.facebook.imagepipeline.animated.base.AnimatedImage;

import java.util.concurrent.ScheduledExecutorService;

/**
 * 一个渲染animated image的{@link Drawable}其中的格式的细节是对于。{@link AnimatedDrawableBackend}的抽象
 * 这个drawable不仅能作为一个{@link Animatable}当客户端开始或者结束动画，还能通过调用{@link Drawable#setLevel}
 * 作为一个层级的drawable当客户端驱动动画的时候
 * A {@link Drawable} that renders a animated image. The details of the format are abstracted by the
 * {@link AnimatedDrawableBackend} interface. The drawable can work either as an {@link Animatable}
 * where the client calls start/stop to animate it or it can work as a level-based drawable where
 * the client drives the animation by calling {@link Drawable#setLevel}.
 */

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AnimatedDrawable extends AbstractAnimatedDrawable implements AnimatableDrawable {

    public AnimatedDrawable(
            ScheduledExecutorService scheduledExecutorServiceForUiThread,
            AnimatedDrawableCachingBackend animatedDrawableBackend,
            AnimatedDrawableDiagnostics animatedDrawableDiagnostics,
            MonotonicClock monotonicClock) {
        super(scheduledExecutorServiceForUiThread,
                animatedDrawableBackend,
                animatedDrawableDiagnostics,
                monotonicClock);
    }

    @Override
    public ValueAnimator createValueAnimator(int maxDurationMs) {
        ValueAnimator animator = createValueAnimator();
        int repeatCount = Math.max((maxDurationMs / getAnimatedDrawableBackend().getDurationMs()), 1);
        animator.setRepeatCount(repeatCount);
        return animator;
    }

    @Override
    public ValueAnimator createValueAnimator() {
        int loopCount = getAnimatedDrawableBackend().getLoopCount();
        ValueAnimator animator = new ValueAnimator();
        animator.setIntValues(0, getDuration());
        animator.setDuration(getDuration());
        animator.setRepeatCount(
                loopCount != AnimatedImage.LOOP_COUNT_INFINITE ? loopCount : ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(createAnimatorUpdateListener());
        return animator;
    }

    @Override
    public ValueAnimator.AnimatorUpdateListener createAnimatorUpdateListener() {
        return new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setLevel((Integer) animation.getAnimatedValue());
            }
        };
    }

}
