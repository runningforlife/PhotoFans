package com.github.runningforlife.photosniffer.ui.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.model.RealmManager;
import com.github.runningforlife.photosniffer.model.VisitedPageInfo;
import com.github.runningforlife.photosniffer.remote.LeanCloudManager;
import com.github.runningforlife.photosniffer.ui.receiver.WallpaperAlarmReceiver;
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
    private static WallpaperAlarmReceiver sAlarmReceiver = new WallpaperAlarmReceiver();;
    //private WallpaperPresenterImpl mPresenter;

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);

        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onResume(){
        super.onResume();

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
                RealmResults<VisitedPageInfo> visited = helper.getAllVisitedPages(realm);

                Iterator it = src.iterator();
                while(it.hasNext()){
                    String url = (String) it.next();
                    if(!visited.contains(url)){
                        helper.savePageAsync(new VisitedPageInfo(url));
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
        }

        boolean isAuto = sharedPreferences.getBoolean(keyAutoWallpaper, false);
        if(keyAutoWallpaper.equals(key)){
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
        int interval = Integer.parseInt(SharedPrefUtil.getString(key, "1800000"));
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),interval, pi);
    }

    private void cancelAutoWallpaperAlarm(){
        AlarmManager alarmMgr = (AlarmManager) getActivity()
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(ALARM_AUTO_WALLPAPER);
        PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        alarmMgr.cancel(pi);
    }
}
