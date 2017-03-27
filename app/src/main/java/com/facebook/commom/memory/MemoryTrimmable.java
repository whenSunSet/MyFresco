package com.facebook.commom.memory;

/**
 * Created by heshixiyang on 2017/3/9.
 */
/**
 * 一个使用了很多内存的类需要实现这个接口，再当系统内存不够的时候
 * 接收来自MemoryTrimmableRegistry接口实现类的消息，以降低该对象使用的内存。
 */
public interface MemoryTrimmable {

    /**
     * 削减内存
     * Trim memory.
     */
    void trim(MemoryTrimType trimType);
}
