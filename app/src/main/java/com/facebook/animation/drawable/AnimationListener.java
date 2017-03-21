package com.facebook.animation.drawable;

/**
 * Created by heshixiyang on 2017/3/10.
 */
/**
 * 这个可以被用在{@link AnimatedDrawable2}中以接受动画过程中的事件，
 * 调用{@link AnimatedDrawable2#setAnimationListener(AnimationListener)}来设置监听器
 * Animation listener that can be used to get notified about {@link AnimatedDrawable2} events.
 * Call {@link AnimatedDrawable2#setAnimationListener(AnimationListener)} to set a listener.
 */
public interface AnimationListener {

    /**
     * Called when the animation is started for the given drawable.
     *
     * @param drawable the affected drawable
     */
    void onAnimationStart(AnimatedDrawable2 drawable);

    /**
     * Called when the animation is stopped for the given drawable.
     *
     * @param drawable the affected drawable
     */
    void onAnimationStop(AnimatedDrawable2 drawable);

    /**
     * Called when the animation is reset for the given drawable.
     *
     * @param drawable the affected drawable
     */
    void onAnimationReset(AnimatedDrawable2 drawable);

    /**
     * Called when the animation is repeated for the given drawable.
     * Animations have a loop count, and frame count, so this is called when
     * the frame count is 0 and the loop count is increased.
     *
     * @param drawable the affected drawable
     */
    void onAnimationRepeat(AnimatedDrawable2 drawable);

    /**
     * Called when a frame of the animation is about to be rendered.
     *
     * @param drawable the affected drawable
     * @param frameNumber the frame number to be rendered
     */
    void onAnimationFrame(AnimatedDrawable2 drawable, int frameNumber);
}
