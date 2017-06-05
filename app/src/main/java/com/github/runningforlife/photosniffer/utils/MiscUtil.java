package com.github.runningforlife.photosniffer.utils;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ImageView;

import com.github.runningforlife.photosniffer.R;

/**
 * Created by jason on 6/5/17.
 */

public class MiscUtil {

    public static void preloadImage(ImageView iv) {
        Resources res = iv.getContext().getResources();
        Drawable d = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            d = res.getDrawable(R.drawable.ic_photo_grey_24dp, null);
        }else{
            d = res.getDrawable(R.drawable.ic_photo_grey_24dp);
        }
        iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        iv.setImageDrawable(d);
    }

    public static int ApiLevel(){
        return Build.VERSION.SDK_INT;
    }
}
