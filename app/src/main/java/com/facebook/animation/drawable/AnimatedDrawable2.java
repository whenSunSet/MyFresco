package com.facebook.animation.drawable;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;

import com.facebook.commom.logging.FLog;
import com.facebook.base.DrawableWithCaches;
import com.facebook.drawee.drawable.impl.DrawableProperties;
import com.facebook.animation.backend.AnimationBackend;
import com.facebook.animation.backend.AnimationInformation;
import com.facebook.animation.frame.DropFramesFrameScheduler;
import com.facebook.animation.frame.FrameScheduler;

import javax.annotation.Nullable;

/**
 * 支持一个实验性的animated drawable
 * Experimental new animated drawable that uses a supplied
 * {@link AnimationBackend} for drawing frames.
 */
public class AnimatedDrawable2 extends Drawable implements Animatable, DrawableWithCaches {

    private static final Class<?> TAG = AnimatedDrawable2.class;

    private static final AnimationListener NO_OP_LISTENER = new BaseAnimationListener();

    @Nullable
    private AnimationBackend mAnimationBackend;
    @Nullable
    private FrameScheduler mFrameScheduler;

    // Animation parameters
    private volatile boolean mIsRunning;
    private long mStartTimeMs;
    private long mLastFrameAnimationTimeMs;

    // Animation statistics
    private int mDroppedFrames;

    // Listeners
    private volatile AnimationListener mAnimationListener = NO_OP_LISTENER;

    //保持一些drawable的属性集，像是alpha之类的，如果backend改变了
    //这个是懒加载
    // Holder for drawable properties like alpha to be able to re-apply if the backend changes.
    // The instance is created lazily only if needed.
    @Nullable
    private DrawableProperties mDrawableProperties;

    /**
     * 将当前的drawable作废，如果下一帧将被调度上来
     * Runnable that invalidates the drawable that will be scheduled according to the next
     * target frame.
     */
    private final Runnable mInvalidateRunnable = new Runnable() {
        @Override
        public void run() {
            //删除其他的潜在的调度runnables
            //例如，如果一个view已经失效
            // Remove all potential other scheduled runnables
            // (e.g. if the view has been invalidated a lot)
            unscheduleSelf(mInvalidateRunnable);
            //绘制下一帧
            // Draw the next frame
            invalidateSelf();
        }
    };

    public AnimatedDrawable2() {
        this(null);
    }

    public AnimatedDrawable2(
            @Nullable AnimationBackend animationBackend) {
        mAnimationBackend = animationBackend;
        mFrameScheduler = createSchedulerForBackendAndDelayMethod(mAnimationBackend);
    }

    @Override
    public int getIntrinsicWidth() {
        if (mAnimationBackend == null) {
            return super.getIntrinsicWidth();
        }
        return mAnimationBackend.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        if (mAnimationBackend == null) {
            return super.getIntrinsicHeight();
        }
        return mAnimationBackend.getIntrinsicHeight();
    }

    /**
     * 开始动画
     * Start the animation.
     */
    @Override
    public void start() {
        if (mIsRunning || mAnimationBackend == null || mAnimationBackend.getFrameCount() <= 1) {
            return;
        }
        mIsRunning = true;
        mStartTimeMs = now();
        mLastFrameAnimationTimeMs = -1;
        invalidateSelf();
        mAnimationListener.onAnimationStart(this);
    }

    /**
     * 停止动画，可以通过调用{@link #start()}重新启动
     * Stop the animation at the current frame. It can be resumed by calling {@link #start()} again.
     */
    @Override
    public void stop() {
        if (!mIsRunning) {
            return;
        }
        mIsRunning = false;
        mStartTimeMs = 0;
        mLastFrameAnimationTimeMs = -1;
        unscheduleSelf(mInvalidateRunnable);
        mAnimationListener.onAnimationStop(this);
    }

    /**
     * 查看是否animation已经在运行
     * Check whether the animation is running.
     * @return true if the animation is currently running
     */
    @Override
    public boolean isRunning() {
        return mIsRunning;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        if (mAnimationBackend != null) {
            mAnimationBackend.setBounds(bounds);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        if (mAnimationBackend == null || mFrameScheduler == null) {
            return;
        }
        long actualRenderTimeStartMs = now();
        long animationTimeMs = mIsRunning
                ? actualRenderTimeStartMs - mStartTimeMs
                : Math.max(mLastFrameAnimationTimeMs, 0);

        // What frame should be drawn?
        int frameNumberToDraw = mFrameScheduler.getFrameNumberToRender(
                animationTimeMs,
                mLastFrameAnimationTimeMs);

        mLastFrameAnimationTimeMs = animationTimeMs;

        // Check if the animation is finished and draw last frame if so
        if (frameNumberToDraw == FrameScheduler.FRAME_NUMBER_DONE) {
            frameNumberToDraw = mAnimationBackend.getFrameCount() - 1;
            mAnimationListener.onAnimationStop(this);
            mIsRunning = false;
        } else if (frameNumberToDraw == 0) {
            mAnimationListener.onAnimationRepeat(this);
        }

        // Notify listeners that we're about to draw a new frame and
        // that the animation might be repeated
        mAnimationListener.onAnimationFrame(this, frameNumberToDraw);

        // Draw the frame
        boolean frameDrawn = mAnimationBackend.drawFrame(this, canvas, frameNumberToDraw);

        // Log potential dropped frames
        if (!frameDrawn) {
            onFrameDropped();
        }

        if (mIsRunning) {
            // Log performance
            long actualRenderTimeEnd = now();
            if (FLog.isLoggable(FLog.VERBOSE)) {
                FLog.v(
                        TAG,
                        "Animation jitter: %s ms. Render time: %s ms. Frame drawn: %s",
                        actualRenderTimeStartMs -
                                (mFrameScheduler.getTargetRenderTimeMs(frameNumberToDraw) + mStartTimeMs),
                        actualRenderTimeEnd - actualRenderTimeStartMs,
                        frameDrawn);
            }
            // Schedule the next frame if needed
            long targetRenderTimeForNextFrameMs =
                    mFrameScheduler.getTargetRenderTimeForNextFrameMs(actualRenderTimeEnd - mStartTimeMs);
            if (targetRenderTimeForNextFrameMs != FrameScheduler.NO_NEXT_TARGET_RENDER_TIME) {
                scheduleNextFrame(targetRenderTimeForNextFrameMs);
            }
        }
    }

    @Override
    public void setAlpha(int alpha) {
        if (mDrawableProperties == null) {
            mDrawableProperties = new DrawableProperties();
        }
        mDrawableProperties.setAlpha(alpha);
        if (mAnimationBackend != null) {
            mAnimationBackend.setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        if (mDrawableProperties == null) {
            mDrawableProperties = new DrawableProperties();
        }
        mDrawableProperties.setColorFilter(colorFilter);
        if (mAnimationBackend != null) {
            mAnimationBackend.setColorFilter(colorFilter);
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    /**
     * 更新animation backend用于动画。这也将停止动画。为了消除当前动画的animation backend,调用这个方法传入null
     * Update the animation backend to be used for the animation.
     * This will also stop the animation.
     * In order to remove the current animation backend, call this method with null.
     *
     * @param animationBackend the animation backend to be used or null
     */
    public void setAnimationBackend(@Nullable AnimationBackend animationBackend) {
        mAnimationBackend = animationBackend;
        if (mAnimationBackend != null) {
            mFrameScheduler = new DropFramesFrameScheduler(mAnimationBackend);
            mAnimationBackend.setBounds(getBounds());
            if (mDrawableProperties != null) {
                // re-apply to the same drawable so that the animation backend is updated.
                mDrawableProperties.applyTo(this);
            }
        }
        mFrameScheduler = createSchedulerForBackendAndDelayMethod(mAnimationBackend);
        stop();
    }

    @Nullable
    public AnimationBackend getAnimationBackend() {
        return mAnimationBackend;
    }

    public long getDroppedFrames() {
        return mDroppedFrames;
    }

    public long getStartTimeMs() {
        return mStartTimeMs;
    }

    public boolean isInfiniteAnimation() {
        return mFrameScheduler != null && mFrameScheduler.isInfiniteAnimation();
    }

    /**
     * 立即跳转到给定的帧数。动画不会暂停如果它正在运行。如果不运行,动画动画不会开始。
     * Jump immediately to the given frame number. The animation will not be paused if
     * it is running. If the animation is not running, the animation will not be started.
     *
     * @param targetFrameNumber the frame number to jump to
     */
    public void jumpToFrame(int targetFrameNumber) {
        if (mAnimationBackend == null || mFrameScheduler == null) {
            return;
        }
        // In order to jump to a given frame, we have to compute the correct start time
        mLastFrameAnimationTimeMs = mFrameScheduler.getTargetRenderTimeMs(targetFrameNumber);
        mStartTimeMs = now() - mLastFrameAnimationTimeMs;
        invalidateSelf();
    }

    /**
     * 使用一个循环，查看整个动画的持续时间
     * Get the animation duration for 1 loop by summing all frame durations.
     *
     * @return the duration of 1 animation loop in ms
     */
    public long getLoopDurationMs() {
        if (mAnimationBackend == null) {
            return 0;
        }
        if (mFrameScheduler != null) {
            return mFrameScheduler.getLoopDurationMs();
        }
        int loopDurationMs = 0;
        for (int i = 0; i < mAnimationBackend.getFrameCount(); i++) {
            loopDurationMs += mAnimationBackend.getFrameDurationMs(i);
        }
        return loopDurationMs;
    }

    /**
     * 获取animation的帧数，如果没有设置animation backend，那么就是0
     * Get the number of frames for the animation.
     * If no animation backend is set, 0 will be returned.
     *
     * @return the number of frames of the animation
     */
    public int getFrameCount() {
        return mAnimationBackend == null ? 0 : mAnimationBackend.getFrameCount();
    }

    /**
     * 获取循环的次数
     * Get the loop count of the animation.
     * The returned value is either {@link AnimationInformation#LOOP_COUNT_INFINITE} if the animation
     * is repeated infinitely or a positive integer that corresponds to the number of loops.
     * If no animation backend is set, {@link AnimationInformation#LOOP_COUNT_INFINITE}
     * will be returned.
     *
     * @return the loop count of the animation or {@link AnimationInformation#LOOP_COUNT_INFINITE}
     */
    public int getLoopCount() {
        return mAnimationBackend == null
                ? 0
                : mAnimationBackend.getLoopCount();
    }

    /**
     * 设置动画的监听器，以监听各种动画事件
     * Set an animation listener that is notified for various animation events.
     * @param animationListener the listener to use
     */
    public void setAnimationListener(@Nullable AnimationListener animationListener) {
        mAnimationListener = animationListener != null
                ? animationListener
                : NO_OP_LISTENER;
    }

    /**
     * 给定一个延时，以延长每次绘制帧的间隔
     * Schedule the next frame to be rendered after the given delay.
     *
     * @param targetAnimationTimeMs the time in ms to update the frame
     */
    private void scheduleNextFrame(long targetAnimationTimeMs) {
        scheduleSelf(mInvalidateRunnable, mStartTimeMs + targetAnimationTimeMs);
    }

    private void onFrameDropped() {
        mDroppedFrames++;
        // we need to drop frames
        if (FLog.isLoggable(FLog.VERBOSE)) {
            FLog.v(TAG, "Dropped a frame. Count: %s", mDroppedFrames);
        }
    }

    /**
     * @return the current uptime in ms
     */
    private long now() {
        // This call has to return {@link SystemClock#uptimeMillis()} in order to preserve correct
        // frame scheduling.
        return SystemClock.uptimeMillis();
    }

    @Nullable
    private static FrameScheduler createSchedulerForBackendAndDelayMethod(
            @Nullable AnimationBackend animationBackend) {
        if (animationBackend == null) {
            return null;
        }
        return new DropFramesFrameScheduler(animationBackend);
    }

    /**
     * Set the animation to the given level. The level represents the animation time in ms.
     * If the animation time is greater than the last frame time for the last loop, the last
     * frame will be displayed.
     *
     * If the animation is running (e.g. if {@link #start()} has been called, the level change
     * will be ignored. In this case, {@link #stop()} the animation first.
     *
     * @param level the animation time in ms
     * @return true if the level change could be performed
     */
    @Override
    protected boolean onLevelChange(int level) {
        if (mIsRunning) {
            // If the client called start on us, they expect us to run the animation. In that case,
            // we ignore level changes.
            return false;
        }
        if (mLastFrameAnimationTimeMs != level) {
            mLastFrameAnimationTimeMs = level;
            invalidateSelf();
            return true;
        }
        return false;
    }

    @Override
    public void dropCaches() {
        if (mAnimationBackend != null) {
            mAnimationBackend.clear();
        }
    }
}
