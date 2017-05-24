package com.github.runningforlife.photofans.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.app.AppGlobals;

import java.util.ArrayList;
import java.util.Arrays;
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
            String[] src = context.getResources().getStringArray(R.array.default_source_url);
            return Arrays.asList(src);
        }else{
            return new ArrayList<>(imgSrc);
        }
    }

    public static boolean getWifiDownloadMode(Context context, String key){
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        return pref.getBoolean(key, true);
    }

    public static int getMaxReservedImages(Context context){
        String key = context.getString(R.string.pref_max_reserved_images);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        return pref.getInt(key,100);
    }

    public static int getMaxReservedImages(){
        Context context = AppGlobals.getInstance();
        String key = context.getString(R.string.pref_max_reserved_images);
        SharedPreferences pref = PreferenceManager.
                getDefaultSharedPreferences(context);

        return pref.getInt(key,100);
    }
}
