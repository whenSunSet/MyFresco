package com.facebook.cache.disk;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

import com.facebook.binaryresource.BinaryResource;
import com.facebook.cache.commom.CacheKey;
import com.facebook.cache.commom.WriterCallback;
import com.facebook.imagepipeline.cache.impl.BufferedDiskCache;
import com.facebook.imagepipeline.image.impl.EncodedImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件缓存的接口，直接对文件缓存进行操作
 * 负责维护状态(数量, 大小, 查看文件是否存在, 可达性)
 * Storage for files in the cache.
 * Responsible for maintaining state (count, size, watch file existence, reachability)
 */
public interface DiskStorage {

    //磁盘转储信息条目
    class DiskDumpInfoEntry {
        public final String path;
        public final String type;
        public final float size;
        public final String firstBits;
        public DiskDumpInfoEntry(String path, String type, float size, String firstBits) {
            this.path = path;
            this.type = type;
            this.size = size;
            this.firstBits = firstBits;
        }
    }

    class DiskDumpInfo {
        public List<DiskDumpInfoEntry> entries;
        public Map<String, Integer> typeCounts;
        public DiskDumpInfo() {
            entries = new ArrayList<>();
            typeCounts = new HashMap<>();
        }
    }

    /**
     * 这种存储是否启用?
     * @return true, if enabled
     */
    boolean isEnabled();

    /**
     * 该硬盘缓存是在外部储存器还是内部储存器?
     * @return true, if external
     */
    boolean isExternal();

    /**
     * 通过id获取字节资源
     * @param resourceId id of the resource
     * @param debugInfo 帮助debug的对象
     * @return ID对应的资源，没找到返回NULL
     * @throws IOException for unexpected behavior.
     */
    BinaryResource getResource(String resourceId, Object debugInfo) throws IOException;

    /**
     * 某个id对应的资源是否存在于硬盘缓存中？
     * @param resourceId id of the resource
     * @param debugInfo helper object for debugging
     * @return true, if the resource is present in the storage, false otherwise
     * @throws IOException
     */
    boolean contains(String resourceId, Object debugInfo) throws IOException;

    /**
     * 某个id对应的资源是否存在?如果存在那么将更新最后使用该缓存的时间，是LRU算法的一部分。
     * 子类默认的实现是将该文件的LastModified修改成现在的时间。
     * @param resourceId id of the resource
     * @param debugInfo helper object for debugging
     * @return true, if the resource is present in the storage, false otherwise
     * @throws IOException
     */
    boolean touch(String resourceId, Object debugInfo) throws IOException;

    //清理无用的缓存资源
    void purgeUnexpectedResources();

    /**
     * 创建一个Inserter，通过这个Inserter来进行缓存条目的写入。
     * 使用这种方式是为了进行并发地写入多条缓存条目。
     * 在Insert.commit()调用之前，这个缓存条目对客户端是不可见的。
     * @param resourceId id of the resource
     * @param debugInfo helper object for debugging
     * @return the Inserter object with methods to write data, commit or cancel the insertion
     * @exception IOException on errors during this operation
     */
    Inserter insert(String resourceId, Object debugInfo) throws IOException;

    /**
     * 这里的Entry是一个接口，其需要在子类中实现，一个Entry代表一个缓存条目
     * 获取当前所有的Entries
     * @return a collection of entries in storage
     * @throws IOException
     */
    Collection<Entry> getEntries() throws IOException;

    /**
     * 通过Entry将其代表的缓存文件移除。
     * @param entry entry of the resource to delete
     * @return size of deleted file if successfully deleted, -1 otherwise
     * @throws IOException
     */
    long remove(Entry entry) throws IOException;

    /**
     * 通过Id将其代表的缓存文件移除。
     * @param resourceId
     * @return size of deleted file if successfully deleted, -1 otherwise
     * @throws IOException
     */
    long remove(String resourceId) throws IOException;

    /**
     * 清除所有的缓存文件
     * @exception IOException
     * @throws IOException
     */
    void clearAll() throws IOException;

    //获取所有硬盘缓存条目的信息，子类通过getEntries()实现。
    DiskDumpInfo getDumpInfo() throws IOException;

    /**
     * 获取这个硬盘缓存的名称，应该是唯一的
     * @return name of the this storage
     */
    String getStorageName();

    interface Entry {
        /** 获取唯一的id。 */
        String getId();
        /** 获取创建时间 **/
        long getTimestamp();
        /** 获取缓存文件大小，创建时候计算，之后不变 **/
        long getSize();
        BinaryResource getResource();
    }

    /**
     * 这是一个builder，当调用insert的时候返回
     * 它持有所有的插入操作
     * - writing data
     * - commiting
     * - clean up
     */
    interface Inserter {

        /**
         * 插入一个新的硬盘缓存条目，WriterCallback会提供一个OutputStream给客户端，客户端只要将需要进行缓存的文件
         * 写入这个OutputStream中就行，如{@link BufferedDiskCache#writeToDiskCache(CacheKey, EncodedImage)}，
         * 最后为了提高效率，客户端需要保证写的是一个大块的数据。例如使用BufferedInputStream或写入所有的数据。
         * Update the contents of the resource to be inserted. Executes outside the session lock.
         * The writer callback will be provided with an OutputStream to write to.
         * For high efficiency client should make sure that data is written in big chunks
         * (for example by employing BufferedInputStream or writing all data at once).
         * @param callback the write callback
         * @param debugInfo helper object for debugging
         * @throws IOException
         */
        void writeData(WriterCallback callback, Object debugInfo) throws IOException;

        /**
         * 在writeData()的时候需要缓存的文件已经写入到了文件系统中，但是缓存文件的命名是临时文件
         * 所以此时缓存文件对于客户端不可见，但是执行了这个方法之后，子类的实现是将之前的临时文件改名
         * 此时该硬盘缓存就对客户端可见了。
         * @param debugInfo debug object for debugging
         * @return the final resource created
         * @exception IOException on errors during the commit
         */

        BinaryResource commit(Object debugInfo) throws IOException;

        /**
         * 丢弃插入过程，如果已经被commit了那么就忽略该方法。
         * @return true if cleanUp is successful (or noop), false if something couldn't be dealt with
         */
        boolean cleanUp();
    }
}
