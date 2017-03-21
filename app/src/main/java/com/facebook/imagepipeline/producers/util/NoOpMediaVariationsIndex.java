package com.facebook.imagepipeline.producers.util;

import com.facebook.cache.commom.CacheKey;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.producers.MediaVariationsIndex;
import com.facebook.imagepipeline.request.impl.MediaVariations;

import java.util.List;

import bolts.Task;

/**
 * Created by Administrator on 2017/3/17 0017.
 */
public class NoOpMediaVariationsIndex implements MediaVariationsIndex {

    @Override
    public Task<List<MediaVariations.Variant>> getCachedVariants(String mediaId) {
        return Task.forResult(null);
    }

    @Override
    public void saveCachedVariant(String mediaId, CacheKey cacheKey, EncodedImage encodedImage) {
        // no-op
    }
}
