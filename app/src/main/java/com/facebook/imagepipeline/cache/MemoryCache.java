package com.facebook.imagepipeline.cache;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import android.support.annotation.Nullable;

import com.android.internal.util.Predicate;
import com.facebook.commom.references.CloseableReference;

/**
 * 内存缓存接口
 * @param <K> the key type
 * @param <V> the value type
 */
public interface MemoryCache<K, V> {

    /**
     * 缓存被给予的键和值
     * Caches the the given key-value pair.
     * 这里应该返回一个传入的CloseableReference的拷贝，用来代替传入的CloseableReference
     * 并且在该CloseableReference没用的时候关闭它
     * <p> The cache returns a new copy of the provided reference which should be used instead of the
     * original one. The client should close the returned reference when it is not required anymore.
     * 如果缓存失败那么就返回null
     * <p> If the cache failed to cache the given value, then the null reference is returned.
     *
     * @param key
     * @param value
     * @return a new reference to be used, or null if the caching failed
     */
    @Nullable
    CloseableReference<V> cache(K key, CloseableReference<V> value);

    /**
     * 返回传入key对应的内存缓存条目，如果没有就返回null
     * Gets the item with the given key, or null if there is no such item.
     *
     * @param key
     * @return a reference to the cached value, or null if the item was not found
     */
    @Nullable
    CloseableReference<V> get(K key);

    /**
     * 删除所有能够和传入Predicate匹配的cache key的缓存
     * Removes all the items from the cache whose keys match the specified predicate.
     *
     * @param predicate returns true if an item with the given key should be removed
     * @return number of the items removed from the cache
     */
    public int removeAll(Predicate<K> predicate);

    /**
     * 查询是否有存在内存缓存的cache key与传入的Predicate匹配
     * Find if any of the items from the cache whose keys match the specified predicate.
     *
     * @param predicate returns true if an item with the given key matches
     * @return true if the predicate was found in the cache, false otherwise
     */
    public boolean contains(Predicate<K> predicate);
}
