package com.itba.atigui.model;

import android.graphics.Point;

public class RectangleSelection {
    public Point p1;
    public Point p2;
    public Integer[][] savedColors;

    public RectangleSelection(Point p1, Point p2, Integer[][] savedColors) {
        this.p1 = p1;
        this.p2 = p2;
        this.savedColors = savedColors;
    }
}
