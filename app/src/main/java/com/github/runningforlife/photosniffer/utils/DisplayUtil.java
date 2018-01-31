package com.github.runningforlife.photosniffer.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.github.runningforlife.photosniffer.app.AppGlobals;

/**
 * a utility class to get device display info
 */

public class DisplayUtil {

    public static DisplayMetrics getScreenDimen() {
        DisplayMetrics metrics = new DisplayMetrics();

        Context context = AppGlobals.getInstance();

        WindowManager winMgr = (WindowManager)context
                .getSystemService(Context.WINDOW_SERVICE);
        winMgr.getDefaultDisplay().getMetrics(metrics);

        return metrics;
    }
}
