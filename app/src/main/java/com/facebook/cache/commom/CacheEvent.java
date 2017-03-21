package com.facebook.cache.commom;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

import android.support.annotation.Nullable;

import java.io.IOException;

/**
 * 提供一个单独的缓存事件的细节的接口
 * 所有的返回值可能为null，是否为null取决于事件的种类。当预期的返回值是可用的时候去文档中查看每个方法
 */
public interface CacheEvent {

    /**
     * 获取与这本事件有关的cache key
     *
     * 这个应该被所有的事件所提供，除了已经被驱逐的缓存
     */
    @Nullable
    CacheKey getCacheKey();

    /**
     * 从一个缓存条目中获取资源ID
     *
     * 这个应该在缓存命中、写成功、读或者写失败出现异常的时候被提供
     *
     * 它也可能在缓存没有命中的事件中被提供，如果一个ID去缓存的index中找，但是那个ID没有被在储存中找到
     */
    @Nullable
    String getResourceId();

    /**
     * 获取新资源的byte数
     *
     * 这个应该在写成功和缓存被驱逐的时候被提供
     */
    long getItemSize();

    /**
     * 获取所有储存资源的总byte数
     *
     * 这个应该在写成功或者被驱逐的时候被提供
     */
    long getCacheSize();

    /**
     * 获取当前的缓存大小限制byte数值
     *
     * 这个应该在驱逐缓存事件时被提供当驱逐是由于要调整限制的大小
     */
    long getCacheLimit();

    /**
     * 获取一个异常当触发了一个读或者写的异常
     */
    @Nullable
    IOException getException();

    /**
     * 获取一个item被驱逐的原因在驱逐事件之中
     */
    @Nullable
    CacheEventListener.EvictionReason getEvictionReason();
}
