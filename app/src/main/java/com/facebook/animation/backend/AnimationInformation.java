package com.facebook.animation.backend;

/**
 * Created by heshixiyang on 2017/3/10.
 */
/**
 * 基本动画的元数据: 帧和循环计数和持续时间
 * Basic animation metadata: Frame and loop count & duration
 */
public interface AnimationInformation {

    /**
     * 当动画需要被不断的重复的时候{@link #getLoopCount()}的返回值
     * Loop count to be returned by {@link #getLoopCount()} when the animation should be repeated
     * indefinitely.
     */
    int LOOP_COUNT_INFINITE = 0;

    /**
     * 获取动画中的帧数
     * Get the number of frames for the animation
     *
     * @return the number of frames
     */
    int getFrameCount();

    /**
     * 获取指定帧停留的时间
     * Get the frame duration for a given frame number in milliseconds.
     *
     * @param frameNumber the frame to get the duration for
     * @return the duration in ms
     */
    int getFrameDurationMs(int frameNumber);

    /**
     * 循环的次数
     * Get the number of loops the animation has or {@link #LOOP_COUNT_INFINITE} for infinite looping.
     *
     * @return the loop count or {@link #LOOP_COUNT_INFINITE}
     */
    int getLoopCount();
}
