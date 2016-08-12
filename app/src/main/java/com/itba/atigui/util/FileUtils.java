package com.itba.atigui.util;

import android.print.pdf.PrintedPdfDocument;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    public static List<String> allFilePaths(File directory) {
        ArrayList<String> filePaths = new ArrayList<>();
        ArrayList<File> files = new ArrayList<>();
        listf(directory, files);
        for (File file: files) {
            filePaths.add(file.getAbsolutePath());
        }
        return filePaths;
    }

    private static void listf(File directory, ArrayList<File> files) {

        // get all the files from a directory
        File[] fList = directory.listFiles();
        if (fList == null) return;
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                listf(file, files);
            }
        }
    }
}
