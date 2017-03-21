package com.facebook.imagepipeline.bitmaps;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import android.graphics.Bitmap;

import com.facebook.commom.references.ResourceReleaser;

/**
 * 一个释放bitmap内存的 回收器
 * A releaser that just recycles (frees) bitmap memory immediately.
 */
public class SimpleBitmapReleaser implements ResourceReleaser<Bitmap> {

    private static SimpleBitmapReleaser sInstance;

    public static SimpleBitmapReleaser getInstance() {
        if (sInstance == null) {
            sInstance = new SimpleBitmapReleaser();
        }
        return sInstance;
    }

    private SimpleBitmapReleaser() {}

    @Override
    public void release(Bitmap value) {
        value.recycle();
    }
}
