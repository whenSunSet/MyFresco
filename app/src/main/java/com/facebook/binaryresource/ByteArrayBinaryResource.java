package com.facebook.binaryresource;

/**
 * Created by Administrator on 2017/3/11 0011.
 */

import com.facebook.commom.internal.Preconditions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 一个简单实现二进制资源封装一个字节数组
 * A trivial implementation of BinaryResource that wraps a byte array
 */
public class ByteArrayBinaryResource implements BinaryResource {
    private final byte[] mBytes;

    public ByteArrayBinaryResource(byte[] bytes) {
        mBytes = Preconditions.checkNotNull(bytes);
    }

    @Override
    public long size() {
        return mBytes.length;
    }

    @Override
    public InputStream openStream() throws IOException {
        return new ByteArrayInputStream(mBytes);
    }

    /**
     * Get the underlying byte array
     * @return the underlying byte array of this resource
     */
    @Override
    public byte[] read() {
        return mBytes;
    }
}
