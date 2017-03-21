package com.facebook.commom.memory;

/**
 * Created by heshixiyang on 2017/3/9.
 */
/**
 * 可以用来接收MemoryTrimmableRegistry发出的修减内存缓存的请求的接口
 */

public interface MemoryTrimmable {

    /**
     * Trim memory.
     */
    void trim(MemoryTrimType trimType);
}
