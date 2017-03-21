package com.facebook.cache.disk;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

import java.util.Comparator;

/**
 * 定义一个比较器，比较哪个缓存应该被驱逐
 * Defines an order the items are being evicted from the cache.
 */
public interface EntryEvictionComparator extends Comparator<DiskStorage.Entry> {
}
