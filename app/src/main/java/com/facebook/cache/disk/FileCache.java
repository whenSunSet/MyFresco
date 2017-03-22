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
 * 硬盘缓存的管理类应该应该实现这个接口
 * Interface that caches based on disk should implement.
 */
public interface FileCache extends DiskTrimmable {

    /**
     * 判断此种缓存是否可用
     * Tells if this cache is enabled. It's important for some caches that can be disabled
     * without further notice (like in removable/unmountable storage). Anyway a disabled
     * cache should just ignore calls, not fail.
     * @return true if this cache is usable, false otherwise.
     */
    boolean isEnabled();

    /**
     * 通过key返回binary resource
     * Returns the binary resource cached with key.
     */
    BinaryResource getResource(CacheKey key);

    /**
     *
     * 如果这个key存在于内存中那么就返回true
     * Returns true if the key is in the in-memory key index.
     * 注意：不能保证是正确的，因为储存key的容器并不是实时更新的，
     * 可能已经插入了一个新的文件缓存，但是key容器并没有更新。
     * Not guaranteed to be correct. The cache may yet have this key even if this returns false.
     * But if it returns true, it definitely has it.
     *
     * Avoids a disk read.
     */
    boolean hasKeySync(CacheKey key);

    boolean hasKey(CacheKey key);
    boolean probe(CacheKey key);

    /**
     * 将一个文件和key插入到硬盘缓存中，这里WriterCallback提供数据插入操作
     * Inserts resource into file with key
     * @param key cache key
     * @param writer Callback that writes to an output stream
     * @return a sequence of bytes
     * @throws IOException
     */
    BinaryResource insert(CacheKey key, WriterCallback writer) throws IOException;

    /**
     * 将一个key对应的硬盘缓存删除
     * Removes a resource by key from cache.
     * @param key cache key
     */
    void remove(CacheKey key);

    /**
     * 获取硬盘缓存的大小
     * @return the in-use size of the cache
     */
    long getSize();

    /**
     * 获取硬盘缓存的条目数量
     * @return the count of pictures in the cache
     */
    long getCount();

    /**
     * 删除老的缓存文件
     * Deletes old cache files.
     * @param cacheExpirationMs files older than this will be deleted.
     * @return the age in ms of the oldest file remaining in the cache.
     */
    long clearOldEntries(long cacheExpirationMs);

    void clearAll();

    DiskStorage.DiskDumpInfo getDumpInfo() throws IOException;
}
