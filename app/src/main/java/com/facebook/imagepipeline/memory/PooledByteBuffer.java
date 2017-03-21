package com.facebook.imagepipeline.memory;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import java.io.Closeable;

/**
 * 一个池子化的抽象字节缓冲区，代表一个不可变的字节序列，该序列被储存在java堆
 * A 'pooled' byte-buffer abstraction. Represents an immutable sequence of bytes stored off the
 * java heap.
 */
public interface PooledByteBuffer extends Closeable {

    /**
     * 获取字节缓冲区的大小
     * Get the size of the byte buffer
     * @return the size of the byte buffer
     */
    int size();

    /**
     * 读取字节以指定的位移
     * Read byte at given offset
     * @param offset
     * @return byte at given offset
     */
    byte read(int offset);

    /**
     * 读取连续的字节
     * Read consecutive bytes.
     *
     * @param offset the position in the PooledByteBuffer of the first byte to read
     * @param buffer the byte array where read bytes will be copied to
     * @param bufferOffset the position within the buffer of the first copied byte
     * @param length number of bytes to copy
     * @return number of bytes copied
     */
    void read(int offset, byte[] buffer, int bufferOffset, int length);

    /**
     * @return pointer to native memory backing this buffer
     */
    long getNativePtr();

    /**
     * 关闭这个PooledByteBuffer并且释放所有正在使用的资源
     * Close this PooledByteBuffer and release all underlying resources
     */
    @Override
    void close();

    /**
     * 判断资源是否被关闭
     * Check if this instance has already been closed
     * @return true, if the instance has been closed
     */
    boolean isClosed();

    /**
     * Exception indicating that the PooledByteBuffer is closed
     */
    class ClosedException extends RuntimeException {
        public ClosedException() {
            super("Invalid bytebuf. Already closed");
        }
    }
}
