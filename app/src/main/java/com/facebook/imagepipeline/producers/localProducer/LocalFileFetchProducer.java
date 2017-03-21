package com.facebook.imagepipeline.producers.localProducer;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.memory.PooledByteBufferFactory;
import com.facebook.imagepipeline.request.impl.ImageRequest;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.Executor;

/**
 * 代表一个本地文件的获取producer
 * Represents a local file fetch producer.
 */
public class LocalFileFetchProducer extends LocalFetchProducer {

    public static final String PRODUCER_NAME = "LocalFileFetchProducer";

    public LocalFileFetchProducer(
            Executor executor,
            PooledByteBufferFactory pooledByteBufferFactory) {
        super(executor, pooledByteBufferFactory);
    }

    @Override
    protected EncodedImage getEncodedImage(final ImageRequest imageRequest) throws IOException {
        return getEncodedImage(
                new FileInputStream(imageRequest.getSourceFile().toString()),
                (int) imageRequest.getSourceFile().length());
    }

    @Override
    protected String getProducerName() {
        return PRODUCER_NAME;
    }
}
