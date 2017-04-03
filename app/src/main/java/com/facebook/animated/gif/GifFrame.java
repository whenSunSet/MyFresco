package com.facebook.animated.gif;

/**
 * Created by Administrator on 2017/3/11 0011.
 */

import android.graphics.Bitmap;

import com.facebook.commom.internal.DoNotStrip;
import com.facebook.imagepipeline.animated.base.AnimatedImageFrame;

import javax.annotation.concurrent.ThreadSafe;

/**
 * GifFrame则是将Gif动画单个帧的数据储存在jni代码管里的本地内存中。
 *
 * A single frame of a {@link GifImage}.
 * 这是jni代码的java类
 */
@ThreadSafe
public class GifFrame implements AnimatedImageFrame {

    // Accessed by native methods
    @SuppressWarnings("unused")
    @DoNotStrip
    private long mNativeContext;

    /**
     * Constructs the frame with the native pointer. This is called by native code.
     *
     * @param nativeContext the native pointer
     */
    @DoNotStrip
    GifFrame(long nativeContext) {
        mNativeContext = nativeContext;
    }

    @Override
    protected void finalize() {
        nativeFinalize();
    }

    @Override
    public void dispose() {
        nativeDispose();
    }

    @Override
    public void renderFrame(int width, int height, Bitmap bitmap) {
        nativeRenderFrame(width, height, bitmap);
    }

    @Override
    public int getDurationMs() {
        return nativeGetDurationMs();
    }

    @Override
    public int getWidth() {
        return nativeGetWidth();
    }

    @Override
    public int getHeight() {
        return nativeGetHeight();
    }

    @Override
    public int getXOffset() {
        return nativeGetXOffset();
    }

    @Override
    public int getYOffset() {
        return nativeGetYOffset();
    }

    public boolean hasTransparency() {
        return nativeHasTransparency();
    }

    public int getTransparentPixelColor() {
        return nativeGetTransparentPixelColor();
    }

    public int getDisposalMode() {
        return nativeGetDisposalMode();
    }

    private native void nativeRenderFrame(int width, int height, Bitmap bitmap);
    private native int nativeGetDurationMs();
    private native int nativeGetWidth();
    private native int nativeGetHeight();
    private native int nativeGetXOffset();
    private native int nativeGetYOffset();
    private native int nativeGetDisposalMode();
    private native int nativeGetTransparentPixelColor();
    private native boolean nativeHasTransparency();
    private native void nativeDispose();
    private native void nativeFinalize();
}
