package com.facebook.commom.executors;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * An {@link ExecutorService} that is backed by a handler.
 */
public interface HandlerExecutorService extends ScheduledExecutorService {

    /**
     * 关闭Handler
     */
    void quit();

    /**
     * 检查我们目前是不是在Handler的线程在这个HandlerExecutorService中
     * Check if we are currently in the handler thread of this HandlerExecutorService.
     */
    boolean isHandlerThread();
}
