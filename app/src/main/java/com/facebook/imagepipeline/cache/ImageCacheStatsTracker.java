package com.facebook.imagepipeline.cache;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import com.facebook.cache.commom.CacheKey;
import com.facebook.imagepipeline.cache.impl.CountingMemoryCache;
import com.facebook.imagepipeline.core.impl.ImagePipelineConfig;

/**
 * 图像缓存的数据跟踪接口
 * Interface for stats tracking for the image cache.
 *
 * 一个该接口的实现传给了{@link ImagePipelineConfig}，
 * 这将会将一个缓存事件否通知出去，在你的app中使用这个以保持缓存状态
 * <p>An implementation of this interface, passed to
 * {@link ImagePipelineConfig}, will be notified for each
 * of the following cache events. Use this to keep cache stats for your app.
 */
public interface ImageCacheStatsTracker {

    /**
     * 当解码图片被放入bitmap缓存的时候被调用
     * Called whenever decoded images are put into the bitmap cache. */
    void onBitmapCachePut();

    /**
     * 当bitmap缓存被击中的时候调用
     * Called on a bitmap cache hit. */
    void onBitmapCacheHit(CacheKey cacheKey);

    /**
     * 当bitmap缓存没有击中的时候被调用
     * Called on a bitmap cache miss. */
    void onBitmapCacheMiss();

    /**
     * 当编码图片被放入编码内存缓存的时候被调用
     * Called whenever encoded images are put into the encoded memory cache. */
    void onMemoryCachePut();

    /**
     * 当编码内存缓存被击中的时候被调用
     * Called on an encoded memory cache hit. */
    void onMemoryCacheHit(CacheKey cacheKey);

    /**
     * 当编码内存缓存没有被击中的时候被调用
     * Called on an encoded memory cache hit. */
    void onMemoryCacheMiss();

    /**
     * 当暂存区域被击中的时候被调用
     * Called on an staging area hit.
     *
     *暂存区域储存编码image，它获取图片在每一个写入硬盘缓存的操作之前
     * <p>The staging area stores encoded images. It gets the images before they are written
     * to disk cache.
     */
    void onStagingAreaHit(CacheKey cacheKey);

    /**
     * 当暂存区域没有内击中的时候被调用
     * Called on a staging area miss hit. */
    void onStagingAreaMiss();

    /**
     * 当硬盘缓存被击中的时候调用
     * Called on a disk cache hit. */
    void onDiskCacheHit();

    /**
     * 当硬盘缓存没有被击中的时候调用
     * Called on a disk cache miss. */
    void onDiskCacheMiss();

    /**
     * 当一个异常从硬盘缓存读取操作中被抛出的时候被调用
     * Called if an exception is thrown on a disk cache read. */
    void onDiskCacheGetFail();

    /**
     * 为内存缓存注册跟踪器
     * Registers a bitmap cache with this tracker.
     *
     * <p>Use this method if you need access to the cache itself to compile your stats.
     */
    void registerBitmapMemoryCache(CountingMemoryCache<?, ?> bitmapMemoryCache);

    /**
     * 为编码缓存注册跟踪器
     * Registers an encoded memory cache with this tracker.
     *
     * <p>Use this method if you need access to the cache itself to compile your stats.
     */
    void registerEncodedMemoryCache(CountingMemoryCache<?, ?> encodedMemoryCache);
}
