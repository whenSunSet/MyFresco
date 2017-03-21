package com.facebook.imagepipeline.cache;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.request.impl.ImageRequest;

import java.util.concurrent.atomic.AtomicBoolean;

import bolts.Task;

/**
 * 提供如果从硬盘缓存中读或者写image的策略
 * Policy on how to read from and write to the image disk cache.
 *
 * 这是很有用的，其将如何决定使用哪个硬盘缓存和是使用main还是small 与 producers分离开来
 * 并且也精确的查找到缓存的key
 * <p> This is useful to separate from the producers how to decide which disk cache(s) to use,
 * whether the main or small cache, and also which precise cache key(s) to look for.
 */
public interface DiskCachePolicy {

    /**
     * 创建和开始进行一个硬盘的读操作的任务，无论使用哪一种缓存和key都支持这个策略
     * Creates and starts the task to carry out a disk cache read, using whichever caches and keys are
     * appropriate for this policy.
     */
    Task<EncodedImage> createAndStartCacheReadTask(
            ImageRequest imageRequest,
            Object callerContext,
            AtomicBoolean isCancelled);

    /**
     * 创建和开始进行一个硬盘的写操作的任务，无论使用哪一种缓存和key都支持这个策略
     * Writes the new image data to whichever cache and with whichever key is appropriate for this
     * policy.
     */
    void writeToCache(
            EncodedImage newResult,
            ImageRequest imageRequest,
            Object callerContext);
}
