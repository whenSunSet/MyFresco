package com.facebook.commom.time.impl;

/**
 * Created by Administrator on 2017/3/13 0013.
 */

import com.facebook.commom.internal.DoNotStrip;
import com.facebook.commom.time.MonotonicClock;

/**
 * 一个时钟,它返回启动以来的毫秒数，它保证了每一次调用now()返回的时间都不会比上一次调用的时间更少。
 * 这种情况不管系统时间改变,时区的改变,夏令时的变化等。
 * A clock that returns number of milliseconds since boot. It guarantees that every next
 * call to now() will return a value that is not less that was returned from previous call to now().
 * This happens regardless system time changes, time zone changes, daylight saving changes etc.
 */
@DoNotStrip
public class RealtimeSinceBootClock implements MonotonicClock {
    private static final RealtimeSinceBootClock INSTANCE = new RealtimeSinceBootClock();

    private RealtimeSinceBootClock() {
    }

    /**
     * Returns a singleton instance of this clock.
     * @return singleton instance
     */
    @DoNotStrip
    public static RealtimeSinceBootClock get() {
        return INSTANCE;
    }

    @Override
    public long now() {
        // Guaranteed to be monotonic according to documentation.
        return android.os.SystemClock.elapsedRealtime/*sic*/();
    }
}
