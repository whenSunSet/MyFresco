package com.facebook.commom.webp;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import java.io.FileDescriptor;
import java.io.InputStream;

/**
 * 一个bitmap工厂的接口，这个工程可以解码WebPimage即使android版本不支持
 * Interface for a bitmap factory that can decode WebP images even on versions of Android that
 * don't support it.
 *
 * <p> Implementation is found in the optional static-webp library. To use, add the following to
 * your build.gradle file: <code>compile 'com.facebook.fresco:static-webp:${FRESCO_VERSION}'</code>
 */
public interface WebpBitmapFactory {

    /**
     * 我们监听Webp的解码
     * We listen to events in Webp direct decoding
     */
    interface WebpErrorLogger {

        /**
         * 当产生error的时候调用
         * Invoked to notify the logger about an error
         *
         * @param message The message to log
         * @param extra Extra message if any
         */
        void onWebpErrorLog(String message, String extra);
    }

    /**
     * 注册一个被给予的监听者作为一个error的观察者
     * Register the given listener as observer of error
     *
     * @param logger The WebpErrorLogger in order to observe webp errors
     */
    void setWebpErrorLogger(WebpErrorLogger logger);

    /**
     * 设置一个创建bitmap的对象
     * Set the object which should create the bg Bitmap
     *
     * @param bitmapCreator The BitmapCreator implementation
     */
    void setBitmapCreator(final BitmapCreator bitmapCreator);

    Bitmap decodeFileDescriptor(
            FileDescriptor fd,
            Rect outPadding,
            BitmapFactory.Options opts);

    Bitmap decodeStream(
            InputStream inputStream,
            Rect outPadding,
            BitmapFactory.Options opts);

    Bitmap decodeFile(
            String pathName,
            BitmapFactory.Options opts);

    Bitmap decodeByteArray(
            byte[] array,
            int offset,
            int length,
            BitmapFactory.Options opts);

}
