package com.github.runningforlife.photosniffer.ui.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.util.Log;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.data.model.ImagePageInfo;
import com.github.runningforlife.photosniffer.data.remote.LeanCloudManager;
import com.github.runningforlife.photosniffer.utils.MiscUtil;
import com.github.runningforlife.photosniffer.utils.WallpaperUtils;

import java.util.HashMap;
import java.util.Set;

import io.realm.RealmResults;

import static com.github.runningforlife.photosniffer.ui.receiver.WallpaperAlarmReceiver.ALARM_AUTO_WALLPAPER;

/**
 * a fragment containing settings
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsFragment";

    private AlarmManager mAlarmMgr;
    private RealmApi mRealmApi;

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);

        addPreferencesFromResource(R.xml.settings);

        mAlarmMgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        mRealmApi = RealmApiImpl.getInstance();
    }

    @Override
    public void onResume(){
        super.onResume();

        if (Build.VERSION.SDK_INT < 24) {
            // cannot set auto lock screen, so remove it
            final Preference pf = findPreference(getString(R.string.pref_enable_auto_lockscreen_wallpaper));
            if (pf != null) {
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

        mRealmApi.decRef();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.v(TAG,"onSharedPreferenceChanged(): key = " + key);

        String keyImgSrc = getString(R.string.pref_choose_image_source);
        String keyAdvice = getString(R.string.pref_give_your_advice);
        String keyAutoWallpaper = getString(R.string.pref_automatic_wallpaper);
        String keyWifiOnly = getString(R.string.pref_wifi_download);

        if (key.equals(keyImgSrc)) {
            Set<String> src = sharedPreferences.getStringSet(key,null);
            if (src != null) {
                HashMap<String,String> params = new HashMap<>();
                params.put("mIsVisited", Boolean.toString(Boolean.TRUE));

                RealmResults<ImagePageInfo> visited = (RealmResults<ImagePageInfo>) mRealmApi.querySync(ImagePageInfo.class, params);

                for (String url : src) {
                    if (!visited.contains(url)) {
                        mRealmApi.insertAsync(new ImagePageInfo(url));
                    }
                }
            }
        }else if (keyAdvice.equals(key)){
            String data = sharedPreferences.getString(key,"");
            if (!TextUtils.isEmpty(data)) {
                uploadAdviceToCloud(data);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(key,"");
                editor.apply();
            }
        } else if (keyAutoWallpaper.equals(key)) {
            boolean isAuto = sharedPreferences.getBoolean(keyAutoWallpaper, true);
            // for OS >= LL, use JobScheduler to do wallpaper setting
            if (Build.VERSION.SDK_INT >= 21) {
                WallpaperUtils.startWallpaperSettingJob(getActivity(), MiscUtil.getJobId(MiscUtil.JOB_WALLPAPER_SET));
            } else {
                if (isAuto) {
                    WallpaperUtils.startAutoWallpaperAlarm(getActivity());
                } else {
                    cancelAutoWallpaperAlarm();
                }
            }
        } else if (keyWifiOnly.equals(key)) {
            if (Build.VERSION.SDK_INT >= 21) {
                WallpaperUtils.startWallpaperUpdaterJob(getActivity(), MiscUtil.getJobId(MiscUtil.JOB_WALLPAPER_CACHE));
            } else {
                WallpaperUtils.startWallpaperCacheUpdaterAlarm(getActivity());
            }
        }
    }

    private void uploadAdviceToCloud(String advice) {
        LeanCloudManager cloud = LeanCloudManager.getInstance();

        cloud.saveAdvice(advice);
    }

    private void cancelAutoWallpaperAlarm() {
        Log.v(TAG,"cancelAutoWallpaperAlarm()");
        PendingIntent alarmIntent = MiscUtil.getPendingIntent(ALARM_AUTO_WALLPAPER, getActivity());

        mAlarmMgr.cancel(alarmIntent);
    }
}
