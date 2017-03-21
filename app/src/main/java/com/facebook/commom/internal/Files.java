package com.facebook.commom.internal;

/**
 * Created by Administrator on 2017/3/11 0011.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 为处理多文件提供了实用方法
 * Provides utility methods for working with files.
 * 所有方法参数必须非空除非另有记录
 * <p>All method parameters must be non-null unless documented otherwise.
 *
 * @author Chris Nokleberg
 * @author Colin Decker
 * @since 1.0
 */
public class Files {
    private Files() {}

    /**
     * Reads a file of the given expected size from the given input stream, if
     * it will fit into a byte array. This method handles the case where the file
     * size changes between when the size is read and when the contents are read
     * from the stream.
     */
    static byte[] readFile(
            InputStream in, long expectedSize) throws IOException {
        if (expectedSize > Integer.MAX_VALUE) {
            throw new OutOfMemoryError("file is too large to fit in a byte array: "
                    + expectedSize + " bytes");
        }

        // some special files may return size 0 but have content, so read
        // the file normally in that case
        return expectedSize == 0
                ? ByteStreams.toByteArray(in)
                : ByteStreams.toByteArray(in, (int) expectedSize);
    }
    /**
     * 读取一个文件的所有byte
     * Reads all bytes from a file into a byte array.
     *
     * @param file the file to read from
     * @return a byte array containing all the bytes from file
     * @throws IllegalArgumentException if the file is bigger than the largest
     *     possible byte array (2^31 - 1)
     * @throws IOException if an I/O error occurs
     */
    public static byte[] toByteArray(File file) throws IOException {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            return readFile(in, in.getChannel().size());
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
