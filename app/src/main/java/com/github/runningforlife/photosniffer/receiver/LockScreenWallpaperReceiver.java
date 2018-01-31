package com.github.runningforlife.photosniffer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.service.WallpaperJobService;

/**
 * Created by jason on 11/30/17.
 */

public class LockScreenWallpaperReceiver extends BroadcastReceiver {
    private static final String TAG = "LockScreenReceiver";

    private Handler mHandler;

    public LockScreenWallpaperReceiver(Handler handler){
        mHandler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isAutoLockScreen = sp.getBoolean(context.getString(R.string.pref_enable_auto_lockscreen_wallpaper), true);

        if(Intent.ACTION_SCREEN_ON.equals(intent.getAction()) && isAutoLockScreen){
            Message message = mHandler.obtainMessage(WallpaperJobService.EVENT_SET_LOCK_SCREEN_WALLPAPER);
            message.sendToTarget();
        }
    }
}
