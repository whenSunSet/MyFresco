package com.facebook.cache.commom.impl;

/**
 * Created by Administrator on 2017/3/11 0011.
 */

import android.net.Uri;

import com.facebook.cache.commom.CacheKey;
import com.facebook.commom.internal.Preconditions;

import java.util.List;

/**
 * 一个包装了多个cache key的 cache key
 * A cache key that wraps multiple cache keys.
 *
 * 注意：{@code equals} 和 {@code hashcode}有一种方式实现：如果两个MultiCacheKeys相等
 * 那么只有其中的cache key的列表相同才行。
 * 后面的翻译不明白
 *
 * Note: {@code equals} and {@code hashcode} are implemented in a way that two MultiCacheKeys are
 * equal if and only if the underlying list of cache keys is equal. That implies AllOf semantics.
 * Unfortunately, it is not possible to implement AnyOf semantics for {@code equals} because the
 * transitivity requirement wouldn't be satisfied. I.e. we would have:
 * {A} = {A, B}, {A, B} = {B}, but {A} != {B}.
 *
 *
 * It is fine to use this key with AnyOf semantics, but one should be aware of {@code equals} and
 * {@code hashcode} behavior, and should implement AnyOf logic manually.
 */
public class MultiCacheKey implements CacheKey {

    final List<CacheKey> mCacheKeys;

    public MultiCacheKey(List<CacheKey> cacheKeys) {
        mCacheKeys = Preconditions.checkNotNull(cacheKeys);
    }

    public List<CacheKey> getCacheKeys() {
        return mCacheKeys;
    }

    @Override
    public String toString() {
        return "MultiCacheKey:" + mCacheKeys.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof MultiCacheKey) {
            final MultiCacheKey otherKey = (MultiCacheKey) o;
            return mCacheKeys.equals(otherKey.mCacheKeys);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mCacheKeys.hashCode();
    }

    @Override
    public boolean containsUri(Uri uri) {
        for (int i = 0; i < mCacheKeys.size(); i++) {
            if (mCacheKeys.get(i).containsUri(uri)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getUriString() {
        return mCacheKeys.get(0).getUriString();
    }
}
