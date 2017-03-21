package com.facebook.drawee.drawable.impl;

/**
 * Created by heshixiyang on 2017/3/20.
 */

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.facebook.commom.internal.Preconditions;

/**
 * 可以通过{@link Matrix}来调整被委托的drawable，类似于缩放和旋转的变换
 * Drawable that can adjust underlying drawable based on specified {@link Matrix}.
 */
public class MatrixDrawable extends ForwardingDrawable {

    //client指定的matrix
    // Specified matrix.
    private Matrix mMatrix;

    //要被使用的matrix，会在运行过程中被赋值为mMatrix，也可能为null
    // Matrix that is actually being used for drawing. In case underlying drawable doesn't have
    // intrinsic dimensions, this will be null (i.e. no matrix will be applied).
    private Matrix mDrawMatrix;

    //被代理的drawable的长和宽，用于避免每次计算范围，如果被代理的drawable没有改变的话
    // Last known dimensions of the underlying drawable. Used to avoid computing bounds every time
    // if underlying size hasn't changed.
    private int mUnderlyingWidth = 0;
    private int mUnderlyingHeight = 0;

    /**
     * Creates a new MatrixDrawable with given underlying drawable and matrix.
     * @param drawable underlying drawable to apply the matrix to
     * @param matrix matrix to be applied to the drawable
     */
    public MatrixDrawable(Drawable drawable, Matrix matrix) {
        super(Preconditions.checkNotNull(drawable));
        mMatrix = matrix;
    }

    @Override
    public Drawable setCurrent(Drawable newDelegate) {
        final Drawable previousDelegate = super.setCurrent(newDelegate);
        configureBounds();

        return previousDelegate;
    }

    /**
     * Gets the current matrix.
     * @return matrix
     */
    public Matrix getMatrix() {
        return mMatrix;
    }

    /**
     * Sets the matrix.
     * @param matrix matrix to set
     */
    public void setMatrix(Matrix matrix) {
        mMatrix = matrix;
        configureBounds();
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        configureBoundsIfUnderlyingChanged();
        if (mDrawMatrix != null) {
            int saveCount = canvas.save();
            canvas.clipRect(getBounds());
            canvas.concat(mDrawMatrix);
            super.draw(canvas);
            canvas.restoreToCount(saveCount);
        } else {
            //如果mDrawMatrix == null ，说明没有对被代理的drawable进行变换，此时就可以直接绘制了
            // mDrawMatrix == null means our bounds match and we can take fast path
            super.draw(canvas);
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        configureBounds();
    }
   //判断被代理的drawable是否需要进行变换
    private void configureBoundsIfUnderlyingChanged() {
        if (mUnderlyingWidth != getCurrent().getIntrinsicWidth() ||
                mUnderlyingHeight != getCurrent().getIntrinsicHeight()) {
            configureBounds();
        }
    }

    /**
     *
     * Determines bounds for the underlying drawable and a matrix that should be applied on it.
     */
    private void configureBounds() {
        Drawable underlyingDrawable = getCurrent();
        Rect bounds = getBounds();
        int underlyingWidth = mUnderlyingWidth = underlyingDrawable.getIntrinsicWidth();
        int underlyingHeight = mUnderlyingHeight = underlyingDrawable.getIntrinsicHeight();

        // In case underlying drawable doesn't have intrinsic dimensions, we cannot set its bounds to
        // -1 so we use our bounds and discard specified matrix. In normal case we use drawable's
        // intrinsic dimensions for its bounds and apply specified matrix to it.
        if (underlyingWidth <= 0 || underlyingHeight <= 0) {
            underlyingDrawable.setBounds(bounds);
            mDrawMatrix = null;
        } else {
            underlyingDrawable.setBounds(0, 0, underlyingWidth, underlyingHeight);
            mDrawMatrix = mMatrix;
        }
    }

    /**
     * TransformationCallback method
     * @param transform
     */
    @Override
    public void getTransform(Matrix transform) {
        super.getTransform(transform);
        if (mDrawMatrix != null) {
            transform.preConcat(mDrawMatrix);
        }
    }
}
