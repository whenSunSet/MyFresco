package com.facebook.cache.commom.impl;

/**
 * Created by Administrator on 2017/3/11 0011.
 */

import com.facebook.cache.commom.CacheErrorLogger;

import javax.annotation.Nullable;

/**
 * 不做任何事情的CacheErrorLogger
 * An implementation of {@link CacheErrorLogger} that doesn't do anything.
 */
public class NoOpCacheErrorLogger implements CacheErrorLogger {
    private static NoOpCacheErrorLogger sInstance = null;

    private NoOpCacheErrorLogger() {
    }

    public static synchronized NoOpCacheErrorLogger getInstance() {
        if (sInstance == null) {
            sInstance = new NoOpCacheErrorLogger();
        }
        return sInstance;
    }

    /**
     *
     * Log an error of the specified category.
     * @param category Error category
     * @param clazz Class reporting the error
     * @param message An optional error message
     * @param throwable An optional exception
     */
    @Override
    public void logError(
            CacheErrorCategory category,
            Class<?> clazz,
            String message,
            @Nullable Throwable throwable) {
    }
}
