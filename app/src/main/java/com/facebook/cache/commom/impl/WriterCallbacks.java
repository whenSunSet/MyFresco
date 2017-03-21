package com.facebook.cache.commom.impl;

/**
 * Created by Administrator on 2017/3/11 0011.
 */

import com.facebook.cache.commom.WriterCallback;
import com.facebook.commom.internal.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 用来创建{@link WriterCallback}s的工具
 * Utility class to create typical {@link WriterCallback}s.
 */
public class WriterCallbacks {

    /**
     * 创建一个writer callback ，其可以拷贝{@link InputStream}中所有的内容，然后放入目标的stream
     * Creates a writer callback that copies all the content read from an {@link InputStream} into
     * the target stream.
     *
     * <p>This writer can be used only once.
     * @param is the source
     * @return the writer callback
     */
    public static WriterCallback from(final InputStream is) {
        return new WriterCallback() {
            @Override
            public void write(OutputStream os) throws IOException {
                ByteStreams.copy(is, os);
            }
        };
    }

    /**
     * 创建一个writer callback，其可以写一些byte array进入一个目标stream
     * Creates a writer callback that writes some byte array to the target stream.
     *
     * <p>This writer can be used many times.
     * @param data the bytes to write
     * @return the writer callback
     */
    public static WriterCallback from(final byte[] data) {
        return new WriterCallback() {
            @Override
            public void write(OutputStream os) throws IOException {
                os.write(data);
            }
        };
    }
}
