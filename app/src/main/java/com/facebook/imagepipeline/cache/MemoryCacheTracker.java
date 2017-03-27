package com.facebook.imagepipeline.cache;

/**
 * Created by heshixiyang on 2017/3/9.
 */
//用于让使用者监听内存缓存事件
public interface MemoryCacheTracker<K> {
    void onCacheHit(K cacheKey);
    void onCacheMiss();
    void onCachePut();
}
