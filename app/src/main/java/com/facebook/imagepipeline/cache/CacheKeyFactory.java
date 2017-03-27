package com.facebook.imagepipeline.cache;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import android.net.Uri;
import android.support.annotation.Nullable;

import com.facebook.cache.commom.CacheKey;
import com.facebook.imagepipeline.cache.impl.BitmapMemoryCacheKey;
import com.facebook.imagepipeline.request.impl.ImageRequest;

/**
 * 创建一个cache key的工厂
 */
public interface CacheKeyFactory {

    /**
     * 为bitmap cache创建一个cache key
     * 一般是这个类型{@link BitmapMemoryCacheKey}
     */
    CacheKey getBitmapCacheKey(ImageRequest request, Object callerContext);

    /**
     * 为一个渐进式bitmap cache创建一个cache key
     * 一般是这个类型{@link BitmapMemoryCacheKey}
     * @return {@link CacheKey} for doing post-processed bitmap cache lookups in the pipeline.
     */
    CacheKey getPostprocessedBitmapCacheKey(ImageRequest request, Object callerContext);

    /**
     * 创建一个encoded memory和disk caches的cache key
     * 一般来说是这个类型{@linnk SimpleCacheKey}
     * Creates a key to be used in the encoded memory and disk caches.
     *
     * 对于相同的请求需要返回相同的值
     * <p>Implementations must return consistent values for the same request or else caches will not
     * work efficiently.
     *
     * @param request the image request to be cached or queried from cache
     * @param callerContext included for optional debugging or logging purposes only
     * @return {@link CacheKey} for doing encoded image lookups in the pipeline.
     */
    CacheKey getEncodedCacheKey(ImageRequest request, @Nullable Object callerContext);

    /**
     * 创建一个encoded memory和disk caches的cache key
     * 一般来说是这个类型{@linnk SimpleCacheKey}
     * Creates a key to be used in the encoded memory and disk caches.
     *
     * 这个方法和上面一个方法不同，该方法指定了一个特别的URI，这个URI与ImageRequest中的URI不同
     * (例如使用MediaVariations的时候)。所以你不应该在ImageRequest中传入URI。因为这里实际使用的是
     * 传入的URI作为{@linnk SimpleCacheKey}中的String id。
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
