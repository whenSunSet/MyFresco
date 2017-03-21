package com.facebook.imagepipeline.producers.base;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.imagepipeline.producers.ProducerContextCallbacks;

/**
 * 空的{@link ProducerContextCallbacks}实现
 * Empty implementation of {@link ProducerContextCallbacks}.
 */
public class BaseProducerContextCallbacks implements ProducerContextCallbacks {

    @Override
    public void onCancellationRequested() {
    }

    @Override
    public void onIsPrefetchChanged() {
    }

    @Override
    public void onIsIntermediateResultExpectedChanged() {
    }

    @Override
    public void onPriorityChanged() {
    }
}

