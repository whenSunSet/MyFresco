package com.facebook.imagepipeline.memory.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import com.facebook.commom.internal.Throwables;
import com.facebook.imagepipeline.memory.PooledByteBuffer;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream that produces a PooledByteBuffer.
 *
 * <p> Expected use for such stream is to first write sequence of bytes to the stream and then call
 * toByteBuffer to produce PooledByteBuffer containing written data. After toByteBuffer returns
 * client can continue writing new data and call toByteBuffer over and over again.
 *
 * <p> Streams implementing this interface are closeable resources and need to be closed in order
 * to release underlying resources. Close is idempotent operation and after stream was closed, no
 * other method should be called. Streams subclassing PooledByteBufferOutputStream are not allowed
 * to throw IOException from close method.
 */
public abstract class PooledByteBufferOutputStream extends OutputStream {
    /**
     * Creates a PooledByteBuffer from the contents of the stream.
     * @return
     */
    public abstract PooledByteBuffer toByteBuffer();

    /**
     * Returns the total number of bytes written to this stream so far.
     * @return the number of bytes written to this stream.
     */
    public abstract int size();

    /**
     * Closes the stream.
     */
    @Override
    public void close() {
        try {
            super.close();
        } catch (IOException ioe) {
            // does not happen
            Throwables.propagate(ioe);
        }
    }
}
