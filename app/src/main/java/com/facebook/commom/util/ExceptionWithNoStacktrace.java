package com.facebook.commom.util;

/**
 * Created by Administrator on 2017/3/13 0013.
 */
/**
 * Instantiate an exception with an empty stacktrace. This is more performant than instantiating
 * a regular exception since it doesn't incur the cost of getting the stack trace.
 */
public class ExceptionWithNoStacktrace extends Exception {
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
    public ExceptionWithNoStacktrace(String detailMessage) {
        super(detailMessage);
    }
}

