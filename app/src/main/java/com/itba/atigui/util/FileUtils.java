package com.itba.atigui.util;

import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

import com.itba.atigui.AtiApplication;

public class FileUtils {
    public static String getRootFolderPath() {
        return Environment.getExternalStorageDirectory().toString();
    }

    public static String getImagesFolderPath() {
        return getRootFolderPath() + "/images";
    }

    public static void scanFile(String path, MediaScannerConnection.OnScanCompletedListener listener) {
        if (listener == null) {
            listener = new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String s, Uri uri) {
                }
            };
        }
        MediaScannerConnection.scanFile(AtiApplication.getInstance(),
                new String[]{path}, null, listener);
    }

    /**
     * used after deleting files
     */
    public static void refreshGallery() {
        scanFile(getRootFolderPath(), null);
    }

    public static String getFileName(String path) {
        String[] splits = path.split("/");
        return splits[splits.length - 1];
    }
}
