package com.facebook.cache.commom;

import com.facebook.cache.commom.impl.MultiCacheKey;
import com.facebook.commom.util.SecureHashUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/3/11 0011.
 */
public final class CacheKeyUtil {

    /**
     * 从MultiCacheKey中获取一系列resourceId，或者从CacheKey中获取单个resourceId
     * Get a list of possible resourceIds from MultiCacheKey or get single resourceId from CacheKey.
     */
    public static List<String> getResourceIds(final CacheKey key) {
        try {
            final List<String> ids;
            if (key instanceof MultiCacheKey) {
                List<CacheKey> keys = ((MultiCacheKey) key).getCacheKeys();
                ids = new ArrayList<>(keys.size());
                for (int i = 0; i < keys.size(); i++) {
                    ids.add(secureHashKey(keys.get(i)));
                }
            } else {
                ids = new ArrayList<>(1);
                ids.add(secureHashKey(key));
            }
            return ids;
        } catch (UnsupportedEncodingException e) {
            // This should never happen. All VMs support UTF-8
            throw new RuntimeException(e);
        }
    }

    /**
     * 从MultiCacheKey中获取第一个resourceId或者，从CacheKey获取单个resourceId
     * Get the resourceId from the first key in MultiCacheKey or get single resourceId from CacheKey.
     */
    public static String getFirstResourceId(final CacheKey key) {
        try {
            if (key instanceof MultiCacheKey) {
                List<CacheKey> keys = ((MultiCacheKey) key).getCacheKeys();
                return secureHashKey(keys.get(0));
            } else {
                return secureHashKey(key);
            }
        } catch (UnsupportedEncodingException e) {
            // This should never happen. All VMs support UTF-8
            throw new RuntimeException(e);
        }
    }

    private static String secureHashKey(final CacheKey key) throws UnsupportedEncodingException {
        return SecureHashUtil.makeSHA1HashBase64(key.getUriString().getBytes("UTF-8"));
    }
}
