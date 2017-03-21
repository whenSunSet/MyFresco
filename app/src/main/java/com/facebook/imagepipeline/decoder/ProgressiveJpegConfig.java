package com.facebook.imagepipeline.decoder;

/**
 * Created by heshixiyang on 2017/3/9.
 */

import com.facebook.imagepipeline.image.QualityInfo;

/**
 * 渐进式JPEG配置
 * Progressive JPEG config.
 */
public interface ProgressiveJpegConfig {

    /**
     * 在获取下一步scan-number之前先解码
     * Gets the next scan-number that should be decoded after the given scan-number.
     */
    int getNextScanNumberToDecode(int scanNumber);

    /**
     * 获取信息质量通过被给予的scan-number
     * Gets the quality information for the given scan-number.
     */
    QualityInfo getQualityInfo(int scanNumber);
}
