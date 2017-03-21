package com.facebook.commom.internal;

/**
 * Created by heshixiyang on 2017/3/12.
 */

import com.android.internal.util.Predicate;

/**
 * Additional predicates.
 */
public class AndroidPredicates {

    private AndroidPredicates() {}

    public static <T> Predicate<T> True() {
        return new Predicate<T>() {
            @Override
            public boolean apply(T t) {
                return true;
            }
        };
    }

    public static <T> Predicate<T> False() {
        return new Predicate<T>() {
            @Override
            public boolean apply(T t) {
                return false;
            }
        };
    }
}
