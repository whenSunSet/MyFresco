package com.facebook.imagepipeline.memory.impl;

/**
 * Created by heshixiyang on 2017/3/17.
 */

import android.util.SparseIntArray;

import com.facebook.commom.util.ByteConstants;
import com.facebook.imagepipeline.memory.ByteArrayPool;

/**
 * 提供一个parameters 给{@link ByteArrayPool}
 * Provides pool parameters ({@link PoolParams}) for common {@link ByteArrayPool}
 */
public class DefaultByteArrayPoolParams {
    private static final int DEFAULT_IO_BUFFER_SIZE = 16 * ByteConstants.KB;

    /*
     * There are up to 5 simultaneous IO operations in new pipeline performed by:
     * - 3 image-fetch threads
     * - 2 image-cache threads
     * We should be able to satisfy these requirements without any allocations
     */
    private static final int DEFAULT_BUCKET_SIZE = 5;
    private static final int MAX_SIZE_SOFT_CAP = 5 * DEFAULT_IO_BUFFER_SIZE;

    /**
     * We don't need hard cap here.
     */
    private static final int MAX_SIZE_HARD_CAP = 1 * ByteConstants.MB;

    /**
     * Get default {@link PoolParams}.
     */
    public static PoolParams get() {
        // This pool supports only one bucket size: DEFAULT_IO_BUFFER_SIZE
        SparseIntArray defaultBuckets = new SparseIntArray();
        defaultBuckets.put(DEFAULT_IO_BUFFER_SIZE, DEFAULT_BUCKET_SIZE);
        return new PoolParams(
                MAX_SIZE_SOFT_CAP,
                MAX_SIZE_HARD_CAP,
                defaultBuckets);
    }
}
