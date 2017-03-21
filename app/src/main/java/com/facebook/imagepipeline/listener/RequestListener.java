package com.facebook.imagepipeline.listener;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import com.facebook.imagepipeline.producers.ProducerListener;
import com.facebook.imagepipeline.request.impl.ImageRequest;

/**
 * {@link ImageRequest}的监听器
 * Listener for {@link ImageRequest}.
 */
public interface RequestListener extends ProducerListener {
    /**
     *
     * 当请求被提交到线程池的请求队列的时候调用
     * Called when request is about to be submitted to the Orchestrator's executor queue.
     * @param request which triggered the event
     * @param callerContext context of the caller of the request
     * @param requestId unique id generated automatically for each request submission
     * @param isPrefetch 是否请求 是预读文件
     */
    void onRequestStart(
            ImageRequest request,
            Object callerContext,
            String requestId,
            boolean isPrefetch);

    /**
     * 当完成了请求并成功了(所有的producers都成功的完成了)
     * Called after successful completion of the request (all producers completed successfully).
     * @param request which triggered the event
     * @param requestId unique id generated automatically for each request submission
     * @param isPrefetch whether the request is a prefetch or not
     */
    void onRequestSuccess(ImageRequest request, String requestId, boolean isPrefetch);

    /**
     * 当请求失败的时候调用(有一些producer失败的)
     * Called after failure to complete the request (some producer failed).
     * @param request which triggered the event
     * @param requestId unique id generated automatically for each request submission
     * @param throwable cause of failure
     * @param isPrefetch whether the request is a prefetch or not
     */
    void onRequestFailure(
            ImageRequest request,
            String requestId,
            Throwable throwable,
            boolean isPrefetch);

    /**
     * 当请求取消的时候被调用
     * Called after the request is cancelled.
     * @param requestId unique id generated automatically for each request submission
     */
    void onRequestCancellation(String requestId);
}
