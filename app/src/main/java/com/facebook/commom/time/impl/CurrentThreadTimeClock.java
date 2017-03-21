package com.facebook.commom.time.impl;

/**
 * Created by Administrator on 2017/3/13 0013.
 */

import com.facebook.commom.time.Clock;

/**
 * 返回当前时间
 * A clock that returns milliseconds running in the current thread.
 * See {@link android.os.SystemClock}
 */
public class CurrentThreadTimeClock implements Clock {

    public CurrentThreadTimeClock() {}

    @Override
    public long now() {
        return android.os.SystemClock.currentThreadTimeMillis();
    }
}
