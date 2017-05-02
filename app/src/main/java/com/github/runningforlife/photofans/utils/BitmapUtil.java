package com.github.runningforlife.photofans.utils;

import android.graphics.Bitmap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 *  a utility class to process bitmap
 */

public class BitmapUtil {

    public static int getBitmapSize(Bitmap bm){
        Bitmap bitmap = bm;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);
        byte[] bytes = bos.toByteArray();
        return bytes.length;
    }
}
