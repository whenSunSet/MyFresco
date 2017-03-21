package com.facebook.imagepipeline.cache.impl;

import com.facebook.cache.commom.CacheKey;
import com.facebook.commom.internal.Supplier;
import com.facebook.commom.memory.MemoryTrimmableRegistry;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.cache.ValueDescriptor;
import com.facebook.imagepipeline.image.impl.CloseableImage;

/**
 * Created by heshixiyang on 2017/3/16.
 */
//BitmapCountingMemoryCache的工厂
public class BitmapCountingMemoryCacheFactory {
    public static CountingMemoryCache<CacheKey, CloseableImage> get(
            Supplier<MemoryCacheParams> bitmapMemoryCacheParamsSupplier,
            MemoryTrimmableRegistry memoryTrimmableRegistry,
            PlatformBitmapFactory platformBitmapFactory,
            boolean isExternalCreatedBitmapLogEnabled) {

        ValueDescriptor<CloseableImage> valueDescriptor =
                new ValueDescriptor<CloseableImage>() {
                    @Override
                    public int getSizeInBytes(CloseableImage value) {
                        return value.getSizeInBytes();
                    }
                };

        CountingMemoryCache.CacheTrimStrategy trimStrategy = new BitmapMemoryCacheTrimStrategy();

        CountingMemoryCache<CacheKey, CloseableImage> countingCache =
                new CountingMemoryCache<>(
                        valueDescriptor,
                        trimStrategy,
                        bitmapMemoryCacheParamsSupplier,
                        platformBitmapFactory,
                        isExternalCreatedBitmapLogEnabled);

        memoryTrimmableRegistry.registerMemoryTrimmable(countingCache);

        return countingCache;
    }
}

