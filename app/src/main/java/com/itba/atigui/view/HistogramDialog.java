package com.itba.atigui.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.itba.atigui.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistogramDialog extends Dialog {

    @BindView(R.id.chart)
    BarChart chart;

    public HistogramDialog(Context context, float[] data) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        setContentView(R.layout.dialog_histogram);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(true);

        setData(data);
    }

    private void setData(float[] data) {
        List<BarEntry> entries = new ArrayList<BarEntry>();
        for (int i = 0; i < data.length; i++) {
            entries.add(new BarEntry(i, data[i]));
        }

        BarDataSet barDataSet = new BarDataSet(entries, "Colors");

        BarData barData = new BarData(barDataSet);
        chart.setData(barData);
        chart.invalidate(); // refresh
        chart.animateY(1500);
    }
}
