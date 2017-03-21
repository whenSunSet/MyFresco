package com.facebook.commom.activitylistener;

/**
 * Created by Administrator on 2017/3/8 0008.
 */
/**
 * 接口支持动态添加的Activity的听众
 */
public interface ListenableActivity {

    /**
     * Adds ActivityListener to the activity
     *
     * @param listener
     */
    void addActivityListener(ActivityListener listener);

    /**
     * Removes ActivityListener from the activity
     *
     * @param listener
     */
    void removeActivityListener(ActivityListener listener);
}
