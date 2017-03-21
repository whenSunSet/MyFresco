package com.facebook.commom.executors;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

import java.util.concurrent.ExecutorService;

/**
 * 串行执行任务的接口，他的任务遵循先进先出
 */
public interface SerialExecutorService extends ExecutorService {
}
