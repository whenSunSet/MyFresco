package com.facebook.imagepipeline.cache;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import android.support.annotation.Nullable;

import com.android.internal.util.Predicate;
import com.facebook.commom.references.CloseableReference;

/**
 * image pipeline的内存缓存接口
 * Interface for the image pipeline memory cache.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface MemoryCache<K, V> {

    /**
     * 缓存被给予的键和值
     * Caches the the given key-value pair.
     *
     *
     * <p> The cache returns a new copy of the provided reference which should be used instead of the
     * original one. The client should close the returned reference when it is not required anymore.
     *
     * <p> If the cache failed to cache the given value, then the null reference is returned.
     *
     * @param key
     * @param value
     * @return a new reference to be used, or null if the caching failed
     */
    @Nullable
    CloseableReference<V> cache(K key, CloseableReference<V> value);

    /**
     * Gets the item with the given key, or null if there is no such item.
     *
     * @param key
     * @return a reference to the cached value, or null if the item was not found
     */
    @Nullable
    CloseableReference<V> get(K key);

    /**
     * Removes all the items from the cache whose keys match the specified predicate.
     *
     * @param predicate returns true if an item with the given key should be removed
     * @return number of the items removed from the cache
     */
    public int removeAll(Predicate<K> predicate);

    /**
     * Find if any of the items from the cache whose keys match the specified predicate.
     *
     * @param predicate returns true if an item with the given key matches
     * @return true if the predicate was found in the cache, false otherwise
     */
    public boolean contains(Predicate<K> predicate);
}
