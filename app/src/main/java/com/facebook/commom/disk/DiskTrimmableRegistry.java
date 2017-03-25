package com.facebook.commom.disk;

/**
 * Created by Administrator on 2017/3/8 0008.
 */
/**
 * 一个让其他一系列class接收系统磁盘事件的类
 * 当需要削减磁盘使用量的时候，实现了这个接口的class应该通知所有已经注册过的trimmable。
 * */
public interface DiskTrimmableRegistry {

    /** Register an object. */
    void registerDiskTrimmable(DiskTrimmable trimmable);

    /** Unregister an object. */
    void unregisterDiskTrimmable(DiskTrimmable trimmable);
}
