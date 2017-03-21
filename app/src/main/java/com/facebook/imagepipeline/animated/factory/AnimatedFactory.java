package com.facebook.imagepipeline.animated.factory;

import android.content.Context;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Created by heshixiyang on 2017/3/10.
 */
@NotThreadSafe
public interface AnimatedFactory {

    AnimatedDrawableFactory getAnimatedDrawableFactory(Context context);

    AnimatedImageFactory getAnimatedImageFactory();
}
