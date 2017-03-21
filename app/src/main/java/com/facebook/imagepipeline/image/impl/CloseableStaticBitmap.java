package com.facebook.imagepipeline.image.impl;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import android.graphics.Bitmap;

import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.references.CloseableReference;
import com.facebook.commom.references.ResourceReleaser;
import com.facebook.imagepipeline.image.QualityInfo;
import com.facebook.imageutils.BitmapUtil;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * 一个CloseableImage只包含一个Bitmap
 * CloseableImage that contains one Bitmap.
 */
@ThreadSafe
public class CloseableStaticBitmap extends CloseableBitmap {

    @GuardedBy("this")
    private CloseableReference<Bitmap> mBitmapReference;

    private volatile Bitmap mBitmap;

    // quality info
    private final QualityInfo mQualityInfo;

    private final int mRotationAngle;

    /**
     *
     * Creates a new instance of a CloseableStaticBitmap.
     *
     * @param bitmap the bitmap to wrap
     * @param resourceReleaser ResourceReleaser to release the bitmap to
     */
    public CloseableStaticBitmap(
            Bitmap bitmap,
            ResourceReleaser<Bitmap> resourceReleaser,
            QualityInfo qualityInfo,
            int rotationAngle) {
        mBitmap = Preconditions.checkNotNull(bitmap);
        mBitmapReference = CloseableReference.of(
                mBitmap,
                Preconditions.checkNotNull(resourceReleaser));
        mQualityInfo = qualityInfo;
        mRotationAngle = rotationAngle;
    }

    /**
     * 创建一个CloseableStaticBitmap从一个已经存在的CloseableReference中
     * CloseableStaticBitmap将持有一个reference知道其关闭
     * Creates a new instance of a CloseableStaticBitmap from an existing CloseableReference. The
     * CloseableStaticBitmap will hold a reference to the Bitmap until it's closed.
     *
     * @param bitmapReference the bitmap reference.
     */
    public CloseableStaticBitmap(
            CloseableReference<Bitmap> bitmapReference,
            QualityInfo qualityInfo,
            int rotationAngle) {
        mBitmapReference = Preconditions.checkNotNull(bitmapReference.cloneOrNull());
        mBitmap = mBitmapReference.get();
        mQualityInfo = qualityInfo;
        mRotationAngle = rotationAngle;
    }

    /**
     * 释放比bitmap
     * Releases the bitmap to the pool.
     */
    @Override
    public void close() {
        CloseableReference<Bitmap> reference = detachBitmapReference();
        if (reference != null) {
            reference.close();
        }
    }

    private synchronized CloseableReference<Bitmap> detachBitmapReference() {
        CloseableReference<Bitmap> reference = mBitmapReference;
        mBitmapReference = null;
        mBitmap = null;
        return reference;
    }

    /**
     * 将这个object转换成一个 CloseableReference<Bitmap>
     * 你不能再这个对象已经关闭之后调用这个方法
     *
     * Convert this object to a CloseableReference&lt;Bitmap&gt;.
     * <p>You cannot call this method on an object that has already been closed.
     * <p>The reference count of the bitmap is preserved. After calling this method, this object
     * can no longer be used and no longer points to the bitmap.
     * @throws IllegalArgumentException if this object has already been closed.
     */
    public synchronized CloseableReference<Bitmap> convertToBitmapReference() {
        Preconditions.checkNotNull(mBitmapReference, "Cannot convert a closed static bitmap");
        return detachBitmapReference();
    }

    /**
     * Returns whether this instance is closed.
     */
    @Override
    public synchronized boolean isClosed() {
        return mBitmapReference == null;
    }

    /**
     * Gets the underlying bitmap.
     *
     * @return the underlying bitmap
     */
    @Override
    public Bitmap getUnderlyingBitmap() {
        return mBitmap;
    }

    /**
     * @return size in bytes of the underlying bitmap
     */
    @Override
    public int getSizeInBytes() {
        return BitmapUtil.getSizeInBytes(mBitmap);
    }

    /**
     * @return width of the image
     */
    @Override
    public int getWidth() {
        if (mRotationAngle == 90 || mRotationAngle == 270) {
            return getBitmapHeight(mBitmap);
        }
        return getBitmapWidth(mBitmap);
    }

    /**
     * @return height of the image
     */
    @Override
    public int getHeight() {
        if (mRotationAngle == 90 || mRotationAngle == 270) {
            return getBitmapWidth(mBitmap);
        }
        return getBitmapHeight(mBitmap);
    }

    private static int getBitmapWidth(@Nullable Bitmap bitmap) {
        return (bitmap == null) ? 0 : bitmap.getWidth();
    }

    private static int getBitmapHeight(@Nullable Bitmap bitmap) {
        return (bitmap == null) ? 0 : bitmap.getHeight();
    }

    /**
     * 返回image旋转的角度
     * @return the rotation angle of the image
     */
    public int getRotationAngle() {
        return mRotationAngle;
    }

    /**
     * Returns quality information for the image.
     */
    @Override
    public QualityInfo getQualityInfo() {
        return mQualityInfo;
    }
}
