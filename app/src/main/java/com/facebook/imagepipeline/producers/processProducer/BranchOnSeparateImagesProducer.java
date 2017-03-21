package com.facebook.imagepipeline.producers.processProducer;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.Producer;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.facebook.imagepipeline.producers.base.DelegatingConsumer;
import com.facebook.imagepipeline.producers.util.ThumbnailSizeChecker;
import com.facebook.imagepipeline.request.impl.ImageRequest;

/**
 * Producer that coordinates fetching two separate images.
 *
 * <p>The first producer is kicked off, and once it has returned all its results, the second
 * producer is kicked off if necessary.
 */
public class BranchOnSeparateImagesProducer
        implements Producer<EncodedImage> {
    private final Producer<EncodedImage> mInputProducer1;
    private final Producer<EncodedImage> mInputProducer2;

    public BranchOnSeparateImagesProducer(
            Producer<EncodedImage> inputProducer1, Producer<EncodedImage> inputProducer2) {
        mInputProducer1 = inputProducer1;
        mInputProducer2 = inputProducer2;
    }

    @Override
    public void produceResults(
            Consumer<EncodedImage> consumer,
            ProducerContext context) {
        OnFirstImageConsumer onFirstImageConsumer = new OnFirstImageConsumer(consumer, context);
        mInputProducer1.produceResults(onFirstImageConsumer, context);
    }

    private class OnFirstImageConsumer extends DelegatingConsumer<EncodedImage, EncodedImage> {

        private ProducerContext mProducerContext;

        private OnFirstImageConsumer(
                Consumer<EncodedImage> consumer,
                ProducerContext producerContext) {
            super(consumer);
            mProducerContext = producerContext;
        }

        @Override
        protected void onNewResultImpl(EncodedImage newResult, boolean isLast) {
            ImageRequest request = mProducerContext.getImageRequest();
            boolean isGoodEnough =
                    ThumbnailSizeChecker.isImageBigEnough(newResult, request.getResizeOptions());
            if (newResult != null && (isGoodEnough || request.getLocalThumbnailPreviewsEnabled())) {
                getConsumer().onNewResult(newResult, isLast && isGoodEnough);
            }
            if (isLast && !isGoodEnough) {
                EncodedImage.closeSafely(newResult);

                mInputProducer2.produceResults(getConsumer(), mProducerContext);
            }
        }

        @Override
        protected void onFailureImpl(Throwable t) {
            mInputProducer2.produceResults(getConsumer(), mProducerContext);
        }
    }
}

