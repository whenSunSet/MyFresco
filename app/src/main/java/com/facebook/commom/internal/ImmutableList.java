package com.facebook.commom.internal;

/**
 * Created by heshixiyang on 2017/3/12.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 不可变的List
 * A dummy representation of an immutable set. This can be used temporarily as a type until we have
 * an actual non-guava implementation.
 */
public class ImmutableList<E> extends ArrayList<E> {

    private ImmutableList(final int capacity) {
        super(capacity);
    }

    private ImmutableList(List<E> list) {
        super(list);
    }

    public static <E> ImmutableList<E> copyOf(List<E> list) {
        return new ImmutableList<>(list);
    }

    public static <E> ImmutableList<E> of(E... elements) {
        final ImmutableList<E> list = new ImmutableList<>(elements.length);
        Collections.addAll(list, elements);
        return list;
    }
}
