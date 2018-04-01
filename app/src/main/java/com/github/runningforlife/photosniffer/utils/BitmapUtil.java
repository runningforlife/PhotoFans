package com.github.runningforlife.photosniffer.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * a utility to decode/encode bitmap
 *
 * @Note scale method reference from
 * https://guides.codepath.com/android/Working-with-the-ImageView#supporting-multiple-densities
 */

public class BitmapUtil {

    private static DateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US);

    public static String saveToFile(Bitmap bitmap, String path) throws FileNotFoundException {
        String imgName = buildImageFileName();
        File file = new File(path, imgName);
        FileOutputStream fos = new FileOutputStream(file);

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        try {
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();
    }

    public static String buildImageFileName() {
        return "img" + "_" + df.format(new Date()) + ".png";
    }
}