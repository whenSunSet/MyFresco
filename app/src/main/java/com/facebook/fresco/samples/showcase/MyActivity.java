package com.facebook.fresco.samples.showcase;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.Pools;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.facebook.R;
import com.facebook.commom.references.CloseableReference;
import com.facebook.commom.util.SDCardUtils;
import com.facebook.imagepipeline.animated.base.impl.AnimatedDrawable;
import com.facebook.imagepipeline.animated.factory.AnimatedDrawableFactory;
import com.facebook.imagepipeline.animated.factory.AnimatedFactory;
import com.facebook.imagepipeline.animated.factory.AnimatedImageFactory;
import com.facebook.imagepipeline.animated.factory.impl.AnimatedFactoryProvider;
import com.facebook.imagepipeline.bitmaps.ArtBitmapFactory;
import com.facebook.imagepipeline.bitmaps.EmptyJpegGenerator;
import com.facebook.imagepipeline.bitmaps.GingerbreadBitmapFactory;
import com.facebook.imagepipeline.bitmaps.HoneycombBitmapFactory;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.core.impl.DefaultExecutorSupplier;
import com.facebook.imagepipeline.decoder.ImageDecoder;
import com.facebook.imagepipeline.decoder.impl.DefaultImageDecoder;
import com.facebook.imagepipeline.image.impl.CloseableImage;
import com.facebook.imagepipeline.image.impl.EncodedImage;
import com.facebook.imagepipeline.image.impl.ImmutableQualityInfo;
import com.facebook.imagepipeline.memory.PooledByteBuffer;
import com.facebook.imagepipeline.memory.PooledByteBufferFactory;
import com.facebook.imagepipeline.memory.impl.PoolConfig;
import com.facebook.imagepipeline.memory.impl.PoolFactory;
import com.facebook.imagepipeline.platform.ArtDecoder;
import com.facebook.imagepipeline.platform.GingerbreadPurgeableDecoder;
import com.facebook.imagepipeline.platform.KitKatPurgeableDecoder;
import com.facebook.imagepipeline.platform.PlatformDecoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MyActivity extends AppCompatActivity {

    private AnimatedDrawableFactory mAnimatedDrawableFactory;
    private AnimatedImageFactory mAnimatedImageFactory;
    private AnimatedFactory mAnimatedFactory;
    private PoolFactory mPoolFactory;
    private PlatformDecoder mPlatformDecoder;
    private PlatformBitmapFactory mPlatformBitmapFactory;
    private ImageDecoder mImageDecoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        mPoolFactory=new PoolFactory(PoolConfig.newBuilder().build());
        mPlatformDecoder=buildPlatformDecoder(mPoolFactory,true);
        mPlatformBitmapFactory=buildPlatformBitmapFactory(
                mPoolFactory
                ,mPlatformDecoder);
        mAnimatedFactory = AnimatedFactoryProvider.getAnimatedFactory(
                mPlatformBitmapFactory,
                new DefaultExecutorSupplier(2) );

        mAnimatedImageFactory=mAnimatedFactory.getAnimatedImageFactory();
        mAnimatedDrawableFactory=mAnimatedFactory.getAnimatedDrawableFactory(this);

        mImageDecoder = new DefaultImageDecoder(
                mAnimatedImageFactory,
                mPlatformDecoder,
                Bitmap.Config.ARGB_8888);

        CloseableReference<PooledByteBuffer> ref = null;
        try {
//            File file=new File(SDCardUtils.getCacheDir(this)+"/1.jpg");
//            File file=new File(SDCardUtils.getCacheDir(this)+"/2.png");
//            File file=new File(SDCardUtils.getCacheDir(this)+"/3.gif");
            File file=new File(SDCardUtils.getCacheDir(this)+"/4.webp");
//            File file=new File(SDCardUtils.getCacheDir(this)+"/5.webp");
            FileInputStream fileInputStream=new FileInputStream(file);
            PooledByteBufferFactory pooledByteBufferFactory=mPoolFactory.getPooledByteBufferFactory();
            PooledByteBuffer pooledByteBuffer=pooledByteBufferFactory.newByteBuffer(fileInputStream);
            ref = CloseableReference.of(pooledByteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        EncodedImage encodedImage=new EncodedImage(ref);

        CloseableImage image=mImageDecoder.decode(encodedImage,encodedImage.getSize(), ImmutableQualityInfo.FULL_QUALITY, ImageDecodeOptions.newBuilder().build());

        AnimatedDrawable animatedDrawable=(AnimatedDrawable)mAnimatedDrawableFactory.create(image);
        ImageView imageView=(ImageView)findViewById(R.id.testImageView);
        imageView.setImageDrawable(animatedDrawable);
        animatedDrawable.start();
    }


    public static PlatformBitmapFactory buildPlatformBitmapFactory(
            PoolFactory poolFactory,
            PlatformDecoder platformDecoder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new ArtBitmapFactory(poolFactory.getBitmapPool());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return new HoneycombBitmapFactory(
                    new EmptyJpegGenerator(poolFactory.getPooledByteBufferFactory()),
                    platformDecoder);
        } else {
            return new GingerbreadBitmapFactory();
        }
    }

    public static PlatformDecoder buildPlatformDecoder(
            PoolFactory poolFactory,
            boolean directWebpDirectDecodingEnabled) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int maxNumThreads = poolFactory.getFlexByteArrayPoolMaxNumThreads();
            return new ArtDecoder(
                    poolFactory.getBitmapPool(),
                    maxNumThreads,
                    new Pools.SynchronizedPool<>(maxNumThreads));
        } else {
            if (directWebpDirectDecodingEnabled
                    && Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                return new GingerbreadPurgeableDecoder();
            } else {
                return new KitKatPurgeableDecoder(poolFactory.getFlexByteArrayPool());
            }
        }
    }
}
