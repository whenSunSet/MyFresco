package com.facebook.imagepipeline.producers.localProducer;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import android.net.Uri;
import android.util.Base64;

import com.facebook.commom.executors.impl.CallerThreadExecutor;
import com.facebook.commom.internal.Preconditions;
import com.facebook.commom.internal.VisibleForTesting;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.memory.PooledByteBufferFactory;
import com.facebook.imagepipeline.request.impl.ImageRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 从data URI获取图片资源
 * Producer for data URIs.
 * Data URI将图片转换成URI，他不指向某一个本地文件，该URI就是一个图片文件，其中的数据可以编码成Base-64
 * <p>Data URIs embed the data in the URI itself. They don't point to a file location;
 * the URI is the data. Data can be encoded in either base-64 or escaped ASCII.
 * See the <a href="http://tools.ietf.org/html/rfc2397">spec</a> for full details.
 *
 * Data URIs可以保存小像素图片，Java中String是在其堆上。如果是一个大的数据，那么就使用其他URI类型
 * <p>Data URIs are intended for small pieces of data only, since the URI lives on the Java
 * heap. For large data, use a another URI type.
 *
 * <p>Charsets specified in the URI are ignored. Only UTF-8 encoding is currently supported.
 */
public class DataFetchProducer extends LocalFetchProducer {

    public static final String PRODUCER_NAME = "DataFetchProducer";

    public DataFetchProducer(
            PooledByteBufferFactory pooledByteBufferFactory) {
        super(CallerThreadExecutor.getInstance(), pooledByteBufferFactory);
    }

    @Override
    protected EncodedImage getEncodedImage(ImageRequest imageRequest) throws IOException {
        byte[] data = getData(imageRequest.getSourceUri().toString());
        return getByteBufferBackedEncodedImage(new ByteArrayInputStream(data), data.length);
    }

    @Override
    protected String getProducerName() {
        return PRODUCER_NAME;
    }

    @VisibleForTesting
    static byte[] getData(String uri) {
    /*
     * Format of a data URL:
     * data:mime/type;param=value;param=value;base64,actual_data
     * everything is optional except the actual data, which is either
     * base-64 or escaped ASCII encoded.
     */
        Preconditions.checkArgument(uri.substring(0, 5).equals("data:"));
        int commaPos = uri.indexOf(',');

        String dataStr = uri.substring(commaPos + 1, uri.length());
        if (isBase64(uri.substring(0, commaPos))) {
            return Base64.decode(dataStr, Base64.DEFAULT);
        } else {
            String str = Uri.decode(dataStr);
            byte[] b = str.getBytes();
            return b;
        }
    }

    @VisibleForTesting
    static boolean isBase64(String prefix) {
        if (!prefix.contains(";")) {
            return false;
        }
        String[] parameters = prefix.split(";");
        return parameters[parameters.length - 1].equals("base64");
    }
}
