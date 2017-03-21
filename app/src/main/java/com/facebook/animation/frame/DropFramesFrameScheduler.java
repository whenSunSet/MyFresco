package com.facebook.animation.frame;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import com.facebook.commom.internal.VisibleForTesting;
import com.facebook.animation.backend.AnimationInformation;

/**
 * 帧的调度器，将时间映射给帧
 * Frame scheduler that maps time values to frames.
 */
public class DropFramesFrameScheduler implements FrameScheduler {

    private static final int UNSET = -1;

    private final AnimationInformation mAnimationInformation;

    private long mLoopDurationMs = UNSET;

    public DropFramesFrameScheduler(AnimationInformation animationInformation) {
        mAnimationInformation = animationInformation;
    }

    @Override
    public int getFrameNumberToRender(long animationTimeMs, long lastFrameTimeMs) {
        if (!isInfiniteAnimation()) {
            long loopCount = animationTimeMs / getLoopDurationMs();
            if (loopCount >= mAnimationInformation.getLoopCount()) {
                return FRAME_NUMBER_DONE;
            }
        }
        long timeInCurrentLoopMs = animationTimeMs % getLoopDurationMs();
        return getFrameNumberWithinLoop(timeInCurrentLoopMs);
    }

    @Override
    public long getLoopDurationMs() {
        if (mLoopDurationMs != UNSET) {
            return mLoopDurationMs;
        }
        mLoopDurationMs = 0;
        int frameCount = mAnimationInformation.getFrameCount();
        for (int i = 0; i < frameCount; i++) {
            mLoopDurationMs += mAnimationInformation.getFrameDurationMs(i);
        }
        return mLoopDurationMs;
    }

    @Override
    public long getTargetRenderTimeMs(int frameNumber) {
        long targetRenderTimeMs = 0;
        for (int i = 0; i < frameNumber; i++) {
            targetRenderTimeMs += mAnimationInformation.getFrameDurationMs(frameNumber);
        }
        return targetRenderTimeMs;
    }

    @Override
    public long getTargetRenderTimeForNextFrameMs(long animationTimeMs) {
        long loopDurationMs = getLoopDurationMs();
        // Sanity check.
        if (loopDurationMs == 0) {
            return NO_NEXT_TARGET_RENDER_TIME;
        }
        if (!isInfiniteAnimation()) {
            long loopCount = animationTimeMs / getLoopDurationMs();
            if (loopCount >= mAnimationInformation.getLoopCount()) {
                return NO_NEXT_TARGET_RENDER_TIME;
            }
        }
        // The animation time in the current loop
        long timePassedInCurrentLoopMs = animationTimeMs % loopDurationMs;
        // The animation time in the current loop for the next frame
        long timeOfNextFrameInLoopMs = 0;

        int frameCount = mAnimationInformation.getFrameCount();
        for (int i = 0; i < frameCount && timeOfNextFrameInLoopMs <= timePassedInCurrentLoopMs; i++) {
            timeOfNextFrameInLoopMs += mAnimationInformation.getFrameDurationMs(i);
        }

        // Difference between current time in loop and next frame in loop
        long timeUntilNextFrameInLoopMs = timeOfNextFrameInLoopMs - timePassedInCurrentLoopMs;
        // Add the difference to the current animation time
        return animationTimeMs + timeUntilNextFrameInLoopMs;
    }

    @Override
    public boolean isInfiniteAnimation() {
        return mAnimationInformation.getLoopCount() == AnimationInformation.LOOP_COUNT_INFINITE;
    }

    @VisibleForTesting
    int getFrameNumberWithinLoop(long timeInCurrentLoopMs) {
        int frame = 0;
        long currentDuration = 0;
        do {
            currentDuration += mAnimationInformation.getFrameDurationMs(frame);
            frame++;
        } while (timeInCurrentLoopMs >= currentDuration);
        return frame - 1;
    }
}

