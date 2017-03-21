package com.facebook.commom.time;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import com.facebook.commom.internal.DoNotStrip;

/**
 * 一个保证不会落后的时钟
 * A clock that is guaranteed not to go backward.
 */
public interface MonotonicClock {

    /**
     * 生成一个时间戳，从这里返回的值只能和另一个同样从这里返回的值进行比较
     * 对于在这个过程之外的值并不需要写入磁盘
     * Produce a timestamp.  Values returned from this method may only be compared to other values
     * returned from this clock in this process.  They have no meaning outside of this process
     * and should not be written to disk.
     *
     * 两个时间戳的间隔
     * The difference between two timestamps is an interval, in milliseconds.
     *
     * @return A timestamp for the current time, in ms.
     */
    @DoNotStrip
    long now();
}
