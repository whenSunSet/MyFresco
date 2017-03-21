package com.facebook.drawee.generic;

/**
 * Created by heshixiyang on 2017/3/19.
 */

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.facebook.commom.internal.VisibleForTesting;
import com.facebook.drawee.drawable.VisibilityAwareDrawable;
import com.facebook.drawee.drawable.VisibilityCallback;
import com.facebook.drawee.drawable.impl.ForwardingDrawable;

import javax.annotation.Nullable;

/**
 * 一个DraweeHierarchy的根drawable
 * The root drawable of a DraweeHierarchy.
 *
 * 这个根drawable有以下几个功能
 * Root drawable has several functions:
 * <ul>
 * 一个hierarchy只有一个根drawable，所以当hierarchy内部结构发送变化的时候，不需要设置一个新的drawable
 * <li> A hierarchy always has the same instance of a root drawable. That means that internal
 * structural changes within the hierarchy don't require setting a new drawable to the view.
 * 这个根drawable会阻止内在的长宽超出hierarchy的范围，这也就反过来阻止了View出现设置错误比例的图片
 * 当scaling变化的时候
 * <li> Root drawable prevents intrinsic dimensions to escape the hierarchy. This in turn prevents
 * view to do any erroneous scaling based on those intrinsic dimensions, as the hierarchy is in
 * charge of all the required scaling.
 * 根drawable可以调整可见性，通过设置Visibility callback 。
 * <li> Root drawable is visibility aware. Visibility callback is used to attach the controller
 * (if not already attached) when the hierarchy needs to be drawn. This prevents photo-not-loading
 * issues in case attach event has not been called (for whatever reason). It also helps with
 * memory management as the controller will get detached if the drawable is not visible.
 * 根drawable的支持控制器覆盖,一个特殊的覆盖设定的控制器。典型用法是调试、诊断和其他情况下控制器——具体覆盖是必需的。
 * <li> Root drawable supports controller overlay, a special overlay set by the controller. Typical
 * usages are debugging, diagnostics and other cases where controller-specific overlay is required.
 * </ul>
 */
public class RootDrawable extends ForwardingDrawable implements VisibilityAwareDrawable {

    @VisibleForTesting
    @Nullable
    Drawable mControllerOverlay = null;

    @Nullable
    private VisibilityCallback mVisibilityCallback;

    public RootDrawable(Drawable drawable) {
        super(drawable);
    }

    @Override
    public int getIntrinsicWidth() {
        return -1;
    }

    @Override
    public int getIntrinsicHeight() {
        return -1;
    }

    @Override
    public void setVisibilityCallback(@Nullable VisibilityCallback visibilityCallback) {
        mVisibilityCallback = visibilityCallback;
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        if (mVisibilityCallback != null) {
            mVisibilityCallback.onVisibilityChange(visible);
        }
        return super.setVisible(visible, restart);
    }

    @SuppressLint("WrongCall")
    @Override
    public void draw(Canvas canvas) {
        if (!isVisible()) {
            return;
        }
        if (mVisibilityCallback != null) {
            mVisibilityCallback.onDraw();
        }
        super.draw(canvas);
        if (mControllerOverlay != null) {
            mControllerOverlay.setBounds(getBounds());
            mControllerOverlay.draw(canvas);
        }
    }

    public void setControllerOverlay(@Nullable Drawable controllerOverlay) {
        mControllerOverlay = controllerOverlay;
        invalidateSelf();
    }
}
