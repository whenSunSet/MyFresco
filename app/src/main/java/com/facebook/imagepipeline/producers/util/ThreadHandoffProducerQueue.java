package com.facebook.imagepipeline.producers.util;

import com.facebook.commom.internal.Preconditions;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executor;

/**
 * Created by Administrator on 2017/3/17 0017.
 */
//这里的Executor使用了DefaultExecutorSupplier的mLightWeightBackgroundExecutor，只有一个线程的Executor。
public class ThreadHandoffProducerQueue {
    private boolean mQueueing = false;
    private final Deque<Runnable> mRunnableList;
    private final Executor mExecutor;

    public ThreadHandoffProducerQueue(Executor executor) {
        mExecutor = Preconditions.checkNotNull(executor);
        mRunnableList = new ArrayDeque<>();
    }

    public synchronized void addToQueueOrExecute(Runnable runnable) {
        if (mQueueing) {
            mRunnableList.add(runnable);
        } else {
            mExecutor.execute(runnable);
        }
    }

    public synchronized void startQueueing() {
        mQueueing = true;
    }

    public synchronized void stopQueuing() {
        mQueueing = false;
        execInQueue();
    }

    private void execInQueue() {
        while (!mRunnableList.isEmpty()) {
            mExecutor.execute(mRunnableList.pop());
        }
        mRunnableList.clear();
    }

    public synchronized void remove(Runnable runnable) {
        mRunnableList.remove(runnable);
    }

    public synchronized boolean isQueueing() {
        return mQueueing;
    }
}
