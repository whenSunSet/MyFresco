package com.facebook.imagepipeline.animated.base;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.facebook.commom.references.CloseableReference;

/**
 * 一个{@link AnimatedDrawableBackend}的包装，其可以添加高速缓存和预读
 * A specialized version of {@link AnimatedDrawableBackend} that adds caching and prefetching.
 */
public interface AnimatedDrawableCachingBackend extends AnimatedDrawableBackend {

    /**
     * 获取指定帧的bitmap，这个bitmap的大小依靠{@link #getRenderedWidth()}和{@link #getRenderedHeight()}确定
     * Gets the bitmap for the specified frame number. The bitmap should be the size of the
     * rendered image according to {@link #getRenderedWidth()} and {@link #getRenderedHeight()} and
     * ready to be drawn in the Drawable's draw method.
     *
     * @param frameNumber the frame number (0-based)
     * @return 这是一个bitmap的引用，这个引用会被使用者释放当事情做完之后或者被设置成null
     * 当为null的时候表明bitmap还没有准备好需要晚一些再试一下。
     * a reference to the bitmap which must be released by the caller when done or null
     *    to indicate to the caller that the bitmap is not ready and it should try again later
     */
    CloseableReference<Bitmap> getBitmapForFrame(int frameNumber);

    /**
     * 获取预览帧的bitmap，这个只能返回非null如果{@code ImageDecodeOptions}已经被配制成解码的预览帧
     * Gets the bitmap for the preview frame. This will only return non-null if the
     * {@code ImageDecodeOptions} were configured to decode the preview frame.
     *
     * @return 这是一个bitmap的引用，这个引用会被使用者释放当事情做完之后或者被设置成null
     * 当被设置成null的时候表示没有预览帧被设置
     * a reference to the preview bitmap which must be released by the caller when done or
     *    null if there is no preview bitmap set
     */
    CloseableReference<Bitmap> getPreviewBitmap();

    /**
     * 可能有用的调试
     * Appends a string about the state of the backend that might be useful for debugging.
     *
     * @param sb the builder to append to
     */
    void appendDebugOptionString(StringBuilder sb);

    // Overridden to restrict the return type.
    @Override
    AnimatedDrawableCachingBackend forNewBounds(Rect bounds);
}
