package com.facebook.imagepipeline.memory;

/**
 * Created by heshixiyang on 2017/3/10.
 */

import com.facebook.imagepipeline.memory.impl.PooledByteBufferOutputStream;

import java.io.IOException;
import java.io.InputStream;

/**
 * 一个用于创建PooledByteBuffer 和 PooledByteBufferOutputStream的工厂
 * A factory to create instances of PooledByteBuffer and PooledByteBufferOutputStream
 */
public interface PooledByteBufferFactory {

    /**
     * 创建一个PooledByteBuffer基于被给定的size
     * Creates a new PooledByteBuffer instance of given size.
     * @param size in bytes
     * @return an instance of PooledByteBuffer
     */
    PooledByteBuffer newByteBuffer(int size);

    /**
     * 创建一个PooledByteBuffer，通过阅读InputStream的全部内容
     * Creates a new bytebuf instance by reading in the entire contents of the input stream
     * @param inputStream the input stream to read from
     * @return an instance of the PooledByteBuffer
     * @throws IOException
     */
    PooledByteBuffer newByteBuffer(InputStream inputStream) throws IOException;

    /**
     * 创建一个PooledByteBuffer通过阅读全部的byte数组
     * Creates a new bytebuf instance by reading in the entire contents of the byte array
     * @param bytes the byte array to read from
     * @return an instance of the PooledByteBuffer
     */
    PooledByteBuffer newByteBuffer(byte[] bytes);

    /**
     * 创建一个PooledByteBuffer通过设置的起始容量，然后获取input stream中的部分内容
     * Creates a new PooledByteBuffer instance with an initial capacity, and reading the entire
     * contents of the input stream
     * @param inputStream the input stream to read from
     * @param initialCapacity initial allocation size for the bytebuf
     * @return an instance of PooledByteBuffer
     * @throws IOException
     */
    PooledByteBuffer newByteBuffer(InputStream inputStream, int initialCapacity) throws IOException;

    /**
     * 创建一个PooledByteBufferOutputStream通过默认的起始的容量
     * Creates a new PooledByteBufferOutputStream instance with default initial capacity
     * @return a new PooledByteBufferOutputStream
     */
    PooledByteBufferOutputStream newOutputStream();

    /**
     * 创建一个PooledByteBufferOutputStream通过传入的起始容量
     * Creates a new PooledByteBufferOutputStream instance with the specified initial capacity
     * @param initialCapacity initial allocation size for the underlying output stream
     * @return a new PooledByteBufferOutputStream
     */
    PooledByteBufferOutputStream newOutputStream(int initialCapacity);
}
