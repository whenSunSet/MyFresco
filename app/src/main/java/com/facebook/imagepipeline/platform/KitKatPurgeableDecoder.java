package com.facebook.imagepipeline.platform;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;

import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.memory.impl.FlexByteArrayPool;
import com.facebook.imageutils.JfifUtil;

import javax.annotation.concurrent.ThreadSafe;

/**
 * Bitmap Decoder implementation for KitKat
 *
 * <p>The MemoryFile trick used in GingerbreadPurgeableDecoder does not work in KitKat. Here, we
 * instead use Java memory to store the encoded images, but make use of a pool to minimize
 * allocations. We cannot decode from a stream, as that does not support purgeable decodes.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
@ThreadSafe
public class KitKatPurgeableDecoder extends DalvikPurgeableDecoder {
    private final FlexByteArrayPool mFlexByteArrayPool;

    public KitKatPurgeableDecoder(FlexByteArrayPool flexByteArrayPool) {
        mFlexByteArrayPool = flexByteArrayPool;
    }

    /**
     * Decodes a byteArray into a purgeable bitmap
     *
     * @param bytesRef the byte buffer that contains the encoded bytes
     * @return
     */
    @Override
    protected Bitmap decodeByteArrayAsPurgeable(
            CloseableReference<PooledByteBuffer> bytesRef,
            BitmapFactory.Options options) {
        final PooledByteBuffer pooledByteBuffer = bytesRef.get();
        final int length = pooledByteBuffer.size();
        final CloseableReference<byte[]> encodedBytesArrayRef = mFlexByteArrayPool.get(length);
        try {
            final byte[] encodedBytesArray = encodedBytesArrayRef.get();
            pooledByteBuffer.read(0, encodedBytesArray, 0, length);
            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    encodedBytesArray,
                    0,
                    length,
                    options);
            return Preconditions.checkNotNull(bitmap, "BitmapFactory returned null");
        } finally {
            CloseableReference.closeSafely(encodedBytesArrayRef);
        }
    }

    /**
     * Decodes a byteArray containing jpeg encoded bytes into a purgeable bitmap
     *
     * <p> Adds a JFIF End-Of-Image marker if needed before decoding.
     *
     * @param bytesRef the byte buffer that contains the encoded bytes
     * @return
     */
    @Override
    protected Bitmap decodeJPEGByteArrayAsPurgeable(
            CloseableReference<PooledByteBuffer> bytesRef,
            int length,
            BitmapFactory.Options options) {
        byte[] suffix = endsWithEOI(bytesRef, length) ? null : EOI;
        final PooledByteBuffer pooledByteBuffer = bytesRef.get();
        Preconditions.checkArgument(length <= pooledByteBuffer.size());
        // allocate bigger array in case EOI needs to be added
        final CloseableReference<byte[]> encodedBytesArrayRef = mFlexByteArrayPool.get(length + 2);
        try {
            byte[] encodedBytesArray = encodedBytesArrayRef.get();
            pooledByteBuffer.read(0, encodedBytesArray, 0, length);
            if (suffix != null) {
                putEOI(encodedBytesArray, length);
                length += 2;
            }
            Bitmap bitmap = BitmapFactory.decodeByteArray(
                    encodedBytesArray,
                    0,
                    length,
                    options);
            return Preconditions.checkNotNull(bitmap, "BitmapFactory returned null");
        } finally {
            CloseableReference.closeSafely(encodedBytesArrayRef);
        }
    }

    private static void putEOI(byte[] imageBytes, int offset) {
        // TODO 5884402: remove dependency on JfifUtil
        imageBytes[offset] = (byte) JfifUtil.MARKER_FIRST_BYTE;
        imageBytes[offset + 1] = (byte) JfifUtil.MARKER_EOI;
    }
}
