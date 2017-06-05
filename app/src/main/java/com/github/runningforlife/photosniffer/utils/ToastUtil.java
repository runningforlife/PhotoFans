package com.github.runningforlife.photosniffer.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * utility to show toast
 */

public class ToastUtil {

    public static void showToast(Context context, String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT)
                .show();
    }
}
