package com.facebook.imagepipeline.core;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.cache.disk.FileCache;

/**
 * 代表一个使用在ImagePipeline中的FileCache工厂
 * 被使用在ImagePipelineConfig/Factory中
 * Represents a factory for the FileCache to use in the ImagePipeline.
 * Used by ImagePipelineConfig/Factory
 */
public interface FileCacheFactory {
    FileCache get(DiskCacheConfig diskCacheConfig);
}
