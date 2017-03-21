package com.facebook.commom.internal;

/**
 * Created by heshixiyang on 2017/3/12.
 */

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 一个记录 bytes数的OutputStream
 * An OutputStream that counts the number of bytes written.
 *
 * @author Chris Nokleberg
 * @since 1.0
 */
public class CountingOutputStream extends FilterOutputStream {

    private long mCount;

    /**
     * Constructs a new {@code FilterOutputStream} with {@code out} as its
     * target stream.
     *
     * @param out the target stream that this stream writes to.
     */
    public CountingOutputStream(OutputStream out) {
        super(out);
        mCount = 0;
    }

    /**
     * Returns the number of bytes written.
     */
    public long getCount() {
        return mCount;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        mCount += len;
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        mCount++;
    }

    // Overriding close() because FilterOutputStream's close() method pre-JDK8 has bad behavior:
    // it silently ignores any exception thrown by flush(). Instead, just close the delegate stream.
    // It should flush itself if necessary.
    @Override
    public void close() throws IOException {
        out.close();
    }
}
