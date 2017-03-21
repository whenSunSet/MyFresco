package com.facebook.cache.disk;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

import com.facebook.binaryresource.BinaryResource;
import com.facebook.cache.commom.WriterCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件缓存
 * 负责维护状态(数量, 大小, 查看文件是否存在, 可达性)
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
     * 这种存储是否是external?
     * @return true, if external
     */
    boolean isExternal();

    /**
     * 通过特别的名字获取字节资源
     * @param resourceId id of the resource
     * @param debugInfo 帮助debug的对象
     * @return ID对应的资源，没找到返回NULL
     * @throws IOException for unexpected behavior.
     */
    BinaryResource getResource(String resourceId, Object debugInfo) throws IOException;

    /**
     * 某个资源名字对应的资源是否存在?
     * @param resourceId id of the resource
     * @param debugInfo helper object for debugging
     * @return true, if the resource is present in the storage, false otherwise
     * @throws IOException
     */
    boolean contains(String resourceId, Object debugInfo) throws IOException;

    /**
     * 某个资源名字对应的资源是否存在?如果存在那么将最后更新最后接触该文件的时间
     * @param resourceId id of the resource
     * @param debugInfo helper object for debugging
     * @return true, if the resource is present in the storage, false otherwise
     * @throws IOException
     */
    boolean touch(String resourceId, Object debugInfo) throws IOException;

    void purgeUnexpectedResources();

    /**
     * 创建一个临时的条目来写缓存内容. 脱离了commit()
     * 是为了允许并发的写缓存条目
     * 这个Entry将对缓存的客户端不可见直到Insert.commit()被调用并且从这个方法返回
     * @param resourceId id of the resource
     * @param debugInfo helper object for debugging
     * @return the Inserter object with methods to write data, commit or cancel the insertion
     * @exception IOException on errors during this operation
     */
    Inserter insert(String resourceId, Object debugInfo) throws IOException;

    /**
     * 获取当前所有的entries
     * @return a collection of entries in storage
     * @throws IOException
     */
    Collection<Entry> getEntries() throws IOException;

    /**
     * 通过entry来移除资源
     * @param entry entry of the resource to delete
     * @return size of deleted file if successfully deleted, -1 otherwise
     * @throws IOException
     */
    long remove(Entry entry) throws IOException;

    /**
     * 通过Id移除资源
     * @param resourceId
     * @return size of deleted file if successfully deleted, -1 otherwise
     * @throws IOException
     */
    long remove(String resourceId) throws IOException;

    /**
     * 清除所有的资源
     * @exception IOException
     * @throws IOException
     */
    void clearAll() throws IOException;

    DiskDumpInfo getDumpInfo() throws IOException;

    /**
     * 获取这个 储存的名称，应该是唯一的
     * @return name of the this storage
     */
    String getStorageName();

    interface Entry {
        /** 资源的id */
        String getId();
        /** 创建的时间，是不可变得 **/
        long getTimestamp();
        /** 在第一次计算，永远不可变的 **/
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
         * 将这个插入提交给缓存，一旦被调用那个entry将会被客户端可见
         * @param debugInfo debug object for debugging
         * @return the final resource created
         * @exception IOException on errors during the commit
         */
        BinaryResource commit(Object debugInfo) throws IOException;

        /**
         * 丢弃插入过程，如果已经被提交了那么久忽略
         * @return true if cleanUp is successful (or noop), false if something couldn't be dealt with
         */
        boolean cleanUp();
    }
}
