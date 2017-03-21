package com.facebook.imagepipeline.nativecode;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import android.os.Build;

import com.facebook.commom.soloader.SoLoaderShim;

/**
 * 确保`static-webp.so` 已经被加载
 * Single place responsible for ensuring that `static-webp.so` is loaded
 */
public class StaticWebpNativeLoader {

    private static boolean sInitialized;

    public static synchronized void ensure() {
        if (!sInitialized) {
            // On Android 4.1.2 the loading of the static-webp native library can fail because
            // of the dependency with fb_jpegturbo. In this case we have to explicitely load that
            // library
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN) {
                try {
                    SoLoaderShim.loadLibrary("fb_jpegturbo");
                } catch (UnsatisfiedLinkError error) {
                    // Head in the sand
                }
            }
            SoLoaderShim.loadLibrary("static-webp");
            sInitialized = true;
        }
    }
}
