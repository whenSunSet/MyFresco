package com.facebook.commom.executors.impl;

/**
 * Created by heshixiyang on 2017/3/12.
 */

import com.facebook.commom.executors.SerialExecutorService;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 一个{@link SerialExecutorService}的默认实现，是一个{@link Executor}的包装
 * Default implementation of {@link SerialExecutorService} that wraps an existing {@link Executor}.
 */
public class DefaultSerialExecutorService extends ConstrainedExecutorService
        implements SerialExecutorService {

    public DefaultSerialExecutorService(Executor executor) {
        // SerialExecutorService is just a ConstrainedExecutorService with a concurrency limit
        // of one and an unbounded work queue.
        super("SerialExecutor", 1, executor, new LinkedBlockingQueue<Runnable>());
    }

    /**
     * Synchronized override of {@link ConstrainedExecutorService#execute(Runnable)} to
     * ensure that view of memory is consistent between different threads executing tasks serially.
     * @param runnable The task to be executed.
     */
    @Override
    public synchronized void execute(Runnable runnable) {
        super.execute(runnable);
    }
}
