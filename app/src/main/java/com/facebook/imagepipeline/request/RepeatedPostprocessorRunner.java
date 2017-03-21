package com.facebook.imagepipeline.request;

/**
 * Created by heshixiyang on 2017/3/10.
 */
/**
 * 这个实例用于运行 postprocessor当客户端请求的时候
 * An instance of this class is used to run a postprocessor whenever the client requires.
 */
public interface RepeatedPostprocessorRunner {

    void update();
}
