package com.facebook.imagepipeline.animated.factory;

import android.content.Context;

import com.facebook.imagepipeline.animated.factory.impl.AnimatedFactoryImpl;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * Created by heshixiyang on 2017/3/10.
 */
/**
 * 提供 动画Factory 的Factory，默认实现是{@link AnimatedFactoryImpl}
 */
@NotThreadSafe
public interface AnimatedFactory {

    AnimatedDrawableFactory getAnimatedDrawableFactory(Context context);

    AnimatedImageFactory getAnimatedImageFactory();
}
