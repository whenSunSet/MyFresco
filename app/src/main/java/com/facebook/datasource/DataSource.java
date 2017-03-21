package com.facebook.datasource;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import java.util.concurrent.Executor;

/**
 * An alternative to Java Futures for the image pipeline.
 *
 * 和Futures不同，DataSource可以返回一系列结果，不仅仅是一个。一个典型的例子就是解码渐进式图片
 * <p>Unlike Futures, DataSource can issue a series of results, rather than just one. A prime
 * example is decoding progressive images, which have a series of intermediate results before the
 * final one.
 *
 * DataSources必须被关闭，否则会造成资源泄露
 * <p>DataSources MUST be closed (close() is called on the DataSource) else resources may leak.
 *
 *@param <T> the type of the result
 */
public interface DataSource<T> {

    /**
     * @return true if the data source is closed, false otherwise
     */
    boolean isClosed();

    /**
     * 最近的异步计算返回的结果
     * The most recent result of the asynchronous computation.
     *
     * 调用者获得对象的所有权并负责释放它。*请注意,后续调用得到的结果可能会给不同的结果。后来结果应该被认为是高质量的。
     * <p>The caller gains ownership of the object and is responsible for releasing it.
     * Note that subsequent calls to getResult might give different results. Later results should be
     * considered to be of higher quality.
     *
     * 这个方法可能会返回null在一下情况下
     * <p>This method will return null in the following cases:
     * <ul>
     *     当DataSource没有任何结果
     * <li>when the DataSource does not have a result ({@code hasResult} returns false).
     *    当最后的结果是null
     * <li>when the last result produced was null.
     * </ul>
     * @return current best result
     */
    @javax.annotation.Nullable
    T getResult();

    /**
     * 如果任何结果(可能是低质量的)现在是可用的,否则假
     * @return true if any result (possibly of lower quality) is available right now, false otherwise
     */
    boolean hasResult();

    /**
     * @return true if request is finished, false otherwise
     */
    boolean isFinished();

    /**
     * @return true if request finished due to error
     */
    boolean hasFailed();

    /**
     * @return failure cause if the source has failed, else null
     */
    @javax.annotation.Nullable
    Throwable getFailureCause();

    /**
     * @return progress in range [0, 1]
     */
    float getProgress();

    /**
     * 取消正在进行的请求和释放所有相关的资源
     * Cancels the ongoing request and releases all associated resources.
     *
     * <p>Subsequent calls to {@link #getResult} will return null.
     * @return true if the data source is closed for the first time
     */
    boolean close();

    /**
     * 通知订阅者每当数据源的状态变化。
     * Subscribe for notifications whenever the state of the DataSource changes.
     *
     * <p>All changes will be observed on the provided executor.
     * @param dataSubscriber
     * @param executor
     */
    void subscribe(DataSubscriber<T> dataSubscriber, Executor executor);
}
