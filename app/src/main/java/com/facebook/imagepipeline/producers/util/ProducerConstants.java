package com.facebook.imagepipeline.producers.util;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.producers.Producer;

/**
 * Constants to be used various {@link Producer}s for logging purposes in the extra maps for the
 * {@link RequestListener}.
 *
 * The elements are package visible on purpose such that the individual producers create public
 * constants of the ones that they actually use.
 */
public class ProducerConstants {

    public static final String EXTRA_CACHED_VALUE_FOUND = "cached_value_found";

    public static final String EXTRA_BITMAP_SIZE = "bitmapSize";
    public static final String EXTRA_HAS_GOOD_QUALITY = "hasGoodQuality";
    public static final String EXTRA_IMAGE_TYPE = "imageType";
    public static final String EXTRA_IS_FINAL = "isFinal";
    public static final String EXTRA_IMAGE_FORMAT_NAME = "imageFormat";
    public static final String ENCODED_IMAGE_SIZE = "encodedImageSize";
    public static final String REQUESTED_IMAGE_SIZE = "requestedImageSize";
    public static final String SAMPLE_SIZE = "sampleSize";
}
