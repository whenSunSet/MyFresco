package com.facebook.cache.disk.impl;

/**
 * Created by Administrator on 2017/3/11 0011.
 */

import android.content.Context;
import android.support.annotation.VisibleForTesting;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.cache.commom.CacheErrorLogger;
import com.facebook.cache.commom.CacheEventListener;
import com.facebook.cache.commom.CacheKey;
import com.facebook.cache.commom.CacheKeyUtil;
import com.facebook.cache.commom.WriterCallback;
import com.facebook.cache.disk.DiskStorage;
import com.facebook.cache.disk.EntryEvictionComparatorSupplier;
import com.facebook.cache.disk.FileCache;
import com.facebook.commom.disk.DiskTrimmableRegistry;
import com.facebook.commom.logging.FLog;
import com.facebook.commom.statfs.StatFsHelper;
import com.facebook.commom.time.Clock;
import com.facebook.commom.time.impl.SystemClock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * 硬盘缓存管理类，其使用DefaultDiskStorage进行管理
 * Cache that manages disk storage.
 */
@ThreadSafe
public class DiskStorageCache implements FileCache{

    private static final Class<?> TAG = DiskStorageCache.class;

    // Any subclass that uses MediaCache/DiskCache's versioning system should use this
    // constant as the very first entry in their list of versions.  When all
    // subclasses of MediaCache have moved on to subsequent versions and are
    // no longer using this constant, it can be removed.
    public static final int START_OF_VERSIONING = 1;

    //有些时候可能文件的时间会出现错误，比如说文件创建的时间在未来，此时我们将使用这个参数
    private static final long FUTURE_TIMESTAMP_THRESHOLD_MS = TimeUnit.HOURS.toMillis(2);

    //以这个时间周期更新CacheStats和mResourceIndex，因为缓存增减很频繁所以不需要实时更新。
    // Force recalculation of the ground truth for filecache size at this interval
    private static final long FILECACHE_SIZE_UPDATE_PERIOD_MS = TimeUnit.MINUTES.toMillis(30);
    private static final double TRIMMING_LOWER_BOUND = 0.02;
    private static final long UNINITIALIZED = -1;
    private static final String SHARED_PREFS_FILENAME_PREFIX = "disk_entries_list";

    private final long mLowDiskSpaceCacheSizeLimit;
    private final long mDefaultCacheSizeLimit;
    private final CountDownLatch mCountDownLatch;
    private long mCacheSizeLimit;

    //缓存事件的监听器，客户端可以设置这个，对缓存的事件进行监听
    private final CacheEventListener mCacheEventListener;

    //所有硬盘缓存的resourceId都储存在这里，不过不是实时更新
    // All resourceId stored on disk (if any).
    @GuardedBy("mLock")
    @VisibleForTesting
    final Set<String> mResourceIndex;

    @GuardedBy("mLock")
    private long mCacheSizeLastUpdateTime;

    private final long mCacheSizeLimitMinimum;

    private final StatFsHelper mStatFsHelper;

    private final DiskStorage mStorage;
    //DiskStorage.Entry的比较器的生产者，通过DiskCacheConfig设置，用于LRU
    private final EntryEvictionComparatorSupplier mEntryEvictionComparatorSupplier;
    private final CacheErrorLogger mCacheErrorLogger;
    private final boolean mIndexPopulateAtStartupEnabled;

    private final CacheStats mCacheStats;

    private final Clock mClock;

    // synchronization object.
    private final Object mLock = new Object();

    private boolean mIndexReady;

    /**
     * 对缓存状态(当前缓存的大小(以字节为单位),缓存条目的数量)进行跟踪
     * Stats about the cache - currently size of the cache (in bytes) and number of items in
     * the cache
     */
    @VisibleForTesting
    static class CacheStats {

        private boolean mInitialized = false;
        private long mSize = UNINITIALIZED;    // size of the cache (in bytes)
        private long mCount = UNINITIALIZED;   // number of items in the cache

        public synchronized boolean isInitialized() {
            return mInitialized;
        }

        public synchronized void reset() {
            mInitialized = false;
            mCount = UNINITIALIZED;
            mSize = UNINITIALIZED;
        }

        public synchronized void set(long size, long count) {
            mCount = count;
            mSize = size;
            mInitialized = true;
        }

        public synchronized void increment(long sizeIncrement, long countIncrement) {
            if (mInitialized) {
                mSize += sizeIncrement;
                mCount += countIncrement;
            }
        }

        public synchronized long getSize() {
            return mSize;
        }

        public synchronized long getCount() {
            return mCount;
        }
    }

    public static class Params {
        //缓存最低限制
        public final long mCacheSizeLimitMinimum;
        //最低磁盘大小限制
        public final long mLowDiskSpaceCacheSizeLimit;
        //默认缓存大小限制
        public final long mDefaultCacheSizeLimit;

        public Params(
                long cacheSizeLimitMinimum,
                long lowDiskSpaceCacheSizeLimit,
                long defaultCacheSizeLimit) {
            mCacheSizeLimitMinimum = cacheSizeLimitMinimum;
            mLowDiskSpaceCacheSizeLimit = lowDiskSpaceCacheSizeLimit;
            mDefaultCacheSizeLimit = defaultCacheSizeLimit;
        }
    }

    public DiskStorageCache(
            DiskStorage diskStorage,
            EntryEvictionComparatorSupplier entryEvictionComparatorSupplier,
            Params params,
            CacheEventListener cacheEventListener,
            CacheErrorLogger cacheErrorLogger,
            @Nullable DiskTrimmableRegistry diskTrimmableRegistry,
            final Context context,
            final Executor executorForBackgrountInit,
            boolean indexPopulateAtStartupEnabled) {
        this.mLowDiskSpaceCacheSizeLimit = params.mLowDiskSpaceCacheSizeLimit;
        this.mDefaultCacheSizeLimit = params.mDefaultCacheSizeLimit;
        this.mCacheSizeLimit = params.mDefaultCacheSizeLimit;
        this.mStatFsHelper = StatFsHelper.getInstance();

        this.mStorage = diskStorage;

        this.mEntryEvictionComparatorSupplier = entryEvictionComparatorSupplier;

        this.mCacheSizeLastUpdateTime = UNINITIALIZED;

        this.mCacheEventListener = cacheEventListener;

        this.mCacheSizeLimitMinimum = params.mCacheSizeLimitMinimum;

        this.mCacheErrorLogger = cacheErrorLogger;

        this.mCacheStats = new CacheStats();

        if (diskTrimmableRegistry != null) {
            diskTrimmableRegistry.registerDiskTrimmable(this);
        }
        this.mClock = SystemClock.get();

        mIndexPopulateAtStartupEnabled = indexPopulateAtStartupEnabled;

        this.mResourceIndex = new HashSet<>();

        if (mIndexPopulateAtStartupEnabled) {
            mCountDownLatch = new CountDownLatch(1);

            executorForBackgrountInit.execute(new Runnable() {

                @Override
                public void run() {
                    synchronized (mLock) {
                        maybeUpdateFileCacheSize();
                    }
                    mCountDownLatch.countDown();
                }
            });
        } else {
            mCountDownLatch = new CountDownLatch(0);
        }

        executorForBackgrountInit.execute(new Runnable() {

            @Override
            public void run() {
                maybeDeleteSharedPreferencesFile(context, mStorage.getStorageName());
            }
        });
    }

    @Override
    public DiskStorage.DiskDumpInfo getDumpInfo() throws IOException {
        return mStorage.getDumpInfo();
    }

    @Override
    public boolean isEnabled() {
        return mStorage.isEnabled();
    }

    /**
     * 阻塞当前线程直到你完成了内存索引的初始化，当你希望内存索引冷启动的时候可以的调用
     * Blocks current thread until having finished initialization in Memory Index. Call only when you
     * need memory index in cold start.
     */
    @VisibleForTesting
    protected void awaitIndex() {
        try {
            mCountDownLatch.await();
        } catch (InterruptedException e) {
            FLog.e(TAG, "Memory Index is not ready yet. ");
        }
    }

    /**
     * 提示内存索引初始化已经完成，只有在当你需要知道是否内存索引已经完成冷启动的时候调用
     * Tells if memory index is completed in initialization. Only call it when you need to know if
     * memory index is completed in cold start.
     */
    public boolean isIndexReady() {
        return mIndexReady || !mIndexPopulateAtStartupEnabled;
    }

    /**
     * 根据key检索缓存文件,如果是在缓存中那么返回缓存，并且touch该条目,从而改变其LRU的时间戳
     * 如果文件不在文件缓存中,返回null。
     * Retrieves the file corresponding to the mKey, if it is in the cache. Also
     * touches the item, thus changing its LRU timestamp. If the file is not
     * present in the file cache, returns null.
     * <p>不能在ui线程中调用
     * This should NOT be called on the UI thread.
     *
     * @param key the mKey to check
     * @return The resource if present in cache, otherwise null
     */
    @Override
    public BinaryResource getResource(final CacheKey key) {
        String resourceId = null;
        SettableCacheEvent cacheEvent = SettableCacheEvent.obtain()
                .setCacheKey(key);
        try {
            synchronized (mLock) {
                BinaryResource resource = null;
                List<String> resourceIds = CacheKeyUtil.getResourceIds(key);
                for (int i = 0; i < resourceIds.size(); i++) {
                    resourceId = resourceIds.get(i);
                    cacheEvent.setResourceId(resourceId);
                    resource = mStorage.getResource(resourceId, key);
                    if (resource != null) {
                        break;
                    }
                }
                if (resource == null) {
                    mCacheEventListener.onMiss(cacheEvent);
                    mResourceIndex.remove(resourceId);
                } else {
                    mCacheEventListener.onHit(cacheEvent);
                    mResourceIndex.add(resourceId);
                }
                return resource;
            }
        } catch (IOException ioe) {
            mCacheErrorLogger.logError(
                    CacheErrorLogger.CacheErrorCategory.GENERIC_IO,
                    TAG,
                    "getResource",
                    ioe);
            cacheEvent.setException(ioe);
            mCacheEventListener.onReadException(cacheEvent);
            return null;
        } finally {
            cacheEvent.recycle();
        }
    }

    /**
     * 查询key所对应的文件缓存是否存在，注意这个方法会改变该文件缓存的时间戳，也就是touch该文件
     * Probes whether the object corresponding to the mKey is in the cache.
     * Note that the act of probing touches the item (if present in cache),
     * thus changing its LRU timestamp.
     * 这个比文件检索有快得多，但是其依然不能出现在UI线程
     * This will be faster than retrieving the object, but it still has
     * file system accesses and should NOT be called on the UI thread.
     *
     * @param key the mKey to check
     * @return whether the keyed mValue is in the cache
     */
    public boolean probe(final CacheKey key) {
        String resourceId = null;
        try {
            synchronized (mLock) {
                List<String> resourceIds = CacheKeyUtil.getResourceIds(key);
                for (int i = 0; i < resourceIds.size(); i++) {
                    resourceId = resourceIds.get(i);
                    if (mStorage.touch(resourceId, key)) {
                        mResourceIndex.add(resourceId);
                        return true;
                    }
                }
                return false;
            }
        } catch (IOException e) {
            SettableCacheEvent cacheEvent = SettableCacheEvent.obtain()
                    .setCacheKey(key)
                    .setResourceId(resourceId)
                    .setException(e);
            mCacheEventListener.onReadException(cacheEvent);
            cacheEvent.recycle();
            return false;
        }
    }

    /**
     * 创建DiskStorage.Inserter，方便客户端写入文件缓存
     * Creates a temp file for writing outside the session lock
     */
    private DiskStorage.Inserter startInsert(
            final String resourceId,
            final CacheKey key)
            throws IOException {
        maybeEvictFilesInCacheDir();
        return mStorage.insert(resourceId, key);
    }

    /**
     * 提交缓存提供的临时文件,将其重命名。
     * Commits the provided temp file to the cache, renaming it to match
     * the cache's hashing convention.
     */
    private BinaryResource endInsert(
            final DiskStorage.Inserter inserter,
            final CacheKey key,
            String resourceId) throws IOException {
        synchronized (mLock) {
            BinaryResource resource = inserter.commit(key);
            mResourceIndex.add(resourceId);
            mCacheStats.increment(resource.size(), 1);
            return resource;
        }
    }

    @Override
    public BinaryResource insert(CacheKey key, WriterCallback callback) throws IOException {
        //写入一个临时文件,然后移入缓存文件夹中。这个操作是为了允许并行性行为。
        // Write to a temp file, then move it into place. This allows more parallelism when writing files.
        SettableCacheEvent cacheEvent = SettableCacheEvent.obtain()
                .setCacheKey(key);
        mCacheEventListener.onWriteAttempt(cacheEvent);
        String resourceId;
        synchronized (mLock) {
            //对多个资源id相同的图像,我们只写一个文件
            // for multiple resource ids associated with the same image, we only write one file
            resourceId = CacheKeyUtil.getFirstResourceId(key);
        }
        cacheEvent.setResourceId(resourceId);
        try {
            // getting the file is synchronized
            DiskStorage.Inserter inserter = startInsert(resourceId, key);
            try {
                inserter.writeData(callback, key);
                // Committing the file is synchronized
                BinaryResource resource = endInsert(inserter, key, resourceId);
                cacheEvent.setItemSize(resource.size()).setCacheSize(mCacheStats.getSize());
                mCacheEventListener.onWriteSuccess(cacheEvent);
                return resource;
            } finally {
                if (!inserter.cleanUp()) {
                    FLog.e(TAG, "Failed to delete temp file");
                }
            }
        } catch (IOException ioe) {
            cacheEvent.setException(ioe);
            mCacheEventListener.onWriteException(cacheEvent);
            FLog.e(TAG, "Failed inserting a file into the cache", ioe);
            throw ioe;
        } finally {
            cacheEvent.recycle();
        }
    }

    @Override
    public void remove(CacheKey key) {
        synchronized (mLock) {
            try {
                String resourceId = null;
                List<String> resourceIds = CacheKeyUtil.getResourceIds(key);
                for (int i = 0; i < resourceIds.size(); i++) {
                    resourceId = resourceIds.get(i);
                    mStorage.remove(resourceId);
                    mResourceIndex.remove(resourceId);
                }
            } catch (IOException e) {
                mCacheErrorLogger.logError(
                        CacheErrorLogger.CacheErrorCategory.DELETE_FILE,
                        TAG,
                        "delete: " + e.getMessage(),
                        e);
            }
        }
    }

    /**
     * 删除老的文件
     * Deletes old cache files.
     * @param cacheExpirationMs 当文件比这个老的时候，就删除文件
     * files older than this will be deleted.
     * @return the age in ms of the oldest file remaining in the cache.
     */
    @Override
    public long clearOldEntries(long cacheExpirationMs) {
        long oldestRemainingEntryAgeMs = 0L;
        synchronized (mLock) {
            try {
                long now = mClock.now();
                Collection<DiskStorage.Entry> allEntries = mStorage.getEntries();
                final long cacheSizeBeforeClearance = mCacheStats.getSize();
                int itemsRemovedCount = 0;
                long itemsRemovedSize = 0L;
                for (DiskStorage.Entry entry : allEntries) {
                    // entry age of zero is disallowed.
                    long entryAgeMs = Math.max(1, Math.abs(now - entry.getTimestamp()));
                    if (entryAgeMs >= cacheExpirationMs) {
                        long entryRemovedSize = mStorage.remove(entry);
                        mResourceIndex.remove(entry.getId());
                        if (entryRemovedSize > 0) {
                            itemsRemovedCount++;
                            itemsRemovedSize += entryRemovedSize;
                            SettableCacheEvent cacheEvent = SettableCacheEvent.obtain()
                                    .setResourceId(entry.getId())
                                    .setEvictionReason(CacheEventListener.EvictionReason.CONTENT_STALE)
                                    .setItemSize(entryRemovedSize)
                                    .setCacheSize(cacheSizeBeforeClearance - itemsRemovedSize);
                            mCacheEventListener.onEviction(cacheEvent);
                            cacheEvent.recycle();
                        }
                    } else {
                        oldestRemainingEntryAgeMs = Math.max(oldestRemainingEntryAgeMs, entryAgeMs);
                    }
                }
                mStorage.purgeUnexpectedResources();
                if (itemsRemovedCount > 0) {
                    maybeUpdateFileCacheSize();
                    mCacheStats.increment(-itemsRemovedSize, -itemsRemovedCount);
                }
            } catch (IOException ioe) {
                mCacheErrorLogger.logError(
                        CacheErrorLogger.CacheErrorCategory.EVICTION,
                        TAG,
                        "clearOldEntries: " + ioe.getMessage(),
                        ioe);
            }
        }
        return oldestRemainingEntryAgeMs;
    }

    /**
     * 判断是否硬盘的容量已经到达了极限，如果是这样,删除一些缓存文件。
     * evictAboveSize()是具体删除文件的方法，其使用了LRU
     * Test if the cache size has exceeded its limits, and if so, evict some files.
     * It also calls maybeUpdateFileCacheSize
     * This method uses mLock for synchronization purposes.
     */
    private void maybeEvictFilesInCacheDir() throws IOException {
        synchronized (mLock) {
            boolean calculatedRightNow = maybeUpdateFileCacheSize();

            //更新mCacheSizeLimit
            // Update the size limit (mCacheSizeLimit)
            updateFileCacheSizeLimit();

            long cacheSize = mCacheStats.getSize();
            // If we are going to evict force a recalculation of the size
            // (except if it was already calculated!)
            if (cacheSize > mCacheSizeLimit && !calculatedRightNow) {
                mCacheStats.reset();
                maybeUpdateFileCacheSize();
            }

            // If size has exceeded the size limit, evict some files
            if (cacheSize > mCacheSizeLimit) {
                evictAboveSize(mCacheSizeLimit * 9 / 10, CacheEventListener.EvictionReason.CACHE_FULL); // 90%
            }
        }
    }

    //具体删除缓存文件的方法，先将文件按时间排序，然后删除缓存文件直至不再超出内存限制
    @GuardedBy("mLock")
    private void evictAboveSize(
            long desiredSize,
            CacheEventListener.EvictionReason reason) throws IOException {
        Collection<DiskStorage.Entry> entries;
        try {
            entries = getSortedEntries(mStorage.getEntries());
        } catch (IOException ioe) {
            mCacheErrorLogger.logError(
                    CacheErrorLogger.CacheErrorCategory.EVICTION,
                    TAG,
                    "evictAboveSize: " + ioe.getMessage(),
                    ioe);
            throw ioe;
        }

        long cacheSizeBeforeClearance = mCacheStats.getSize();
        long deleteSize = cacheSizeBeforeClearance - desiredSize;
        int itemCount = 0;
        long sumItemSizes = 0L;
        for (DiskStorage.Entry entry: entries) {
            if (sumItemSizes > (deleteSize)) {
                break;
            }
            long deletedSize = mStorage.remove(entry);
            mResourceIndex.remove(entry.getId());
            if (deletedSize > 0) {
                itemCount++;
                sumItemSizes += deletedSize;
                SettableCacheEvent cacheEvent = SettableCacheEvent.obtain()
                        .setResourceId(entry.getId())
                        .setEvictionReason(reason)
                        .setItemSize(deletedSize)
                        .setCacheSize(cacheSizeBeforeClearance - sumItemSizes)
                        .setCacheLimit(desiredSize);
                mCacheEventListener.onEviction(cacheEvent);
                cacheEvent.recycle();
            }
        }
        mCacheStats.increment(-sumItemSizes, -itemCount);
        mStorage.purgeUnexpectedResources();
    }

    /**
     *
     * If any file timestamp is in the future (beyond now + FUTURE_TIMESTAMP_THRESHOLD_MS), we will
     * set its effective timestamp to 0 (the beginning of unix time), thus sending it to the head of
     * the queue for eviction (entries with the lowest timestamps are evicted first). This is a
     * safety check in case we get files that are written with a future timestamp.
     * We are adding a small delta (this constant) to account for network time changes, timezone
     * changes, etc.
     */
    private Collection<DiskStorage.Entry> getSortedEntries(Collection<DiskStorage.Entry> allEntries) {
        final long threshold = mClock.now() + DiskStorageCache.FUTURE_TIMESTAMP_THRESHOLD_MS;
        ArrayList<DiskStorage.Entry> sortedList = new ArrayList<>(allEntries.size());
        ArrayList<DiskStorage.Entry> listToSort = new ArrayList<>(allEntries.size());
        for (DiskStorage.Entry entry : allEntries) {
            if (entry.getTimestamp() > threshold) {
                sortedList.add(entry);
            } else {
                listToSort.add(entry);
            }
        }
        Collections.sort(listToSort, mEntryEvictionComparatorSupplier.get());
        sortedList.addAll(listToSort);
        return sortedList;
    }

    /**
     * 助手方法,设置缓存大小限制是高,还是低的极限。*如果没有足够的空闲空间来满足高限制,它被设置为下限
     * Helper method that sets the cache size limit to be either a high, or a low limit.
     * If there is not enough free space to satisfy the high limit, it is set to the low limit.
     */
    @GuardedBy("mLock")
    private void updateFileCacheSizeLimit() {
        // Test if mCacheSizeLimit can be set to the high limit
        boolean isAvailableSpaceLowerThanHighLimit;
        StatFsHelper.StorageType storageType =
                mStorage.isExternal()
                        ? StatFsHelper.StorageType.EXTERNAL
                        : StatFsHelper.StorageType.INTERNAL;
        isAvailableSpaceLowerThanHighLimit =
                mStatFsHelper.testLowDiskSpace(
                        storageType,
                        mDefaultCacheSizeLimit - mCacheStats.getSize());
        if (isAvailableSpaceLowerThanHighLimit) {
            mCacheSizeLimit = mLowDiskSpaceCacheSizeLimit;
        } else {
            mCacheSizeLimit = mDefaultCacheSizeLimit;
        }
    }

    public long getSize() {
        return mCacheStats.getSize();
    }

    public long getCount() {
        return mCacheStats.getCount();
    }

    public void clearAll() {
        synchronized (mLock) {
            try {
                mStorage.clearAll();
                mResourceIndex.clear();
                mCacheEventListener.onCleared();
            } catch (IOException ioe) {
                mCacheErrorLogger.logError(
                        CacheErrorLogger.CacheErrorCategory.EVICTION,
                        TAG,
                        "clearAll: " + ioe.getMessage(),
                        ioe);
            }
            mCacheStats.reset();
        }
    }

    @Override
    public boolean hasKeySync(CacheKey key) {
        synchronized (mLock) {
            String resourceId = null;
            List<String> resourceIds = CacheKeyUtil.getResourceIds(key);
            for (int i = 0; i< resourceIds.size(); i++) {
                resourceId = resourceIds.get(i);
                if (mResourceIndex.contains(resourceId)) {
                    return true;
                }
            }
            return false;
        }
    }

    //hasKeySync(CacheKey key)的升级版，不仅在内存中查看是否有key还去mStorage中查看
    @Override
    public boolean hasKey(final CacheKey key) {
        synchronized (mLock) {
            if (hasKeySync(key)) {
                return true;
            }
            try {
                String resourceId = null;
                List<String> resourceIds = CacheKeyUtil.getResourceIds(key);
                for (int i = 0; i < resourceIds.size(); i++) {
                    resourceId = resourceIds.get(i);
                    if (mStorage.contains(resourceId, key)) {
                        mResourceIndex.add(resourceId);
                        return true;
                    }
                }
                return false;
            } catch (IOException e) {
                return false;
            }
        }
    }

    //实现了DiskTrimmable中的方法
    @Override
    public void trimToMinimum() {
        synchronized (mLock) {
            maybeUpdateFileCacheSize();
            long cacheSize = mCacheStats.getSize();
            if (mCacheSizeLimitMinimum <= 0 || cacheSize <= 0 || cacheSize < mCacheSizeLimitMinimum) {
                return;
            }
            double trimRatio = 1 - (double) mCacheSizeLimitMinimum / (double) cacheSize;
            if (trimRatio > TRIMMING_LOWER_BOUND) {
                trimBy(trimRatio);
            }
        }
    }

    //实现了DiskTrimmable中的方法
    @Override
    public void trimToNothing() {
        clearAll();
    }

    private void trimBy(final double trimRatio) {
        synchronized (mLock) {
            try {
                // Force update the ground truth if we are about to evict
                mCacheStats.reset();
                maybeUpdateFileCacheSize();
                long cacheSize = mCacheStats.getSize();
                long newMaxBytesInFiles = cacheSize - (long) (trimRatio * cacheSize);
                evictAboveSize(
                        newMaxBytesInFiles,
                        CacheEventListener.EvictionReason.CACHE_MANAGER_TRIMMED);
            } catch (IOException ioe) {
                mCacheErrorLogger.logError(
                        CacheErrorLogger.CacheErrorCategory.EVICTION,
                        TAG,
                        "trimBy: " + ioe.getMessage(),
                        ioe);
            }
        }
    }

    /**
     * 如果文件缓存大小没有被计算过，或者已经到了再次计算的周期，那么就调用更新CacheStats和mResourceIndex
     * If file cache size is not calculated or if it was calculated
     * a long time ago (FILECACHE_SIZE_UPDATE_PERIOD_MS) recalculated from file listing.
     * @return true if it was recalculated, false otherwise.
     */
    @GuardedBy("mLock")
    private boolean maybeUpdateFileCacheSize() {
        long now = mClock.now();
        if ((!mCacheStats.isInitialized()) ||
                mCacheSizeLastUpdateTime == UNINITIALIZED ||
                (now - mCacheSizeLastUpdateTime) > FILECACHE_SIZE_UPDATE_PERIOD_MS) {
            return maybeUpdateFileCacheSizeAndIndex();
        }
        return false;
    }

    //具体更新CacheStats和mResourceIndex的逻辑
    @GuardedBy("mLock")
    private boolean maybeUpdateFileCacheSizeAndIndex() {
        long size = 0;
        int count = 0;
        boolean foundFutureTimestamp = false;
        int numFutureFiles = 0;
        int sizeFutureFiles = 0;
        long maxTimeDelta = -1;
        long now = mClock.now();
        long timeThreshold = now + FUTURE_TIMESTAMP_THRESHOLD_MS;
        Set<String> tempResourceIndex;
        if (mIndexPopulateAtStartupEnabled && mResourceIndex.isEmpty()) {
            tempResourceIndex = mResourceIndex;
        } else if (mIndexPopulateAtStartupEnabled) {
            tempResourceIndex = new HashSet<>();
        } else {
            tempResourceIndex = null;
        }
        try {
            Collection<DiskStorage.Entry> entries = mStorage.getEntries();
            for (DiskStorage.Entry entry: entries) {
                count++;
                size += entry.getSize();

                //Check if any files have a future timestamp, beyond our threshold
                if (entry.getTimestamp() > timeThreshold) {
                    foundFutureTimestamp = true;
                    numFutureFiles++;
                    sizeFutureFiles += entry.getSize();
                    maxTimeDelta = Math.max(entry.getTimestamp() - now, maxTimeDelta);
                } else if (mIndexPopulateAtStartupEnabled) {
                    tempResourceIndex.add(entry.getId());
                }
            }
            if (foundFutureTimestamp) {
                mCacheErrorLogger.logError(
                        CacheErrorLogger.CacheErrorCategory.READ_INVALID_ENTRY,
                        TAG,
                        "Future timestamp found in " + numFutureFiles +
                                " files , with a total size of " + sizeFutureFiles +
                                " bytes, and a maximum time delta of " + maxTimeDelta + "ms",
                        null);
            }
            if (mCacheStats.getCount() != count || mCacheStats.getSize() != size) {
                if (mIndexPopulateAtStartupEnabled && mResourceIndex != tempResourceIndex) {
                    mIndexReady = true;
                } else if (mIndexPopulateAtStartupEnabled) {
                    mResourceIndex.clear();
                    mResourceIndex.addAll(tempResourceIndex);
                }
                mCacheStats.set(size, count);
            }
        } catch (IOException ioe) {
            mCacheErrorLogger.logError(
                    CacheErrorLogger.CacheErrorCategory.GENERIC_IO,
                    TAG,
                    "calcFileCacheSize: " + ioe.getMessage(),
                    ioe);
            return false;
        }
        mCacheSizeLastUpdateTime = now;
        return true;
    }

    //TODO(t12287315): Remove the temp method for deleting created Preference in next release
    private static void maybeDeleteSharedPreferencesFile(
            Context context,
            String directoryName) {
        try {
            Context applicationContext = context.getApplicationContext();
            String path =
                    applicationContext.getFilesDir().getParent()
                            + File.separator
                            + "shared_prefs"
                            + File.separator
                            + SHARED_PREFS_FILENAME_PREFIX
                            + directoryName;
            File file = new File(path + ".xml");
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            FLog.e(TAG, "Fail to delete SharedPreference from file system. ");
        }
    }
}
