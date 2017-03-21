package com.facebook.imagepipeline.listener.impl;

import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.request.impl.ImageRequest;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by heshixiyang on 2017/3/17.
 */
public class BaseRequestListener implements RequestListener {

    @Override
    public void onRequestStart(
            ImageRequest request, Object callerContext, String requestId, boolean isPrefetch) {
    }

    @Override
    public void onRequestSuccess(
            ImageRequest request, String requestId, boolean isPrefetch) {
    }

    @Override
    public void onRequestFailure(
            ImageRequest request, String requestId, Throwable throwable, boolean isPrefetch) {
    }

    @Override
    public void onRequestCancellation(String requestId) {
    }

    @Override
    public void onProducerStart(String requestId, String producerName) {
    }

    @Override
    public void onProducerEvent(String requestId, String producerName, String eventName) {
    }

    @Override
    public void onProducerFinishWithSuccess(
            String requestId, String producerName, @Nullable Map<String, String> extraMap) {
    }

    @Override
    public void onProducerFinishWithFailure(
            String requestId,
            String producerName,
            Throwable t,
            @Nullable Map<String, String> extraMap) {
    }

    @Override
    public void onProducerFinishWithCancellation(
            String requestId, String producerName, @Nullable Map<String, String> extraMap) {
    }

    @Override
    public boolean requiresExtraMap(String requestId) {
        return false;
    }
}
