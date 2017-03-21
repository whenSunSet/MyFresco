package com.facebook.commom.internal;

/**
 * Created by heshixiyang on 2017/3/12.
 */

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 不可变的set
 * A dummy representation of an immutable set. This can be used temporarily as a type until we have
 * an actual non-gauva implementation.
 */
public class ImmutableSet<E> extends HashSet<E> {

    // Prevent direct instantiation.
    private ImmutableSet(Set<E> set) {
        super(set);
    }

    public static <E> ImmutableSet<E> copyOf(Set<E> set) {
        return new ImmutableSet<>(set);
    }

    public static <E> ImmutableSet<E> of(E... elements) {
        HashSet<E> set = new HashSet<>();
        Collections.addAll(set, elements);
        return new ImmutableSet<>(set);
    }
}
