package com.github.runningforlife.photofans.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.app.AppGlobals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * a utility class to access shared preference
 */

public class SharedPrefUtil {

    @SuppressWarnings("unchecked")
    public static List<String> getImageSource(){
        Context context = AppGlobals.getInstance();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        String key = context.getString(R.string.pref_choose_image_source);
        Set<String> imgSrc = pref.getStringSet(key, null);

        if (imgSrc == null) {
            return Collections.EMPTY_LIST;
        }else{
            return new ArrayList<>(imgSrc);
        }
    }

    public static boolean isAllowedDownload(){
        Context context = AppGlobals.getInstance();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        String key = context.getString(R.string.pref_wifi_download);

        return pref.getBoolean(key, true);
    }
}
