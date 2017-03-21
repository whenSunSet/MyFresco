package com.facebook.imagepipeline.cache.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import com.facebook.commom.logging.FLog;
import com.facebook.commom.memory.MemoryTrimType;

/**
 * CountingMemoryCache 允许的缓存驱逐策略，该缓存在堆上
 * CountingMemoryCache eviction strategy appropriate for caches that store resources off the Dalvik
 * heap.
 *
 * <p>In case of OnCloseToDalvikHeapLimit nothing will be done. In case of other trim types
 * eviction queue of the cache will be cleared.
 */
public class NativeMemoryCacheTrimStrategy implements CountingMemoryCache.CacheTrimStrategy {
    private static final String TAG = "NativeMemoryCacheTrimStrategy";

    public NativeMemoryCacheTrimStrategy() {}

    @Override
    public double getTrimRatio(MemoryTrimType trimType) {
        switch (trimType) {
            case OnCloseToDalvikHeapLimit:
                // Resources cached on native heap do not consume Dalvik heap, so no trimming here.
                return 0;
            case OnAppBackgrounded:
            case OnSystemLowMemoryWhileAppInForeground:
            case OnSystemLowMemoryWhileAppInBackground:
                return 1;
            default:
                FLog.wtf(TAG, "unknown trim type: %s", trimType);
                return 0;
        }
    }
}

