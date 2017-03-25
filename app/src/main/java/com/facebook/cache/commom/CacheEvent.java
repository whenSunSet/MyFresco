package com.facebook.cache.commom;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

import android.support.annotation.Nullable;

import java.io.IOException;

/**
 * 当某个缓存事件发生的时候(如缓存命中或是老的缓存被删除)，会产生这样一个缓存事件，然后该缓存事件被送入
 * CacheEventListener中，便于使用者监听，有些返回值可能会返回null，是否为null取决于事件的种类。
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
     * 获取一个硬盘缓存的id
     * 这个应该在缓存命中、写成功、读或者写失败出现异常的时候被提供
     */
    @Nullable
    String getResourceId();

    /**
     * 获取文件缓存的byte大小
     * 这个应该在缓存被写成功和缓存被删除的时候被提供
     */
    long getItemSize();

    /**
     * 获取该硬盘缓存使用的空间大小，byte为单位。
     * 这个应该在缓存被写成功或者缓存被删除的时候被提供
     */
    long getCacheSize();

    /**
     * 获取当前的硬盘缓存的空间限制，byte为单位
     * 这个应该在删除缓存的时候被提供，因为一旦要删除缓存，就是因为要调整硬盘缓存的总大小了
     */
    long getCacheLimit();

    /**
     * 当触发了一个读或者写的异常的时候，就返回这个异常
     */
    @Nullable
    IOException getException();

    /**
     * 获取一个文件缓存被删除的原因
     */
    @Nullable
    CacheEventListener.EvictionReason getEvictionReason();
}
