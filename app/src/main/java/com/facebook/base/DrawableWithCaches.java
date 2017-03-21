package com.facebook.base;

/**
 * Created by heshixiyang on 2017/3/10.
 */
/**
 * 一个drawable可以实现这个接口，便于当为了节约内存而删除缓存的时候收到通知
 * 这是最后的努力，Drawable不应该依靠这个调用
 * A drawable can implement this interface to be notified when it might be convenient to
 * drop its caches in order conserve memory. This is best effort and the Drawable should not
 * depend on it being called.
 */
public interface DrawableWithCaches {

    /**
     * 提醒Drawable删除缓存
     * Informs the Drawable to drop its caches.
     */
    void dropCaches();
}
