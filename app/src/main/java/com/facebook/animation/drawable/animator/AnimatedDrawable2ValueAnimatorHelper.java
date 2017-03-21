package com.facebook.animation.drawable.animator;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;

import com.facebook.animation.backend.AnimationInformation;
import com.facebook.animation.drawable.AnimatedDrawable2;

/**
 *
 * 帮助创建一个{@link ValueAnimator}通过{@link AnimatedDrawable2}
 * Helper class to create {@link ValueAnimator}s for {@link AnimatedDrawable2}.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class AnimatedDrawable2ValueAnimatorHelper {

    public static ValueAnimator createValueAnimator(
            AnimatedDrawable2 animatedDrawable,
            int maxDurationMs) {
        ValueAnimator animator = createValueAnimator(animatedDrawable);
        if (animator == null) {
            return null;
        }
        int repeatCount = (int) Math.max(maxDurationMs / animatedDrawable.getLoopDurationMs(), 1);
        animator.setRepeatCount(repeatCount);
        return animator;
    }

    public static ValueAnimator createValueAnimator(AnimatedDrawable2 animatedDrawable) {
        int loopCount = animatedDrawable.getLoopCount();
        ValueAnimator animator = new ValueAnimator();
        animator.setIntValues(0, (int) animatedDrawable.getLoopDurationMs());
        animator.setDuration(animatedDrawable.getLoopDurationMs());
        animator.setRepeatCount(
                loopCount != AnimationInformation.LOOP_COUNT_INFINITE ? loopCount : ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        // Use a linear interpolator
        animator.setInterpolator(null);
        ValueAnimator.AnimatorUpdateListener animatorUpdateListener =
                createAnimatorUpdateListener(animatedDrawable);
        animator.addUpdateListener(animatorUpdateListener);
        return animator;
    }

    public static ValueAnimator.AnimatorUpdateListener createAnimatorUpdateListener(
            final AnimatedDrawable2 drawable) {
        return new ValueAnimator.AnimatorUpdateListener() {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                drawable.setLevel((Integer) animation.getAnimatedValue());
            }
        };
    }

    private AnimatedDrawable2ValueAnimatorHelper() {}
}
