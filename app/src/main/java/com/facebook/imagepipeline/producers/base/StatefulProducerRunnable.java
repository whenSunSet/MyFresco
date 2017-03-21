package com.facebook.imagepipeline.producers.base;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.commom.executors.impl.StatefulRunnable;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.ProducerListener;

import java.util.Map;

/**
 * {@link StatefulRunnable}的实现，在各个producers中被实现为匿名的内部类
 * {@link StatefulRunnable} intended to be used by producers.
 *
 * <p> Class implements common functionality related to handling producer instrumentation and
 * resource management.
 */
public abstract class StatefulProducerRunnable<T>
        extends StatefulRunnable<T> {

    private final Consumer<T> mConsumer;
    private final ProducerListener mProducerListener;
    private final String mProducerName;
    private final String mRequestId;

    public StatefulProducerRunnable(
            Consumer<T> consumer,
            ProducerListener producerListener,
            String producerName,
            String requestId) {
        mConsumer = consumer;
        mProducerListener = producerListener;
        mProducerName = producerName;
        mRequestId = requestId;

        mProducerListener.onProducerStart(mRequestId, mProducerName);
    }

    @Override
    protected void onSuccess(T result) {
        mProducerListener.onProducerFinishWithSuccess(
                mRequestId,
                mProducerName,
                mProducerListener.requiresExtraMap(mRequestId) ? getExtraMapOnSuccess(result) : null);
        mConsumer.onNewResult(result, true);
    }

    @Override
    protected void onFailure(Exception e) {
        mProducerListener.onProducerFinishWithFailure(
                mRequestId,
                mProducerName,
                e,
                mProducerListener.requiresExtraMap(mRequestId) ? getExtraMapOnFailure(e) : null);
        mConsumer.onFailure(e);
    }

    @Override
    protected void onCancellation() {
        mProducerListener.onProducerFinishWithCancellation(
                mRequestId,
                mProducerName,
                mProducerListener.requiresExtraMap(mRequestId) ? getExtraMapOnCancellation() : null);
        mConsumer.onCancellation();
    }

    /**
     * Create extra map for result
     */
    protected Map<String, String> getExtraMapOnSuccess(T result) {
        return null;
    }

    /**
     * Create extra map for exception
     */
    protected Map<String, String> getExtraMapOnFailure(Exception exception) {
        return null;
    }

    /**
     * Create extra map for cancellation
     */
    protected Map<String, String> getExtraMapOnCancellation() {
        return null;
    }

    @Override
    protected abstract void disposeResult(T result);
}
