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
     * 当被给于一个key去获取缓存，但是失败的时候被调用，
     */
    void onMiss(CacheEvent cacheEvent);

    /**
     * 当开启一个写缓存的时候被调用
     */
    void onWriteAttempt(CacheEvent cacheEvent);

    /**
     * 当写一个缓存成功的时候被调用
     */
    void onWriteSuccess(CacheEvent cacheEvent);

    /**
     * 当出现读取异常的时候被调用
     */
    void onReadException(CacheEvent cacheEvent);

    /**
     * 当出现写异常的时候被调用
     */
    void onWriteException(CacheEvent cacheEvent);

    /**
     * 当一个文件缓存被删除的时候被调用
     */
    void onEviction(CacheEvent cacheEvent);

    /**
     * 当所有缓存被清除的时候调用
     */
    void onCleared();

    //一个文件缓存被删除的原因
    enum EvictionReason {
        CACHE_FULL,
        CONTENT_STALE,
        USER_FORCED,
        CACHE_MANAGER_TRIMMED
    }
}