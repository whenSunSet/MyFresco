package com.facebook.imagepipeline.memory.impl;

/**
 * Created by heshixiyang on 2017/3/17.
 */

import com.facebook.commom.references.OOMSoftReference;

import java.util.LinkedList;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * 一个储存OOMSoftReferences的Bucket
 * A Bucket that uses OOMSoftReferences to store its free list.
 */
@NotThreadSafe
class OOMSoftReferenceBucket<V> extends Bucket<V> {

    private LinkedList<OOMSoftReference<V>> mSpareReferences;

    public OOMSoftReferenceBucket(int itemSize, int maxLength, int inUseLength) {
        super(itemSize, maxLength, inUseLength);
        mSpareReferences = new LinkedList<>();
    }

    @Override
    public V pop() {
        OOMSoftReference<V> ref = (OOMSoftReference<V>) mFreeList.poll();
        V value = ref.get();
        ref.clear();
        mSpareReferences.add(ref);
        return value;
    }

    @Override
    void addToFreeList(V value) {
        OOMSoftReference<V> ref = mSpareReferences.poll();
        if (ref == null) {
            ref = new OOMSoftReference<>();
        }
        ref.set(value);
        mFreeList.add(ref);
    }
}
