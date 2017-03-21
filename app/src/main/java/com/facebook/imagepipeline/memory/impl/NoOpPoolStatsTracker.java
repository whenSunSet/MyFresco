package com.facebook.imagepipeline.memory.impl;

/**
 * Created by heshixiyang on 2017/3/17.
 */

import com.facebook.imagepipeline.memory.PoolStatsTracker;

/**
 *
 * Empty implementation of PoolStatsTracker that does not perform any tracking.
 */
public class NoOpPoolStatsTracker implements PoolStatsTracker {
    private static NoOpPoolStatsTracker sInstance = null;

    private NoOpPoolStatsTracker() {
    }

    public static synchronized NoOpPoolStatsTracker getInstance() {
        if (sInstance == null) {
            sInstance = new NoOpPoolStatsTracker();
        }
        return sInstance;
    }

    @Override
    public void setBasePool(BasePool basePool) {
    }

    @Override
    public void onValueReuse(int bucketedSize) {
    }

    @Override
    public void onSoftCapReached() {
    }

    @Override
    public void onHardCapReached() {
    }

    @Override
    public void onAlloc(int size) {
    }

    @Override
    public void onFree(int sizeInBytes) {
    }

    @Override
    public void onValueRelease(int sizeInBytes) {
    }
}

