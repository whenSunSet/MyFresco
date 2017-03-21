package com.facebook.datasource;

/**
 * Created by heshixiyang on 2017/3/9.
 */
/**
 * Subscribes to DataSource<T>.
 * @param <T>
 */
public interface DataSubscriber<T> {

    /**
     * Called whenever a new value is ready to be retrieved from the DataSource.
     *
     * <p>To retrieve the new value, call {@code dataSource.getResult()}.
     *
     * <p>To determine if the new value is the last, use {@code dataSource.isFinished()}.
     *
     * @param dataSource
     */
    void onNewResult(DataSource<T> dataSource);

    /**
     * Called whenever an error occurs inside of the pipeline.
     *
     * <p>No further results will be produced after this method is called.
     *
     * <p>The throwable resulting from the failure can be obtained using
     * {@code dataSource.getFailureCause}.
     *
     * @param dataSource
     */
    void onFailure(DataSource<T> dataSource);

    /**
     * Called whenever the request is cancelled (a request being cancelled means that is was closed
     * before it finished).
     *
     * <p>No further results will be produced after this method is called.
     *
     * @param dataSource
     */
    void onCancellation(DataSource<T> dataSource);

    /**
     * Called when the progress updates.
     *
     * @param dataSource
     */
    void onProgressUpdate(DataSource<T> dataSource);
}
