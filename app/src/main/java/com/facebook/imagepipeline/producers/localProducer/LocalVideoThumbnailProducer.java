package com.facebook.imagepipeline.producers.localProducer;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.facebook.commom.internal.ImmutableMap;
import com.facebook.commom.internal.VisibleForTesting;
import com.facebook.commom.references.CloseableReference;
import com.facebook.imagepipeline.bitmaps.SimpleBitmapReleaser;
import com.facebook.imagepipeline.image.impl.CloseableImage;
import com.facebook.imagepipeline.image.impl.CloseableStaticBitmap;
import com.facebook.imagepipeline.image.impl.ImmutableQualityInfo;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.facebook.imagepipeline.producers.ProducerListener;
import com.facebook.imagepipeline.producers.base.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.base.StatefulProducerRunnable;
import com.facebook.imagepipeline.request.impl.ImageRequest;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * A producer that creates video thumbnails.
 *
 * <p>At present, these thumbnails are created on the java heap rather than being pinned
 * purgeables. This is deemed okay as the thumbnails are only very small.
 */
public class LocalVideoThumbnailProducer implements
        Producer<CloseableReference<CloseableImage>> {

    public static final String PRODUCER_NAME = "VideoThumbnailProducer";
    @VisibleForTesting
    static final String CREATED_THUMBNAIL = "createdThumbnail";

    private final Executor mExecutor;

    public LocalVideoThumbnailProducer(Executor executor) {
        mExecutor = executor;
    }

    @Override
    public void produceResults(
            final Consumer<CloseableReference<CloseableImage>> consumer,
            final ProducerContext producerContext) {

        final ProducerListener listener = producerContext.getListener();
        final String requestId = producerContext.getId();
        final ImageRequest imageRequest = producerContext.getImageRequest();
        final StatefulProducerRunnable cancellableProducerRunnable =
                new StatefulProducerRunnable<CloseableReference<CloseableImage>>(
                        consumer,
                        listener,
                        PRODUCER_NAME,
                        requestId) {
                    @Override
                    protected CloseableReference<CloseableImage> getResult() throws Exception {
                        Bitmap thumbnailBitmap = ThumbnailUtils.createVideoThumbnail(
                                imageRequest.getSourceFile().getPath(),
                                calculateKind(imageRequest));
                        if (thumbnailBitmap == null) {
                            return null;
                        }

                        return CloseableReference.<CloseableImage>of(
                                new CloseableStaticBitmap(
                                        thumbnailBitmap,
                                        SimpleBitmapReleaser.getInstance(),
                                        ImmutableQualityInfo.FULL_QUALITY,
                                        0));
                    }

                    @Override
                    protected Map<String, String> getExtraMapOnSuccess(
                            final CloseableReference<CloseableImage> result) {
                        return ImmutableMap.of(CREATED_THUMBNAIL, String.valueOf(result != null));
                    }

                    @Override
                    protected void disposeResult(CloseableReference<CloseableImage> result) {
                        CloseableReference.closeSafely(result);
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

    private static int calculateKind(ImageRequest imageRequest) {
        if (imageRequest.getPreferredWidth() > 96 || imageRequest.getPreferredHeight() > 96) {
            return MediaStore.Images.Thumbnails.MINI_KIND;
        }
        return MediaStore.Images.Thumbnails.MICRO_KIND;
    }
}
