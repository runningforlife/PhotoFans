package com.github.runningforlife.photosniffer.ui.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.util.Log;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.data.model.ImagePageInfo;
import com.github.runningforlife.photosniffer.data.local.RealmManager;
import com.github.runningforlife.photosniffer.data.remote.LeanCloudManager;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

import java.util.Iterator;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmResults;

import static com.github.runningforlife.photosniffer.ui.receiver.WallpaperAlarmReceiver.ALARM_AUTO_WALLPAPER;

/**
 * a fragment containing settings
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsFragment";

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);

        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onResume(){
        super.onResume();

        if(Build.VERSION.SDK_INT < 24){
            // cannot set auto lock screen, so remote it
            final Preference pf = findPreference(getString(R.string.pref_enable_auto_lockscreen_wallpaper));
            if(pf != null) {
                boolean isRemoved = getPreferenceScreen().removePreference(pf);
                Log.v(TAG, "cannot support lock screen auto wallpaper=" + isRemoved);
            }
        }

        getPreferenceScreen()
                .getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        getPreferenceScreen()
                .getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.v(TAG,"onSharedPreferenceChanged(): key = " + key);

        String keyImgSrc = getString(R.string.pref_choose_image_source);
        String keyAdvice = getString(R.string.pref_give_your_advice);
        String keyAutoWallpaper = getString(R.string.pref_automatic_wallpaper);

        if(key.equals(keyImgSrc)) {
            Set<String> src = sharedPreferences.getStringSet(key,null);
            if(src != null) {
                RealmManager helper = RealmManager.getInstance();

                Realm realm = Realm.getDefaultInstance();
                RealmResults<ImagePageInfo> visited = RealmManager.getAllVisitedPages(realm);

                Iterator it = src.iterator();
                while(it.hasNext()){
                    String url = (String) it.next();
                    if(!visited.contains(url)){
                        helper.savePageAsync(new ImagePageInfo(url));
                    }
                }

                realm.close();
            }
        }else if(keyAdvice.equals(key)){
            String data = sharedPreferences.getString(key,"");
            if(!TextUtils.isEmpty(data)) {
                uploadAdviceToCloud(data);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(key,"");
            }
        }else if(keyAutoWallpaper.equals(key)){
            boolean isAuto = sharedPreferences.getBoolean(keyAutoWallpaper, true);
            if(isAuto) {
                startAutoWallpaperAlarm();
            }else{
                cancelAutoWallpaperAlarm();
            }
        }
    }

    private void uploadAdviceToCloud(String advice){
        LeanCloudManager cloud = LeanCloudManager.getInstance();

        cloud.saveAdvice(advice);
    }

    private void startAutoWallpaperAlarm(){
        Log.v(TAG,"startAutoWallpaperAlarm()");
        // start a alarm to enable automatic wallpaper setting
        Intent intent = new Intent(ALARM_AUTO_WALLPAPER);
        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmMgr = (AlarmManager) getActivity()
                .getSystemService(Context.ALARM_SERVICE);

        String key = getString(R.string.pref_auto_wallpaper_interval);
        int interval = Integer.parseInt(SharedPrefUtil.getString(key, "30000"));
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 10,interval, pi);
    }

    private void cancelAutoWallpaperAlarm(){
        AlarmManager alarmMgr = (AlarmManager) getActivity()
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ALARM_AUTO_WALLPAPER);
        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.cancel(pi);
    }
}
