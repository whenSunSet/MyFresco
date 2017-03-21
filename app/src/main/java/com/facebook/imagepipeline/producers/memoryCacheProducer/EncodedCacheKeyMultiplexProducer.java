package com.facebook.imagepipeline.producers.memoryCacheProducer;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import android.util.Pair;

import com.facebook.cache.commom.CacheKey;
import com.facebook.imagepipeline.cache.CacheKeyFactory;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.facebook.imagepipeline.producers.base.MultiplexProducer;
import com.facebook.imagepipeline.request.impl.ImageRequest;

/**
 * Multiplex producer that uses the encoded cache key to combine requests.
 */
public class EncodedCacheKeyMultiplexProducer extends
        MultiplexProducer<Pair<CacheKey, ImageRequest.RequestLevel>, EncodedImage> {

    private final CacheKeyFactory mCacheKeyFactory;

    public EncodedCacheKeyMultiplexProducer(
            CacheKeyFactory cacheKeyFactory,
            Producer inputProducer) {
        super(inputProducer);
        mCacheKeyFactory = cacheKeyFactory;
    }

    protected Pair<CacheKey, ImageRequest.RequestLevel> getKey(ProducerContext producerContext) {
        return Pair.create(
                mCacheKeyFactory.getEncodedCacheKey(
                        producerContext.getImageRequest(),
                        producerContext.getCallerContext()),
                producerContext.getLowestPermittedRequestLevel());
    }

    public EncodedImage cloneOrNull(EncodedImage encodedImage) {
        return EncodedImage.cloneOrNull(encodedImage);
    }
}
