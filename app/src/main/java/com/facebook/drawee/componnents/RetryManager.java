package com.facebook.drawee.componnents;

/**
 * Created by heshixiyang on 2017/3/19.
 */
/**
 * 尝试点击重新加载图片的Manages，默认最大4次。
 * Manages retries for an image.
 */
public class RetryManager {
    private static final int MAX_TAP_TO_RETRY_ATTEMPTS = 4;

    private boolean mTapToRetryEnabled;
    private int mMaxTapToRetryAttempts;
    private int mTapToRetryAttempts;

    public RetryManager() {
        init();
    }

    public static RetryManager newInstance() {
        return new RetryManager();
    }

    /**
     * Initializes component to its initial state.
     */
    public void init() {
        mTapToRetryEnabled = false;
        mMaxTapToRetryAttempts = MAX_TAP_TO_RETRY_ATTEMPTS;
        reset();
    }

    /**
     * Resets component.
     * This will reset the number of attempts.
     */
    public void reset() {
        mTapToRetryAttempts = 0;
    }

    public boolean isTapToRetryEnabled() {
        return mTapToRetryEnabled;
    }

    public void setTapToRetryEnabled(boolean tapToRetryEnabled) {
        mTapToRetryEnabled = tapToRetryEnabled;
    }

    public void setMaxTapToRetryAttemps(int maxTapToRetryAttemps) {
        this.mMaxTapToRetryAttempts = maxTapToRetryAttemps;
    }

    public boolean shouldRetryOnTap() {
        return mTapToRetryEnabled && mTapToRetryAttempts < mMaxTapToRetryAttempts;
    }

    public void notifyTapToRetry() {
        mTapToRetryAttempts++;
    }
}

