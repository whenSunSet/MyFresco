package com.facebook.drawee.drawable;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import android.graphics.drawable.Drawable;

/**
 * 一个drawable的父亲，只有一个子
 * A drawable parent that has a single child.
 */
public interface DrawableParent {

    /**
     * 放置子drawable
     * Sets the new child drawable.
     * @param newDrawable a new child drawable to set
     * @return the old child drawable
     */
    Drawable setDrawable(Drawable newDrawable);

    /**
     * 获取子drawable
     * Gets the child drawable.
     * @return the current child drawable
     */
    Drawable getDrawable();

}
