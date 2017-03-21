package com.facebook.drawee.drawable;

/**
 * Created by heshixiyang on 2017/3/10.
 */
/**
 * 一个允许设置是否可见的回调的接口
 * Interface that enables setting a visibility callback.
 */
public interface VisibilityAwareDrawable {

    /**
     * 设置是否可见的回调
     * Sets a visibility callback.
     *
     * @param visibilityCallback the visibility callback to be set
     */
    void setVisibilityCallback(VisibilityCallback visibilityCallback);
}
