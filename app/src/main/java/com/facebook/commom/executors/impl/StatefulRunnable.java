package com.facebook.commom.executors.impl;

/**
 * Created by heshixiyang on 2017/3/12.
 */

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 表示抽象的计算任务
 * Abstraction for computation.
 *
 * StatefulRunnable表示可以计算可以取消，但是取消只能在没有开始的时候
 * <p> Computation expressed as StatefulRunnable can be cancelled, but only if it has not
 * started yet.
 * 为了更好的解耦代码的计算结果和处理它的代码,提供单独的四个方法:getResult, onSuccess, onFailure and onCancellation.
 * <p> For better decoupling of the code computing the result and the code that handles it, 4
 * separate methods are provided: getResult, onSuccess, onFailure and onCancellation.
 *
 * 这个可运行可以只运行一次。后续调用run方法不会产生任何影响。
 * <p> This runnable can be run only once. Subsequent calls to run method won't have any effect.
 */
abstract public class StatefulRunnable<T> implements Runnable {
    protected static final int STATE_CREATED = 0;
    protected static final int STATE_STARTED = 1;
    protected static final int STATE_CANCELLED = 2;
    protected static final int STATE_FINISHED = 3;
    protected static final int STATE_FAILED = 4;

    protected final AtomicInteger mState;

    public StatefulRunnable() {
        mState = new AtomicInteger(STATE_CREATED);
    }

    @Override
    public final void run() {
        if (!mState.compareAndSet(STATE_CREATED, STATE_STARTED)) {
            return;
        }
        T result;
        try {
            result = getResult();
        } catch (Exception e) {
            mState.set(STATE_FAILED);
            onFailure(e);
            return;
        }

        mState.set(STATE_FINISHED);
        try {
            onSuccess(result);
        } finally {
            disposeResult(result);
        }
    }

    public void cancel() {
        if (mState.compareAndSet(STATE_CREATED, STATE_CANCELLED)) {
            onCancellation();
        }
    }

    /**
     * 成功的时候调用
     * Called after computing result successfully.
     * @param result
     */
    protected void onSuccess(T result) {}

    /**
     * 计算时抛出异常的时候调用
     * Called if exception occurred during computation.
     * @param e
     */
    protected void onFailure(Exception e) {}

    /**
     * 取消的时候调用
     * Called when the runnable is cancelled.
     */
    protected void onCancellation() {}

    /**
     * 为成功后回调完成为了处理结果。
     * Called after onSuccess callback completes in order to dispose the result.
     * @param result
     */
    protected void disposeResult(T result) {}

    abstract protected T getResult() throws Exception;
}
