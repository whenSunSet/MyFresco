package com.facebook.commom.activitylistener;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

import android.app.Activity;

/**
 * Activity的生命周期事件的侦听器接口。
 * <p>
 * All methods take the Activity so it's possible to implement a singleton version of this
 * interface that has no-state.
 */
public interface ActivityListener {

    /**
     * Called by the Activity base class after the Activity's <code>onActivityCreate</code>
     * method has run.
     *
     * @param activity the activity
     */
    void onActivityCreate(Activity activity);

    /**
     * Called by the Activity base class from the {@link Activity#onStart} method.
     *
     * @param activity the activity
     */
    void onStart(Activity activity);

    /**
     * Called by the Activity base class from the {@link Activity#onResume} method.
     *
     * @param activity the activity
     */
    void onResume(Activity activity);

    /**
     * Called by the Activity base class from the {@link Activity#onPause} method.
     *
     * @param activity the activity
     */
    void onPause(Activity activity);

    /**
     * Called by the Activity base class from the {@link Activity#onStop} method.
     *
     * @param activity the activity
     */
    void onStop(Activity activity);

    /**
     * Called by the Activity base class from the {@link Activity#onDestroy} method.
     *
     * @param activity the activity
     */
    void onDestroy(Activity activity);
}
