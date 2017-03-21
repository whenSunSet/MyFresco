package com.facebook.drawee.drawable;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import android.graphics.Matrix;
import android.graphics.RectF;

/**
 * 一个允许传入任何变换矩阵和根边界的回调
 * Callback that is used to pass any transformation matrix and the root bounds from a parent
 * drawable to its child.
 */
public interface TransformCallback {

    /**
     * 当drawable需要获取所有应用矩阵
     * Called when the drawable needs to get all matrices applied to it.
     *
     * @param transform Matrix that is applied to the drawable by the parent drawables.
     */
    void getTransform(Matrix transform);

    /**
     * 当drawable需要获取他的 root bounds的时候调用
     * Called when the drawable needs to get its root bounds.
     *
     * @param bounds The root bounds of the drawable.
     */
    void getRootBounds(RectF bounds);
}
