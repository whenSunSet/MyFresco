package com.facebook.cache.commom;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

import android.net.Uri;

/**
 * 一个强缓存键代替Object
 *
 */
public interface CacheKey {

    /**
     * 这个对于调试有用处
     * This is useful for instrumentation and debugging purposes. */
    String toString();

    /**
     * 这个方法需要被实现，否则cache key会使用引用地址来判断相同
     * This method must be implemented, otherwise the cache keys will be be compared by reference. */
    boolean equals(Object o);

    /**
     * 这个方法需要被实现，其被用在{@link #equals}方法中
     * This method must be implemented with accordance to the {@link #equals} method. */
    int hashCode();

    /**
     * 返回true，如果这个key是使用{@link Uri}构建的
     * Returns true if this key was constructed from this {@link Uri}.
     *
     * Used for cases like deleting all keys for a given uri.
     */
    boolean containsUri(Uri uri);

    /**
     * 返回一个字符串表示的URI的核心缓存键。在包含多个*键的情况下,返回第一个
     * Returns a string representation of the URI at the heart of the cache key. In cases of multiple
     * keys being contained, the first is returned.
     */
    String getUriString();
}
