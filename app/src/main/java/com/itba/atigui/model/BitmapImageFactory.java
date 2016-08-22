package com.itba.atigui.model;

import android.graphics.Bitmap;

import com.goodengineer.atibackend.ImageFactory;
import com.goodengineer.atibackend.ImageSource;

public class BitmapImageFactory implements ImageFactory {
    @Override
    public ImageSource createEmpty(int width, int height) {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        return new BitmapImageSource(Bitmap.createBitmap(width, height, conf));
    }
}
