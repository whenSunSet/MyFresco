package com.facebook.imageformat;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import javax.annotation.Nullable;

/**
 * 表示所有的image格式的类
 * Class representing all used image formats.
 */
public class ImageFormat {

    public interface FormatChecker {

        /**
         * 获取头字节格式检查的数量
         * Get the number of header bytes the format checker requires
         * @return the number of header bytes needed
         */
        int getHeaderSize();

        /**
         * 如果检查能确定格式那么就返回{@link ImageFormat}，否则返回null
         * Returns an {@link ImageFormat} if the checker is able to determine the format
         * or null otherwise.
         * @param headerBytes the header bytes to check
         * @param headerSize the size of the header in bytes
         * @return the image format or null if unknown
         */
        @Nullable
        ImageFormat determineFormat(byte[] headerBytes, int headerSize);
    }

    // Unknown image format
    public static final ImageFormat UNKNOWN = new ImageFormat("UNKNOWN", null);

    private final String mFileExtension;
    private final String mName;

    public ImageFormat(String name, @Nullable String fileExtension) {
        mName = name;
        mFileExtension = fileExtension;
    }

    /**
     * 获取给定image格式的默认文件扩展名
     * Get the default file extension for the given image format.
     * @return file extension for the image format
     */
    @Nullable
    public String getFileExtension() {
        return mFileExtension;
    }

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return mName;
    }
}
