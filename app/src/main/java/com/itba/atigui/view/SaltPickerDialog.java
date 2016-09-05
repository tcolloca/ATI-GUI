package com.itba.atigui.view;

import android.app.Dialog;
import android.content.Context;
import android.widget.SeekBar;
import android.widget.TextView;

import com.itba.atigui.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SaltPickerDialog extends Dialog {

    @BindView(R.id.dialog_range_picker_contamination_number)
    TextView contaminationNumberText;
    @BindView(R.id.dialog_range_picker_p1_number)
    TextView p1NumberText;
    @BindView(R.id.dialog_range_picker_contamination_seekbar)
    SeekBar contaminationSeekbar;
    @BindView(R.id.dialog_range_picker_p1_seekbar)
    SeekBar p1Seekbar;

    private Listener listener;

    public interface Listener {
        void onSaltAvailable(int percentage, double salt);
    }

    public SaltPickerDialog(Context context, Listener listener) {
        super(context);
        setContentView(R.layout.dialog_salt_picker);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(true);
        this.listener = listener;
        contaminationSeekbar.setProgress(0);
        p1Seekbar.setProgress(0);
        contaminationNumberText.setText(String.valueOf(0));
        p1NumberText.setText(String.valueOf(0.0));

        contaminationSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                contaminationNumberText.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        p1Seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                p1NumberText.setText(String.valueOf(progress/1000.0));
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
            listener.onSaltAvailable(contaminationSeekbar.getProgress(), p1Seekbar.getProgress()/1000.0);
        }
        dismiss();
    }
}
