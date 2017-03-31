package com.facebook.imagepipeline.animated.base;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import android.graphics.Canvas;
import android.graphics.Rect;

import com.facebook.imagepipeline.animated.base.impl.AnimatedDrawable;


/**
 * 用来在动画上面绘制debug信息的接口
 * Diagnostics interface for {@link AnimatedDrawable}.
 */
public interface AnimatedDrawableDiagnostics {

    /**
     * 设置{@link AnimatedDrawable}正在使用的backend
     * Sets the backend that the {@link AnimatedDrawable} is using.
     *
     * @param animatedDrawableBackend the backend
     */
    void setBackend(AnimatedDrawableCachingBackend animatedDrawableBackend);

    /**
     * 在{@link AnimatedDrawable#onStart}开始的时候调用，
     * 这个方法表示animation被重置了或者开始了
     * Called when the {@link AnimatedDrawable#onStart} method begins, which is the method that
     * resets and starts the animation.
     */
    void onStartMethodBegin();

    /**
     * 在{@link AnimatedDrawable#onStart}结束的时候调用
     * Called when the {@link AnimatedDrawable#onStart} method ends.
     */
    void onStartMethodEnd();

    /**
     * 在{@link AnimatedDrawable#onNextFrame}开始的时候调用
     * 这个方法是决定下一帧的渲染和配置
     * Called when the {@link AnimatedDrawable#onNextFrame} method begins, which is the method that
     * determines the next frame to render and configures itself to do so.
     */
    void onNextFrameMethodBegin();

    /**
     * 在{@link AnimatedDrawable#onNextFrame}结束的时候调用
     * Called when the {@link AnimatedDrawable#onNextFrame} method ends.
     */
    void onNextFrameMethodEnd();

    /**
     * 增加了掉帧用于统计
     * Increments the number of dropped frames for stats purposes.
     *
     * @param droppedFrames the number of dropped frames
     */
    void incrementDroppedFrames(int droppedFrames);

    /**
     * 增加了绘制帧用于统计
     * Increments the number of drawn frames for stats purposes.
     *
     * @param drawnFrames the number of drawn frames
     */
    void incrementDrawnFrames(int drawnFrames);

    /**
     * 当{@link AnimatedDrawable#draw}调开始的时候调用
     * Called when the {@link AnimatedDrawable#draw} method begins.
     */
    void onDrawMethodBegin();

    /**
     * 当{@link AnimatedDrawable#draw}结束的时候调用
     * Called when the {@link AnimatedDrawable#draw} method emds.
     */
    void onDrawMethodEnd();

    /**
     * 用于覆盖一个诊断的代码，可能被用来debug
     * Allows the diagnostics code to draw an overlay that may be useful for debugging.
     *
     * @param canvas the canvas to draw to
     * @param destRect the rectangle bounds to draw to
     */
    void drawDebugOverlay(Canvas canvas, Rect destRect);
}
