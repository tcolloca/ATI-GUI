package com.itba.atigui.model;

import android.graphics.Point;

public class PixelSelection {
    public Point pixel;
    public Integer[][] savedColors;

    public PixelSelection(Point pixel, Integer[][] savedColors) {
        this.pixel = pixel;
        this.savedColors = savedColors;
    }
}
