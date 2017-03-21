package com.facebook.commom.memory;

/**
 * Created by heshixiyang on 2017/3/9.
 */
public enum MemoryTrimType {

    /**
     * 应用程序正在接近设备特定的Java堆限制
     * */
    OnCloseToDalvikHeapLimit(0.5),

    /**
     * app在前台并且系统内存达到很低的水平
     * */
    OnSystemLowMemoryWhileAppInForeground(0.5),

    /**
     * app在后台并且系统内存达到一个很低的水平
     *  */
    OnSystemLowMemoryWhileAppInBackground(1),

    /**
     * 这个app进入了后台，通常是用户打开了另一个app
     * */
    OnAppBackgrounded(1);

    //建议的内存比率
    private double mSuggestedTrimRatio;

    private MemoryTrimType(double suggestedTrimRatio) {
        mSuggestedTrimRatio = suggestedTrimRatio;
    }

    /** 当接收到对应的事件的时候所建议的内存比率. */
    public double getSuggestedTrimRatio () {
        return mSuggestedTrimRatio;
    }
}
