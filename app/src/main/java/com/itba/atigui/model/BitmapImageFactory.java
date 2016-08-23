package com.itba.atigui.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.goodengineer.atibackend.ImageFactory;
import com.goodengineer.atibackend.ImageSource;

public class BitmapImageFactory implements ImageFactory {

    @Override
    public ImageSource createEmpty(int width, int height) {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Bitmap.createBitmap(width, height, conf);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPaint(paint);
        return new BlackAndWhiteBitmapImageSource(bitmap);
    }
}
