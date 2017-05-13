package com.github.runningforlife.photofans.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

/**
 * a utility to decode/encode bitmap
 */

public class BitmapUtil {

    public static String saveToFile(Bitmap bitmap, String path, String name) throws FileNotFoundException {
        String imgName = buildFileName(name);
        File file = new File(path, imgName);
        FileOutputStream fos = new FileOutputStream(file);

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);

        try {
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imgName;
    }

    public static Bitmap drawableToBitmap(Drawable d) {
        Bitmap bitmap = null;

        if (d instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) d;
            bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null) {
                return bitmap;
            }
        }

        if (d.getIntrinsicWidth() <= 0 || d.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(), d.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        d.draw(canvas);

        return bitmap;
    }

    private static String buildFileName(String name) {
        return name + "_" + System.currentTimeMillis() + ".jpg";
    }
}