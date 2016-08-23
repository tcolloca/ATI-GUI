package com.itba.atigui.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.itba.atigui.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ColorPickerDialog extends Dialog {

    private static final String TITLE = "Pick a color";

    @BindView(R.id.dialog_color_picker_color)
    View colorView;
    @BindView(R.id.dialog_color_picker_number)
    TextView numberTextView;
    @BindView(R.id.dialog_color_picker_seekbar)
    SeekBar seekBar;

    private int initialColor;
    private Listener listener;

    public interface Listener {
        void onColorAvailable(int color);
    }

    public ColorPickerDialog(Context context, int initialColor, Listener listener) {
        super(context);
        setContentView(R.layout.dialog_color_picker);
        setTitle(TITLE);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(true);
        this.initialColor = initialColor;
        this.listener = listener;
        numberTextView.setText(String.valueOf(initialColor));
        colorView.setBackgroundColor(Color.rgb(initialColor, initialColor, initialColor));
        seekBar.setProgress(initialColor);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                colorView.setBackgroundColor(Color.rgb(progress, progress, progress));
                numberTextView.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @OnClick(R.id.dialog_color_picker_cancel_button)
    void onCancelButtonClick() {
        dismiss();
    }

    @OnClick(R.id.dialog_color_picker_ok_button)
    void onOkButtonClick() {
        if (listener != null && initialColor != seekBar.getProgress()) {
            listener.onColorAvailable(seekBar.getProgress());
        }
        dismiss();
    }
}
