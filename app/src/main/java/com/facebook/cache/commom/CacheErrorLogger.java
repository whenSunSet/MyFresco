package com.facebook.cache.commom;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

import android.support.annotation.Nullable;

/**
 * 一个用于记录各种缓存错误的接口
 */
public interface CacheErrorLogger {

    /**
     * 一个用于记录所有缓存和储存错误的记录表
     */
    enum CacheErrorCategory {
        READ_DECODE,
        READ_FILE,
        READ_FILE_NOT_FOUND,
        READ_INVALID_ENTRY,

        WRITE_ENCODE,
        WRITE_CREATE_TEMPFILE,
        WRITE_UPDATE_FILE_NOT_FOUND,
        WRITE_RENAME_FILE_TEMPFILE_NOT_FOUND,
        WRITE_RENAME_FILE_TEMPFILE_PARENT_NOT_FOUND,
        WRITE_RENAME_FILE_OTHER,
        WRITE_CREATE_DIR,
        WRITE_CALLBACK_ERROR,
        WRITE_INVALID_ENTRY,

        DELETE_FILE,

        EVICTION,
        GENERIC_IO,
        OTHER
    }

    /**
     * 记录一个错误对于一个特别的 类目
     * @param category 错误的类目
     * @param clazz Class 被报告的错误
     * @param message 一个可选的错误消息
     * @param throwable 一个可选的异常
     */
    void logError(
            CacheErrorCategory category,
            Class<?> clazz,
            String message,
            @Nullable Throwable throwable);
}
