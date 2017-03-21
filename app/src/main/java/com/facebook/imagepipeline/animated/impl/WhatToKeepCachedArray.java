package com.facebook.imagepipeline.animated.impl;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import com.facebook.imagepipeline.animated.util.AnimatedDrawableUtil;

import javax.annotation.concurrent.NotThreadSafe;

/**
 *一个简单的数据结构来跟踪如何保持缓存
 * A simple data structure for tracking what to keep cached.
 */
@NotThreadSafe
public class WhatToKeepCachedArray {

    private final boolean[] mData;

    public WhatToKeepCachedArray(int size) {
        mData = new boolean[size];
    }

    public boolean get(int index) {
        return mData[index];
    }

    public void setAll(boolean value) {
        for (int i = 0; i < mData.length; i++) {
            mData[i] = value;
        }
    }

    public void removeOutsideRange(int start, int end) {
        for (int i = 0; i < mData.length; i++) {
            if (AnimatedDrawableUtil.isOutsideRange(start, end, i)) {
                mData[i] = false;
            }
        }
    }

    public void set(int index, boolean value) {
        mData[index] = value;
    }
}
