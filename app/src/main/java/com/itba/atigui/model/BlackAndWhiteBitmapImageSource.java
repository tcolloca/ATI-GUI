package com.itba.atigui.model;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.goodengineer.atibackend.ImageSource;
import com.goodengineer.atibackend.ImageUtils;
import com.itba.atigui.util.BitmapUtils;

public class BlackAndWhiteBitmapImageSource implements ImageSource {

    private Bitmap bitmap;
    private SingleBandImageSource singleBandImageSource;

    public BlackAndWhiteBitmapImageSource(Bitmap bitmap) {
        if (!BitmapUtils.isBlackAndWhiteBitmap(bitmap)) {
            throw new IllegalArgumentException("Bitmap is not black and white!");
        }
        this.bitmap = bitmap;
        singleBandImageSource = new SingleBandImageSource(new ColorBand(getWidth(), getHeight()));
        ImageUtils.copy(this, singleBandImageSource);
    }

    /**
     *
     * @return is going to be a value between 0 and 255 only if normalize() was called before.
     */
    @Override
    public int getPixel(int x, int y) {
//        since this is a black and white image, we can return the value of just one of the bands
        return Color.blue(bitmap.getPixel(x, y));
    }

    /**
     *
     * @param color can be any value. it will be normalized later
     */
    @Override
    public void setPixel(int x, int y, int color) {
        singleBandImageSource.setPixel(x, y, color);
    }

    @Override
    public int getWidth() {
        return bitmap.getWidth();
    }

    @Override
    public int getHeight() {
        return bitmap.getHeight();
    }

    @Override
    public void dispose() {
        bitmap.recycle();
        singleBandImageSource.dispose();
        bitmap = null;
        singleBandImageSource = null;
    }

    @Override
    public void normalize() {
        singleBandImageSource.normalize();
        for (int x = 0; x < getWidth(); x++) {
            for (int y = 0; y < getHeight(); y++) {
//                creates black and white pixel
                int color = singleBandImageSource.getPixel(x, y);
                int pixel = Color.rgb(color, color, color);
                bitmap.setPixel(x, y, pixel);
            }
        }
    }

    @Override
    public ImageSource copy() {
        return new BlackAndWhiteBitmapImageSource(bitmap.copy(bitmap.getConfig(), true));
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
