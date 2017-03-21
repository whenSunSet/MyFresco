package com.facebook.imagepipeline.image.impl;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import android.graphics.Bitmap;

/**
 * 包装一个bitmap
 * {@link CloseableImage} that wraps a bitmap.
 */
public abstract class CloseableBitmap extends CloseableImage {

    /**
     * 获取bitmap
     * Gets the underlying bitmap.
     * 注意:必须小心,因为子类可能比这更复杂。例如,*动画图可能有许多帧和该方法将只返回第一个。
     * Note: care must be taken because subclasses might be more sophisticated than that. For example,
     * animated bitmap may have many frames and this method will only return the first one.
     * @return the underlying bitmap
     */
    public abstract Bitmap getUnderlyingBitmap();

}
