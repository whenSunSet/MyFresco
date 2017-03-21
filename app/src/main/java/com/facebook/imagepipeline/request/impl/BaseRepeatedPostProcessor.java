package com.facebook.imagepipeline.request.impl;

import com.facebook.imagepipeline.request.RepeatedPostprocessor;
import com.facebook.imagepipeline.request.RepeatedPostprocessorRunner;

/**
 * Created by Administrator on 2017/3/17 0017.
 */
public abstract class BaseRepeatedPostProcessor extends BasePostprocessor
        implements RepeatedPostprocessor {
    private RepeatedPostprocessorRunner mCallback;

    @Override
    public synchronized void setCallback(RepeatedPostprocessorRunner runner) {
        mCallback = runner;
    }

    private synchronized RepeatedPostprocessorRunner getCallback() {
        return mCallback;
    }

    public void update() {
        RepeatedPostprocessorRunner callback = getCallback();
        if (callback != null) {
            callback.update();
        }
    }
}

