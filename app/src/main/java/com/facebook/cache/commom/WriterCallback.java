package com.facebook.cache.commom;

/**
 * Created by Administrator on 2017/3/8 0008.
 */

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * Callback that writes to an {@link OutputStream}.
 */
public interface WriterCallback {
    void write(OutputStream os) throws IOException;
}
