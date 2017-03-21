package com.facebook.imagepipeline.request;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import android.graphics.Bitmap;
import android.support.annotation.Nullable;

import com.facebook.cache.commom.CacheKey;
import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;

/**
 * 使用一个实例来执行被用户post的对bitmap进行的操作
 * Use an instance of this class to perform post-process operations on a bitmap.
 */
public interface Postprocessor {

    /**
     * 在完成其他步骤之后被pipeline调用
     * Called by the pipeline after completing other steps.
     *
     * @param sourceBitmap The source bitmap.
     * @param bitmapFactory 创建最终的bitmap的工厂
     *
     * 后处理程序不能修改原始的bitmap,因为该bitmap可能是由其他客户端共享
     * 安全的实现必须创建一个新的位图要修改并返回一个引用，使用被提供的bitmapFactory
     * <p> The Postprocessor must not modify the source bitmap as it may be shared by the other
     * clients. The implementation must create a new bitmap that is safe to be modified and return a
     * reference to it. To create a bitmap, use the provided <code>bitmapFactory</code>.
     */
    CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory);

    /**
     * 返回postprocessor的name
     * Returns the name of this postprocessor.
     *
     * <p>Used for logging and analytics.
     */
    String getName();

    /**
     * 实现这个方法是为了缓存postprocessor生成的bitmap
     * Implement this method in order to cache the result of a postprocessor in the bitmap cache
     * along with the unmodified image.
     * 当读取内存缓存，只有这个缓存的key的和request的一致，才会命中。
     * <p>When reading from memory cache, there will be a hit only if the cache's value for this key
     * matches that of the request.
     * 每一种postprocessor只允许一个实体在缓存中，当向内存缓存中写的时候，
     * <p>Each postprocessor class is only allowed one entry in the cache. When <i>writing</i> to
     * memory cache, this key is not considered and any image for this request with the same
     * postprocessor class will be overwritten.
     * @return The CacheKey to use for the result of this postprocessor
     */
    @Nullable
    CacheKey getPostprocessorCacheKey();
}
