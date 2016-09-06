package com.itba.atigui.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.itba.atigui.R;
import com.itba.atigui.model.SliderInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MultipleSliderDialog extends Dialog {

    @BindView(R.id.dialog_title)
    TextView titleTextView;
    @BindView(R.id.slider_container)
    LinearLayout sliderContainer;

    private Listener listener;

    private List<SliderInfo> sliderInfos;
    private Map<SliderInfo, SliderInfoView> slidersMap;

    public interface Listener {
        void onValuesAvailable(List<Integer> values);
    }

    public MultipleSliderDialog(Context context, String title, List<SliderInfo> sliderInfos, Listener listener) {
        super(context);
        setContentView(R.layout.dialog_multiple_sliders);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(true);
        this.listener = listener;
        titleTextView.setText(title);
        slidersMap = new HashMap<>();
        this.sliderInfos = sliderInfos;

        for (SliderInfo sliderInfo: sliderInfos) {
            SliderInfoView sliderInfoView = new SliderInfoView(getContext(), sliderInfo);
            slidersMap.put(sliderInfo, sliderInfoView);
            sliderContainer.addView(sliderInfoView);
        }
    }

    @OnClick(R.id.dialog_color_picker_cancel_button)
    void onCancelButtonClick() {
        dismiss();
    }

    @OnClick(R.id.dialog_color_picker_ok_button)
    void onOkButtonClick() {
        if (listener != null) {
            List<Integer> values = new ArrayList<>();
            for (SliderInfo sliderInfo: sliderInfos) {
                values.add(slidersMap.get(sliderInfo).getValue());
            }
            listener.onValuesAvailable(values);
        }
        dismiss();
    }
}
