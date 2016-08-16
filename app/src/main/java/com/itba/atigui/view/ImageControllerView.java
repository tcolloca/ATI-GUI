package com.itba.atigui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v4.view.VelocityTrackerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

import com.itba.atigui.model.PixelSelection;
import com.itba.atigui.util.AspectRatioImageView;

public class ImageControllerView extends AspectRatioImageView implements View.OnTouchListener {

    private static final String TAG = "ImageControllerView";
    private static final int THRESHOLD = 500;

    private VelocityTracker velocityTracker = VelocityTracker.obtain();

    private PixelSelection currentPixelSelection;
    private ImageListener imageListener = new ImageListener() {
        @Override
        public void onPixelSelected(Point pixel, int color) {
        }
    };

    public interface ImageListener {
        void onPixelSelected(Point pixel, int color);
    }

    private void init() {
        setOnTouchListener(this);
    }

    public void setImageListener(@NonNull ImageListener imageListener) {
        this.imageListener = imageListener;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int index = event.getActionIndex();
        int pointerId = event.getPointerId(index);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            velocityTracker.clear();
            velocityTracker.addMovement(event);

            if (currentPixelSelection != null) {
                return true;
            }
            Point pixel = toPixel(event.getX(), event.getY());
            currentPixelSelection = selectPixel(pixel);
            imageListener.onPixelSelected(pixel, getPixelColor(pixel.x, pixel.y));
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            velocityTracker.addMovement(event);
            velocityTracker.computeCurrentVelocity(1000);
            float xVelocity = VelocityTrackerCompat.getXVelocity(velocityTracker, pointerId);
            float yVelocity = VelocityTrackerCompat.getYVelocity(velocityTracker, pointerId);
            Point pixelSpeed = new Point(toPixelSpeed(xVelocity), toPixelSpeed(yVelocity));
            if (pixelSpeed.x != 0 || pixelSpeed.y != 0) {
                Point pixel = new Point(currentPixelSelection.pixel);
                undoPixelSelection(currentPixelSelection);
                pixel.offset(pixelSpeed.x, pixelSpeed.y);
                Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
                if (pixel.x < 0
                        || pixel.x >= bitmap.getWidth()
                        || pixel.y < 0
                        || pixel.y >= bitmap.getHeight()) {
                    currentPixelSelection = selectPixel(currentPixelSelection.pixel);
                    return true;
                }
                currentPixelSelection = selectPixel(pixel);
                imageListener.onPixelSelected(pixel, getPixelColor(pixel.x, pixel.y));
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            velocityTracker.recycle();
        }

        return true;
    }

    private int toPixelSpeed(float speed) {
        if (speed <= -THRESHOLD) return -1;
        if (speed <= THRESHOLD) return 0;
        return 1;
    }

    private PixelSelection selectPixel(Point pixel) {
        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();

        Integer[][] savedColors = new Integer[3][3];
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                if (pixel.x + i < 0
                        || pixel.x + i >= bitmap.getWidth()
                        || pixel.y + j < 0
                        || pixel.y + j >= bitmap.getHeight())
                    continue;
                savedColors[i + 1][j + 1] = bitmap.getPixel(pixel.x + i, pixel.y + j);
                bitmap.setPixel(pixel.x + i, pixel.y + j, Color.parseColor("#ff0000"));
            }
        }
        setImageBitmap(bitmap);
        return new PixelSelection(pixel, savedColors);
    }

    private void undoPixelSelection(PixelSelection pixelSelection) {
        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                if (pixelSelection.savedColors[i + 1][j + 1] == null) continue;
                int savedColor = pixelSelection.savedColors[i + 1][j + 1];
                bitmap.setPixel(pixelSelection.pixel.x + i, pixelSelection.pixel.y + j, savedColor);
            }
        }
        setImageBitmap(bitmap);
    }

    /**
     *
     * @param color goes from 0 to 255
     */
    public void setCurrentPixelColor(int color) {
        if (currentPixelSelection == null) {
            Log.e(TAG, "attempting to change pixel color when there is none selected!");
            return;
        }
        Point pixel = currentPixelSelection.pixel;
        setPixelColor(pixel.x, pixel.y, Color.rgb(color, color, color));
        imageListener.onPixelSelected(pixel, color);
    }

    private void setPixelColor(int x, int y, int color) {
        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        bitmap.setPixel(x, y, color);
    }

    /**
     * Doesn't need to be accurate.
     * Swipe feature will provide accuracy.
     */
    private Point toPixel(float x, float y) {
        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();

        float xFactor = x / getWidth();
        float yFactor = y / getHeight();

        int xPixel = (int) (xFactor * bitmap.getWidth());
        int yPixel = (int) (yFactor * bitmap.getHeight());

        return new Point(xPixel, yPixel);
    }

    public int getPixelColor(int x, int y) {
        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        return bitmap.getPixel(x, y) & 0xFF;
    }

    //    region needed constructors
    public ImageControllerView(Context context) {
        super(context);
        init();
    }

    public ImageControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ImageControllerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
//    endregion
}
