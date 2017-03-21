package com.facebook.imagepipeline.cache.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import android.os.Build;

import com.facebook.commom.logging.FLog;
import com.facebook.commom.memory.MemoryTrimType;

/**
 * 适用于bitmap caches的内存回收策略，在CountingMemoryCache中使用
 * CountingMemoryCache eviction strategy appropriate for bitmap caches.
 * 在不同的android 版本下表现为部同的行为
 * <p>If run on KitKat or below, then this TrimStrategy behaves exactly as
 * NativeMemoryCacheTrimStrategy. If run on Lollipop, then BitmapMemoryCacheTrimStrategy will trim
 * cache in one additional case: when OnCloseToDalvikHeapLimit trim type is received, cache's
 * eviction queue will be trimmed according to OnCloseToDalvikHeapLimit's suggested trim ratio.
 */
public class BitmapMemoryCacheTrimStrategy implements CountingMemoryCache.CacheTrimStrategy {
    private static final String TAG = "BitmapMemoryCacheTrimStrategy";

    @Override
    public double getTrimRatio(MemoryTrimType trimType) {
        switch (trimType) {
            case OnCloseToDalvikHeapLimit:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return MemoryTrimType.OnCloseToDalvikHeapLimit.getSuggestedTrimRatio();
                } else {
                    // On pre-lollipop versions we keep bitmaps on the native heap, so no need to trim here
                    // as it wouldn't help Dalvik heap anyway.
                    return 0;
                }
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
