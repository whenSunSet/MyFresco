package com.facebook.imagepipeline.request;

/**
 * Created by heshixiyang on 2017/3/10.
 */
/**
 * 使用这个接口的一个实例去执行post-process操作的时候可能执行多次
 * Use an instance of this interface to perform post-process operations that must be performed
 * more than once.
 */
public interface RepeatedPostprocessor extends Postprocessor {

    /**
     * 这个回调使用在当客户端请求的时候运行{@code PostProcessor#process}这个方法
     * Callback used to pass the postprocessor a reference to the object that will run the
     * postprocessor's {@code PostProcessor#process} method when the client requires.
     * @param runner
     */
    void setCallback(RepeatedPostprocessorRunner runner);
}
