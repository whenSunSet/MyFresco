package com.facebook.drawee.interfaces;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import android.graphics.drawable.Drawable;

/**
 * 一个表示可变的Drawee层次结构的接口，Hierarchy应该显示占位image直到真正的image 被设置。
 * 比如说图片获取失败，Hierarchy可以选择展示失败的Image
 *
 * 重要：这个接口的方法只能被controller使用
 * <p>
 * Example hierarchy:
 *
 *   o FadeDrawable (top level drawable)
 *   |
 *   +--o ScaleTypeDrawable
 *   |  |
 *   |  +--o ColorDrawable (placeholder image)
 *   |
 *   +--o ScaleTypeDrawable
 *   |  |
 *   |  +--o BitmapDrawable (failure image)
 *   |
 *   +--o ScaleTypeDrawable
 *      |
 *      +--o SettableDrawable
 *         |
 *         +--o BitmapDrawable (actual image)
 *
 *   SettableDraweeHierarchy在这个被给予的例子里有一个  FadeDrawable作为顶层的drawable
 *   顶层的drawable可以被立即放置到view中。一旦真正的image已经准备好，
 *   他将会被设置到hierarchy的SettableDrawable 中，并且过渡的动画会在占位符和真正的图片中展示
 *   直到占位符被替代。举个例子就是失败的时候，hierarchy将会选择失败的image
 *   所有的image分支都被ScaleType drawable 包裹，这个drawable允许图片使用不同的scale type
 *
 */
public interface SettableDraweeHierarchy extends DraweeHierarchy {

    /**
     * 当hierarchy应该被设置为起始状态的时候被controller调用
     * 任何image在通过{@code setImage}调用之前应该被detached并且不再被使用
     */
    void reset();

    /**
     * 当真正的image已经被提供完毕的时候调用
     * Hierarchy应该显示真正的image
     * @param drawable drawable to be set as the temporary image
     * @param progress number in range [0, 1] that indicates progress
     * @param immediate 如果是true，image应该被直接显示，没有过渡动画
     */
    void setImage(Drawable drawable, float progress, boolean immediate);

    /**
     * 在更新进度的时候被controller调用
     * Hierarchy可以选择隐藏progressbar 当进度到1的时候
     * @param progress number in range [0, 1] that indicates progress
     * @param immediate if true, progressbar will be shown/hidden immediately (without fade effect)
     */
    void setProgress(float progress, boolean immediate);

    /**
     * 当真正图片获取失败的时候被controller调用
     * Hierarchy可以基于不同的错误选择显示不同的错误图片
     * @param throwable cause of failure
     */
    void setFailure(Throwable throwable);

    /**
     * 大概真正图片获取失败但是controller允许轻触重新加载的时候被controller调用
     * Hierarchy 可以选择显示重新加载的图片
     * @param throwable cause of failure
     */
    void setRetry(Throwable throwable);

    /**
     * 当controller需要显示一些覆盖物的时候调用
     * @param drawable drawable to be displayed as controller overlay
     */
    void setControllerOverlay(Drawable drawable);
}
