package com.itba.atigui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.itba.atigui.R;
import com.itba.atigui.model.SliderInfo;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SliderInfoView extends FrameLayout {

    @BindView(R.id.view_slider_info_title)
    TextView titleView;
    @BindView(R.id.view_slider_info_seekbar)
    SeekBar seekBarView;
    @BindView(R.id.view_slider_info_number)
    TextView seekBarNumberView;

    private SliderInfo sliderInfo;

    public SliderInfoView(Context context, SliderInfo sliderInfo) {
        super(context);
        this.sliderInfo = sliderInfo;
        init();
    }

    public SliderInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SliderInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        View.inflate(getContext(), R.layout.view_slider_info, this);
        ButterKnife.bind(this);

        titleView.setText(sliderInfo.name);
        seekBarView.setMax(sliderInfo.maxValue);
        seekBarView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                seekBarNumberView.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public int getValue() {
        return seekBarView.getProgress();
    }
}
