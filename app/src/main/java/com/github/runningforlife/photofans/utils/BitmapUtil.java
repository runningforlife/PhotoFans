package com.github.runningforlife.photofans.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * a utility to decode/encode bitmap
 */

public class BitmapUtil {

    public static void saveToFile(Bitmap bitmap, String path) throws FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(path);

        bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);

        try {
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Bitmap drawableToBitmap(Drawable d){
        Bitmap bitmap = null;

        if(d instanceof BitmapDrawable){
            BitmapDrawable bitmapDrawable = (BitmapDrawable)d;
            bitmap = bitmapDrawable.getBitmap();
            if(bitmap != null){
                return bitmap;
            }
        }

        if(d.getIntrinsicWidth() <= 0 || d.getIntrinsicHeight() <= 0){
            bitmap = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888);
        }else{
            bitmap = Bitmap.createBitmap(d.getIntrinsicWidth(),d.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        d.setBounds(0,0,canvas.getWidth(),canvas.getHeight());
        d.draw(canvas);

        return bitmap;
    }
}
