package com.facebook.imagepipeline.nativecode;

/**
 * Created by heshixiyang on 2017/3/10.
 */


import com.facebook.imageformat.ImageFormat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 一个抽象的WebpTranscoder
 * The abstraction for WebpTranscoder
 */
public interface WebpTranscoder {

    /**
     * 返回true，如果被给予的WebP的类型被本机所支持
     * @return true if given type of WebP is supported natively by the framework
     */
    boolean isWebpNativelySupported(ImageFormat webpFormat);

    /**
     * 将webp image转码到jpeg中通过指定的input stream
     * Transcodes webp image given by input stream into jpeg.
     */
    void transcodeWebpToJpeg(
            InputStream inputStream,
            OutputStream outputStream,
            int quality) throws IOException;

    /**
     * 将webp image转码到png中通过指定的input stream
     * Transcodes Webp image given by input stream into png.
     */
    void transcodeWebpToPng(
            InputStream inputStream,
            OutputStream outputStream) throws IOException;

}
