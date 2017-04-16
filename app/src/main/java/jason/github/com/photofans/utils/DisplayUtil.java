package jason.github.com.photofans.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import jason.github.com.photofans.app.AppGlobals;

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

        return metrics.widthPixels/metrics.heightPixels;
    }

    public static int dp2px(int px){
        final float scale = getScreenDimen().density;

        return (int)(scale*px  + 0.5f);
    }
}
