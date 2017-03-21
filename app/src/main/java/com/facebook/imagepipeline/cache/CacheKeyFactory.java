package com.facebook.imagepipeline.cache;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import android.net.Uri;
import android.support.annotation.Nullable;

import com.facebook.cache.commom.CacheKey;
import com.facebook.imagepipeline.request.impl.ImageRequest;

/**
 * 创建一个cache key的工厂
 * Factory methods for creating cache keys for the pipeline.
 */
public interface CacheKeyFactory {

    /**
     * @return {@link CacheKey} for doing bitmap cache lookups in the pipeline.
     */
    CacheKey getBitmapCacheKey(ImageRequest request, Object callerContext);

    /**
     * 返回一个{@link CacheKey}给渐进式bitmap cache，在pipeline中查看
     * @return {@link CacheKey} for doing post-processed bitmap cache lookups in the pipeline.
     */
    CacheKey getPostprocessedBitmapCacheKey(ImageRequest request, Object callerContext);

    /**
     * 创建一个key在编码内存缓存和硬盘缓存中使用
     * Creates a key to be used in the encoded memory and disk caches.
     *
     * 实现的方法需要返回同样的值对于相同的请求或者缓存不会影响工作效率
     * <p>Implementations must return consistent values for the same request or else caches will not
     * work efficiently.
     *
     * @param request the image request to be cached or queried from cache
     * @param callerContext included for optional debugging or logging purposes only
     * @return {@link CacheKey} for doing encoded image lookups in the pipeline.
     */
    CacheKey getEncodedCacheKey(ImageRequest request, @Nullable Object callerContext);

    /**
     * Creates a key to be used in the encoded memory and disk caches.
     *
     *
     * <p>This version of the method receives a specific URI which may differ from the one held by the
     * request (in cases such as when using MediaVariations). You should not consider the URI in the
     * request.
     *
     * <p>Implementations must return consistent values for the same request or else caches will not
     * work efficiently.
     *
     * @param request the image request to be cached or queried from cache
     * @param sourceUri the URI to use for the key, which may override the one held in the request
     * @param callerContext included for optional debugging or logging purposes only
     * @return {@link CacheKey} for doing encoded image lookups in the pipeline.
     */
    CacheKey getEncodedCacheKey(ImageRequest request, Uri sourceUri, @Nullable Object callerContext);
}
