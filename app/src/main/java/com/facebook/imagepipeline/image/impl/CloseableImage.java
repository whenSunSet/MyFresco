package com.facebook.imagepipeline.image.impl;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import com.facebook.commom.logging.FLog;
import com.facebook.imagepipeline.image.ImageInfo;
import com.facebook.imagepipeline.image.QualityInfo;

import java.io.Closeable;

/**
 * 一个简单的image包装类，实现了{@link Closeable}
 * A simple wrapper around an image that implements {@link Closeable}
 */
public abstract class CloseableImage implements Closeable, ImageInfo {
    private static final String TAG = "CloseableImage";

    /**
     * @return size in bytes of the bitmap(s)
     */
    public abstract int getSizeInBytes();

    /**
     * 关闭实体是否资源
     * Closes this instance and releases the resources.
     */
    @Override
    public abstract void close();

    /**
     * 返回该实体是否应该被关闭
     * Returns whether this instance is closed.
     */
    public abstract boolean isClosed();

    /**
     * 返回图像的质量信息
     * Returns quality information for the image.
     * 图像的类可以包含中间结果应该覆盖这是适当的。
     * <p> Image classes that can contain intermediate results should override this as appropriate.
     */
    @Override
    public QualityInfo getQualityInfo() {
        return ImmutableQualityInfo.FULL_QUALITY;
    }

    /**
     * 这张图片是否包含图像的特定视图的状态(例如,*动画GIF图像可能包含当前帧正在查看)。
     * 这意味着,不应存储在图像位图缓存。
     * Whether or not this image contains state for a particular view of the image (for example,
     * the image for an animated GIF might contain the current frame being viewed). This means
     * that the image should not be stored in the bitmap cache.
     */
    public boolean isStateful() {
        return false;
    }

    /**
     * Ensures that the underlying resources are always properly released.
     */
    @Override
    protected void finalize() throws Throwable {
        if (isClosed()) {
            return;
        }
        FLog.w(
                TAG,
                "finalize: %s %x still open.",
                this.getClass().getSimpleName(),
                System.identityHashCode(this));
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}
