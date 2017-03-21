package com.facebook.imagepipeline.animated.impl;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import com.facebook.imagepipeline.animated.base.AnimatedDrawableBackend;
import com.facebook.imagepipeline.animated.base.impl.AnimatedDrawableOptions;
import com.facebook.imagepipeline.animated.impl.impl.AnimatedDrawableCachingBackendImpl;

/**
 * 一个提提供{@link AnimatedDrawableCachingBackendImpl}的类
 * Assisted provider for {@link AnimatedDrawableCachingBackendImpl}.
 */
public interface AnimatedDrawableCachingBackendImplProvider {

    /**
     * Creates a new {@link AnimatedDrawableCachingBackendImpl}.
     *
     * @param animatedDrawableBackend the backend to delegate to
     * @param options the options for the drawable
     * @return a new {@link AnimatedDrawableCachingBackendImpl}
     */
    AnimatedDrawableCachingBackendImpl get(
            AnimatedDrawableBackend animatedDrawableBackend,
            AnimatedDrawableOptions options);
}
