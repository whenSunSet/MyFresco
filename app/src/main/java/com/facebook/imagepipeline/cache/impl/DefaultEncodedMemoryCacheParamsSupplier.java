package com.facebook.imagepipeline.cache.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import com.facebook.commom.internal.Supplier;
import com.facebook.commom.util.ByteConstants;

/**
 * 一个{@link MemoryCacheParams}的提供者，为了encoded image内存缓存
 * Supplies {@link MemoryCacheParams} for the encoded image memory cache
 */
public class DefaultEncodedMemoryCacheParamsSupplier implements Supplier<MemoryCacheParams> {

    // We want memory cache to be bound only by its memory consumption
    private static final int MAX_CACHE_ENTRIES = Integer.MAX_VALUE;
    private static final int MAX_EVICTION_QUEUE_ENTRIES = MAX_CACHE_ENTRIES;

    @Override
    public MemoryCacheParams get() {
        final int maxCacheSize = getMaxCacheSize();
        final int maxCacheEntrySize = maxCacheSize / 8;
        return new MemoryCacheParams(
                maxCacheSize,
                MAX_CACHE_ENTRIES,
                maxCacheSize,
                MAX_EVICTION_QUEUE_ENTRIES,
                maxCacheEntrySize);
    }

    private int getMaxCacheSize() {
        final int maxMemory = (int) Math.min(Runtime.getRuntime().maxMemory(), Integer.MAX_VALUE);
        if (maxMemory < 16 * ByteConstants.MB) {
            return 1 * ByteConstants.MB;
        } else if (maxMemory < 32 * ByteConstants.MB) {
            return 2 * ByteConstants.MB;
        } else {
            return 4 * ByteConstants.MB;
        }
    }
}

