package com.facebook.drawee.view;

/**
 * Created by heshixiyang on 2017/3/20.
 */

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;

import com.facebook.commom.activitylistener.ListenableActivity;
import com.facebook.commom.internal.Objects;
import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.logging.FLog;
import com.facebook.drawee.componnents.DraweeEventTracker;
import com.facebook.drawee.drawable.VisibilityAwareDrawable;
import com.facebook.drawee.drawable.VisibilityCallback;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.interfaces.DraweeHierarchy;

import javax.annotation.Nullable;

import static com.facebook.drawee.componnents.DraweeEventTracker.Event;

/**
 * 持有Drawee controller 和hierarchy的holder
 * A holder class for Drawee controller and hierarchy.
 *
 * Drawee的使用者，应该遵循一个规则，使用{@link DraweeView}或其子类。在需要自定义view的时候。
 * 这个class就是在这种情况下使用
 * <p>Drawee users, should, as a rule, use {@link DraweeView} or its subclasses. There are
 * situations where custom views are required, however, and this class is for those circumstances.
 * 每一个{@link DraweeHierarchy}都应该被包括在一个这样的对象中
 * <p>Each {@link DraweeHierarchy} object should be contained in a single instance of this
 * class.
 * 使用这个class的使用者必须调用 {@link Drawable#setBounds}在DraweeHierarchy的顶层drawable中
 * 此外drawable不需要被绘制
 * <p>Users of this class must call {@link Drawable#setBounds} on the top-level drawable
 * of the DraweeHierarchy. Otherwise the drawable will not be drawn.
 *
 * <p>The containing view must also call {@link #onDetach()} from its
 * {@link View#onStartTemporaryDetach()} and {@link View#onDetachedFromWindow()} methods. It must
 * call {@link #onAttach} from its {@link View#onFinishTemporaryDetach()} and
 * {@link View#onAttachedToWindow()} methods.
 */
public class DraweeHolder<DH extends DraweeHierarchy>
        implements VisibilityCallback {

    private boolean mIsControllerAttached = false;
    private boolean mIsHolderAttached = false;
    private boolean mIsVisible = true;
    private DH mHierarchy;

    private DraweeController mController = null;

    private final DraweeEventTracker mEventTracker = DraweeEventTracker.newInstance();

    /**
     *
     * Creates a new instance of DraweeHolder that detaches / attaches controller whenever context
     * notifies it about activity's onStop and onStart callbacks.
     *
     * <p>It is recommended to pass a {@link ListenableActivity} as context. This will help in a future release.
     */
    public static <DH extends DraweeHierarchy> DraweeHolder<DH> create(
            @Nullable DH hierarchy,
            Context context) {
        DraweeHolder<DH> holder = new DraweeHolder<DH>(hierarchy);
        holder.registerWithContext(context);
        return holder;
    }

    /** For future use. */
    public void registerWithContext(Context context) {
    }

    /**
     * Creates a new instance of DraweeHolder.
     * @param hierarchy
     */
    public DraweeHolder(@Nullable DH hierarchy) {
        if (hierarchy != null) {
            setHierarchy(hierarchy);
        }
    }

    /**
     * 获取controller准备显示image
     * Gets the controller ready to display the image.
     * view应该调用这个方法在{@link View#onFinishTemporaryDetach()}或者{@link View#onAttachedToWindow()}中
     * <p>The containing view must call this method from both {@link View#onFinishTemporaryDetach()}
     * and {@link View#onAttachedToWindow()}.
     */
    public void onAttach() {
        mEventTracker.recordEvent(Event.ON_HOLDER_ATTACH);
        mIsHolderAttached = true;
        attachOrDetachController();
    }

    /**
     * Checks whether the view that uses this holder is currently attached to a window.
     *
     * {@see #onAttach()}
     * {@see #onDetach()}
     *
     * @return true if the holder is currently attached
     */
    public boolean isAttached() {
        return mIsHolderAttached;
    }

    /**
     * 释放显示的image的资源，
     * Releases resources used to display the image.
     * 这个方法应该在{@link View#onStartTemporaryDetach()}和{@link View#onDetachedFromWindow()}中调用
     * <p>The containing view must call this method from both {@link View#onStartTemporaryDetach()}
     * and {@link View#onDetachedFromWindow()}.
     */
    public void onDetach() {
        mEventTracker.recordEvent(Event.ON_HOLDER_DETACH);
        mIsHolderAttached = false;
        attachOrDetachController();
    }

    /**
     * 将触摸事件分发给controller
     * Forwards the touch event to the controller.
     * @param event touch event to handle
     * @return whether the event was handled or not
     */
    public boolean onTouchEvent(MotionEvent event) {
        if (!isControllerValid()) {
            return false;
        }
        return mController.onTouchEvent(event);
    }

    /**
     * 回调用于通知关于top-level-drawable's 的可见性的改变
     * Callback used to notify about top-level-drawable's visibility changes.
     */
    @Override
    public void onVisibilityChange(boolean isVisible) {
        if (mIsVisible == isVisible) {
            return;
        }
        mEventTracker.recordEvent(isVisible ? Event.ON_DRAWABLE_SHOW : Event.ON_DRAWABLE_HIDE);
        mIsVisible = isVisible;
        attachOrDetachController();
    }

    /**
     * 回调用于通知关于top-level-drawable's 的绘制
     * Callback used to notify about top-level-drawable being drawn.
     */
    @Override
    public void onDraw() {
        // draw is only expected if the controller is attached
        if (mIsControllerAttached) {
            return;
        }

        // something went wrong here; controller is not attached, yet the hierarchy has to be drawn
        // log error and attach the controller
        FLog.wtf(
                DraweeEventTracker.class,
                "%x: Draw requested for a non-attached controller %x. %s",
                System.identityHashCode(this),
                System.identityHashCode(mController),
                toString());

        mIsHolderAttached = true;
        mIsVisible = true;
        attachOrDetachController();
    }

    /**
     * 将visibility callback 设置给当前的top-level-drawable
     * Sets the visibility callback to the current top-level-drawable.
     */
    private void setVisibilityCallback(@Nullable VisibilityCallback visibilityCallback) {
        Drawable drawable = getTopLevelDrawable();
        if (drawable instanceof VisibilityAwareDrawable) {
            ((VisibilityAwareDrawable) drawable).setVisibilityCallback(visibilityCallback);
        }
    }

    /**
     * Sets a new controller.
     */
    public void setController(@Nullable DraweeController draweeController) {
        boolean wasAttached = mIsControllerAttached;
        if (wasAttached) {
            detachController();
        }

        // Clear the old controller
        if (isControllerValid()) {
            mEventTracker.recordEvent(Event.ON_CLEAR_OLD_CONTROLLER);
            mController.setHierarchy(null);
        }
        mController = draweeController;
        if (mController != null) {
            mEventTracker.recordEvent(Event.ON_SET_CONTROLLER);
            mController.setHierarchy(mHierarchy);
        } else {
            mEventTracker.recordEvent(Event.ON_CLEAR_CONTROLLER);
        }

        if (wasAttached) {
            attachController();
        }
    }

    /**
     * 获取controller如果被设置了
     * Gets the controller if set, null otherwise.
     */
    @Nullable public DraweeController getController() {
        return mController;
    }

    /**
     * Sets the drawee hierarchy.
     */
    public void setHierarchy(DH hierarchy) {
        mEventTracker.recordEvent(Event.ON_SET_HIERARCHY);
        final boolean isControllerValid = isControllerValid();

        setVisibilityCallback(null);
        mHierarchy = Preconditions.checkNotNull(hierarchy);
        Drawable drawable = mHierarchy.getTopLevelDrawable();
        onVisibilityChange(drawable == null || drawable.isVisible());
        setVisibilityCallback(this);

        if (isControllerValid) {
            mController.setHierarchy(hierarchy);
        }
    }

    /**
     * Gets the drawee hierarchy if set, throws NPE otherwise.
     */
    public DH getHierarchy() {
        return Preconditions.checkNotNull(mHierarchy);
    }

    /**
     * Returns whether the hierarchy is set or not.
     */
    public boolean hasHierarchy() {
        return mHierarchy != null;
    }

    /**
     * 获取top-level drawable 如果hierarchy已经被设置了
     * Gets the top-level drawable if hierarchy is set, null otherwise.
     */
    public Drawable getTopLevelDrawable() {
        return mHierarchy == null ? null : mHierarchy.getTopLevelDrawable();
    }

    protected DraweeEventTracker getDraweeEventTracker() {
        return mEventTracker;
    }

    private void attachController() {
        if (mIsControllerAttached) {
            return;
        }
        mEventTracker.recordEvent(Event.ON_ATTACH_CONTROLLER);
        mIsControllerAttached = true;
        if (mController != null &&
                mController.getHierarchy() != null) {
            mController.onAttach();
        }
    }

    private void detachController() {
        if (!mIsControllerAttached) {
            return;
        }
        mEventTracker.recordEvent(Event.ON_DETACH_CONTROLLER);
        mIsControllerAttached = false;
        if (isControllerValid()) {
            mController.onDetach();
        }
    }

    private void attachOrDetachController() {
        if (mIsHolderAttached && mIsVisible) {
            attachController();
        } else {
            detachController();
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("controllerAttached", mIsControllerAttached)
                .add("holderAttached", mIsHolderAttached)
                .add("drawableVisible", mIsVisible)
                .add("events", mEventTracker.toString())
                .toString();
    }

    private boolean isControllerValid() {
        return mController != null && mController.getHierarchy() == mHierarchy;
    }
}
