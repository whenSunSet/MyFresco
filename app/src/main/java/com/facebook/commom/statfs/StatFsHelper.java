package com.facebook.commom.statfs;

/**
 * Created by heshixiyang on 2017/3/12.
 */

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;

import com.facebook.commom.internal.Throwables;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * 助手类,定期检查可用的自由空间数量。
 * Helper class that periodically checks the amount of free space available.
 * 保持低的开销,它缓存自由空间信息,和*仅更新信息后两分钟
 * <p>To keep the overhead low, it caches the free space information, and
 * only updates that info after two minutes.
 *
 * 单例并且线程安全
 * <p>It is a singleton, and is thread-safe.
 *
 * 初始化是推迟到第一次使用,所以第一次调用任何方法可能会产生一些额外费用
 * <p>Initialization is delayed until first use, so the first call to any method may incur some
 * additional cost.
 */
@ThreadSafe
public class StatFsHelper {

    public enum StorageType {
        INTERNAL,
        EXTERNAL
    }

    private static StatFsHelper sStatsFsHelper;

    //时间间隔为更新磁盘信息
    // Time interval for updating disk information
    private static final long RESTAT_INTERVAL_MS = TimeUnit.MINUTES.toMillis(2);

    private volatile StatFs mInternalStatFs = null;
    private volatile File mInternalPath;

    private volatile StatFs mExternalStatFs = null;
    private volatile File mExternalPath;

    @GuardedBy("lock")
    private long mLastRestatTime;

    private final Lock lock;
    private volatile boolean mInitialized = false;

    public synchronized static StatFsHelper getInstance() {
        if (sStatsFsHelper == null) {
            sStatsFsHelper = new StatFsHelper();
        }
        return sStatsFsHelper;
    }

    /**
     * Constructor.
     *
     * 初始化延长到第一次是，所以我们需要调用 {@link #ensureInitialized()}当我们实现成员方法
     * <p>Initialization is delayed until first use, so we must call {@link #ensureInitialized()}
     * when implementing member methods.
     */
    protected StatFsHelper() {
        lock = new ReentrantLock();
    }

    /**
     * 初始化代码,有时需要很长时间。
     * Initialization code that can sometimes take a long time.
     */
    private void ensureInitialized() {
        if (!mInitialized) {
            lock.lock();
            try {
                if (!mInitialized) {
                    mInternalPath = Environment.getDataDirectory();
                    mExternalPath = Environment.getExternalStorageDirectory();
                    updateStats();
                    mInitialized = true;
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 检测文件系统的可用控件是不是大于给定的阈值
     * 需要注意可用空间信息缓存的时间间隔RESTAT_INTERVAL_MS
     * 如果目前的缓存空间已经小于阈值，那么其将返回错误结果，直至下一次缓存空间的测量
     * Check if available space in the filesystem is greater than the given threshold.
     * Note that the free space stats are cached and updated in intervals of RESTAT_INTERVAL_MS.
     * If the amount of free space has crossed over the threshold since the last update, it will
     * return incorrect results till the space stats are updated again.
     *
     * @param storageType StorageType (internal or external) to test
     * @param freeSpaceThreshold compare the available free space to this size
     * @return whether free space is lower than the input freeSpaceThreshold,
     * returns true if disk information is not available
     */
    public boolean testLowDiskSpace(StorageType storageType, long freeSpaceThreshold) {
        ensureInitialized();

        long availableStorageSpace = getAvailableStorageSpace(storageType);
        if (availableStorageSpace > 0) {
            return availableStorageSpace < freeSpaceThreshold;
        }
        return true;
    }

    /**
     * 获取剩余空间的信息，是内部还是外部储存空间，看给定的输入
     * Gets the information about the free storage space, including reserved blocks,
     * either internal or external depends on the given input
     * @param storageType Internal or external storage type
     * @return available space in bytes, -1 if no information is available
     */
    @SuppressLint("DeprecatedMethod")
    public long getFreeStorageSpace(StorageType storageType) {
        ensureInitialized();

        maybeUpdateStats();

        StatFs statFS = storageType == StorageType.INTERNAL ? mInternalStatFs : mExternalStatFs;
        if (statFS != null) {
            long blockSize, availableBlocks;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = statFS.getBlockSizeLong();
                availableBlocks = statFS.getFreeBlocksLong();
            } else {
                blockSize = statFS.getBlockSize();
                availableBlocks = statFS.getFreeBlocks();
            }
            return blockSize * availableBlocks;
        }
        return -1;
    }

    /**
     * 获取总的空间，是内部还是外部看输入
     * Gets the information about the total storage space,
     * either internal or external depends on the given input
     * @param storageType Internal or external storage type
     * @return available space in bytes, -1 if no information is available
     */
    @SuppressLint("DeprecatedMethod")
    public long getTotalStorageSpace(StorageType storageType) {
        ensureInitialized();

        maybeUpdateStats();

        StatFs statFS = storageType == StorageType.INTERNAL ? mInternalStatFs : mExternalStatFs;
        if (statFS != null) {
            long blockSize, totalBlocks;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = statFS.getBlockSizeLong();
                totalBlocks = statFS.getBlockCountLong();
            } else {
                blockSize = statFS.getBlockSize();
                totalBlocks = statFS.getBlockCount();
            }
            return blockSize * totalBlocks;
        }
        return -1;
    }

    /**
     * 查看可用的储存空间，是内部还是外部看被给定的输入
     * Gets the information about the available storage space
     * either internal or external depends on the give input
     * @param storageType Internal or external storage type
     * @return available space in bytes, 0 if no information is available
     */
    @SuppressLint("DeprecatedMethod")
    public long getAvailableStorageSpace(StorageType storageType) {
        ensureInitialized();

        maybeUpdateStats();

        StatFs statFS = storageType == StorageType.INTERNAL ? mInternalStatFs : mExternalStatFs;
        if (statFS != null) {
            long blockSize, availableBlocks;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = statFS.getBlockSizeLong();
                availableBlocks = statFS.getAvailableBlocksLong();
            } else {
                blockSize = statFS.getBlockSize();
                availableBlocks = statFS.getAvailableBlocks();
            }
            return blockSize * availableBlocks;
        }
        return 0;
    }

    /**
     * 线程安全地更新硬盘信息，如果时间间隔已经到了
     * Thread-safe call to update disk stats. Update occurs if the thread is able to acquire
     * the lock (i.e., no other thread is updating it at the same time), and it has been
     * at least RESTAT_INTERVAL_MS since the last update.
     * Assumes that initialization has been completed before this method is called.
     */
    private void maybeUpdateStats() {
        // Update the free space if able to get the lock,
        // with a frequency of once in RESTAT_INTERVAL_MS
        if (lock.tryLock()) {
            try {
                if ((SystemClock.uptimeMillis() - mLastRestatTime) > RESTAT_INTERVAL_MS) {
                    updateStats();
                }
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * 线程安全地重新设置硬盘状态
     * 如果我们知道最近空闲空间发生了变化(例如,如果我们有删除文件),
     * 使用这种方法重置内部状态和开始跟踪磁盘重新统计,重置内部定时器更新统计数据。
     * Thread-safe call to reset the disk stats.
     * If we know that the free space has changed recently (for example, if we have
     * deleted files), use this method to reset the internal state and
     * start tracking disk stats afresh, resetting the internal timer for updating stats.
     */
    public void resetStats() {
        // Update the free space if able to get the lock
        if (lock.tryLock()) {
            try {
                ensureInitialized();

                updateStats();
            } finally {
                lock.unlock();
            }
        }
    }

    /**
     * (重新)计算统计数据。
     * 调用者的责任是确保线程安全。假设其在初始化之后调用(或在其尽头调用)
     * (Re)calculate the stats.
     * It is the callers responsibility to ensure thread-safety.
     * Assumes that it is called after initialization (or at the end of it).
     */
    @GuardedBy("lock")
    private void updateStats() {
        mInternalStatFs = updateStatsHelper(mInternalStatFs, mInternalPath);
        mExternalStatFs = updateStatsHelper(mExternalStatFs, mExternalPath);
        mLastRestatTime = SystemClock.uptimeMillis();
    }

    /**
     * Update stats for a single directory and return the StatFs object for that directory. If the
     * directory does not exist or the StatFs restat() or constructor fails (throws), a null StatFs
     * object is returned.
     */
    private StatFs updateStatsHelper(@Nullable StatFs statfs, @Nullable File dir) {
        if(dir == null || !dir.exists()) {
            // The path does not exist, do not track stats for it.
            return null;
        }

        try {
            if (statfs == null) {
                // Create a new StatFs object for this path.
                statfs = createStatFs(dir.getAbsolutePath());
            } else {
                // Call restat and keep the existing StatFs object.
                statfs.restat(dir.getAbsolutePath());
            }
        } catch (IllegalArgumentException ex) {
            // Invalidate the StatFs object for this directory. The native StatFs implementation throws
            // IllegalArgumentException in the case that the statfs() system call fails and it invalidates
            // its internal data structures so subsequent calls against the StatFs object will fail or
            // throw (so we should make no more calls on the object). The most likely reason for this call
            // to fail is because the provided path no longer exists. The next call to updateStats() will
            // a new statfs object if the path exists. This will handle the case that a path is unmounted
            // and later remounted (but it has to have been mounted when this object was initialized).
            statfs = null;
        } catch (Throwable ex) {
            // Any other exception types are not expected and should be propagated as runtime errors.
            throw Throwables.propagate(ex);
        }

        return statfs;
    }

    protected static StatFs createStatFs(String path) {
        return new StatFs(path);
    }
}
