package com.facebook.imagepipeline.animated.factory;

import com.facebook.imagepipeline.animated.base.AnimatedImage;

/**
 * Created by heshixiyang on 2017/3/10.
 */
public interface AnimatedImageDecoder {

    /**
     * Factory method to create the AnimatedImage from the
     * @param nativePtr The native pointer
     * @param sizeInBytes The size in byte to allocate
     * @return The AnimatedImage allocation
     */
    AnimatedImage decode(long nativePtr, int sizeInBytes);
}
