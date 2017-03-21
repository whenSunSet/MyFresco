package com.facebook.imagepipeline.core;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.cache.disk.DiskStorage;

/**
 * 代表一个使用在ImagePipeline中的DiskStorage工厂
 * 被使用在ImagePipelineConfig/Factory中
 * Represents a factory for the DiskStorage to use in the ImagePipeline.
 * Used by ImagePipelineConfig/Factory
 */
public interface DiskStorageFactory {
    DiskStorage get(DiskCacheConfig diskCacheConfig);
}
