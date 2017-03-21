package com.facebook.drawee.drawable;

/**
 * Created by heshixiyang on 2017/3/10.
 */
/**
 * 一个允许设置一个变化回调的接口
 * Interface that enables setting a transform callback.
 */
public interface TransformAwareDrawable {

    /**
     * 设置一个回调接口
     * Sets a transform callback.
     *
     * @param transformCallback the transform callback to be set
     */
    void setTransformCallback(TransformCallback transformCallback);
}
