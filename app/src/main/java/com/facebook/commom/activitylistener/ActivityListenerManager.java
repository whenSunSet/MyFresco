package com.facebook.commom.activitylistener;

/**
 * Created by heshixiyang on 2017/3/12.
 */

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;

import com.facebook.commom.internal.Preconditions;

import java.lang.ref.WeakReference;

/**
 * Registers ActivityListener with ActivityListener.
 * 弱引用用于包装一个ActivityVisibilityListener。无效时ActivityListener将自动删除从ListenableActivity中
 * <p> A WeakReference is used to wrap an ActivityVisibilityListener. When it is nullified
 * ActivityListener is automatically removed from the listened ListenableActivity.
 */
public class ActivityListenerManager {

    /**
     * 如果被给予的context是一个ListenableActivity的实体，那么创建一个
     * WeakReferenceActivityListenerAdapter实体然后注册activity的监听列表
     * If given context is an instance of ListenableActivity then creates new instance of
     * WeakReferenceActivityListenerAdapter and adds it to activity's listeners
     */
    public static void register(
            ActivityListener activityListener,
            Context context) {
        if (!(context instanceof ListenableActivity) && context instanceof ContextWrapper) {
            context = ((ContextWrapper) context).getBaseContext();
        }
        if (context instanceof ListenableActivity) {
            ListenableActivity listenableActivity = (ListenableActivity) context;
            Listener listener = new Listener(activityListener);
            listenableActivity.addActivityListener(listener);
        }
    }

    private static class Listener extends BaseActivityListener {
        private final WeakReference<ActivityListener> mActivityListenerRef;

        public Listener(ActivityListener activityListener) {
            mActivityListenerRef = new WeakReference<ActivityListener>(activityListener);
        }

        @Override
        public void onActivityCreate(Activity activity) {
            ActivityListener activityVisibilityListener = getListenerOrCleanUp(activity);
            if (activityVisibilityListener != null) {
                activityVisibilityListener.onActivityCreate(activity);
            }
        }

        @Override
        public void onDestroy(Activity activity) {
            ActivityListener activityVisibilityListener = getListenerOrCleanUp(activity);
            if (activityVisibilityListener != null) {
                activityVisibilityListener.onDestroy(activity);
            }
        }

        @Override
        public void onStart(Activity activity) {
            ActivityListener activityVisibilityListener = getListenerOrCleanUp(activity);
            if (activityVisibilityListener != null) {
                activityVisibilityListener.onStart(activity);
            }
        }

        @Override
        public void onStop(Activity activity) {
            ActivityListener activityVisibilityListener = getListenerOrCleanUp(activity);
            if (activityVisibilityListener != null) {
                activityVisibilityListener.onStop(activity);
            }
        }

        @Override
        public void onResume(Activity activity) {
            ActivityListener activityVisibilityListener = getListenerOrCleanUp(activity);
            if (activityVisibilityListener != null) {
                activityVisibilityListener.onResume(activity);
            }
        }

        @Override
        public void onPause(Activity activity) {
            ActivityListener activityVisibilityListener = getListenerOrCleanUp(activity);
            if (activityVisibilityListener != null) {
                activityVisibilityListener.onPause(activity);
            }
        }

        private ActivityListener getListenerOrCleanUp(Activity activity) {
            ActivityListener activityVisibilityListener = mActivityListenerRef.get();
            if (activityVisibilityListener == null) {
                Preconditions.checkArgument(activity instanceof ListenableActivity);
                ListenableActivity listenableActivity = (ListenableActivity) activity;
                listenableActivity.removeActivityListener(this);
            }
            return activityVisibilityListener;
        }
    }
}
