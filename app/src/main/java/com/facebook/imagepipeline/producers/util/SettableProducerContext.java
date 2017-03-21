package com.facebook.imagepipeline.producers.util;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.producers.ProducerListener;
import com.facebook.imagepipeline.producers.base.BaseProducerContext;
import com.facebook.imagepipeline.request.impl.ImageRequest;

import javax.annotation.concurrent.ThreadSafe;

/**
 * ProducerContext that allows the client to change its internal state.
 */
@ThreadSafe
public class SettableProducerContext extends BaseProducerContext {

    public SettableProducerContext(
            ImageRequest imageRequest,
            String id,
            ProducerListener producerListener,
            Object callerContext,
            ImageRequest.RequestLevel lowestPermittedRequestLevel,
            boolean isPrefetch,
            boolean isIntermediateResultExpected,
            Priority priority) {
        super(
                imageRequest,
                id,
                producerListener,
                callerContext,
                lowestPermittedRequestLevel,
                isPrefetch,
                isIntermediateResultExpected,
                priority);
    }

    /**
     * Set whether the request is a prefetch request or not.
     * @param isPrefetch
     */
    public void setIsPrefetch(boolean isPrefetch) {
        BaseProducerContext.callOnIsPrefetchChanged(setIsPrefetchNoCallbacks(isPrefetch));
    }

    /**
     * Set whether intermediate result is expected or not
     * @param isIntermediateResultExpected
     */
    public void setIsIntermediateResultExpected(boolean isIntermediateResultExpected) {
        BaseProducerContext.callOnIsIntermediateResultExpectedChanged(
                setIsIntermediateResultExpectedNoCallbacks(isIntermediateResultExpected));
    }

    /**
     * Set the priority of the request
     * @param priority
     */
    public void setPriority(Priority priority) {
        BaseProducerContext.callOnPriorityChanged(setPriorityNoCallbacks(priority));
    }

}
