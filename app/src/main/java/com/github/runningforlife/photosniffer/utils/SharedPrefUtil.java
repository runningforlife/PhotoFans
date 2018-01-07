package com.github.runningforlife.photosniffer.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.app.AppGlobals;

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
    public static List<String> getArrayList(String key) {
        Context context = AppGlobals.getInstance();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        Set<String> list = pref.getStringSet(key, Collections.EMPTY_SET);

        return new ArrayList<>(list);
    }

    public static void putArrayList(String key, Set<String> list) {
        Context context = AppGlobals.getInstance();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet(key, list);
        editor.apply();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getImageSource(){
        Context context = AppGlobals.getInstance();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        String key = context.getString(R.string.pref_choose_image_source);
        Set<String> imgSrc = pref.getStringSet(key, Collections.EMPTY_SET);

        if (imgSrc == null || imgSrc.isEmpty()) {
            String[] src = context.getResources().getStringArray(R.array.default_source_url);
            return Arrays.asList(src);
        }else{
            return new ArrayList<>(imgSrc);
        }
    }

    public static boolean isWifiOnlyDownloadMode(Context context){
        String key = context.getString(R.string.pref_wifi_download);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        return pref.getBoolean(key, true);
    }

    public static String getString(String key, String val){
        Context context = AppGlobals.getInstance();
        SharedPreferences pref = PreferenceManager.
                getDefaultSharedPreferences(context);

        return pref.getString(key, val);
    }

    public static void putString(String key, String val) {
        Context context = AppGlobals.getInstance();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, val);
        editor.apply();
    }

    public static boolean getBoolean(String key, boolean def){
        Context context = AppGlobals.getInstance();
        SharedPreferences pref = PreferenceManager.
                getDefaultSharedPreferences(context);

        return pref.getBoolean(key, def);
    }

    public static void putBoolean(String key, boolean val){
        Context context = AppGlobals.getInstance();
        SharedPreferences pref = PreferenceManager.
                getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key,val)
                .apply();
    }

    public static void putInt(String key, int n){
        Context context = AppGlobals.getInstance();
        SharedPreferences pref = PreferenceManager.
                getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, n);
        editor.apply();
    }

    public static int getInt(String key, int def){
        Context context = AppGlobals.getInstance();
        SharedPreferences pref = PreferenceManager.
                getDefaultSharedPreferences(context);

        return pref.getInt(key, def);
    }

    public static void putLong(String key, long value){
        Context context = AppGlobals.getInstance();
        SharedPreferences pref = PreferenceManager.
                getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static long getLong(String key, long def){
        Context context = AppGlobals.getInstance();
        SharedPreferences pref = PreferenceManager.
                getDefaultSharedPreferences(context);

        return pref.getLong(key,def);
    }
}
