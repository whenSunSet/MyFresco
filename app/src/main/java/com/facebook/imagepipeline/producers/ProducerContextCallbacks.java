package com.facebook.imagepipeline.producers;

/**
 * Created by heshixiyang on 2017/3/9.
 */
/**
 * Callbacks that are called when something changes in a request sequence.
 */
public interface ProducerContextCallbacks {

    /**
     * 客户端取消请求的时候回调
     * Method that is called when a client cancels the request.
     */
    void onCancellationRequested();

    /**
     * 在一个请求的 预取 发生变化的时候回调
     * Method that is called when a request is no longer a prefetch, or vice versa.
     */
    void onIsPrefetchChanged();

    /**
     * 当中间结果的预期发生改变的时候调用
     * Method that is called when intermediate results start or stop being expected.
     */
    void onIsIntermediateResultExpectedChanged();

    /**
     * 当优先级发生变化的时候回调
     * Method that is called when the priority of the request changes.
     */
    void onPriorityChanged();
}
