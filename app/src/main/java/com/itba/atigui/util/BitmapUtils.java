package com.itba.atigui.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

public class BitmapUtils {

    public static Bitmap createTransparent(Bitmap bitmap) {
        return createTransparent(bitmap.getWidth(), bitmap.getHeight());
    }

    public static Bitmap createTransparent(int width, int height) {
        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        return Bitmap.createBitmap(width, height, conf);
    }

    public static Bitmap decodeFile(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(path, options);
    }

    public static boolean isBlackAndWhiteBitmap(Bitmap bitmap) {
        for (int x = 0; x < bitmap.getWidth(); x++) {
            for (int y = 0; y < bitmap.getHeight(); y++) {
                if (!isBlackAndWhitePixel(bitmap.getPixel(x, y))) return false;
            }
        }
        return true;
    }

    public static boolean isBlackAndWhitePixel(int pixel) {
        return Color.red(pixel) == Color.green(pixel)
                && Color.green(pixel) == Color.blue(pixel)
                && Color.alpha(pixel) == 255;
    }
}
