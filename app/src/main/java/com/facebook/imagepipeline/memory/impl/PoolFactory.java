package com.facebook.imagepipeline.memory.impl;

/**
 * Created by heshixiyang on 2017/3/16.
 */

import com.facebook.commom.internal.Preconditions;
import com.facebook.imagepipeline.memory.ByteArrayPool;
import com.facebook.imagepipeline.memory.PooledByteBufferFactory;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * 一个Pool的工厂，里面有所有的Pool
 * Factory class for pools.
 */
@NotThreadSafe
public class PoolFactory {

    private final PoolConfig mConfig;

    private BitmapPool mBitmapPool;
    private FlexByteArrayPool mFlexByteArrayPool;
    private NativeMemoryChunkPool mNativeMemoryChunkPool;
    private PooledByteBufferFactory mPooledByteBufferFactory;
    private PooledByteStreams mPooledByteStreams;
    private SharedByteArray mSharedByteArray;
    private ByteArrayPool mSmallByteArrayPool;

    public PoolFactory(PoolConfig config) {
        mConfig = Preconditions.checkNotNull(config);
    }

    public BitmapPool getBitmapPool() {
        if (mBitmapPool == null) {
            mBitmapPool = new BitmapPool(
                    mConfig.getMemoryTrimmableRegistry(),
                    mConfig.getBitmapPoolParams(),
                    mConfig.getBitmapPoolStatsTracker());
        }
        return mBitmapPool;
    }

    public FlexByteArrayPool getFlexByteArrayPool() {
        if (mFlexByteArrayPool == null) {
            mFlexByteArrayPool = new FlexByteArrayPool(
                    mConfig.getMemoryTrimmableRegistry(),
                    mConfig.getFlexByteArrayPoolParams());
        }
        return mFlexByteArrayPool;
    }

    public int getFlexByteArrayPoolMaxNumThreads() {
        return mConfig.getFlexByteArrayPoolParams().maxNumThreads;
    }

    public NativeMemoryChunkPool getNativeMemoryChunkPool() {
        if (mNativeMemoryChunkPool == null) {
            mNativeMemoryChunkPool = new NativeMemoryChunkPool(
                    mConfig.getMemoryTrimmableRegistry(),
                    mConfig.getNativeMemoryChunkPoolParams(),
                    mConfig.getNativeMemoryChunkPoolStatsTracker());
        }
        return mNativeMemoryChunkPool;
    }

    public PooledByteBufferFactory getPooledByteBufferFactory() {
        if (mPooledByteBufferFactory == null) {
            mPooledByteBufferFactory = new NativePooledByteBufferFactory(
                    getNativeMemoryChunkPool(),
                    getPooledByteStreams());
        }
        return mPooledByteBufferFactory;
    }

    public PooledByteStreams getPooledByteStreams() {
        if (mPooledByteStreams == null) {
            mPooledByteStreams = new PooledByteStreams(getSmallByteArrayPool());
        }
        return mPooledByteStreams;
    }

    public SharedByteArray getSharedByteArray() {
        if (mSharedByteArray == null) {
            mSharedByteArray = new SharedByteArray(
                    mConfig.getMemoryTrimmableRegistry(),
                    mConfig.getFlexByteArrayPoolParams());
        }
        return mSharedByteArray;
    }

    public ByteArrayPool getSmallByteArrayPool() {
        if (mSmallByteArrayPool == null) {
            mSmallByteArrayPool = new GenericByteArrayPool(
                    mConfig.getMemoryTrimmableRegistry(),
                    mConfig.getSmallByteArrayPoolParams(),
                    mConfig.getSmallByteArrayPoolStatsTracker());
        }
        return mSmallByteArrayPool;
    }
}
