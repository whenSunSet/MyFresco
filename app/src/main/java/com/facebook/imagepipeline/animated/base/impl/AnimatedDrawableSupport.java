package com.facebook.imagepipeline.animated.base.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import android.animation.ValueAnimator;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.animation.LinearInterpolator;

import com.facebook.commom.time.MonotonicClock;
import com.facebook.imagepipeline.animated.base.AnimatableDrawableSupport;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableBackend;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableCachingBackend;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableDiagnostics;

import java.util.concurrent.ScheduledExecutorService;

/**
 * A {@link Drawable} that renders a animated image. The details of the format are abstracted by the
 * {@link AnimatedDrawableBackend} interface. The drawable can work either as an {@link Animatable}
 * where the client calls start/stop to animate it or it can work as a level-based drawable where
 * the client drives the animation by calling {@link Drawable#setLevel}.
 */
public class AnimatedDrawableSupport extends AbstractAnimatedDrawable
        implements AnimatableDrawableSupport {

    public AnimatedDrawableSupport(
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
        animator.setRepeatCount(loopCount != 0 ? loopCount : ValueAnimator.INFINITE);
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
