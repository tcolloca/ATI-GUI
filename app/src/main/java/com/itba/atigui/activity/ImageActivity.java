package com.itba.atigui.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.goodengineer.atibackend.ImageUtils;
import com.goodengineer.atibackend.transformation.CompoundImageTransformation;
import com.goodengineer.atibackend.transformation.ConstrastTransformation;
import com.goodengineer.atibackend.transformation.DynamicRangeCompressionTransformation;
import com.goodengineer.atibackend.transformation.ImageTransformation;
import com.goodengineer.atibackend.transformation.NegativeTransformation;
import com.goodengineer.atibackend.transformation.PaintPixelTransformation;
import com.goodengineer.atibackend.transformation.RectBorderTransformation;
import com.goodengineer.atibackend.transformation.ScaleTransformation;
import com.goodengineer.atibackend.transformation.ThresholdingTransformation;
import com.itba.atigui.R;
import com.itba.atigui.async.BackgroundTask;
import com.itba.atigui.model.BitmapImageFactory;
import com.itba.atigui.model.BlackAndWhiteBitmapImageSource;
import com.itba.atigui.util.AspectRatioImageView;
import com.itba.atigui.util.BitmapUtils;
import com.itba.atigui.util.FileUtils;
import com.itba.atigui.view.ColorPickerDialog;
import com.itba.atigui.view.HistogramDialog;
import com.itba.atigui.view.ImageControllerView;
import com.itba.atigui.view.NumberPickerDialog;
import com.itba.atigui.view.RangePickerDialog;

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

    @BindView(R.id.image_controller)
    ImageControllerView imageControllerView;
    @BindView(R.id.image_original)
    AspectRatioImageView originalImage;
    @BindView(R.id.image_mutable)
    AspectRatioImageView mutableImage;
    @BindView(R.id.activity_image_paint_pixel_transformation_button)
    View pixelColorView;
    @BindView(R.id.activity_image_pixel_color_number)
    TextView pixelColorNumber;
    @BindView(R.id.activity_image_pixel_x)
    TextView pixelXText;
    @BindView(R.id.activity_image_pixel_y)
    TextView pixelYText;
    @BindView(R.id.activity_image_pixel_width)
    TextView pixelWidthText;
    @BindView(R.id.activity_image_pixel_height)
    TextView pixelHeightText;
    @BindView(R.id.activity_image_show_original_text)
    TextView showOriginalText;
    @BindView(R.id.activity_image_title)
    TextView imageTitle;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.image_container)
    FrameLayout imageContainer;
    @BindView(R.id.activity_image_precision_checkbox)
    CheckBox precisionCheckbox;

    View rectangleView;

    private String currentImagePath;

    private BlackAndWhiteBitmapImageSource originalImageSource;
    private BlackAndWhiteBitmapImageSource mutableImageSource;
    CompoundImageTransformation transformation = new CompoundImageTransformation();

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
            public void onNewDimensions(int width, int height) {
                pixelWidthText.setText(String.valueOf(width));
                pixelHeightText.setText(String.valueOf(height));
            }

            @Override
            public void onPixelSelected(Point pixel) {
                int color = mutableImageSource.getPixel(pixel.x, pixel.y);
                pixelColorView.setBackgroundColor(Color.rgb(color, color, color));
                pixelColorNumber.setText(String.valueOf(color));
                pixelXText.setText(String.valueOf(pixel.x));
                pixelYText.setText(String.valueOf(pixel.y));
            }

            @Override
            public void onPixelUnselected() {
                pixelColorView.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
                pixelColorNumber.setText("");
                pixelXText.setText("");
                pixelYText.setText("");
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

        FileUtils.refreshGallery();
        clearAll();
        toolbarTitle.setText(getString(R.string.app_name));
    }

    public void loadImage(String path) {
        clearAll();
        currentImagePath = path;
        imageTitle.setText(FileUtils.getFileName(path));

        Bitmap bitmap = BitmapUtils.decodeFile(path);

        originalImageSource = new BlackAndWhiteBitmapImageSource(bitmap);
        mutableImageSource = (BlackAndWhiteBitmapImageSource) originalImageSource.copy();

        originalImage.setImageBitmap(originalImageSource.getBitmap());
        mutableImage.setImageBitmap(mutableImageSource.getBitmap());

//        sets transparent bitmap for pixel and area selection
        imageControllerView.setImageBitmap(BitmapUtils.createTransparent(originalImageSource.getBitmap()));

    }

    private void clearAll() {
        currentImagePath = null;
        imageControllerView.clear();
        originalImage.clear();
        imageTitle.setText(null);
        pixelWidthText.setText(null);
        pixelHeightText.setText(null);
        if (rectangleView != null) {
            imageContainer.removeView(rectangleView);
            rectangleView = null;
        }
    }

    /**
     * call this method as few times as possible.
     */
    private void refreshImage() {
        if (mutableImageSource == null) return;
        mutableImageSource.dispose();

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Undo");
        progress.setMessage("Wait while the undo is performed...");
        progress.setCancelable(false);
        progress.show();

        new BackgroundTask(new BackgroundTask.Callback() {
            @Override
            public void onBackground() {
                mutableImageSource = (BlackAndWhiteBitmapImageSource) originalImageSource.copy();
                transformation.transform(mutableImageSource);
            }

            @Override
            public void onUiThread() {
                mutableImage.setImageBitmap(mutableImageSource.getBitmap());
                progress.dismiss();
            }
        }).execute();
    }

    private void addTransformation(final ImageTransformation imageTransformation) {
        transformation.addTransformation(imageTransformation);

        final ProgressDialog progress = new ProgressDialog(this);
        progress.setTitle("Add transformation");
        progress.setMessage("Wait while the transformation is added...");
        progress.setCancelable(false);
        progress.show();

        new BackgroundTask(new BackgroundTask.Callback() {
            @Override
            public void onBackground() {
                imageTransformation.transform(mutableImageSource);
                mutableImageSource.normalize();
            }

            @Override
            public void onUiThread() {
                mutableImage.setImageBitmap(mutableImageSource.getBitmap());
                progress.dismiss();
            }
        }).execute();
    }

    private void undo() {
        transformation.removeLastTransformation();
        refreshImage();
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

    @OnClick(R.id.toolbar_histogram)
    void onHistogramButtonClick() {
        if (!imageControllerView.hasBitmap()) return;
        new HistogramDialog(this, ImageUtils.createHistogram(mutableImageSource)).show();
    }

    @OnClick(R.id.toolbar_export)
    void onExportButtonClick() {
        if (!imageControllerView.isReadyToExport()) {
            Toast.makeText(ImageActivity.this, "cant export yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Rect rect = imageControllerView.getSelectedRectangle();
        BlackAndWhiteBitmapImageSource croppedImageSource = (BlackAndWhiteBitmapImageSource) ImageUtils.crop(mutableImageSource, rect.left, rect.top, rect.width(), rect.height(), new BitmapImageFactory());
        ImageActivityPermissionsDispatcher.showChooseImageNameDialogWithCheck(this, croppedImageSource.getBitmap() );
    }

    @OnClick(R.id.toolbar_undo)
    void onUndoButtonClick() {
        undo();
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

    @OnClick(R.id.activity_image_paint_pixel_transformation_button)
    void onPaintPixelTransformationButtonClick() {
        if (imageControllerView.getCurrentPixel() == null) return;
        final Point currentPixel = imageControllerView.getCurrentPixel();
        int initialColor = mutableImageSource.getPixel(currentPixel.x, currentPixel.y);
        new ColorPickerDialog(this, initialColor, new ColorPickerDialog.Listener() {
            @Override
            public void onColorAvailable(int color) {
                addTransformation(new PaintPixelTransformation(currentPixel.x, currentPixel.y, color));
                pixelColorView.setBackgroundColor(Color.rgb(color, color, color));
            }
        }).show();
    }

    @OnClick(R.id.activity_image_draw_rect_transformation_button)
    void onDrawRectTransformationButtonClick() {
        if (!imageControllerView.isReadyToExport()) {
            Toast.makeText(ImageActivity.this, "cant draw rect yet", Toast.LENGTH_SHORT).show();
            return;
        }
        new ColorPickerDialog(this, 0, new ColorPickerDialog.Listener() {
            @Override
            public void onColorAvailable(int color) {
                Rect rect = imageControllerView.getSelectedRectangle();
                addTransformation(new RectBorderTransformation(rect.left, rect.top, rect.right, rect.bottom, color));
            }
        }).show();
    }

    @OnClick(R.id.activity_image_negative_transformation_button)
    void onNegativeTransformationButtonClick() {
        if (!imageControllerView.hasBitmap()) return;
        addTransformation(new NegativeTransformation());
    }

    @OnClick(R.id.activity_image_thresholding_transformation_button)
    void onThresholdingTransformationButtonClick() {
        if (!imageControllerView.hasBitmap()) return;
        new ColorPickerDialog(this, 0, new ColorPickerDialog.Listener() {
            @Override
            public void onColorAvailable(int color) {
                addTransformation(new ThresholdingTransformation(color));
            }
        }).show();
    }

    @OnClick(R.id.activity_image_scale_transformation_button)
    void onScaleTransformationButtonClick() {
        if (!imageControllerView.hasBitmap()) return;
        new NumberPickerDialog(this, new NumberPickerDialog.Listener() {
            @Override
            public void onNumberAvailable(int number) {
                addTransformation(new ScaleTransformation(number));
            }
        }).show();
    }

    @OnClick(R.id.activity_image_drc_transformation_button)
    void onDynamicRangeCompressionTransformationButtonClick() {
        if (!imageControllerView.hasBitmap()) return;
        addTransformation(new DynamicRangeCompressionTransformation());
    }

    @OnClick(R.id.activity_image_contrast_transformation_button)
    void onContrastTransformationButtonClick() {
        if (!imageControllerView.hasBitmap()) return;
        new RangePickerDialog(this, new RangePickerDialog.Listener() {
            @Override
            public void onRangeAvailable(int left, int right) {
                addTransformation(new ConstrastTransformation(left, right));
            }
        }).show();
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
