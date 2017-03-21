package com.facebook.cache.commom.impl;

/**
 * Created by Administrator on 2017/3/11 0011.
 */

import javax.annotation.Nullable;

/**
 * 扩展{@link SimpleCacheKey}，其中添加了caller context，用于debug
 * Extension of {@link SimpleCacheKey} which adds the ability to hold a caller context. This can be
 * of use for debugging and has no bearing on equality.
 */
public class DebuggingCacheKey extends SimpleCacheKey {

    private final Object mCallerContext;

    public DebuggingCacheKey(String key, @Nullable Object callerContext) {
        super(key);
        mCallerContext = callerContext;
    }

    @Nullable
    public Object getCallerContext() {
        return mCallerContext;
    }
}
