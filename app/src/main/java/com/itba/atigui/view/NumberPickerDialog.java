package com.itba.atigui.view;

import android.app.Dialog;
import android.content.Context;
import android.widget.EditText;

import com.itba.atigui.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NumberPickerDialog extends Dialog {

    @BindView(R.id.dialog_number_picker_edittext)
    EditText editText;

    private Listener listener;

    public interface Listener {
        void onNumberAvailable(int number);
    }

    public NumberPickerDialog(Context context, Listener listener) {
        super(context);
        setContentView(R.layout.dialog_number_picker);
        ButterKnife.bind(this);
        setCanceledOnTouchOutside(true);
        this.listener = listener;
    }

    @OnClick(R.id.dialog_number_picker_cancel_button)
    void onCancelButtonClick() {
        dismiss();
    }

    @OnClick(R.id.dialog_number_picker_ok_button)
    void onOkButtonClick() {
        if (listener == null || editText.getText().toString().isEmpty()) return;
        listener.onNumberAvailable(Integer.valueOf(editText.getText().toString()));
        dismiss();
    }
}
