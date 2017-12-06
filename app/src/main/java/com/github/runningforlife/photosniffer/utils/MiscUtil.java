package com.github.runningforlife.photosniffer.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
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

    public static boolean isConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni.isConnected();
    }

    public static boolean isWifiConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni.isConnected() && ni.getType() == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isMobileConnected(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo ni = cm.getActiveNetworkInfo();

        return ni.isConnected() && ni.getType() == ConnectivityManager.TYPE_MOBILE;
    }
}
