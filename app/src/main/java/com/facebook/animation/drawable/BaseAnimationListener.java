package com.facebook.animation.drawable;

/**
 * Created by Administrator on 2017/3/14 0014.
 */
/**
 * 基本的动画侦听器。这个方便的类可以用来简化代码如果扩展类是所有事件不感兴趣。只是你需要覆盖的。
 * Base animation listener. This convenience class can be used to simplify the code if the extending
 * class is not interested in all events. Just override the ones you need.
 *
 * See {@link AnimationListener} for more information.
 */
public class BaseAnimationListener implements AnimationListener {

    @Override
    public void onAnimationStart(AnimatedDrawable2 drawable) {
    }

    @Override
    public void onAnimationStop(AnimatedDrawable2 drawable) {
    }

    @Override
    public void onAnimationReset(AnimatedDrawable2 drawable) {
    }

    @Override
    public void onAnimationRepeat(AnimatedDrawable2 drawable) {
    }

    @Override
    public void onAnimationFrame(AnimatedDrawable2 drawable, int frameNumber) {
    }
}

