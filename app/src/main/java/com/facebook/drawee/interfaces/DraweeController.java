package com.facebook.drawee.interfaces;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import android.graphics.drawable.Animatable;
import android.support.annotation.Nullable;
import android.view.MotionEvent;

/**
 * 一个代表Drawee controller的接口，被DraweeView使用
 *
 * 视图将事件转发给控制器。控制器基于这些事件控制其层次结构
 */
public interface DraweeController {

    /** Gets the hierarchy. */
    @Nullable
    DraweeHierarchy getHierarchy();

    /** Sets a new hierarchy. */
    void setHierarchy(@Nullable DraweeHierarchy hierarchy);

    /**
     * 当view包含的视图层次已经attached了一个window的时候调用
     */
    void onAttach();

    /**
     * 当view包含的视图层次已经detached了一个window的时候调用
     */
    void onDetach();

    /**
     * An optional hint whether the view containing the hierarchy is currently within the visible
     * viewport or not.
     */
    void onViewportVisibilityHint(boolean isVisibleInViewportHint);

    /**
     * 当view包含的视图层次接收到触摸事件的时候被调用
     * @return true if the event was handled by the controller, false otherwise
     */
    boolean onTouchEvent(MotionEvent event);

    /**
     * 对于一个有动画的image，返回一个Animatable让客户端控制动画
     * @return animatable, or null if the image is not animated or not loaded yet
     */
    Animatable getAnimatable();

    /** 设置可访问性内容描述. */
    void setContentDescription(String contentDescription);

    /**
     * 获取可访问性内容描述
     * @return content description, or null if the image has no content description
     */
    String getContentDescription();
}
