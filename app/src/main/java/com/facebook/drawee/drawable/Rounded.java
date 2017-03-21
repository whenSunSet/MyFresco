package com.facebook.drawee.drawable;

/**
 * Created by heshixiyang on 2017/3/10.
 */
/**
 * 一个可以实现圆角或者圆环的Drawable的接口
 * Interface for Drawables that round corners or form a circle.
 */
public interface Rounded {

    void setCircle(boolean isCircle);
    boolean isCircle();

    void setRadius(float radius);
    void setRadii(float[] radii);
    float[] getRadii();

    void setBorder(int color, float width);
    int getBorderColor();
    float getBorderWidth();

    void setPadding(float padding);
    float getPadding();
}
