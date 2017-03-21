package com.facebook.cache.disk.impl;

/**
 * Created by Administrator on 2017/3/11 0011.
 */

import android.support.annotation.VisibleForTesting;

import com.facebook.cache.disk.DiskStorage;
import com.facebook.cache.disk.EntryEvictionComparator;
import com.facebook.cache.disk.EntryEvictionComparatorSupplier;

/**
 * 清除缓存项基于它们的大小和时间戳，需要设置age和size的权重，最后比价总分的大小
 * Evicts cache items based on a mix of their size and timestamp.
 */
public class ScoreBasedEvictionComparatorSupplier implements EntryEvictionComparatorSupplier {

    private final float mAgeWeight;
    private final float mSizeWeight;

    public ScoreBasedEvictionComparatorSupplier(float ageWeight, float sizeWeight) {
        mAgeWeight = ageWeight;
        mSizeWeight = sizeWeight;
    }

    @Override
    public EntryEvictionComparator get() {
        return new EntryEvictionComparator() {

            long now = System.currentTimeMillis();

            /**
             * Return <0 if lhs should be evicted before rhs.
             */
            @Override
            public int compare(DiskStorage.Entry lhs, DiskStorage.Entry rhs) {
                float score1 = calculateScore(lhs, now);
                float score2 = calculateScore(rhs, now);
                return score1 < score2 ? 1 : ((score2 == score1) ? 0 : -1);
            }
        };
    }

    /**
     * Calculates an eviction score.
     *
     * Entries with a higher eviction score should be evicted first.
     */
    @VisibleForTesting
    float calculateScore(DiskStorage.Entry entry, long now) {
        long ageMs = now - entry.getTimestamp();
        long bytes = entry.getSize();
        return mAgeWeight * ageMs + mSizeWeight * bytes;
    }
}
