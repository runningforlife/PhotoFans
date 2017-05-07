package com.github.runningforlife.photofans.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * add images to media store
 */

public class MediaStoreUtil {

    public static void addImageToGallery(Context context, File imgPath){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(imgPath);
        intent.setData(uri);

        context.sendBroadcast(intent);
    }

    public static void addImageToGallery(Context context, Bitmap bitmap, String name){
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.MIME_TYPE,"image/jpg");
        cv.put(MediaStore.Images.Media.TITLE,name);
        cv.put(MediaStore.Images.Media.BUCKET_ID,name);
        cv.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        cv.put(MediaStore.Images.Media.DATE_MODIFIED,System.currentTimeMillis());
        cv.put(MediaStore.Images.Media.DATE_TAKEN,System.currentTimeMillis());

        ContentResolver cr = context.getContentResolver();
        Uri uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                cv);

        try{
            OutputStream os = cr.openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,os);

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
