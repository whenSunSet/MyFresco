package com.facebook.imageformat;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import com.facebook.commom.internal.Preconditions;

import java.io.UnsupportedEncodingException;

/**
 * 为{@link ImageFormatChecker}服务的方法
 * {@link ImageFormatChecker} utility methods
 */
public class ImageFormatCheckerUtils {

    /**
     * Helper method that transforms provided string into it's byte representation
     * using ASCII encoding.
     * @param value the string to use
     * @return byte array representing ascii encoded value
     */
    public static byte[] asciiBytes(String value) {
        Preconditions.checkNotNull(value);
        try {
            return value.getBytes("ASCII");
        } catch (UnsupportedEncodingException uee) {
            // won't happen
            throw new RuntimeException("ASCII not found!", uee);
        }
    }

    /**
     * Checks if byteArray interpreted as sequence of bytes starts with pattern
     * starting at position equal to offset.
     * @param byteArray the byte array to be checked
     * @param pattern the pattern to check
     * @return true if byteArray starts with pattern
     */
    public static boolean startsWithPattern(
            final byte[] byteArray,
            final byte[] pattern) {
        Preconditions.checkNotNull(byteArray);
        Preconditions.checkNotNull(pattern);
        if (pattern.length > byteArray.length) {
            return false;
        }

        for (int i = 0; i < pattern.length; ++i) {
            if (byteArray[i] != pattern[i]) {
                return false;
            }
        }

        return true;
    }

    private ImageFormatCheckerUtils() {}
}
