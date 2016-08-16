package com.itba.atigui.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.itba.atigui.R;
import com.itba.atigui.util.AspectRatioImageView;
import com.itba.atigui.util.FileUtils;
import com.itba.atigui.view.ImageControllerView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.image_container)
    FrameLayout imageContainer;
    @BindView(R.id.activity_image_precision_checkbox)
    CheckBox precisionCheckbox;

    View rectangleView;

    private String currentImagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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
                pixelColorSeekbar.setEnabled(true);
            }

            @Override
            public void onPixelUnselected() {
                pixelColorView.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
                pixelColorNumber.setText("");
                pixelXText.setText("");
                pixelYText.setText("");
                pixelColorSeekbar.setEnabled(false);
            }

            @Override
            public void onRectangleAvailable(PointF p1, PointF p2) {
                if (rectangleView == null) {
                    rectangleView = new View(ImageActivity.this);
                    imageContainer.addView(rectangleView);
                }
                float width = p2.x - p1.x;
                float height = p2.y - p1.y;
                rectangleView.setLayoutParams(new FrameLayout.LayoutParams((int) width, (int) height));
                rectangleView.setBackgroundColor(Color.parseColor("#66ff0000"));
                rectangleView.setX(imageControllerView.getX() + p1.x);
                rectangleView.setY(imageControllerView.getY() + p1.y);
            }

            @Override
            public void onRectangleUnselected() {
                imageContainer.removeView(rectangleView);
                rectangleView = null;
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

        FileUtils.refreshGallery();
        clearAll();
    }

    public void loadImage(String path) {
        clearAll();
        currentImagePath = path;
        toolbarTitle.setText(FileUtils.getFileName(path));

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inMutable = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        imageControllerView.setImageBitmap(bitmap);

        options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap originalBitmap = BitmapFactory.decodeFile(path, options);
        originalImage.setImageBitmap(originalBitmap);

        pixelColorSeekbar.setEnabled(true);
    }

    private void clearAll() {
        pixelColorSeekbar.setEnabled(false);
        currentImagePath = null;
        imageControllerView.clear();
        originalImage.clear();
        toolbarTitle.setText(getString(R.string.app_name));
        if (rectangleView != null) {
            imageContainer.removeView(rectangleView);
            rectangleView = null;
        }
    }

    @OnTouch(R.id.activity_image_show_original_button)
    boolean onShowOriginalButtonTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                originalImage.setVisibility(View.VISIBLE);
                showOriginalText.setText("Showing original");
                view.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                if (rectangleView != null) rectangleView.setVisibility(View.INVISIBLE);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_HOVER_EXIT:
            case MotionEvent.ACTION_UP:
                originalImage.setVisibility(View.GONE);
                showOriginalText.setText("Show original");
                view.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                if (rectangleView != null) rectangleView.setVisibility(View.VISIBLE);
                break;
        }
        return false;
    }

    //    region BUTTON LISTENERS
    @OnClick(R.id.toolbar_upload)
    void onUploadButtonClick() {
        ImageActivityPermissionsDispatcher.showFileChooserWithCheck(this);
    }

    @OnClick(R.id.toolbar_save)
    void onSaveButtonClick() {
        if (!imageControllerView.hasBitmap()) {
            Toast.makeText(ImageActivity.this, "nothing to save", Toast.LENGTH_SHORT).show();
            return;
        }
        ImageActivityPermissionsDispatcher.showChooseImageNameDialogWithCheck(this, ((BitmapDrawable) imageControllerView.getDrawable()).getBitmap());
    }

    @OnClick(R.id.toolbar_delete)
    void onDeleteButtonClick() {
        if (currentImagePath == null || !imageControllerView.hasBitmap()) {
            Toast.makeText(ImageActivity.this, "nothing to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete " + FileUtils.getFileName(currentImagePath) + "?")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        (new File(currentImagePath)).delete();
                        clearAll();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @OnClick(R.id.toolbar_export)
    void onExportButtonClick() {
        if (!imageControllerView.isReadyToExport()) {
            Toast.makeText(ImageActivity.this, "cant export yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap croppedBitmap = imageControllerView.getBitmapInRectangle();
        ImageActivityPermissionsDispatcher.showChooseImageNameDialogWithCheck(this, croppedBitmap);
    }

    @OnClick(R.id.toolbar_check)
    void onCheckButtonClick() {
        imageControllerView.check();
    }

    @OnClick(R.id.toolbar_cancel)
    void onCancelButtonClick() {
        imageControllerView.cancel();
    }

    @OnClick(R.id.activity_image_precision_checkbox)
    void onPrecisionCheckboxClick() {
        imageControllerView.setPrecisionMode(precisionCheckbox.isChecked());
    }

//    endregion

    //    region SAVE-IMAGE
    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showChooseImageNameDialog(final Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a name");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = input.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(ImageActivity.this, "must choose a name!", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveImage(name, bitmap);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    void saveImage(String name, Bitmap bitmap) {
        if (TextUtils.isEmpty(name)) return;
        imageControllerView.unselectCurrentSelectedPixel();
        String path = FileUtils.getImagesFolderPath() + "/" + name + ".png";
        File file = new File(path);
        file.mkdirs();
        if (file.exists()) file.delete();
        try {
            OutputStream stream = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            FileUtils.scanFile(file.getPath(), new MediaScannerConnection.OnScanCompletedListener() {
                public void onScanCompleted(String path, Uri uri) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ImageActivity.this, "Image saved", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//    endregion

    //    region FILE-CHOOSER
    @NeedsPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
    void showFileChooser() {
        DialogProperties properties = new DialogProperties();
        properties.selection_mode = DialogConfigs.SINGLE_MODE;
        properties.selection_type = DialogConfigs.FILE_SELECT;
        properties.root = new File(FileUtils.getImagesFolderPath());
        properties.extensions = null;

        FilePickerDialog dialog = new FilePickerDialog(ImageActivity.this, properties);

        dialog.setDialogSelectionListener(new DialogSelectionListener() {
            @Override
            public void onSelectedFilePaths(String[] files) {
                loadImage(files[0]);
            }
        });

        dialog.show();
    }
//    endregion

    //    region RUN TIME PERMISSIONS
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
