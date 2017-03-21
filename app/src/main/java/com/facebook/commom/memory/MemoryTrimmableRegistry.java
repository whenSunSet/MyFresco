package com.facebook.commom.memory;

/**
 * Created by heshixiyang on 2017/3/9.
 */
/**
 * 一个用于维护一个被接受内存事件的class列表的class
 *
 * 如果一个class使用了过多的内存并且需要从系统发出消息，那么他就需要实现MemoryTrimmable接口。
 */
public interface MemoryTrimmableRegistry {

    /** Register an object. */
    void registerMemoryTrimmable(MemoryTrimmable trimmable);

    /** Unregister an object. */
    void unregisterMemoryTrimmable(MemoryTrimmable trimmable);
}
