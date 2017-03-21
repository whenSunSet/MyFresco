package com.facebook.imagepipeline.datasource;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import android.graphics.Bitmap;

import com.facebook.commom.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.datasource.impl.BaseDataSubscriber;
import com.facebook.imagepipeline.image.impl.CloseableBitmap;
import com.facebook.imagepipeline.image.impl.CloseableImage;

import javax.annotation.Nullable;

/**
 * Implementation of {@link DataSubscriber} for cases where the client wants access to a bitmap.
 *
 * <p>
 * Sample usage:
 * <pre>
 * <code>
 * dataSource.subscribe(
 *   new BaseBitmapDataSubscriber() {
 *     {@literal @}Override
 *     public void onNewResultImpl(@Nullable Bitmap bitmap) {
 *       // Pass bitmap to system, which makes a copy of the bitmap.
 *       updateStatus(bitmap);
 *       // No need to do any cleanup.
 *     }
 *
 *     {@literal @}Override
 *     public void onFailureImpl(DataSource dataSource) {
 *       // No cleanup required here.
 *     }
 *   });
 * </code>
 * </pre>
 */
public abstract class BaseBitmapDataSubscriber extends
        BaseDataSubscriber<CloseableReference<CloseableImage>> {

    @Override
    public void onNewResultImpl(DataSource<CloseableReference<CloseableImage>> dataSource) {
        if (!dataSource.isFinished()) {
            return;
        }

        CloseableReference<CloseableImage> closeableImageRef = dataSource.getResult();
        Bitmap bitmap = null;
        if (closeableImageRef != null &&
                closeableImageRef.get() instanceof CloseableBitmap) {
            bitmap = ((CloseableBitmap) closeableImageRef.get()).getUnderlyingBitmap();
        }

        try {
            onNewResultImpl(bitmap);
        } finally {
            CloseableReference.closeSafely(closeableImageRef);
        }
    }

    /**
     * The bitmap provided to this method is only guaranteed to be around for the lifespan of the
     * method.
     *
     * <p>The framework will free the bitmap's memory after this method has completed.
     * @param bitmap
     */
    protected abstract void onNewResultImpl(@Nullable Bitmap bitmap);
}
