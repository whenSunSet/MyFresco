package com.facebook.imagepipeline.producers.processProducer;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.commom.internal.Preconditions;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.facebook.imagepipeline.producers.ProducerListener;
import com.facebook.imagepipeline.producers.util.ThreadHandoffProducerQueue;
import com.facebook.imagepipeline.producers.base.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.base.StatefulProducerRunnable;

/**
 * 将接下来的操作放入后台线程进行
 * Uses ExecutorService to move further computation to different thread
 */
public class ThreadHandoffProducer<T> implements Producer<T> {

    public static final String PRODUCER_NAME = "BackgroundThreadHandoffProducer";

    private final Producer<T> mInputProducer;
    private final ThreadHandoffProducerQueue mThreadHandoffProducerQueue;

    public ThreadHandoffProducer(final Producer<T> inputProducer,
                                 final  ThreadHandoffProducerQueue inputThreadHandoffProducerQueue) {
        mInputProducer = Preconditions.checkNotNull(inputProducer);
        mThreadHandoffProducerQueue = inputThreadHandoffProducerQueue;
    }

    @Override
    public void produceResults(final Consumer<T> consumer, final ProducerContext context) {
        final ProducerListener producerListener = context.getListener();
        final String requestId = context.getId();
        final StatefulProducerRunnable<T> statefulRunnable = new StatefulProducerRunnable<T>(
                consumer,
                producerListener,
                PRODUCER_NAME,
                requestId) {
            @Override
            protected void onSuccess(T ignored) {
                producerListener.onProducerFinishWithSuccess(requestId, PRODUCER_NAME, null);
                mInputProducer.produceResults(consumer, context);
            }

            @Override
            protected void disposeResult(T ignored) {}

            @Override
            protected T getResult() throws Exception {
                return null;
            }
        };
        context.addCallbacks(
                new BaseProducerContextCallbacks() {
                    @Override
                    public void onCancellationRequested() {
                        statefulRunnable.cancel();
                        mThreadHandoffProducerQueue.remove(statefulRunnable);
                    }
                });
        mThreadHandoffProducerQueue.addToQueueOrExecute(statefulRunnable);
    }
}

