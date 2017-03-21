package com.facebook.imagepipeline.animated.base.impl;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

import com.facebook.imagepipeline.animated.base.AnimatableDrawable;

import javax.annotation.concurrent.Immutable;

/**
 * 创建{@link AnimatableDrawable}的Options
 * Options for creating {@link AnimatableDrawable}.
 */
@Immutable
public class AnimatedDrawableOptions {

    /**
     * 默认的options
     * Default options.
     */
    public static AnimatedDrawableOptions DEFAULTS = AnimatedDrawableOptions.newBuilder().build();

    /**
     * 所有的渲染帧是否应该保存在内存中无视其他约束
     * Whether all the rendered frames should be held in memory disregarding other constraints.
     */
    public final boolean forceKeepAllFramesInMemory;

    /**
     * 是否可以使用工作线程来绘制的乐观预取帧。
     * Whether the drawable can use worker threads to optimistically prefetch frames.
     */
    public final boolean allowPrefetching;

    /**
     * backend可以使用的最大帧缓存字节，-1是默认
     * The maximum bytes that the backend can use to cache image frames in memory or -1
     * to use the default
     */
    public final int maximumBytes;

    /**
     * 是否添加debug
     * Whether to enable additional verbose debugging diagnostics.
     */
    public final boolean enableDebugging;

    /**
     * Creates {@link AnimatedDrawableOptions} with default options.
     */
    public AnimatedDrawableOptions(AnimatedDrawableOptionsBuilder builder) {
        this.forceKeepAllFramesInMemory = builder.getForceKeepAllFramesInMemory();
        this.allowPrefetching = builder.getAllowPrefetching();
        this.maximumBytes = builder.getMaximumBytes();
        this.enableDebugging = builder.getEnableDebugging();
    }

    /**
     * Creates a new builder.
     *
     * @return the builder
     */
    public static AnimatedDrawableOptionsBuilder newBuilder() {
        return new AnimatedDrawableOptionsBuilder();
    }
}
