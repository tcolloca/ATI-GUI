package com.itba.atigui.model;

import com.goodengineer.atibackend.ImageSource;
import com.goodengineer.atibackend.ImageUtils;

/**
 * this class allows invalid pixel values (negative values or values above 255)
 */
public class SingleBandImageSource implements ImageSource {

    private ColorBand colorBand;

    public SingleBandImageSource(ColorBand colorBand) {
        this.colorBand = colorBand;
    }

    @Override
    public int getPixel(int x, int y) {
        return colorBand.band[x][y];
    }

    @Override
    public void setPixel(int x, int y, int color) {
        colorBand.band[x][y] = color;
    }

    @Override
    public int getWidth() {
        return colorBand.band.length;
    }

    @Override
    public int getHeight() {
        return colorBand.band[0].length;
    }

    @Override
    public void dispose() {
        colorBand = null;
    }

    @Override
    public void normalize() {
        ImageUtils.normalize(this);
    }

    /**
     *
     * @return null. This implementation is not intended to be copied!
     */
    @Override
    public ImageSource copy() {
        return null;
    }
}
