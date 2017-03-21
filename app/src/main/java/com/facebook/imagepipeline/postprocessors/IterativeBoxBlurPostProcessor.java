package com.facebook.imagepipeline.postprocessors;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import android.graphics.Bitmap;

import com.facebook.cache.commom.CacheKey;
import com.facebook.cache.commom.impl.SimpleCacheKey;
import com.facebook.commom.internal.Preconditions;
import com.facebook.imagepipeline.nativecode.NativeBlurFilter;
import com.facebook.imagepipeline.request.impl.BasePostprocessor;

import java.util.Locale;

import javax.annotation.Nullable;

/**
 * 一个快速以及高效内存的迭代模糊处理器,更多信息可以看{@link NativeBlurFilter#iterativeBoxBlur(Bitmap, int, int)}
 * A fast and memory-efficient post processor performing an iterative box blur.  For details see
 * {@link NativeBlurFilter#iterativeBoxBlur(Bitmap, int, int)}.
 */
public class IterativeBoxBlurPostProcessor extends BasePostprocessor {

    private static final int DEFAULT_ITERATIONS = 3;

    private final int mIterations;
    private final int mBlurRadius;

    private CacheKey mCacheKey;

    public IterativeBoxBlurPostProcessor(int blurRadius) {
        this(DEFAULT_ITERATIONS, blurRadius);
    }

    public IterativeBoxBlurPostProcessor(int iterations, int blurRadius) {
        Preconditions.checkArgument(iterations > 0);
        Preconditions.checkArgument(blurRadius > 0);
        mIterations = iterations;
        mBlurRadius = blurRadius;
    }

    @Override
    public void process(Bitmap bitmap) {
        NativeBlurFilter.iterativeBoxBlur(bitmap, mIterations, mBlurRadius);
    }

    @Nullable
    @Override
    public CacheKey getPostprocessorCacheKey() {
        if (mCacheKey == null) {
            final String key = String.format((Locale) null, "i%dr%d", mIterations, mBlurRadius);
            mCacheKey = new SimpleCacheKey(key);
        }
        return mCacheKey;
    }
}
