package com.facebook.cache.disk;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

import com.facebook.binaryresource.BinaryResource;
import com.facebook.cache.commom.CacheKey;
import com.facebook.cache.commom.WriterCallback;
import com.facebook.commom.disk.DiskTrimmable;

import java.io.IOException;

/**
 * 基于硬盘的缓存应该实现这个接口
 * Interface that caches based on disk should implement.
 */
public interface FileCache extends DiskTrimmable {

    /**
     * 这是一个重要的功能对于一些
     * Tells if this cache is enabled. It's important for some caches that can be disabled
     * without further notice (like in removable/unmountable storage). Anyway a disabled
     * cache should just ignore calls, not fail.
     * @return true if this cache is usable, false otherwise.
     */
    boolean isEnabled();

    /**
     * 通过kye返回binary resource
     * Returns the binary resource cached with key.
     */
    BinaryResource getResource(CacheKey key);

    /**
     * 如果key在in-memory中返回true
     * Returns true if the key is in the in-memory key index.
     *
     * 不能保证是正确的，缓存还可能有key,即使这返回false。但是如果它返回true,它肯定有key
     * Not guaranteed to be correct. The cache may yet have this key even if this returns false.
     * But if it returns true, it definitely has it.
     *
     * Avoids a disk read.
     */
    boolean hasKeySync(CacheKey key);

    boolean hasKey(CacheKey key);
    boolean probe(CacheKey key);

    /**
     * Inserts resource into file with key
     * @param key cache key
     * @param writer Callback that writes to an output stream
     * @return a sequence of bytes
     * @throws IOException
     */
    BinaryResource insert(CacheKey key, WriterCallback writer) throws IOException;

    /**
     * Removes a resource by key from cache.
     * @param key cache key
     */
    void remove(CacheKey key);

    /**
     * @return the in-use size of the cache
     */
    long getSize();

    /**
     * @return the count of pictures in the cache
     */
    long getCount();

    /**
     * 删除老的files
     * Deletes old cache files.
     * @param cacheExpirationMs files older than this will be deleted.
     * @return the age in ms of the oldest file remaining in the cache.
     */
    long clearOldEntries(long cacheExpirationMs);
    void clearAll();

    DiskStorage.DiskDumpInfo getDumpInfo() throws IOException;
}
