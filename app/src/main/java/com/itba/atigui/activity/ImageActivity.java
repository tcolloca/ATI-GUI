package com.itba.atigui.activity;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.itba.atigui.R;
import com.itba.atigui.util.AspectRatioImageView;
import com.itba.atigui.util.FileUtils;
import com.itba.atigui.view.ImageControllerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class ImageActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 147;
    public static final String TAG = "ImageActivity";

    @BindView(R.id.image)
    ImageControllerView imageControllerView;
    @BindView(R.id.image_original)
    AspectRatioImageView originalImage;
    @BindView(R.id.activity_image_pixel_color)
    View pixelColorView;
    @BindView(R.id.activity_image_pixel_color_seekbar)
    SeekBar pixelColorSeekbar;
    @BindView(R.id.activity_image_pixel_color_number)
    TextView pixelColorNumber;
    @BindView(R.id.activity_image_pixel_x)
    TextView pixelXText;
    @BindView(R.id.activity_image_pixel_y)
    TextView pixelYText;
    @BindView(R.id.activity_image_show_original_text)
    TextView showOriginalText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);

        init();
    }

    private void init() {
        imageControllerView.setImageListener(new ImageControllerView.ImageListener() {
            @Override
            public void onPixelSelected(Point pixel, int color) {
                pixelColorView.setBackgroundColor(Color.rgb(color, color, color));
                pixelColorSeekbar.setProgress(color);
                pixelColorNumber.setText(String.valueOf(color));
                pixelXText.setText(String.valueOf(pixel.x));
                pixelYText.setText(String.valueOf(pixel.y));
            }

            @Override
            public void onPixelUnselected() {
                pixelColorView.setBackgroundColor(Color.rgb(255, 255, 255));
                pixelColorSeekbar.setProgress(255);
                pixelColorNumber.setText(String.valueOf(255));
                pixelXText.setText(String.valueOf(-1));
                pixelYText.setText(String.valueOf(-1));
            }
        });

        pixelColorSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                imageControllerView.setCurrentPixelColor(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    public void loadImage(Bitmap bitmap) {
        imageControllerView.unselectCurrentSelectedPixel();
        Bitmap originalBitmap = bitmap.copy(bitmap.getConfig(), true);
        imageControllerView.setImageBitmap(bitmap);
        originalImage.setImageBitmap(originalBitmap);
    }

    @OnTouch(R.id.activity_image_show_original_button)
    boolean onShowOriginalButtonTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                originalImage.setVisibility(View.VISIBLE);
                showOriginalText.setText("Showing original");
                view.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_HOVER_EXIT:
            case MotionEvent.ACTION_UP:
                originalImage.setVisibility(View.GONE);
                showOriginalText.setText("Show original");
                view.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                break;
        }
        return false;
    }

    @OnClick(R.id.activity_image_upload_button)
    void onUploadButtonClick() {
        ImageActivityPermissionsDispatcher.showFileChooserWithCheck(this);
    }

    //    region FILE-CHOOSER related code
    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options.inMutable = true;
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
                loadImage(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
//    endregion

    //    region PERMISSION HANDLING
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ImageActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @OnShowRationale(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showRationaleForCamera(final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage(R.string.permission_camera_rationale)
                .setPositiveButton(R.string.button_allow, (new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        request.proceed();
                    }
                }))
                .setNegativeButton(R.string.button_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        request.cancel();
                    }
                })
                .show();
    }

    @OnPermissionDenied(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showDeniedForCamera() {
        Toast.makeText(this, R.string.permission_camera_denied, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showNeverAskForCamera() {
        Toast.makeText(this, R.string.permission_camera_neverask, Toast.LENGTH_SHORT).show();
    }
//    endregion
}
