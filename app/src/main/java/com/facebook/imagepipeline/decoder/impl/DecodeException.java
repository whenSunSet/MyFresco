package com.facebook.imagepipeline.decoder.impl;

/**
 * Created by heshixiyang on 2017/3/17.
 */
public class DecodeException extends RuntimeException {
    public DecodeException(String message) {
        super(message);
    }

    public DecodeException(String message, Throwable t) {
        super(message, t);
    }
}

