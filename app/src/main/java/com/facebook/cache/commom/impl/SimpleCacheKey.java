package com.facebook.cache.commom.impl;

/**
 * Created by Administrator on 2017/3/11 0011.
 */

import android.net.Uri;

import com.facebook.cache.commom.CacheKey;
import com.facebook.commom.internal.Preconditions;

/**
 * {@link CacheKey}的实现者，这个只是简单的包装了{@link String}
 * {@link CacheKey} implementation that is a simple wrapper around a {@link String} object.
 *
 * 使用这个CacheKey需要构建一个唯一的string，并且明确的表示缓存资源
 * <p>Users of CacheKey should construct it by providing a unique string that unambiguously
 * identifies the cached resource.
 */
public class SimpleCacheKey implements CacheKey {
    final String mKey;

    public SimpleCacheKey(final String key) {
        mKey = Preconditions.checkNotNull(key);
    }

    @Override
    public String toString() {
        return mKey;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof SimpleCacheKey) {
            final SimpleCacheKey otherKey = (SimpleCacheKey) o;
            return mKey.equals(otherKey.mKey);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mKey.hashCode();
    }

    @Override
    public boolean containsUri(Uri uri) {
        return mKey.contains(uri.toString());
    }

    @Override
    public String getUriString() {
        return mKey;
    }
}
