package com.facebook.imagepipeline.producers;

/**
 * Created by heshixiyang on 2017/3/10.
 */
/**
 * 是在image pipeline中构建image获取过程中的基本构架
 * Building block for image processing in the image pipeline.
 *
 * 在执行image请求的过程之中包括了不同的任务：比如网络获取、硬盘缓存、内存缓存、解码等操作
 * Producer代表一个单独的任务，这个任务的result是T，将一整个请求分成若干块能让我们自由的给
 * 整个任务添加不同的模块。
 * <p> Execution of image request consists of multiple different tasks such as network fetch,
 * disk caching, memory caching, decoding, applying transformations etc. Producer<T> represents
 * single task whose result is an instance of T. Breaking entire request into sequence of
 * Producers allows us to construct different requests while reusing the same blocks.
 *
 * Producer支持多返回值和流
 * <p> Producer supports multiple values and streaming.
 *
 * @param <T>
 */
public interface Producer<T> {

    /**
     * 开始一个任务对被给予的context，提供一个consumer以供过程结束之后处理数据。
     * Start producing results for given context. Provided consumer is notified whenever progress is
     * made (new value is ready or error occurs).
     * @param consumer
     * @param context
     */
    void produceResults(Consumer<T> consumer, ProducerContext context);
}
