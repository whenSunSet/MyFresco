package com.facebook.drawee.drawable.impl;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import android.annotation.SuppressLint;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

/**
 * drawable的属性集。没有默认值,如果被显式地设置
 * Set of properties for drawable. There are no default values and only gets applied if were set
 * explicitly.
 */
public class DrawableProperties {

    private static final int UNSET = -1;

    private int mAlpha = UNSET;
    private boolean mIsSetColorFilter = false;
    private ColorFilter mColorFilter = null;
    private int mDither = UNSET;
    private int mFilterBitmap = UNSET;

    public void setAlpha(int alpha) {
        mAlpha = alpha;
    }

    public void setColorFilter(ColorFilter colorFilter) {
        mColorFilter = colorFilter;
        mIsSetColorFilter = true;
    }

    public void setDither(boolean dither) {
        mDither = dither ? 1 : 0;
    }

    public void setFilterBitmap(boolean filterBitmap) {
        mFilterBitmap = filterBitmap ? 1 : 0;
    }

    @SuppressLint("Range")
    public void applyTo(Drawable drawable) {
        if (drawable == null) {
            return;
        }
        if (mAlpha != UNSET) {
            drawable.setAlpha(mAlpha);
        }
        if (mIsSetColorFilter) {
            drawable.setColorFilter(mColorFilter);
        }
        if (mDither != UNSET) {
            drawable.setDither(mDither != 0);
        }
        if (mFilterBitmap != UNSET) {
            drawable.setFilterBitmap(mFilterBitmap != 0);
        }
    }
}
