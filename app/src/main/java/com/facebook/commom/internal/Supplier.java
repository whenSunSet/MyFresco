package com.facebook.commom.internal;

/**
 * Created by heshixiyang on 2017/3/9.
 */
/**
 * 一个可以一个简单类型的class，一般的说这个可以是一个factory, generator, builder, closure
 * 或者其他东西。不能保证这个是一个确定的东西
 *
 * @author Harry Heymann
 * @since 2.0 (imported from Google Collections Library)
 */
public interface Supplier<T> {
    /**
     * Retrieves an instance of the appropriate type. The returned object may or
     * may not be a new instance, depending on the implementation.
     *
     * @return an instance of the appropriate type
     */
    T get();
}
