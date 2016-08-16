package com.itba.atigui.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.itba.atigui.model.PixelSelection;

public class AspectRatioImageView extends ImageView {

    //    region class related constructors/methods

    public AspectRatioImageView(Context context) {
        super(context);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0;
        int height = 0;

        if (getParent() != null) {
            ViewGroup parent = (ViewGroup) getParent();
            boolean tallParent = parent.getWidth() <= parent.getHeight();
            if (getDrawable() != null) {
                if (tallParent) {
                    width = MeasureSpec.getSize(widthMeasureSpec);
                    height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();
                } else {
                    height = MeasureSpec.getSize(heightMeasureSpec);
                    width = height * getDrawable().getIntrinsicWidth() / getDrawable().getIntrinsicHeight();
                }
            }
        }

        setMeasuredDimension(width, height);
    }
//    endregion
}