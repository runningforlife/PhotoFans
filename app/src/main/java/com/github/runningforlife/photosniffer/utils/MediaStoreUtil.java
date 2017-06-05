package com.github.runningforlife.photosniffer.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * add images to media store
 */

public class MediaStoreUtil {

    public static void addImageToGallery(Context context, String imgPath,String fileName, String description) throws FileNotFoundException {
        MediaStore.Images.Media.insertImage(context.getContentResolver(),imgPath, fileName, description);
    }

    public static void addImageToGallery(Context context, File imgPath){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(imgPath);
        intent.setData(uri);

        context.sendBroadcast(intent);
    }

    //FIXME: why this fails: file not found exception
    public static String addImageToGallery(Context context, Bitmap bitmap, String name) throws FileNotFoundException {
        ContentValues cv = new ContentValues();
        cv.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");
        cv.put(MediaStore.Images.Media.TITLE,name);
        cv.put(MediaStore.Images.Media.BUCKET_ID,name);
        cv.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis());
        cv.put(MediaStore.Images.Media.DATE_MODIFIED,System.currentTimeMillis());
        cv.put(MediaStore.Images.Media.DATE_TAKEN,System.currentTimeMillis());

        ContentResolver cr = context.getContentResolver();
        Uri uri = null;
        if(Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            uri = cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    cv);
        }else{
            uri = cr.insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI,cv);
        }

        String result = null;
        if(uri != null) {
            result = uri.toString();
        }

        if(bitmap != null) {
            OutputStream os = cr.openOutputStream(uri);
            try {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //ContentResolver cv = context.getContentResolver();
        //String result = MediaStore.Images.Media.insertImage(cv,bitmap,name,"");
        return result;
    }
}
