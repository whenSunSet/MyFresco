package com.facebook.imagepipeline.cache.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */
/**
 * memory cache的配置
 * Configuration for a memory cache.
 */
public class MemoryCacheParams {

    public final int maxCacheSize;
    public final int maxCacheEntries;
    public final int maxEvictionQueueSize;
    public final int maxEvictionQueueEntries;
    public final int maxCacheEntrySize;

    /**
     * Pass arguments to control the cache's behavior in the constructor.
     *
     * @param maxCacheSize The maximum size of the cache, in bytes.
     * @param maxCacheEntries The maximum number of items that can live in the cache.
     * @param maxEvictionQueueSize The eviction queue is an area of memory that stores items ready
     *                             for eviction but have not yet been deleted. This is the maximum
     *                             size of that queue in bytes.
     * @param maxEvictionQueueEntries The maximum number of entries in the eviction queue.
     * @param maxCacheEntrySize The maximum size of a single cache entry.
     */
    public MemoryCacheParams(
            int maxCacheSize,
            int maxCacheEntries,
            int maxEvictionQueueSize,
            int maxEvictionQueueEntries,
            int maxCacheEntrySize) {
        this.maxCacheSize = maxCacheSize;
        this.maxCacheEntries = maxCacheEntries;
        this.maxEvictionQueueSize = maxEvictionQueueSize;
        this.maxEvictionQueueEntries = maxEvictionQueueEntries;
        this.maxCacheEntrySize = maxCacheEntrySize;
    }
}
