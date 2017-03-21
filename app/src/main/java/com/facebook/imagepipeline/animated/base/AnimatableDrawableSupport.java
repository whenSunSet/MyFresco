package com.facebook.imagepipeline.animated.base;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import android.animation.ValueAnimator;
import android.graphics.drawable.Animatable;

/**
 * 一个可以构造animatable drawables 的接口
 * An interface for animatable drawables that can be asked to construct a value animator.
 */
public interface AnimatableDrawableSupport extends Animatable {

    /**
     * An animator that will animate the drawable directly. The loop count and duration will
     * be determined by metadata in the original image. Update listener is attached automatically.
     *
     * @return a new animator
     */
    ValueAnimator createValueAnimator();

    /**
     * An animator that will animate the drawable directly. The loop count will be set based on
     * the specified duration. Update listener is attached automatically.
     *
     * @param maxDurationMs maximum duration animate
     * @return a new animator
     */
    ValueAnimator createValueAnimator(int maxDurationMs);

    /**
     * Creates an animator update listener that will animate the drawable directly. This is useful
     * when the drawable needs to be animated by an existing value animator.
     * @return a new update listener
     */
    ValueAnimator.AnimatorUpdateListener createAnimatorUpdateListener();
}
