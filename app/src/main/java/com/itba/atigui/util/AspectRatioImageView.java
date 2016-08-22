package com.itba.atigui.util;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ImageView;

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

    public void clear() {
        setImageBitmap(null);
        setImageDrawable(null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getDrawable() == null || getParent() == null) {
            setMeasuredDimension(0, 0);
            return;
        }
        ViewGroup parent = (ViewGroup) getParent();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = width * getDrawable().getIntrinsicHeight() / getDrawable().getIntrinsicWidth();

        Rect rect = new Rect();
        parent.getGlobalVisibleRect(rect);
        if (width > rect.width() || height > rect.height()) {
            height = MeasureSpec.getSize(heightMeasureSpec);
            width = height * getDrawable().getIntrinsicWidth() / getDrawable().getIntrinsicHeight();
        }

        setMeasuredDimension(width, height);
    }
//    endregion
}