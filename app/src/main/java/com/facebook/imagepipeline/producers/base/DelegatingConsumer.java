package com.facebook.imagepipeline.producers.base;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.imagepipeline.producers.Consumer;

/**
 * 代理consumer
 * Delegating consumer.
 */
public abstract class DelegatingConsumer<I, O> extends BaseConsumer<I> {

    private final Consumer<O> mConsumer;

    public DelegatingConsumer(Consumer<O> consumer) {
        mConsumer = consumer;
    }

    public Consumer<O> getConsumer() {
        return mConsumer;
    }

    @Override
    protected void onFailureImpl(Throwable t) {
        mConsumer.onFailure(t);
    }

    @Override
    protected void onCancellationImpl() {
        mConsumer.onCancellation();
    }

    @Override
    protected void onProgressUpdateImpl(float progress) {
        mConsumer.onProgressUpdate(progress);
    }
}
