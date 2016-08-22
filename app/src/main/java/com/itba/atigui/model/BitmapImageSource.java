package com.itba.atigui.model;

import android.graphics.Bitmap;

import com.goodengineer.atibackend.ImageSource;

public class BitmapImageSource implements ImageSource {

    private Bitmap bitmap;

    public BitmapImageSource(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    @Override
    public int getPixel(int x, int y) {
        return bitmap.getPixel(x, y);
    }

    @Override
    public void setPixel(int x, int y, int color) {
        bitmap.setPixel(x, y, color);
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
    }

    @Override
    public ImageSource copy() {
        return new BitmapImageSource(bitmap.copy(bitmap.getConfig(), true));
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
