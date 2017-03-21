package com.facebook.animation.bitmap.wrapper;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import com.facebook.animation.backend.AnimationInformation;
import com.facebook.imagepipeline.animated.base.AnimatedDrawableBackend;

/**
 * 包装一个{@link AnimatedDrawableBackend}的{@link AnimationInformation}
 * {@link AnimationInformation} that wraps an {@link AnimatedDrawableBackend}.
 */
public class AnimatedDrawableBackendAnimationInformation
        implements AnimationInformation {

    private final AnimatedDrawableBackend mAnimatedDrawableBackend;

    public AnimatedDrawableBackendAnimationInformation(
            AnimatedDrawableBackend animatedDrawableBackend) {
        mAnimatedDrawableBackend = animatedDrawableBackend;
    }

    @Override
    public int getFrameCount() {
        return mAnimatedDrawableBackend.getFrameCount();
    }

    @Override
    public int getFrameDurationMs(int frameNumber) {
        return mAnimatedDrawableBackend.getDurationMsForFrame(frameNumber);
    }

    @Override
    public int getLoopCount() {
        return mAnimatedDrawableBackend.getLoopCount();
    }
}

