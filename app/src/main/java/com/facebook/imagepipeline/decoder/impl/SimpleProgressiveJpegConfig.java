package com.facebook.imagepipeline.decoder.impl;

/**
 * Created by heshixiyang on 2017/3/17.
 */

import com.facebook.commom.internal.Preconditions;
import com.facebook.imagepipeline.decoder.ProgressiveJpegConfig;
import com.facebook.imagepipeline.image.impl.ImmutableQualityInfo;
import com.facebook.imagepipeline.image.QualityInfo;

import java.util.Collections;
import java.util.List;

/**
 * 简单的 {@link ProgressiveJpegConfig}配置，
 * Simple {@link ProgressiveJpegConfig} with predefined scans to decode and good-enough scan number.
 *
 * 如果没有提供具体的扫描解码,每个扫描允许解码。
 * <p/> If no specific scans to decode are provided, every scan is allowed to be decoded.
 */
public class SimpleProgressiveJpegConfig implements ProgressiveJpegConfig {
    public interface DynamicValueConfig {
        List<Integer> getScansToDecode();
        int getGoodEnoughScanNumber();
    }

    private static class DefaultDynamicValueConfig implements DynamicValueConfig {
        public List<Integer> getScansToDecode() {
            return Collections.EMPTY_LIST;
        }

        public int getGoodEnoughScanNumber() {
            return 0;
        }
    }

    private final DynamicValueConfig mDynamicValueConfig;

    public SimpleProgressiveJpegConfig() {
        this (new DefaultDynamicValueConfig());
    }



    public SimpleProgressiveJpegConfig(DynamicValueConfig dynamicValueConfig) {
        mDynamicValueConfig = Preconditions.checkNotNull(dynamicValueConfig);
    }

    @Override
    public int getNextScanNumberToDecode(int scanNumber) {
        final List<Integer> scansToDecode = mDynamicValueConfig.getScansToDecode();
        if (scansToDecode == null || scansToDecode.isEmpty()) {
            return scanNumber + 1;
        }

        for (int i = 0; i < scansToDecode.size(); i++) {
            if (scansToDecode.get(i) > scanNumber) {
                return scansToDecode.get(i);
            }
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public QualityInfo getQualityInfo(int scanNumber) {
        return ImmutableQualityInfo.of(
                scanNumber,
        /* isOfGoodEnoughQuality */ scanNumber >= mDynamicValueConfig.getGoodEnoughScanNumber(),
        /* isOfFullQuality */ false);
    }
}
