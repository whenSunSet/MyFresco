package com.facebook.imagepipeline.cache;

/**
 * Created by heshixiyang on 2017/3/10.
 */
/**
 * Interface used to get the information about the values.
 */
public interface ValueDescriptor<V> {

    /** Returns the size in bytes of the given value. */
    int getSizeInBytes(V value);
}
