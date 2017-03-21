package com.facebook.imagepipeline.nativecode;

/**
 * Created by Administrator on 2017/3/17 0017.
 */

import com.facebook.commom.internal.DoNotStrip;
import com.facebook.commom.internal.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 改造jpeg images的帮助方法
 * Helper methods for modifying jpeg images.
 */
@DoNotStrip
public class JpegTranscoder {

    static {
        ImagePipelineNativeLoader.load();
    }

    public static final int MIN_QUALITY = 0;
    public static final int MAX_QUALITY = 100;
    public static final int MIN_SCALE_NUMERATOR = 1;
    public static final int MAX_SCALE_NUMERATOR = 16;
    public static final int SCALE_DENOMINATOR = 8;

    /**
     * @return true if and only if given number of degrees is allowed rotation angle, that is
     *   it is equal to 0, 90, 180 or 270
     */
    public static boolean isRotationAngleAllowed(int degrees) {
        return (degrees >= 0) && (degrees <= 270) && (degrees % 90 == 0);
    }

    /**
     * Downscales and rotates jpeg image
     *
     * @param inputStream
     * @param outputStream
     * @param rotationAngle 0, 90, 180 or 270
     * @param scaleNumerator 1 - 16, image will be scaled using scaleNumerator/8 factor
     * @param quality 1 - 100
     */
    public static void transcodeJpeg(
            final InputStream inputStream,
            final OutputStream outputStream,
            final int rotationAngle,
            final int scaleNumerator,
            final int quality) throws IOException {
        Preconditions.checkArgument(scaleNumerator >= MIN_SCALE_NUMERATOR);
        Preconditions.checkArgument(scaleNumerator <= MAX_SCALE_NUMERATOR);
        Preconditions.checkArgument(quality >= MIN_QUALITY);
        Preconditions.checkArgument(quality <= MAX_QUALITY);
        Preconditions.checkArgument(isRotationAngleAllowed(rotationAngle));
        Preconditions.checkArgument(
                scaleNumerator != SCALE_DENOMINATOR || rotationAngle != 0,
                "no transformation requested");
        nativeTranscodeJpeg(
                Preconditions.checkNotNull(inputStream),
                Preconditions.checkNotNull(outputStream),
                rotationAngle,
                scaleNumerator,
                quality);
    }

    @DoNotStrip
    private static native void nativeTranscodeJpeg(
            InputStream inputStream,
            OutputStream outputStream,
            int rotationAngle,
            int scaleNominator,
            int quality) throws IOException;
}
