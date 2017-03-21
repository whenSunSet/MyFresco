package com.facebook.imagepipeline.animated.base;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.graphics.drawable.Animatable;
import android.os.Build;

/**
 * 一个将 可以被做成动画的drawable 构建成一个Animator对象
 * An interface for animatable drawables that can be asked to construct a value animator.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public interface AnimatableDrawable extends Animatable {

    /**
     * 一个可以立即使drawable变成可播放的animator，循环计数和持续时间将由原始图像元数据决定
     * 刷新的监听器将自动attach
     *
     * @return a new animator
     */
    ValueAnimator createValueAnimator();

    /**
     *
     * @param maxDurationMs maximum duration animate
     * @return a new animator
     */
    ValueAnimator createValueAnimator(int maxDurationMs);

    /**
     * 创建igeanimator的更新监听器，
     * @return a new update listener
     */
    ValueAnimator.AnimatorUpdateListener createAnimatorUpdateListener();
}
