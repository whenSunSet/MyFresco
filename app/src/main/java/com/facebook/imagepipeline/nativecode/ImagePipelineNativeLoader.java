package com.facebook.imagepipeline.nativecode;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.commom.soloader.SoLoaderShim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 负责加载libimagepipeline.so 和他的依赖
 * Single place responsible for loading libimagepipeline.so and its dependencies.
 *
 * 如果你的class有native方法，该方法在libimagepipeline.so 中那么调用
 * {@link ImagePipelineNativeLoader#load}这个方法
 * If your class has a native method whose implementation lives in libimagepipeline.so then call
 * {@link ImagePipelineNativeLoader#load} in its static initializer:
 * <code>
 *   public class ClassWithNativeMethod {
 *     static {
 *       ImagePipelineNativeLoader.load();
 *     }
 *
 *     private static native void aNativeMethod();
 *   }
 * </code>
 */
public class ImagePipelineNativeLoader {
    public static final String DSO_NAME = "imagepipeline";

    public static final List<String> DEPENDENCIES;
    static {
        List<String> dependencies = new ArrayList<String>();
        DEPENDENCIES = Collections.unmodifiableList(dependencies);
    }

    public static void load() {
        SoLoaderShim.loadLibrary("imagepipeline");
    }
}
