package com.facebook.drawee.interfaces;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import android.graphics.drawable.Drawable;

/**
 * 一个代表Drawee视图层次的接口
 *
 * 一个层次的聚合，一Drawables的棵树为了动态的显示可变的动画
 * 这是一个比android视图嵌套更轻量级的视图
 *
 * 层次的细节对外部隐藏，所有的可见就是在顶层的drawable。
 * <p> Example hierarchy:
 *
 *   o FadeDrawable (top level drawable)
 *   |
 *   +--o ScaleTypeDrawable
 *   |  |
 *   |  +--o BitmapDrawable
 *   |
 *   +--o ScaleTypeDrawable
 *      |
 *      +--o BitmapDrawable
 *
 */
public interface DraweeHierarchy {

    /**
     * 从相匹配的hierarchy中，返回顶层的drawable。hierarchy应该总是和顶层drawable相同的实例
     * @return top level drawable
     */
    Drawable getTopLevelDrawable();
}
