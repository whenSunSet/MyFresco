package com.facebook.imagepipeline.cache.impl;

import com.facebook.cache.commom.CacheKey;
import com.facebook.commom.internal.Supplier;
import com.facebook.commom.memory.MemoryTrimmableRegistry;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.cache.ValueDescriptor;
import com.facebook.imagepipeline.memory.PooledByteBuffer;

/**
 * Created by heshixiyang on 2017/3/16.
 */
//一个EncodedCountingMemoryCache的工厂
public class EncodedCountingMemoryCacheFactory {

    public static CountingMemoryCache<CacheKey, PooledByteBuffer> get(
            Supplier<MemoryCacheParams> encodedMemoryCacheParamsSupplier,
            MemoryTrimmableRegistry memoryTrimmableRegistry,
            PlatformBitmapFactory platformBitmapFactory) {

        ValueDescriptor<PooledByteBuffer> valueDescriptor =
                new ValueDescriptor<PooledByteBuffer>() {
                    @Override
                    public int getSizeInBytes(PooledByteBuffer value) {
                        return value.size();
                    }
                };

        CountingMemoryCache.CacheTrimStrategy trimStrategy = new NativeMemoryCacheTrimStrategy();

        CountingMemoryCache<CacheKey, PooledByteBuffer> countingCache =
                new CountingMemoryCache<>(
                        valueDescriptor,
                        trimStrategy,
                        encodedMemoryCacheParamsSupplier,
                        platformBitmapFactory,
                        false);

        memoryTrimmableRegistry.registerMemoryTrimmable(countingCache);

        return countingCache;
    }
}

