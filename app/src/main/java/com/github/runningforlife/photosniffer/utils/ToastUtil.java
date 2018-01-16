package com.github.runningforlife.photosniffer.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * utility to show toast
 */

public class ToastUtil {

    public static void showToast(Context context, String msg){
        Toast toast = Toast.makeText(context,msg,Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER,0, 0);
        toast.show();
    }
}
