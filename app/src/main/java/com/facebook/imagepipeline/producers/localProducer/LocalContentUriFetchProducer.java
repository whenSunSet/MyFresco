package com.facebook.imagepipeline.producers.localProducer;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;

import com.facebook.commom.util.UriUtil;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.memory.PooledByteBufferFactory;
import com.facebook.imagepipeline.request.impl.ImageRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

/**
 * 从本地Uri中获取数据
 * Represents a local content Uri fetch producer.
 */
public class LocalContentUriFetchProducer extends LocalFetchProducer {

    public static final String PRODUCER_NAME = "LocalContentUriFetchProducer";

    private static final String[] PROJECTION = new String[] {
            MediaStore.Images.Media._ID,
            MediaStore.Images.ImageColumns.DATA
    };

    private final ContentResolver mContentResolver;

    public LocalContentUriFetchProducer(
            Executor executor,
            PooledByteBufferFactory pooledByteBufferFactory,
            ContentResolver contentResolver) {
        super(executor, pooledByteBufferFactory);
        mContentResolver = contentResolver;
    }

    @Override
    protected EncodedImage getEncodedImage(ImageRequest imageRequest) throws IOException {
        Uri uri = imageRequest.getSourceUri();
        if (UriUtil.isLocalContactUri(uri)) {
            final InputStream inputStream;
            if (uri.toString().endsWith("/photo")) {
                inputStream =  mContentResolver.openInputStream(uri);
            } else {
                inputStream = ContactsContract.Contacts.openContactPhotoInputStream(mContentResolver, uri);
                if (inputStream == null) {
                    throw new IOException("Contact photo does not exist: " + uri);
                }
            }
            // If a Contact URI is provided, use the special helper to open that contact's photo.
            return getEncodedImage(
                    inputStream,
                    EncodedImage.UNKNOWN_STREAM_SIZE);
        }

        if (UriUtil.isLocalCameraUri(uri)) {
            EncodedImage cameraImage = getCameraImage(uri);
            if (cameraImage != null) {
                return cameraImage;
            }
        }

        return getEncodedImage(
                mContentResolver.openInputStream(uri),
                EncodedImage.UNKNOWN_STREAM_SIZE);
    }

    private @Nullable
    EncodedImage getCameraImage(Uri uri) throws IOException {
        Cursor cursor = mContentResolver.query(uri, PROJECTION, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToFirst();
            final String pathname =
                    cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            if (pathname != null) {
                return getEncodedImage(new FileInputStream(pathname), getLength(pathname));
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    private static int getLength(String pathname) {
        return pathname == null ? -1 : (int) new File(pathname).length();
    }

    @Override
    protected String getProducerName() {
        return PRODUCER_NAME;
    }
}
