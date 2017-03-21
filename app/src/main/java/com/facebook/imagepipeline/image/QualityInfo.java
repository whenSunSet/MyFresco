package com.facebook.imagepipeline.image;

/**
 * Created by heshixiyang on 2017/3/9.
 */
/**
 * 图片信息质量接口
 * Interface for image quality information
 */
public interface QualityInfo {

    /**
     * 只用于比较两个图像质量指向相同的资源(uri)
     * Used only to compare quality of two images that points to the same resource (uri).
     * 越高的数字表示质量越高
     * <p> Higher number means higher quality.
     *
     * 这是为了确定是否新的结果比原来已经在缓存中的结果质量更高而使用的
     * <p> This is useful for caching in order to determine whether the new result is of higher
     * quality than what's already in the cache.
     */
    int getQuality();

    /**
     * 是否这个图片的质量已经足够
     * Whether the image is of good-enough quality.
     * 当去获取过程式图片的时候，最开始的几次图片质量可能很差
     * 但是最终他们会非常接近原图，我们标记那些已经足够好的。
     * <p> When fetching image progressively, the few first results can be of really poor quality,
     * but eventually, they get really close to original image, and we mark those as good-enough.
     */
    boolean isOfGoodEnoughQuality();

    /**
     * 是否图像质量已经完整
     * Whether the image is of full quality.
     * 对于渐进式图片，他有一个最终的扫描，对于一般的图片种类，他总是true
     * <p> For progressive JPEGs, this is the final scan. For other image types, this is always true.
     */
    boolean isOfFullQuality();

}
