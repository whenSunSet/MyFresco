package com.facebook.imagepipeline.core.impl;

/**
 * Created by heshixiyang on 2017/3/17.
 */

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.cache.disk.DiskStorage;
import com.facebook.cache.disk.impl.DynamicDefaultDiskStorage;
import com.facebook.imagepipeline.core.DiskStorageFactory;

/**
 * Factory for the default implementation of the DiskStorage.
 */
public class DynamicDefaultDiskStorageFactory implements DiskStorageFactory {

    @Override
    public DiskStorage get(DiskCacheConfig diskCacheConfig) {
        return new DynamicDefaultDiskStorage(
                diskCacheConfig.getVersion(),
                diskCacheConfig.getBaseDirectoryPathSupplier(),
                diskCacheConfig.getBaseDirectoryName(),
                diskCacheConfig.getCacheErrorLogger());
    }
}

