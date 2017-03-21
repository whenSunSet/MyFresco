package com.facebook.commom.internal;

/**
 * Created by heshixiyang on 2017/3/12.
 */

import java.util.Arrays;

/**
 * 找到最大数字
 * Static utility methods pertaining to {@code int} primitives, that are not
 * already found in either {@link Integer} or {@link Arrays}.
 *
 * @author Kevin Bourrillion
 * @since 1.0
 */
public class Ints {
    private Ints() {}

    /**
     * Returns the greatest value present in {@code array}.
     *
     * @param array a <i>nonempty</i> array of {@code int} values
     * @return the value present in {@code array} that is greater than or equal to
     *     every other value in the array
     * @throws IllegalArgumentException if {@code array} is empty
     */
    public static int max(int... array) {
        Preconditions.checkArgument(array.length > 0);
        int max = array[0];
        for (int i = 1; i < array.length; i++) {
            if (array[i] > max) {
                max = array[i];
            }
        }
        return max;
    }

}
