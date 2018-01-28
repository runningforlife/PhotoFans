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

    public static void addImageToGallery(Context context, File imgPath){
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(imgPath);
        intent.setData(uri);

        context.sendBroadcast(intent);
    }
}
