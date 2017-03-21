package com.facebook.commom.time;

/**
 * Created by heshixiyang on 2017/3/10.
 */
/**
 * 获取当前时间的接口
 * Interface for getting the current time.
 */
public interface Clock {

    /**
     * 最长时间
     * The maximum time.
     */
    long MAX_TIME = Long.MAX_VALUE;

    /**
     * 获取当前的时间milliseconds
     * Gets the current time in milliseconds.
     *
     * @return the current time in milliseconds.
     */
    long now();
}
