package com.facebook.imagepipeline.producers.localProducer;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.commom.internal.Closeables;
import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.memory.PooledByteBufferFactory;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.facebook.imagepipeline.producers.ProducerListener;
import com.facebook.imagepipeline.producers.base.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.base.StatefulProducerRunnable;
import com.facebook.imagepipeline.request.impl.ImageRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

/**
 * 表示一个本地资源的获取producer
 * Represents a local fetch producer.
 */
public abstract class LocalFetchProducer implements Producer<EncodedImage> {

    private final Executor mExecutor;
    private final PooledByteBufferFactory mPooledByteBufferFactory;

    protected LocalFetchProducer(
            Executor executor,
            PooledByteBufferFactory pooledByteBufferFactory) {
        mExecutor = executor;
        mPooledByteBufferFactory = pooledByteBufferFactory;
    }

    @Override
    public void produceResults(
            final Consumer<EncodedImage> consumer,
            final ProducerContext producerContext) {

        final ProducerListener listener = producerContext.getListener();
        final String requestId = producerContext.getId();
        final ImageRequest imageRequest = producerContext.getImageRequest();
        final StatefulProducerRunnable cancellableProducerRunnable =
                new StatefulProducerRunnable<EncodedImage>(
                        consumer,
                        listener,
                        getProducerName(),
                        requestId) {

                    @Override
                    protected EncodedImage getResult() throws Exception {
                        EncodedImage encodedImage = getEncodedImage(imageRequest);
                        if (encodedImage == null) {
                            return null;
                        }
                        encodedImage.parseMetaData();
                        return encodedImage;
                    }

                    @Override
                    protected void disposeResult(EncodedImage result) {
                        EncodedImage.closeSafely(result);
                    }
                };

        producerContext.addCallbacks(
                new BaseProducerContextCallbacks() {
                    @Override
                    public void onCancellationRequested() {
                        cancellableProducerRunnable.cancel();
                    }
                });
        mExecutor.execute(cancellableProducerRunnable);
    }


    /**
     * 从一个stream中创建一个encoded image。
     * Creates a memory-backed encoded image from the stream. The stream is closed. */
    protected EncodedImage getByteBufferBackedEncodedImage(
            InputStream inputStream,
            int length) throws IOException {
        CloseableReference<PooledByteBuffer> ref = null;
        try {
            if (length <= 0) {
                ref = CloseableReference.of(mPooledByteBufferFactory.newByteBuffer(inputStream));
            } else {
                ref = CloseableReference.of(mPooledByteBufferFactory.newByteBuffer(inputStream, length));
            }
            return new EncodedImage(ref);
        } finally {
            Closeables.closeQuietly(inputStream);
            CloseableReference.closeSafely(ref);
        }
    }

    protected EncodedImage getEncodedImage(
            InputStream inputStream,
            int length) throws IOException {
        return getByteBufferBackedEncodedImage(inputStream, length);
    }

    /**
     * 从本地资源中获取encoded image，其可以从FileInputStream或PooledByteBuffer中获取
     * Gets an encoded image from the local resource. It can be either backed by a FileInputStream or
     * a PooledByteBuffer
     * @param imageRequest request that includes the local resource that is being accessed
     * @throws IOException
     */
    protected abstract EncodedImage getEncodedImage(ImageRequest imageRequest) throws IOException;

    /**
     * @return name of the Producer
     */
    protected abstract String getProducerName();
}
