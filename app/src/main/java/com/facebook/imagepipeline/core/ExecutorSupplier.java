package com.facebook.imagepipeline.core;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import java.util.concurrent.Executor;

/**
 * 实现了这个接口的class有责任提供不同的executors，以供image pipeline的不同阶段使用
 * Implementations of this interface are responsible for supplying the different executors
 * used by different sections of the image pipeline.
 *
 * 一个非常基本的实现将为四个操作只提供一个线程池
 * 建议{@link #forLocalStorageRead}和{@link #forLocalStorageWrite}最少使用不同的线程池
 * 他们的实现人最好是I/O型的而不是CPU型的
 * <p>A very basic implementation would supply a single thread pool for all four operations.
 * It is recommended that {@link #forLocalStorageRead} and {@link #forLocalStorageWrite} at least
 * be different, as their threads will be I/O-bound, rather than CPU-bound as the others are.
 *
 * 实现应该返回单例
 * <p>Implementations should return singleton objects from these methods.
 *
 * <p>{@see Executor}
 */
public interface ExecutorSupplier {

    /**
     * 这个线程池使用于所有的硬盘读操作，无论是硬盘缓存还是本地文件读取
     * Executor used to do all disk reads, whether for disk cache or local files. */
    Executor forLocalStorageRead();

    /**
     *  这个线程池使用于所有的硬盘写操作，无论是硬盘缓存还是本地文件读取
     *  Executor used to do all disk writes, whether for disk cache or local files. */
    Executor forLocalStorageWrite();

    /**
     * 这个线程池用于所有的解码操作
     * Executor used for all decodes. */
    Executor forDecode();

    /**
     *  这个线程池用于后台任务例如image的转码，重设大小、旋转和被提交的任务
     *  Executor used for background tasks such as image transcoding, resizing, rotating and
     *  post processing.
     */
    Executor forBackgroundTasks();

    /**
     * Executor used for lightweight background operations, such as handing request off the
     * main thread.
     */
    Executor forLightweightBackgroundTasks();
}
