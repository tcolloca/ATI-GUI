package com.itba.atigui.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.goodengineer.atibackend.model.Band;
import com.goodengineer.atibackend.model.BlackAndWhiteImage;
import com.goodengineer.atibackend.translator.BlackAndWhiteImageTranslator;

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

    public static BlackAndWhiteImageTranslator<Bitmap> translator() {
        return new BlackAndWhiteImageTranslator<Bitmap>() {
            @Override
            public BlackAndWhiteImage translateForward(Bitmap bitmap) {
                double[][] pixels = new double[bitmap.getWidth()][bitmap.getHeight()];
                for (int x = 0; x < bitmap.getWidth(); x++) {
                    for (int y = 0; y < bitmap.getHeight(); y++) {
                        int color = Color.blue(bitmap.getPixel(x, y));
                        pixels[x][y] = color;
                    }
                }
                Band band = new Band(pixels);
                return new BlackAndWhiteImage(band);
            }

            @Override
            public Bitmap translateBackward(BlackAndWhiteImage blackAndWhiteImage) {
                int width = blackAndWhiteImage.getWidth();
                int height = blackAndWhiteImage.getHeight();
                Bitmap bitmap = createTransparent(width, height);
                for (int x = 0; x < bitmap.getWidth(); x++) {
                    for (int y = 0; y < bitmap.getHeight(); y++) {
                        int color = blackAndWhiteImage.getPixel(x, y);
                        bitmap.setPixel(x, y, Color.rgb(color, color, color));
                    }
                }
                return bitmap;
            }
        };
    }

    public static Bitmap copy(Bitmap bitmap) {
        return bitmap.copy(bitmap.getConfig(), true);
    }
}
