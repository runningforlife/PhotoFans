package com.github.runningforlife.photosniffer.utils;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

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
