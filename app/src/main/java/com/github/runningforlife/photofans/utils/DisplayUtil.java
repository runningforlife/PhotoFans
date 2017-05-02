package com.github.runningforlife.photofans.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.github.runningforlife.photofans.app.AppGlobals;

/**
 * a utility class to get device display info
 */

public class DisplayUtil {

    public static DisplayMetrics getScreenDimen(){
        DisplayMetrics metrics = new DisplayMetrics();

        Context context = AppGlobals.getInstance();

        WindowManager winMgr = (WindowManager)context
                .getSystemService(Context.WINDOW_SERVICE);
        winMgr.getDefaultDisplay().getMetrics(metrics);

        return metrics;
    }

    public static double getScreenRatio(){
        DisplayMetrics metrics = getScreenDimen();

        return ((double)metrics.widthPixels)/metrics.heightPixels;
    }

    public static int dp2px(int px){
        final float scale = getScreenDimen().density;

        return (int)(scale*px  + 0.5f);
    }

    public static int px2dp(int px){
        final float scale = getScreenDimen().density;

        return (int)(px/scale + 0.5f);
    }
}
