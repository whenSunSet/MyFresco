package com.facebook.cache.commom.impl;

/**
 * Created by Administrator on 2017/3/11 0011.
 */

import com.facebook.cache.commom.CacheEvent;
import com.facebook.cache.commom.CacheEventListener;

/**
 * 不做任何事情的CacheEventListener
 * Implementation of {@link CacheEventListener} that doesn't do anything.
 */
public class NoOpCacheEventListener implements CacheEventListener {
    private static NoOpCacheEventListener sInstance = null;

    private NoOpCacheEventListener() {
    }

    public static synchronized NoOpCacheEventListener getInstance() {
        if (sInstance == null) {
            sInstance = new NoOpCacheEventListener();
        }
        return sInstance;
    }

    @Override
    public void onHit(CacheEvent cacheEvent) {

    }

    @Override
    public void onMiss(CacheEvent cacheEvent) {
    }

    @Override
    public void onWriteAttempt(CacheEvent cacheEvent) {
    }

    @Override
    public void onWriteSuccess(CacheEvent cacheEvent) {
    }

    @Override
    public void onReadException(CacheEvent cacheEvent) {
    }

    @Override
    public void onWriteException(CacheEvent cacheEvent) {
    }

    @Override
    public void onEviction(CacheEvent cacheEvent) {
    }

    @Override
    public void onCleared() {
    }
}
