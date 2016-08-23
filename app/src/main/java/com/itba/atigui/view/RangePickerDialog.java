package com.itba.atigui.view;

import android.app.Dialog;
import android.content.Context;
import android.widget.SeekBar;
import android.widget.TextView;

import com.itba.atigui.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RangePickerDialog extends Dialog {

    @BindView(R.id.dialog_range_picker_r1_number)
    TextView numberTextView1;
    @BindView(R.id.dialog_range_picker_r2_number)
    TextView numberTextView2;
    @BindView(R.id.dialog_range_picker_r1_seekbar)
    SeekBar seekBar1;
    @BindView(R.id.dialog_range_picker_r2_seekbar)
    SeekBar seekBar2;

    private Listener listener;

    public interface Listener {
        void onRangeAvailable(int left, int right);
    }

    public RangePickerDialog(Context context, Listener listener) {
        super(context);
        setContentView(R.layout.dialog_range_picker);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(true);
        this.listener = listener;
        seekBar1.setProgress(0);
        seekBar2.setProgress(255);
        numberTextView1.setText(String.valueOf(0));
        numberTextView2.setText(String.valueOf(255));
        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                numberTextView1.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                numberTextView2.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    @OnClick(R.id.dialog_range_picker_cancel_button)
    void onCancelButtonClick() {
        dismiss();
    }

    @OnClick(R.id.dialog_range_picker_ok_button)
    void onOkButtonClick() {
        if (listener != null) {
            int min = Math.min(seekBar1.getProgress(), seekBar2.getProgress());
            int max = Math.max(seekBar1.getProgress(), seekBar2.getProgress());
            listener.onRangeAvailable(min, max);
        }
        dismiss();
    }
}
