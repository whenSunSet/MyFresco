package com.facebook.imagepipeline.cache.impl;

/**
 * Created by heshixiyang on 2017/3/26.
 */

import android.os.SystemClock;
import android.support.annotation.VisibleForTesting;

import com.android.internal.util.Predicate;
import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.internal.Supplier;
import com.facebook.commom.memory.MemoryTrimType;
import com.facebook.commom.memory.MemoryTrimmable;
import com.facebook.commom.references.CloseableReference;
import com.facebook.commom.references.ResourceReleaser;
import com.facebook.imagepipeline.cache.MemoryCache;
import com.facebook.imagepipeline.cache.ValueDescriptor;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * 我的理解：
 * 1.首先CountingMemoryCache使用了内部类Entry，作为每个内存缓存的数据结构。我们可以把一个Entry看成一个内存缓存。
 * 2.Entry中的key和value不用说，clientCount是用来记录该Entry正在被几个客户端使用，我们前面了解了CloseableReference的特性，
 * 这里在创建的时候使用了CloseableReference#of()创建新的客户端使用的CloseableReference，所以我们可以知道这里的clientCount
 * 与SharedReference.sLiveObjects中该资源的SharedReference数量是一致的。isOrphan则是用来标记该Entry的资源是否应该被close。
 * 3.CountingMemoryCache中使用了两个包装了LinkedHashMap的CountingLruMap，一个mCachedEntries用来储存所有的Entry，一个mExclusiveEntries
 * 用来储存所有clientCount为0的Entry，因为Entry只有在cache()中会被调用，而cache()会返回一个CloseableReference，所以该Entry在创建的时候
 * clientCount为1。
 * 4.由于mExclusiveEntries是一个LinkedHashMap，所以在清楚缓存的时候，我们只要按照顺序清除就已经实现了LRU算法了。而在一般情况下
 * clientCount为0的Entry比不为0的Entry多许多，所以基本不需要担心内存清理问题。
 * 5.我们在2中说了clientCount就代表着该资源所有的引用数，在newClientReference()中使用CloseableReference#of()创建CloseableReference的时候
 * 传入了一个ResourceReleaser的实现，这个实现调用了releaseClientReference()，这里的实现和CloseableReference中的实现是不同的
 * 不同之处在于CloseableReference中第一个SharedReference清零了之后资源就被释放了，完全不顾其他SharedReference的死活。
 * 而这里的实现是所有的SharedReference都失效了之后才释放资源。
 */
@ThreadSafe
public class CountingMemoryCache<K, V> implements MemoryCache<K, V>, MemoryTrimmable {

    /**
     * 当改变缓存策略的时候调用
     * Interface used to specify the trimming strategy for the cache.
     */
    public interface CacheTrimStrategy {
        double getTrimRatio(MemoryTrimType trimType);
    }

    /**
     * 当Entry的状态发生改变的时候使用
     * Interface used to observe the state changes of an entry.
     */
    public interface EntryStateObserver<K> {

        /**
         * 当一个Entry被视为需要被驱逐的时候，会被调用
         * Called when the exclusivity status of the entry changes.
         *
         * <p> The item can be reused if it is exclusively owned by the cache.
         */
        void onExclusivityChanged(K key, boolean isExclusive);
    }

    /**
     * 将一对键值对储存在内存中,每一个Entry代表一个内存条目
     * The internal representation of a key-value pair stored by the cache.
     */
    @VisibleForTesting
    static class Entry<K, V> {
        public final K key;
        public final CloseableReference<V> valueRef;
        // 引用这个缓存的客户端的数量.
        public int clientCount;
        // Whether or not this entry is tracked by this cache. Orphans are not tracked by the cache and
        // as soon as the last client of an orphaned entry closes their reference, the entry's copy is
        // closed too.
        // 使用了这个标记表示该Entry就要被删除了，只要clientCount等于零。
        public boolean isOrphan;
        @Nullable public final EntryStateObserver<K> observer;

        private Entry(K key, CloseableReference<V> valueRef, @Nullable EntryStateObserver<K> observer) {
            this.key = Preconditions.checkNotNull(key);
            this.valueRef = Preconditions.checkNotNull(CloseableReference.cloneOrNull(valueRef));
            this.clientCount = 0;
            this.isOrphan = false;
            this.observer = observer;
        }

        /** Creates a new entry with the usage count of 0. */
        @VisibleForTesting
        static <K, V> Entry<K, V> of(
                final K key,
                final CloseableReference<V> valueRef,
                final @Nullable EntryStateObserver<K> observer) {
            return new Entry<>(key, valueRef, observer);
        }
    }

    // How often the cache checks for a new cache configuration.
    // 多久检查一次新的缓存配置
    @VisibleForTesting
    static final long PARAMS_INTERCHECK_INTERVAL_MS = TimeUnit.MINUTES.toMillis(5);

    // Contains the items that are not being used by any client and are hence viable for eviction
    // 包括所有没有任何客户端引用的内存条目。
    @GuardedBy("this")
    @VisibleForTesting
    final CountingLruMap<K, Entry<K, V>> mExclusiveEntries;

    // Contains all the cached items including the exclusively owned ones.
    // 包含所有的缓存条目，包括上面的条目。
    @GuardedBy("this")
    @VisibleForTesting
    final CountingLruMap<K, Entry<K, V>> mCachedEntries;

    //value包装器
    private final ValueDescriptor<V> mValueDescriptor;

    //缓存策略器，一般为null
    private final CacheTrimStrategy mCacheTrimStrategy;

    // 内存大小的约束条件的提供器
    private final Supplier<MemoryCacheParams> mMemoryCacheParamsSupplier;

    @GuardedBy("this")
    protected MemoryCacheParams mMemoryCacheParams;

    //上一次检查缓存信息的时间
    @GuardedBy("this")
    private long mLastCacheParamsCheck;

    public CountingMemoryCache(
            ValueDescriptor<V> valueDescriptor,
            CacheTrimStrategy cacheTrimStrategy,
            Supplier<MemoryCacheParams> memoryCacheParamsSupplier) {
        mValueDescriptor = valueDescriptor;
        mExclusiveEntries = new CountingLruMap<>(wrapValueDescriptor(valueDescriptor));
        mCachedEntries = new CountingLruMap<>(wrapValueDescriptor(valueDescriptor));
        mCacheTrimStrategy = cacheTrimStrategy;
        mMemoryCacheParamsSupplier = memoryCacheParamsSupplier;
        mMemoryCacheParams = mMemoryCacheParamsSupplier.get();
        mLastCacheParamsCheck = SystemClock.uptimeMillis();

    }

    private ValueDescriptor<Entry<K, V>> wrapValueDescriptor(
            final ValueDescriptor<V> evictableValueDescriptor) {
        return new ValueDescriptor<Entry<K,V>>() {
            @Override
            public int getSizeInBytes(Entry<K, V> entry) {
                return evictableValueDescriptor.getSizeInBytes(entry.valueRef.get());
            }
        };
    }

    /**
     * 将一个键值对缓存
     * Caches the given key-value pair.
     * 客户端应该使用返回的 引用 而不是原来传入的 引用 ，关闭这个返回的引用的责任是调用者的，如果不再需要这个引用
     * <p> Important: the client should use the returned reference instead of the original one.
     * It is the caller's responsibility to close the returned reference once not needed anymore.
     *
     * @return the new reference to be used, null if the value cannot be cached
     */
    public CloseableReference<V> cache(final K key, final CloseableReference<V> valueRef) {
        return cache(key, valueRef, null);
    }

    /**
     * Caches the given key-value pair.
     *
     * 客户端应该使用返回的 引用 而不是原来传入的 引用 ，关闭这个返回的引用的责任是调用者的，如果不再需要这个引用
     * <p> Important: the client should use the returned reference instead of the original one.
     * It is the caller's responsibility to close the returned reference once not needed anymore.
     *
     * @return the new reference to be used, null if the value cannot be cached
     */
    public CloseableReference<V> cache(
            final K key,
            final CloseableReference<V> valueRef,
            final EntryStateObserver<K> observer) {
        Preconditions.checkNotNull(key);
        Preconditions.checkNotNull(valueRef);

        maybeUpdateCacheParams();

        Entry<K, V> oldExclusive;
        CloseableReference<V> oldRefToClose = null;
        CloseableReference<V> clientRef = null;
        synchronized (this) {
            // 删除 待销毁Map 和 全部条目Map 中的该键值对
            oldExclusive = mExclusiveEntries.remove(key);
            Entry<K, V> oldEntry = mCachedEntries.remove(key);

            //如果之前 全部条目Map 中已经有了这个键值对 那么将其设置为Orphan
            if (oldEntry != null) {
                makeOrphan(oldEntry);
                oldRefToClose = referenceToClose(oldEntry);
            }

            //如果剩余内存容量，可以添加支持新的cache，那么就添加
            if (canCacheNewValue(valueRef.get())) {
                Entry<K, V> newEntry = Entry.of(key, valueRef, observer);
                mCachedEntries.put(key, newEntry);
                clientRef = newClientReference(newEntry);
            }
        }

        //安全的关闭旧的条目
        CloseableReference.closeSafely(oldRefToClose);
        //如果设置该缓存原本是属于要被删除的条目，那么此时其状态改变了，可以调用状态改变的监听者
        maybeNotifyExclusiveEntryRemoval(oldExclusive);
        //判断添加了缓存之后，是否应该进行缓存清理
        maybeEvictEntries();
        //返回一个新的CloseableReference代替原来的
        return clientRef;
    }

    /**
     * 判断是否可以插入一个新缓存
     * Checks the cache constraints to determine whether the new value can be cached or not. */
    private synchronized boolean canCacheNewValue(V value) {
        int newValueSize = mValueDescriptor.getSizeInBytes(value);
        return (newValueSize <= mMemoryCacheParams.maxCacheEntrySize) &&
                (getInUseCount() <= mMemoryCacheParams.maxCacheEntries - 1) &&
                (getInUseSizeInBytes() <= mMemoryCacheParams.maxCacheSize - newValueSize);
    }

    /**
     * 获取一个缓存条目
     * Gets the item with the given key, or null if there is no such item.
     * <p> It is the caller's responsibility to close the returned reference once not needed anymore.
     */
    @Nullable
    public CloseableReference<V> get(final K key) {
        Preconditions.checkNotNull(key);
        Entry<K, V> oldExclusive;
        CloseableReference<V> clientRef = null;
        synchronized (this) {
            //将该条目从 待销毁条目Map 中删除
            oldExclusive = mExclusiveEntries.remove(key);
            Entry<K, V> entry = mCachedEntries.get(key);
            //如果从 全部条目Map 中获取的条目不为空 那么新建一个 CloseableReference引用
            if (entry != null) {
                clientRef = newClientReference(entry);
            }
        }

        //如果设置该缓存原本是属于要被删除的条目，那么此时其状态改变了，可以调用状态改变的监听者
        maybeNotifyExclusiveEntryRemoval(oldExclusive);
        //可能会更新缓存配置
        maybeUpdateCacheParams();
        //判断是否应该进行缓存清理
        maybeEvictEntries();
        return clientRef;
    }

    /**
     * 为客户端创建一个新的引用
     * Creates a new reference for the client. */
    private synchronized CloseableReference<V> newClientReference(final Entry<K, V> entry) {
        //增加该缓存条目的客户端引用数值
        increaseClientCount(entry);

        //返回一个客户端引用，并放入一个回调当关闭这个引用的时候调用
        return CloseableReference.of(
                entry.valueRef.get(),
                new ResourceReleaser<V>() {
                    @Override
                    public void release(V unused) {
                        releaseClientReference(entry);
                    }
                });
    }

    /**
     * 当一个客户端关闭了一个CloseableReference引用的时候调用，将该条目的客户端引用数量减1
     * 当客户端引用数量为0的时候释放资源。
     * Called when the client closes its reference. */
    private void releaseClientReference(final Entry<K, V> entry) {
        Preconditions.checkNotNull(entry);
        boolean isExclusiveAdded;
        CloseableReference<V> oldRefToClose;
        synchronized (this) {
            //减少该条目客户端应用数值
            decreaseClientCount(entry);
            //判断是否需要添加到 待销毁条目Map 中
            isExclusiveAdded = maybeAddToExclusives(entry);

            oldRefToClose = referenceToClose(entry);
        }
        //关闭这个客户端引用
        CloseableReference.closeSafely(oldRefToClose);
        //如果需要添加到 待销毁条目Map 中，就去调用
        maybeNotifyExclusiveEntryInsertion(isExclusiveAdded ? entry : null);

        maybeUpdateCacheParams();
        maybeEvictEntries();
    }

    /**
     * 将条目添加到 待销毁条目Map 中，如果这是可行的
     * Adds the entry to the exclusively owned queue if it is viable for eviction. */
    private synchronized boolean maybeAddToExclusives(Entry<K, V> entry) {
        if (!entry.isOrphan && entry.clientCount == 0) {
            mExclusiveEntries.put(entry.key, entry);
            return true;
        }
        return false;
    }

    /**
     *
     * Gets the value with the given key to be reused, or null if there is no such value.
     * <p> The item can be reused only if it is exclusively owned by the cache.
     */
    @Nullable
    public CloseableReference<V> reuse(K key) {
        Preconditions.checkNotNull(key);
        CloseableReference<V> clientRef = null;
        boolean removed = false;
        Entry<K, V> oldExclusive = null;
        synchronized (this) {
            oldExclusive = mExclusiveEntries.remove(key);
            if (oldExclusive != null) {
                Entry<K, V> entry = mCachedEntries.remove(key);
                Preconditions.checkNotNull(entry);
                Preconditions.checkState(entry.clientCount == 0);
                // optimization: instead of cloning and then closing the original reference,
                // we just do a move
                clientRef = entry.valueRef;
                removed = true;
            }
        }
        if (removed) {
            maybeNotifyExclusiveEntryRemoval(oldExclusive);
        }
        return clientRef;
    }

    /**
     * 销毁所有的被Predicate指定的条目
     * Removes all the items from the cache whose key matches the specified predicate.
     *
     * @param predicate returns true if an item with the given key should be removed
     * @return number of the items removed from the cache
     */
    public int removeAll(Predicate<K> predicate) {
        ArrayList<Entry<K, V>> oldExclusives;
        ArrayList<Entry<K, V>> oldEntries;
        synchronized (this) {
            oldExclusives = mExclusiveEntries.removeAll(predicate);
            oldEntries = mCachedEntries.removeAll(predicate);
            makeOrphans(oldEntries);
        }
        maybeClose(oldEntries);
        maybeNotifyExclusiveEntryRemoval(oldExclusives);
        maybeUpdateCacheParams();
        maybeEvictEntries();
        return oldEntries.size();
    }

    /**
     * 清空条目
     * Removes all the items from the cache. */
    public void clear() {
        ArrayList<Entry<K, V>> oldExclusives;
        ArrayList<Entry<K, V>> oldEntries;
        synchronized (this) {
            oldExclusives = mExclusiveEntries.clear();
            oldEntries = mCachedEntries.clear();
            makeOrphans(oldEntries);
        }
        maybeClose(oldEntries);
        maybeNotifyExclusiveEntryRemoval(oldExclusives);
        maybeUpdateCacheParams();
    }

    /**
     * 查找所有条目如果能匹配上指定的Predicate则返回true
     * Check if any items from the cache whose key matches the specified predicate.
     *
     * @param predicate returns true if an item with the given key matches
     * @return true is any items matches from the cache
     */
    @Override
    public synchronized boolean contains(Predicate<K> predicate) {
        return !mCachedEntries.getMatchingEntries(predicate).isEmpty();
    }

    /**
     * 当系统内存已经很少的时候，削减内存缓存
     * Trims the cache according to the specified trimming strategy and the given trim type. */
    @Override
    public void trim(MemoryTrimType trimType) {
        ArrayList<Entry<K, V>> oldEntries;
        final double trimRatio = mCacheTrimStrategy.getTrimRatio(trimType);
        synchronized (this) {
            int targetCacheSize = (int) (mCachedEntries.getSizeInBytes() * (1 - trimRatio));
            int targetEvictionQueueSize = Math.max(0, targetCacheSize - getInUseSizeInBytes());
            oldEntries = trimExclusivelyOwnedEntries(Integer.MAX_VALUE, targetEvictionQueueSize);
            makeOrphans(oldEntries);
        }
        maybeClose(oldEntries);
        maybeNotifyExclusiveEntryRemoval(oldEntries);
        maybeUpdateCacheParams();
        maybeEvictEntries();
    }

    /**
     * 根据给定的时间去修改更新缓存配置
     * Updates the cache params (constraints) if enough time has passed since the last update.
     */
    private synchronized void maybeUpdateCacheParams() {
        if (mLastCacheParamsCheck + PARAMS_INTERCHECK_INTERVAL_MS > SystemClock.uptimeMillis()) {
            return;
        }
        mLastCacheParamsCheck = SystemClock.uptimeMillis();
        mMemoryCacheParams = mMemoryCacheParamsSupplier.get();
    }

    /**
     * 销毁条目直至目前的缓存满足约束，这里删除的是mExclusiveEntries中的条目，
     * 因为其用的是LinkedHashMap，所以只要按顺序删除，那么就是按LRU算法删除
     * 而且一般情况下没有使用的条目比正在使用的条目多许多。
     * Removes the exclusively owned items until the cache constraints are met.
     *
     * <p> This method invokes the external {@link CloseableReference#close} method,
     * so it must not be called while holding the <code>this</code> lock.
     */
    private void maybeEvictEntries() {
        ArrayList<Entry<K, V>> oldEntries;
        synchronized (this) {
            int maxCount = Math.min(
                    mMemoryCacheParams.maxEvictionQueueEntries,
                    mMemoryCacheParams.maxCacheEntries - getInUseCount());
            int maxSize = Math.min(
                    mMemoryCacheParams.maxEvictionQueueSize,
                    mMemoryCacheParams.maxCacheSize - getInUseSizeInBytes());
            oldEntries = trimExclusivelyOwnedEntries(maxCount, maxSize);
            makeOrphans(oldEntries);
        }
        maybeClose(oldEntries);
        maybeNotifyExclusiveEntryRemoval(oldEntries);
    }

    /**
     * 削减内存缓存的具体方法，按顺序从mExclusiveEntries中删除条目，直至符合限制
     * Removes the exclusively owned items until there is at most <code>count</code> of them
     * and they occupy no more than <code>size</code> bytes.
     *
     * <p> This method returns the removed items instead of actually closing them, so it is safe to
     * be called while holding the <code>this</code> lock.
     */
    @Nullable
    private synchronized ArrayList<Entry<K, V>> trimExclusivelyOwnedEntries(int count, int size) {
        count = Math.max(count, 0);
        size = Math.max(size, 0);
        // fast path without array allocation if no eviction is necessary
        if (mExclusiveEntries.getCount() <= count && mExclusiveEntries.getSizeInBytes() <= size) {
            return null;
        }
        ArrayList<Entry<K, V>> oldEntries = new ArrayList<>();
        while (mExclusiveEntries.getCount() > count || mExclusiveEntries.getSizeInBytes() > size) {
            K key = mExclusiveEntries.getFirstKey();
            mExclusiveEntries.remove(key);
            oldEntries.add(mCachedEntries.remove(key));
        }
        return oldEntries;
    }

    /**
     * 关闭一系列CloseableReference引用
     * Notifies the client that the cache no longer tracks the given items.
     *
     * <p> This method invokes the external {@link CloseableReference#close} method,
     * so it must not be called while holding the <code>this</code> lock.
     */
    private void maybeClose(@Nullable ArrayList<Entry<K, V>> oldEntries) {
        if (oldEntries != null) {
            for (Entry<K, V> oldEntry : oldEntries) {
                CloseableReference.closeSafely(referenceToClose(oldEntry));
            }
        }
    }
    //通知一系列Entry可能发生状态变化
    private void maybeNotifyExclusiveEntryRemoval(@Nullable ArrayList<Entry<K, V>> entries) {
        if (entries != null) {
            for (Entry<K, V> entry : entries) {
                maybeNotifyExclusiveEntryRemoval(entry);
            }
        }
    }

    private static <K, V> void maybeNotifyExclusiveEntryRemoval(@Nullable Entry<K, V> entry) {
        if (entry != null && entry.observer != null) {
            entry.observer.onExclusivityChanged(entry.key, false);
        }
    }

    private static <K, V> void maybeNotifyExclusiveEntryInsertion(@Nullable Entry<K, V> entry) {
        if (entry != null && entry.observer != null) {
            entry.observer.onExclusivityChanged(entry.key, true);
        }
    }

    /** Marks the given entries as orphans. */
    private synchronized void makeOrphans(@Nullable ArrayList<Entry<K, V>> oldEntries) {
        if (oldEntries != null) {
            for (Entry<K, V> oldEntry : oldEntries) {
                makeOrphan(oldEntry);
            }
        }
    }

    /** Marks the entry as orphan. */
    private synchronized void makeOrphan(Entry<K, V> entry) {
        Preconditions.checkNotNull(entry);
        Preconditions.checkState(!entry.isOrphan);
        entry.isOrphan = true;
    }

    /** Increases the entry's client count. */
    private synchronized void increaseClientCount(Entry<K, V> entry) {
        Preconditions.checkNotNull(entry);
        Preconditions.checkState(!entry.isOrphan);
        entry.clientCount++;
    }

    /** Decreases the entry's client count. */
    private synchronized void decreaseClientCount(Entry<K, V> entry) {
        Preconditions.checkNotNull(entry);
        Preconditions.checkState(entry.clientCount > 0);
        entry.clientCount--;
    }

    /** Returns the value reference of the entry if it should be closed, null otherwise. */
    @Nullable
    private synchronized CloseableReference<V> referenceToClose(Entry<K, V> entry) {
        Preconditions.checkNotNull(entry);
        return (entry.isOrphan && entry.clientCount == 0) ? entry.valueRef : null;
    }

    /** Gets the total number of all currently cached items. */
    public synchronized int getCount() {
        return mCachedEntries.getCount();
    }

    /** Gets the total size in bytes of all currently cached items. */
    public synchronized int getSizeInBytes() {
        return mCachedEntries.getSizeInBytes();
    }

    /** Gets the number of the cached items that are used by at least one client. */
    public synchronized int getInUseCount() {
        return mCachedEntries.getCount() - mExclusiveEntries.getCount();
    }

    /** Gets the total size in bytes of the cached items that are used by at least one client. */
    public synchronized int getInUseSizeInBytes() {
        return mCachedEntries.getSizeInBytes() - mExclusiveEntries.getSizeInBytes();
    }

    /** Gets the number of the exclusively owned items. */
    public synchronized int getEvictionQueueCount() {
        return mExclusiveEntries.getCount();
    }

    /** Gets the total size in bytes of the exclusively owned items. */
    public synchronized int getEvictionQueueSizeInBytes() {
        return mExclusiveEntries.getSizeInBytes();
    }
}
