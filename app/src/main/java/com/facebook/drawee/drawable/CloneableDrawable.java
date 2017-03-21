package com.facebook.drawee.drawable;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import android.graphics.drawable.Drawable;

/**
 * 一个可克隆自己的Drawable
 * A drawable that is capable of cloning itself.
 */
public interface CloneableDrawable {

    /**
     * 返回一个被拷贝的drawable
     * Creates a copy of the drawable.
     * @return the drawable copy
     */
    Drawable cloneDrawable();
}
