package com.facebook.drawee.drawable;

/**
 * Created by heshixiyang on 2017/3/10.
 */
/**
 * 通知所有drawable发送可见性变化的回调
 * Callback used to notify about drawable's visibility changes.
 */
public interface VisibilityCallback {

    /**
     * 当drawable可见性发送变化的时候调用
     * Called when the drawable's visibility changes.
     *
     * @param visible whether or not the drawable is visible
     */
    void onVisibilityChange(boolean visible);

    /**
     * 当drawable绘制的时候调用
     * Called when the drawable gets drawn.
     */
    void onDraw();
}
