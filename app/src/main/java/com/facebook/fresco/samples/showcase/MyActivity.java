package com.facebook.fresco.samples.showcase;

import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.Pools;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.facebook.R;
import com.facebook.commom.references.CloseableReference;
import com.facebook.commom.util.SDCardUtils;
import com.facebook.imagepipeline.animated.base.impl.AnimatedDrawable;
import com.facebook.imagepipeline.animated.factory.AnimatedDrawableFactory;
import com.facebook.imagepipeline.animated.factory.AnimatedFactory;
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
import com.facebook.imagepipeline.image.impl.CloseableStaticBitmap;
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
import java.io.FileNotFoundException;
import java.io.IOException;

public class MyActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);

        initView();
        initFresco();

        pngButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseableImage closeableImage=initCloseableImage("1.png");
                if (closeableImage!=null)imageView.setImageBitmap(((CloseableStaticBitmap)closeableImage).getUnderlyingBitmap());
            }
        });
        jpgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseableImage closeableImage=initCloseableImage("2.jpg");
                if (closeableImage!=null)imageView.setImageBitmap(((CloseableStaticBitmap)closeableImage).getUnderlyingBitmap());
            }
        });
        staticWebpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseableImage closeableImage=initCloseableImage("3.webp");
                if (closeableImage!=null)imageView.setImageBitmap(((CloseableStaticBitmap)closeableImage).getUnderlyingBitmap());
            }
        });
        dynamicWebpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseableImage closeableImage=initCloseableImage("4.webp");
                AnimatedDrawable animatedDrawable=(AnimatedDrawable)mAnimatedDrawableFactory.create(closeableImage);
                if (closeableImage!=null)imageView.setImageDrawable(animatedDrawable);
                animatedDrawable.start();
            }
        });
        gifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseableImage closeableImage=initCloseableImage("5.gif");
                AnimatedDrawable animatedDrawable=(AnimatedDrawable)mAnimatedDrawableFactory.create(closeableImage);
                if (closeableImage!=null)imageView.setImageDrawable(animatedDrawable);
                ValueAnimator valueAnimator=animatedDrawable.createValueAnimator();
                valueAnimator.start();
            }
        });
    }

    Button pngButton;
    Button jpgButton;
    Button staticWebpButton;
    Button dynamicWebpButton;
    Button gifButton;
    ImageView imageView;
    private void initView(){
        pngButton=(Button)findViewById(R.id.setPng);
        jpgButton=(Button)findViewById(R.id.setJpg);
        staticWebpButton=(Button)findViewById(R.id.setStaticWebp);
        dynamicWebpButton=(Button)findViewById(R.id.setDynamicWebp);
        gifButton=(Button)findViewById(R.id.setGif);
        imageView=(ImageView)findViewById(R.id.testImageView);
    }

    AnimatedDrawableFactory mAnimatedDrawableFactory;
    ImageDecoder mImageDecoder;
    PooledByteBufferFactory pooledByteBufferFactory;
    private void initFresco(){
        PoolFactory poolFactory=new PoolFactory(PoolConfig.newBuilder().build());
        PlatformDecoder mPlatformDecoder=buildPlatformDecoder(poolFactory,true);
        PlatformBitmapFactory mPlatformBitmapFactory=buildPlatformBitmapFactory(
                poolFactory
                ,mPlatformDecoder);
        AnimatedFactory mAnimatedFactory= AnimatedFactoryProvider.getAnimatedFactory(
                mPlatformBitmapFactory,
                new DefaultExecutorSupplier(2) );

        mImageDecoder = new DefaultImageDecoder(
                mAnimatedFactory.getAnimatedImageFactory(),
                mPlatformDecoder,
                Bitmap.Config.ARGB_8888);

        mAnimatedDrawableFactory=mAnimatedFactory.getAnimatedDrawableFactory(this);
        pooledByteBufferFactory=poolFactory.getPooledByteBufferFactory();
    }

    private CloseableImage initCloseableImage(String fileName){
        CloseableReference<PooledByteBuffer> ref;
        File file=new File(SDCardUtils.getCacheDir(MyActivity.this)+"/"+fileName);
        FileInputStream fileInputStream;
        PooledByteBuffer pooledByteBuffer=null;
        try {
            if (file==null)return null;
            fileInputStream=new FileInputStream(file);
            pooledByteBuffer=pooledByteBufferFactory.newByteBuffer(fileInputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ref = CloseableReference.of(pooledByteBuffer);
        EncodedImage encodedImage=new EncodedImage(ref);
        CloseableImage image=mImageDecoder.decode(encodedImage,encodedImage.getSize(),ImmutableQualityInfo.FULL_QUALITY, ImageDecodeOptions.newBuilder().build());
        return image;
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
