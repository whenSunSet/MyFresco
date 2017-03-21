package com.facebook.cache.disk.impl;

import com.facebook.cache.disk.DiskStorage;
import com.facebook.cache.disk.EntryEvictionComparator;
import com.facebook.cache.disk.EntryEvictionComparatorSupplier;

/**
 * Created by Administrator on 2017/3/11 0011.
 */
//默认的基于时间戳清理缓存的比较器
public class DefaultEntryEvictionComparatorSupplier implements EntryEvictionComparatorSupplier {

    @Override
    public EntryEvictionComparator get() {
        return new EntryEvictionComparator() {
            @Override
            public int compare(DiskStorage.Entry e1, DiskStorage.Entry e2) {
                long time1 = e1.getTimestamp();
                long time2 = e2.getTimestamp();
                return time1 < time2 ? -1 : ((time2 == time1) ? 0 : 1);
            }
        };
    }
}
