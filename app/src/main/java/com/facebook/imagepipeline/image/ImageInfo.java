package com.facebook.imagepipeline.image;

/**
 * Created by heshixiyang on 2017/3/9.
 */
/**
 * Interface containing information about an image.
 */
public interface ImageInfo {

    /**
     * @return width of the image
     */
    int getWidth();

    /**
     * @return height of the image
     */
    int getHeight();

    /**
     * @return quality information for the image
     */
    QualityInfo getQualityInfo();
}
