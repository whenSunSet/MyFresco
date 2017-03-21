package com.facebook.imagepipeline.memory.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.internal.VisibleForTesting;

import java.util.LinkedList;
import java.util.Queue;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * 这个类在{@link BasePool}中使用，该pool以<Integer,Bucket>的键值对的形式，维持一系列不固定长度的buckets，
 * 这里的Integer就是该Bucket中每个结点所分配的内存大小，由于内存是一个大数，所以Pool中使用了稀疏数组
 * 使用这样可以方便的使用二分法查找内存。
 * 每一个bucket维持一个LinkedList，当Pool调用{@link BasePool#(Object)}的时候，会将稀疏数组初始化
 * 成为一个适当的bucket列表。
 * {@link #get()}方法可以返回一个空的V，如果本Bucket已经没有更多的空V，会返回null
 * {@link #release(Object)}方法可以释放传入的对象，然后将该对象初始化成空的V，放入队列之中
 * The Bucket is a constituent class of {@link BasePool}. The pool maintains its free values
 * in a set of buckets, where each bucket represents a set of values of the same 'size'.
 * <p>
 * Each bucket maintains a freelist of values.
 * When the pool receives a {@link BasePool#(Object)} request for a particular size, it finds the
 * appropriate bucket, and delegates the request to the bucket ({@link #get()}.
 * If the bucket's freelist is  non-empty, then one of the entries on the freelist is returned (and
 * removed from the freelist).
 * Similarly, when a value is released to the pool via a call to {@link BasePool#release(Object)},
 * the pool locates the appropriate bucket and returns the value to the bucket's freelist - see
 * ({@link #release(Object)}
 * <p>
 * The bucket also maintains the current number of items (from this bucket) that are "in use" i.e.
 * values that came from this bucket, but are now in use by the caller, and no longer on the
 * freelist.
 * The 'length' of the bucket is the number of values from this bucket that are currently in use
 * (mInUseCount), plus the size of the freeList. The maxLength of the bucket is that maximum length
 * that this bucket should grow to - and is used by the pool to determine whether values should
 * be released to the bucket ot freed.
 * @param <V> type of values to be 'stored' in the bucket
 */
@NotThreadSafe
@VisibleForTesting
class Bucket<V> {
    //该Bucket的每个V的被分配的内存大小，也就是Pool中在稀疏数组中的下标
    public final int mItemSize; // size in bytes of items in this bucket
    //该Bucket所能分配的最大V的数量
    public final int mMaxLength; // 'max' length for this bucket
    //储存V的队列
    final Queue mFreeList; // the free list for this bucket, subclasses can vary type
    //现在Bucket已经用掉的V的数量
    private int mInUseLength; // current number of entries 'in use' (i.e.) not in the free list

    /**
     * Constructs a new Bucket instance. The constructed bucket will have an empty freelist
     * @param itemSize size in bytes of each item in this bucket
     * @param maxLength max length for the bucket (used + free)
     * @param inUseLength current in-use-length for the bucket
     */
    public Bucket(int itemSize, int maxLength, int inUseLength) {
        Preconditions.checkState(itemSize > 0);
        Preconditions.checkState(maxLength >= 0);
        Preconditions.checkState(inUseLength >= 0);

        mItemSize = itemSize;
        mMaxLength = maxLength;
        mFreeList = new LinkedList();
        mInUseLength = inUseLength;
    }

    /**
     * 如果 当前使用的V+没有使用的V 超过了 最大的长度返回true
     * Determines if the current length of the bucket (free + used) exceeds the max length
     * specified
     */
    public boolean isMaxLengthExceeded() {
        return (mInUseLength + getFreeListSize() > mMaxLength);
    }

    int getFreeListSize() {
        return mFreeList.size();
    }

    /**
     * 获取一个空的V，如果队列中还有的话
     * Gets a free item if possible from the freelist. Returns null if the free list is empty
     * Updates the bucket inUse count
     * @return an item from the free list, if available
     */
    @Nullable
    public V get() {
        V value = pop();
        if (value != null) {
            mInUseLength++;
        }
        return value;
    }

    /**
     * 将队列中的V移除，并使用
     * Remove the first item (if any) from the freelist. Returns null if the free list is empty
     * Does not update the bucket inUse count
     * @return the first value (if any) from the free list
     */
    @Nullable
    public V pop() {
        return (V) mFreeList.poll();
    }

    /**
     * Increment the mInUseCount field.
     * Used by the pool to update the bucket info when a value was 'alloc'ed (because no free value
     * was available)
     */
    public void incrementInUseCount() {
        mInUseLength++;
    }

    /**
     * 将传入的V放回到队列之后，以供后续使用
     * Releases a value to this bucket and decrements the inUse count
     * @param value the value to release
     */
    public void release(V value) {
        Preconditions.checkNotNull(value);
        Preconditions.checkState(mInUseLength > 0);
        mInUseLength--;
        addToFreeList(value);
    }

    void addToFreeList(V value) {
        mFreeList.add(value);
    }

    /**
     * Decrement the mInUseCount field.
     * Used by the pool to update the bucket info when a value was freed, instead of being returned
     * to the bucket's free list
     */
    public void decrementInUseCount() {
        Preconditions.checkState(mInUseLength > 0);
        mInUseLength--;
    }

    public int getInUseCount() {
        return mInUseLength;
    }
}
