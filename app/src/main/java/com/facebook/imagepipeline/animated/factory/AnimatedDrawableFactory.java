package com.facebook.imagepipeline.animated.factory;

import android.graphics.drawable.Drawable;

import com.facebook.imagepipeline.animated.base.AnimatedImage;
import com.facebook.imagepipeline.animated.base.impl.AnimatedDrawable;
import com.facebook.imagepipeline.image.impl.CloseableImage;

/**
 * Created by heshixiyang on 2017/3/9.
 */
public interface AnimatedDrawableFactory {

    /**
     * 创建一个基于{@link AnimatedImage}的{@link AnimatedDrawable}
     * Creates an {@link AnimatedDrawable} based on an {@link AnimatedImage}.
     * @param closeableImage the result of the code
     * @return a newly constructed {@link Drawable}
     */
    Drawable create(CloseableImage closeableImage);


}
