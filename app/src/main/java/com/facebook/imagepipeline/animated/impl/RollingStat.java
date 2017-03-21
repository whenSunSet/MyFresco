package com.facebook.imagepipeline.animated.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import android.os.SystemClock;

/**
 * 简单的类来跟踪一个滚动的统计效率。
 * Simple class to track a rolling stat efficiently.
 */
public class RollingStat {

    private static final int WINDOWS = 60;

    private final short[] mStat;

    public RollingStat() {
        mStat = new short[WINDOWS];
    }

    public void incrementStats(int toAdd) {
        long nowMs = SystemClock.uptimeMillis();
        long nowSeconds = nowMs / 1000;
        int statsIndex = (int) (nowSeconds % WINDOWS);
        int marker = (int) ((nowSeconds / WINDOWS) & 0xff);

        short bucketData = mStat[statsIndex];
        int prevCount = bucketData & 0xff;
        int prevMarker = (bucketData >> 8) & 0xff;

        int newCount;
        if (marker != prevMarker) {
            newCount = toAdd;
        } else {
            newCount = prevCount + toAdd;
        }

        int newData = (marker << 8) | newCount;
        mStat[statsIndex] = (short) newData;
    }

    public int getSum(int previousSeconds) {
        long nowMs = SystemClock.uptimeMillis();
        long nowSeconds = nowMs / 1000;
        int statsIndexStart = (int) ((nowSeconds - previousSeconds) % WINDOWS);
        int currentMarker = (int) ((nowSeconds / WINDOWS) & 0xff);

        int sum = 0;
        for (int i = 0; i < previousSeconds; i++) {
            short bucketData = mStat[(statsIndexStart + i) % WINDOWS];
            int count = bucketData & 0xff;
            int marker = (bucketData >> 8) & 0xff;
            if (marker == currentMarker) {
                sum += count;
            }
        }
        return sum;
    }
}
