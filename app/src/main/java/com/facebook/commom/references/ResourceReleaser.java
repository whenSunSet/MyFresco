package com.facebook.commom.references;

/**
 * Created by heshixiyang on 2017/3/9.
 */
/**
 * 一个表示抽象释放资源的接口
 *
 * 有多个组件,由他人共享自己的资源,像池和缓存。应该实现这个接口的类想要执行一些动作，当一个特定的资源不再是必要的
 * 如Bitmap的recycle()，和流的close()等等
 * @param <T> 由此资源类型的资源管理释放者
 */
public interface ResourceReleaser<T> {

    /**
     * 释放给定的资源.
     *
     * 调用该方法后,调用者不再是负责管理该资源的生命周期
     *
     * 这个方法是不允许抛出异常,总是需要成功。从上下文通常被称为像catch块或最后块清理资源。
     * @param value
     */
    void release(T value);
}
