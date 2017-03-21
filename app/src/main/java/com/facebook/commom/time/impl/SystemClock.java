package com.facebook.commom.time.impl;

import com.facebook.commom.time.Clock;

/**
 * Created by Administrator on 2017/3/11 0011.
 */
public class SystemClock implements Clock {

    private static final SystemClock INSTANCE = new SystemClock();

    private SystemClock() {
    }

    public static SystemClock get() {
        return INSTANCE;
    }

    @Override
    public long now() {
        return System.currentTimeMillis();
    }
}
