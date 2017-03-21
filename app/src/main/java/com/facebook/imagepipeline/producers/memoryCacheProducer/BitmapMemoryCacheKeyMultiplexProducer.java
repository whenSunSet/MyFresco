package com.facebook.imagepipeline.producers.memoryCacheProducer;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import android.util.Pair;

import com.facebook.cache.commom.CacheKey;
import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.cache.CacheKeyFactory;
import com.facebook.imagepipeline.image.impl.CloseableImage;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.facebook.imagepipeline.producers.base.MultiplexProducer;
import com.facebook.imagepipeline.request.impl.ImageRequest;

/**
 * 多个相同的请求通过bitmap memory cache key 合并
 * Multiplex producer that uses the bitmap memory cache key to combine requests.
 */
public class BitmapMemoryCacheKeyMultiplexProducer extends
        MultiplexProducer<Pair<CacheKey, ImageRequest.RequestLevel>,
                        CloseableReference<CloseableImage>> {

    private final CacheKeyFactory mCacheKeyFactory;

    public BitmapMemoryCacheKeyMultiplexProducer(
            CacheKeyFactory cacheKeyFactory,
            Producer inputProducer) {
        super(inputProducer);
        mCacheKeyFactory = cacheKeyFactory;
    }

    protected Pair<CacheKey, ImageRequest.RequestLevel> getKey(
            ProducerContext producerContext) {
        return Pair.create(
                mCacheKeyFactory.getBitmapCacheKey(
                        producerContext.getImageRequest(),
                        producerContext.getCallerContext()),
                producerContext.getLowestPermittedRequestLevel());
    }

    public CloseableReference<CloseableImage> cloneOrNull(
            CloseableReference<CloseableImage> closeableImage) {
        return CloseableReference.cloneOrNull(closeableImage);
    }

}
