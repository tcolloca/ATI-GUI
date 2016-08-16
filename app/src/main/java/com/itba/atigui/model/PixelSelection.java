package com.itba.atigui.model;

import android.graphics.Point;

public class PixelSelection {
    public Point pixel;
    public Integer[][] savedColors;

    public PixelSelection(Point pixel, Integer[][] savedColors) {
        this.pixel = pixel;
        this.savedColors = savedColors;
    }

    public void update(PixelSelection pixelSelection) {
        this.pixel = pixelSelection.pixel;
        this.savedColors = pixelSelection.savedColors;
    }

    public void update(Point pixel, Integer[][] savedColors) {
        this.pixel = pixel;
        this.savedColors = savedColors;
    }
}
