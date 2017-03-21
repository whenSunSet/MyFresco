package com.facebook.imagepipeline.producers;

import com.facebook.cache.commom.CacheKey;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.request.impl.MediaVariations;

import java.util.List;

import bolts.Task;

/**
 * Created by heshixiyang on 2017/3/10.
 */
public interface MediaVariationsIndex {

    Task<List<MediaVariations.Variant>> getCachedVariants(String mediaId);

    void saveCachedVariant(
            String mediaId,
            CacheKey cacheKey,
            EncodedImage encodedImage);
}
