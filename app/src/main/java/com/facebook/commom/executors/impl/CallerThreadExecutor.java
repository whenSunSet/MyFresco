package com.facebook.commom.executors.impl;

/**
 * Created by heshixiyang on 2017/3/12.
 */

import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 一个executor service，其将每一个任务运行在其他线程上，通过 {@code execute/submit}调用
 * An executor service that runs each task in the thread that invokes {@code execute/submit}.
 *
 * 这个既适用于单独提交任务又适用与提交任务集合，通过{@code invokeAll} 或者 {@code invokeAny}
 * 在后一种情况下,任务将运行连续*调用线程，任务将在{@code Future}返回给调用者之前完成
 * <p> This applies both to individually submitted tasks and to collections of tasks submitted via
 * {@code invokeAll} or {@code invokeAny}. In the latter case, tasks will run serially on the
 * calling thread. Tasks are run to completion before a {@code Future} is returned to the caller.
 *
 * 这个实现与{@code ExecutorService}有所偏离，以下是{@code shutdownNow} 和 {@code awaitTermination}的规范
 * <p> The implementation deviates from the {@code ExecutorService} specification with regards to
 * the {@code shutdownNow} and {@code awaitTermination} methods.
 * 1.调用{@code shutdown} 或者 {@code shutdownNow}是空方法，调用{@code isTerminated}总是返回false
 * 2.调用{@code awaitTermination}总是立即返回true，返回true是为了避免客户端潜在的无限循环。
 * 3.“best-effort”关于取消运行任务被实现为“no-effort”，没有中断或其他方式阻止线程执行任务
 * 4.返回的list总是为空，因为任何提交的任务被认为已经开始执行。这也适用于任务给{@code invokeAll} or {@code invokeAny}
 * 等待串行执行,包括的任务还没有开始执行。
 * 1. A call to {@code shutdown} or {@code shutdownNow} is a no-op. A call to {@code isTerminated}
 *    always returns false.
 * 2. A call to {@code awaitTermination} always returns true immediately. True is returned in order
 *    to avoid potential infinite loop in the clients.
 * 3. "best-effort" with regards to canceling running tasks is implemented as "no-effort".
 *    No interrupts or other attempts are made to stop threads executing tasks.
 * 4. The returned list will always be empty, as any submitted task is considered to have started
 *    execution. This applies also to tasks given to {@code invokeAll} or {@code invokeAny} which
 *    are pending serial execution, including the tasks that have not yet started execution.
 */
public class CallerThreadExecutor extends AbstractExecutorService {

    private static final CallerThreadExecutor sInstance = new CallerThreadExecutor();

    public static CallerThreadExecutor getInstance() {
        return sInstance;
    }

    private CallerThreadExecutor() {
    }

    @Override
    public void execute(Runnable command) {
        command.run();
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public void shutdown() {
        // no-op
    }

    @Override
    public List<Runnable> shutdownNow() {
        shutdown();
        return Collections.emptyList();
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return true;
    }
}
