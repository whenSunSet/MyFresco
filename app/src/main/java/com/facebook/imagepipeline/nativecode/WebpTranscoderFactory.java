package com.facebook.imagepipeline.nativecode;

/**
 * Created by Administrator on 2017/3/17 0017.
 */
/**
 * 返回一个WebpTranscoder的class
 * This is the class responsible to return the WebpTranscoder if any
 */
public class WebpTranscoderFactory {

    private static WebpTranscoder sWebpTranscoder;

    public static boolean sWebpTranscoderPresent = false;

    static {
        try {
            sWebpTranscoder = (WebpTranscoder) Class
                    .forName("com.facebook.imagepipeline.nativecode.WebpTranscoderImpl")
                    .newInstance();
            sWebpTranscoderPresent = true;
        } catch (Throwable e) {
            sWebpTranscoderPresent = false;
        }
    }

    public static WebpTranscoder getWebpTranscoder() {
        return sWebpTranscoder;
    }

}

