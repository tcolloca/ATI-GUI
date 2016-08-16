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
import com.itba.atigui.model.RectangleSelection;
import com.itba.atigui.util.AspectRatioImageView;

public class ImageControllerView extends AspectRatioImageView implements View.OnTouchListener {

    /**
     * TO_SELECT represents that period in which you haven't selected a pixel yet
     */
    public enum State {
        TO_SELECT_FIRST, SELECTING_FIRST, TO_SELECT_SECOND, SELECTING_SECOND, DONE
    }

    private static final String TAG = "ImageControllerView";
    private static final int THRESHOLD = 500;
    private static final int COLOR_FREE = Color.parseColor("#0000ff");
    private static final int COLOR_LOCKED = Color.parseColor("#ff0000");

    private VelocityTracker velocityTracker = VelocityTracker.obtain();

    private PixelSelection firstPixelSelection;
    private PixelSelection secondPixelSelection;
    private PixelSelection currentPixelSelection;
    private RectangleSelection rectangleSelection;
    private State state = State.TO_SELECT_FIRST;

    private ImageListener imageListener = new ImageListener() {
        @Override
        public void onPixelSelected(Point pixel, int color) {
        }

        @Override
        public void onPixelUnselected() {

        }
    };

    public interface ImageListener {
        void onPixelSelected(Point pixel, int color);
        void onPixelUnselected();
//        void onRectangleConfirmed(Point p1, Point p2);
    }

    private void init() {
        setOnTouchListener(this);
    }

    public void setImageListener(@NonNull ImageListener imageListener) {
        this.imageListener = imageListener;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (!hasBitmap()) return false;
        int index = event.getActionIndex();
        int pointerId = event.getPointerId(index);

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            velocityTracker.clear();
            velocityTracker.addMovement(event);

            if (state == State.SELECTING_FIRST
                    || state == State.SELECTING_SECOND
                    || state == State.DONE) {
                return true;
            }
            Point pixel;
            switch (state) {
                case TO_SELECT_FIRST:
                    pixel = toPixel(event.getX(), event.getY());
                    firstPixelSelection = selectPixel(pixel);
                    currentPixelSelection = firstPixelSelection;
                    imageListener.onPixelSelected(pixel, getPixelColor(pixel.x, pixel.y));
                    state = State.SELECTING_FIRST;
                    break;
                case TO_SELECT_SECOND:
                    pixel = toPixel(event.getX(), event.getY());
                    secondPixelSelection = selectPixel(pixel);
                    currentPixelSelection = secondPixelSelection;
                    imageListener.onPixelSelected(pixel, getPixelColor(pixel.x, pixel.y));
                    state = State.SELECTING_SECOND;
                    rectangleSelection = selectRectangle(firstPixelSelection.pixel, secondPixelSelection.pixel);
                    break;
                case SELECTING_FIRST:
                case SELECTING_SECOND:
                case DONE:
                    return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (state == State.DONE) return true;
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
                    currentPixelSelection.update(selectPixel(currentPixelSelection.pixel));
                    return true;
                }
                currentPixelSelection.update(selectPixel(pixel));
                imageListener.onPixelSelected(pixel, getPixelColor(pixel.x, pixel.y));
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            velocityTracker.recycle();
        }

        return true;
    }

    /**
     * TODO: see what happens with states
     */
    public void unselectCurrentSelectedPixel() {
        if (currentPixelSelection == null) return;
        undoPixelSelection(currentPixelSelection);
        currentPixelSelection = null;
        imageListener.onPixelUnselected();
    }

    @Override
    public void clear() {
        super.clear();
        imageListener.onPixelUnselected();
        firstPixelSelection = null;
        secondPixelSelection = null;
        currentPixelSelection = null;
        state = State.TO_SELECT_FIRST;
    }

    public void check() {
        if (!hasBitmap()) return;
        switch(state) {
            case SELECTING_FIRST:
                state = State.TO_SELECT_SECOND;
                colorPixelSelection(firstPixelSelection, COLOR_LOCKED);
                break;
            case SELECTING_SECOND:
                state = State.DONE;
                colorPixelSelection(secondPixelSelection, COLOR_LOCKED);
                break;
        }
    }

    public void cancel() {
        if (!hasBitmap()) return;
        switch (state) {
            case DONE:
//                simply unlock second pixel by returning to SELECTING_SECOND
                state = State.SELECTING_SECOND;
                colorPixelSelection(secondPixelSelection, COLOR_FREE);
                break;
            case SELECTING_SECOND:
//                undo second selection + change current
                undoPixelSelection(secondPixelSelection);
                secondPixelSelection = null;
                currentPixelSelection = firstPixelSelection;
                state = State.TO_SELECT_SECOND;
                Point firstPixel = firstPixelSelection.pixel;
                imageListener.onPixelSelected(firstPixel, getPixelColor(firstPixel.x, firstPixel.y));
                break;
            case TO_SELECT_SECOND:
//                TODO: add something to distinguish locked pixels of unlocked
                state = State.SELECTING_FIRST;
                colorPixelSelection(firstPixelSelection, COLOR_FREE);
                break;
            case SELECTING_FIRST:
                undoPixelSelection(firstPixelSelection);
                currentPixelSelection = null;
                firstPixelSelection = null;
                state = State.TO_SELECT_FIRST;
                imageListener.onPixelUnselected();
                break;
            case TO_SELECT_FIRST:
//                do nothing
                break;
        }
    }

    private int toPixelSpeed(float speed) {
        if (speed <= -THRESHOLD) return -1;
        if (speed <= THRESHOLD) return 0;
        return 1;
    }

    private PixelSelection selectPixel(Point pixel) {
        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();

        PixelSelection pixelSelection = new PixelSelection(pixel, null);
        colorPixelSelection(pixelSelection, COLOR_FREE);
        return pixelSelection;
    }

    private RectangleSelection selectRectangle(Point p1, Point p2) {
        Point min = new Point(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y));
        Point max = new Point(Math.max(p1.x, p2.x), Math.max(p1.y, p2.y));
        int width = max.x - min.x + 1;
        int height = max.y - min.y + 1;
        Integer[][] savedColors = new Integer[width][height];
        for (int i = min.x; i <= max.x; i++) {
            for (int j = min.y; j <= max.y; j++) {

            }
        }
        return new RectangleSelection(p1, p2, savedColors);
    }

    private void colorPixelSelection(PixelSelection pixelSelection, int color) {
        if (pixelSelection.savedColors != null) undoPixelSelection(pixelSelection);
        Point pixel = pixelSelection.pixel;
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
                bitmap.setPixel(pixel.x + i, pixel.y + j, color);
            }
        }
        pixelSelection.update(pixel, savedColors);
        setImageBitmap(bitmap);
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
     * TODO: see what happens with states
     * @param color goes from 0 to 255
     */
    public void setCurrentPixelColor(int color) {
        if (currentPixelSelection == null) {
            Log.e(TAG, "attempting to change pixel color when there is none selected!");
            return;
        }
        Point pixel = currentPixelSelection.pixel;
        setBitmapPixelColor(pixel.x, pixel.y, Color.rgb(color, color, color));
        imageListener.onPixelSelected(pixel, color);
    }

    private void setBitmapPixelColor(int x, int y, int color) {
        if (!hasBitmap()) return;
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

    public boolean hasBitmap() {
        return getDrawable() != null;
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
