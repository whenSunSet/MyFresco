package com.facebook.imagepipeline.memory;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import com.facebook.commom.memory.MemoryTrimType;
import com.facebook.commom.memory.MemoryTrimmable;
import com.facebook.commom.references.ResourceReleaser;

/**
 * 管理一个V类型的可重用的池子，
 * Manages a pool of reusable values of type V. The sizes of the values are described by the type S
 * 这个池子主要支持两个操作
 * The pool supports two main operations
 * {@link #get(Object)} - 返回一个大于或者等于传入值的池子
 * {@link #get(Object)} - returns a value of size that's the same or larger than the requested size
 * {@link #release(Object)}  - 回收池子
 * {@link #release(Object)}  - releases the value to the pool
 * <p>
 * 此外，这个池子订阅了内存管理，当内存过少的时候会调用{@link MemoryTrimmable#trim(MemoryTrimType)}
 * 来释放池子
 * In addition, the pool subscribes to the memory manager, and responds to low-memory events via
 * calls to {@link MemoryTrimmable#trim(MemoryTrimType)}. Some percent (perhaps all) of the
 * values in the pool are then 'freed'.
 * <p>
 * 已知的实现：BasePool (GenericByteArrayPool, SingleByteArrayPool, BitmapPool)
 * Known implementations: BasePool (GenericByteArrayPool, SingleByteArrayPool, BitmapPool)
 */
public interface Pool<V> extends ResourceReleaser<V>, MemoryTrimmable {

    /**
     * Gets a 'value' of size 'S' (or larger) from the pool, if available.
     * Allocates a new value if necessary.
     * @param size the logical size to allocate
     * @return a new value
     */
    V get(int size);

    /**
     * Releases the given value to the pool.
     * The pool may decide to
     *  - reuse the value (for future {@link #get(int)} operations OR
     *  - 'free' the value
     * @param value the value to release to the pool
     */
    void release(V value);
}
