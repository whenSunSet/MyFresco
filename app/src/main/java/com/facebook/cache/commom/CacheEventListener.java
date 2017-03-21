package com.facebook.cache.commom;

/**
 * Created by Administrator on 2017/3/8 0008.
 */
public interface CacheEventListener {

    /**
     * 在获取缓存命中的时候被调用
     */
    void onHit(CacheEvent cacheEvent);

    /**
     * 在获取缓存失败的时候被调用，当被给于一个key的时候
     */
    void onMiss(CacheEvent cacheEvent);

    /**
     * 当开启一个在缓存中储存资源的过程时被调用
     */
    void onWriteAttempt(CacheEvent cacheEvent);

    /**
     * 当一个储存资源过程成功的时候被调用
     */
    void onWriteSuccess(CacheEvent cacheEvent);

    /**
     * 当异常读取失败的时候被调用
     */
    void onReadException(CacheEvent cacheEvent);

    /**
     * 当一次写的操作失败的时候被调用
     */
    void onWriteException(CacheEvent cacheEvent);

    /**
     * 当一个缓存被驱逐的时候被调用
     */
    void onEviction(CacheEvent cacheEvent);

    /**
     * 当一个完整的缓存被清除的时候被调用
     */
    void onCleared();

    //一个缓存条目被驱逐的原因
    enum EvictionReason {
        CACHE_FULL,
        CONTENT_STALE,
        USER_FORCED,
        CACHE_MANAGER_TRIMMED
    }
}