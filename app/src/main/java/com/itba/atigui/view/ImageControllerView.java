package com.itba.atigui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
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

    /**
     * TO_SELECT represents that period in which you haven't selected a pixel yet
     */
    public enum State {
        TO_SELECT_FIRST, SELECTING_FIRST, TO_SELECT_SECOND, SELECTING_SECOND, DONE
    }

    private static final String TAG = "ImageControllerView";
    private static final int ACCURATE_THRESHOLD = 250;
    private static final int ACCURATE_THRESHOLD2 = 15;

    private static final int NOT_ACCURATE_THRESHOLD = 100;
    private static final int NOT_ACCURATE_THRESHOLD2 = 1;

    private static final int COLOR_FREE = Color.parseColor("#ffdf00");
    private static final int COLOR_LOCKED = Color.parseColor("#ff0000");

    private VelocityTracker velocityTracker = VelocityTracker.obtain();

    private PixelSelection firstPixelSelection;
    private PixelSelection secondPixelSelection;
    private PixelSelection currentPixelSelection;
    private State state = State.TO_SELECT_FIRST;

    private int threshold1;
    private int threshold2;

    private int xSpeedAcum = 0;
    private int ySpeedAcum = 0;
    private boolean precisionModeEnabled;

    private ImageListener imageListener = new ImageListener() {
        @Override
        public void onPixelSelected(Point pixel, int color) {
        }

        @Override
        public void onPixelUnselected() {

        }

        @Override
        public void onRectangleAvailable(PointF p1, PointF p2) {

        }

        @Override
        public void onRectangleUnselected() {

        }
    };

    public interface ImageListener {
        void onPixelSelected(Point pixel, int color);

        void onPixelUnselected();

        void onRectangleAvailable(PointF p1, PointF p2);

        void onRectangleUnselected();
    }

    private void init() {
        setOnTouchListener(this);
        setPrecisionMode(false);
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
                    selectRectangle(firstPixelSelection.pixel, secondPixelSelection.pixel);
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
            Point pixelSpeed = new Point(xVelToPixelSpeed(xVelocity), yVelToPixelSpeed(yVelocity));
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
                if (secondPixelSelection != null)
                    selectRectangle(firstPixelSelection.pixel, secondPixelSelection.pixel);
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
        switch (state) {
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
                imageListener.onRectangleUnselected();
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

    private int xVelToPixelSpeed(float xSpeed) {
        if (xSpeed <= -threshold1) xSpeedAcum -= 1;
        if (xSpeed >= threshold1) xSpeedAcum += 1;

        int step = 3;
        if (precisionModeEnabled) step = 1;
        if (xSpeedAcum < -threshold2) {
            xSpeedAcum = 0;
            return -step;
        }
        if (xSpeedAcum > threshold2) {
            xSpeedAcum = 0;
            return step;
        }
        return 0;
    }

    private int yVelToPixelSpeed(float ySpeed) {
        if (ySpeed <= -threshold1) ySpeedAcum -= 1;
        if (ySpeed >= threshold1) ySpeedAcum += 1;

        int step = 3;
        if (precisionModeEnabled) step = 1;
        if (ySpeedAcum < -threshold2) {
            ySpeedAcum = 0;
            return -step;
        }
        if (ySpeedAcum > threshold2) {
            ySpeedAcum = 0;
            return step;
        }
        return 0;
    }

    public void setPrecisionMode(boolean enabled) {
        precisionModeEnabled = enabled;
        if (enabled) {
            threshold1 = ACCURATE_THRESHOLD;
            threshold2 = ACCURATE_THRESHOLD2;
        } else {
            threshold1 = NOT_ACCURATE_THRESHOLD;
            threshold2 = NOT_ACCURATE_THRESHOLD2;
        }
    }

    private PixelSelection selectPixel(Point pixel) {
        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();

        PixelSelection pixelSelection = new PixelSelection(pixel, null);
        colorPixelSelection(pixelSelection, COLOR_FREE);
        return pixelSelection;
    }

    private void selectRectangle(Point p1, Point p2) {
        Point min = new Point(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y));
        Point max = new Point(Math.max(p1.x, p2.x), Math.max(p1.y, p2.y));
        imageListener.onRectangleAvailable(toScreenCoordinate(min), toScreenCoordinate(max));
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
     *
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

    private PointF toScreenCoordinate(Point pixel) {
        int x = pixel.x;
        int y = pixel.y;
        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();

        float xFactor = ((float) x) / bitmap.getWidth();
        float yFactor = ((float) y) / bitmap.getHeight();

        int xPixel = (int) (xFactor * getWidth());
        int yPixel = (int) (yFactor * getHeight());

        return new PointF(xPixel, yPixel);
    }

    public int getPixelColor(int x, int y) {
        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        return bitmap.getPixel(x, y) & 0xFF;
    }

    public boolean hasBitmap() {
        return getDrawable() != null;
    }

    public boolean isReadyToExport() {
        return state == State.DONE;
    }

    public Bitmap getBitmapInRectangle() {
        if (!isReadyToExport()) return null;
        Point p1 = firstPixelSelection.pixel;
        Point p2 = secondPixelSelection.pixel;
        Point min = new Point(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y));
        Point max = new Point(Math.max(p1.x, p2.x), Math.max(p1.y, p2.y));
        undoPixelSelection(firstPixelSelection);
        undoPixelSelection(secondPixelSelection);

        int width = max.x - min.x + 1;
        int height = max.y - min.y + 1;

        Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, min.x, min.y, width, height);
        return croppedBitmap;
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
