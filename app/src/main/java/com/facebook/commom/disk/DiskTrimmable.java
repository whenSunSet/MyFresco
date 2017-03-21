package com.facebook.commom.disk;

/**
 * Created by Administrator on 2017/3/8 0008.
 */
/**
 * Any class that uses a lot of disk space and should implement this interface.
 * 任何使用了大量硬盘空间的类应该实现这个接口
 */
public interface DiskTrimmable {
    /**
     * 当硬盘空间很小的时候被调用
     */
    void trimToMinimum();

    /**
     * 当硬盘空间已经接近没有的时候被调用，这个app已经快要crash了
     */
    void trimToNothing();
}
