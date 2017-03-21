package com.facebook.imagepipeline.producers;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.request.impl.ImageRequest;

/**
 * 为Producer提供上下文数据
 * Used to pass context information to producers.
 *
 * 实现了这个接口的实体将被传递给所有参加这个pipeline的请求{@see Producer#produceResults}
 * 这个实体也有责任提示producer哪一个image需要被fetched/decoded/resized/cached 等等操作。
 * 这个实体也提供了一个请求的取消功能
 * <p> Object implementing this interface is passed to all producers participating in pipeline
 * request {@see Producer#produceResults}. Its responsibility is to instruct producers which image
 * should be fetched/decoded/resized/cached etc. This class also handles request cancellation.
 *
 * 为了受到请求被取消的通知，一个producer应该使用{@code runOnCancellationRequested}方法
 * 这个方法会在客户端取消请求的时候被调用，其保存了一个Runnable以便被执行
 * <p>  In order to be notified when cancellation is requested, a producer should use the
 * {@code runOnCancellationRequested} method which takes an instance of Runnable and executes it
 * when the pipeline client cancels the image request.
 */
public interface ProducerContext {

    /**
     * @return image request that is being executed
     */
    ImageRequest getImageRequest();

    /**
     * @return id of this request
     */
    String getId();

    /**
     * Peoducer的监听器，监听其事件
     * @return ProducerListener for producer's events
     */
    ProducerListener getListener();

    /**
     *
     * @return the {@link Object} that indicates the caller's context
     */
    Object getCallerContext();

    /**
     * 获取最低被允许的请求等级
     * @return the lowest permitted {@link ImageRequest.RequestLevel}
     */
    ImageRequest.RequestLevel getLowestPermittedRequestLevel();

    /**
     * 返回true，如果请求允许被预读
     * @return true if the request is a prefetch, false otherwise.
     */
    boolean isPrefetch();

    /**
     * 获取请求的优先级
     * @return priority of the request.
     */
    Priority getPriority();

    /**
     *
     * @return true if request's owner expects intermediate results
     */
    boolean isIntermediateResultExpected();

    /**
     * 添加一个监听器在各个执行点执行回调
     * Adds callbacks to the set of callbacks that are executed at various points during the
     * processing of a request.
     * @param callbacks callbacks to be executed
     */
    void addCallbacks(ProducerContextCallbacks callbacks);
}

