package com.facebook.drawee.controller;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import android.graphics.drawable.Animatable;
import android.support.annotation.Nullable;

/**
 * AbstractDraweeController 的监听器接口
 *
 * 控制器id传递给每个侦听器方法,用于调试和仪表是有用的,这些事件可以被关联到一个序列.
 * 观察者是完全可以忽略这个id的，回调的正确性由控制器自己保证
 *
 * @param <INFO> imageInfo类型
 */
public interface ControllerListener<INFO> {

    /**
     * 在image 请求之前调用
     * 重要：在一个回调中重复使用一个控制器是不安全的。
     * @param id controller id
     * @param callerContext caller context
     */
    void onSubmit(String id, Object callerContext);

    /**
     * 在image被设置之后被调
     * @param id controller id
     * @param imageInfo image info
     * @param animatable
     */
    void onFinalImageSet(String id, @Nullable INFO imageInfo, @Nullable Animatable animatable);

    /**
     * 在中间image产物被设置的时候被调用
     * @param id controller id
     * @param imageInfo image info
     */
    void onIntermediateImageSet(String id, @Nullable INFO imageInfo);

    /**
     * 当中间image获取失败的时候被调用
     * Called after the fetch of the intermediate image failed.
     * @param id controller id
     * @param throwable failure cause
     */
    void onIntermediateImageFailed(String id, Throwable throwable);

    /**
     * 当image获取失败的时候被调用
     * @param id controller id
     * @param throwable failure cause
     */
    void onFailure(String id, Throwable throwable);

    /**
     * 当控制器释放获取的Image的时候被调用
     * 重要：在一个回调中重复使用一个控制器是不安全的。
     * @param id controller id
     */
    void onRelease(String id);
}
