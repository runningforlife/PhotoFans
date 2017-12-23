package com.github.runningforlife.photosniffer.ui.fragment;

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
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.data.remote.LeanCloudManager;
import com.github.runningforlife.photosniffer.utils.MiscUtil;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;
import com.github.runningforlife.photosniffer.utils.WallpaperUtils;

import java.util.HashMap;
import java.util.Set;

import io.realm.RealmResults;

/**
 * a fragment containing settings
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingsFragment";

    private RealmApi mRealmApi;

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);

        addPreferencesFromResource(R.xml.settings);

        mRealmApi = RealmApiImpl.getInstance();
    }

    @Override
    public void onResume() {
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

        mRealmApi.closeRealm();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.v(TAG,"onSharedPreferenceChanged(): key = " + key);

        String keyImgSrc = getString(R.string.pref_choose_image_source);
        String keyAdvice = getString(R.string.pref_give_your_advice);
        String keyAutoWallpaper = getString(R.string.pref_automatic_wallpaper);
        String keyWifiOnly = getString(R.string.pref_wifi_download);
        String keyWallpaperInterval = getString(R.string.pref_auto_wallpaper_interval);
        String keyLockScreenWallpaper = getString(R.string.pref_enable_auto_lockscreen_wallpaper);
        String keyMaxImages = getString(R.string.max_reserved_images);

        if (key.equals(keyImgSrc)) {
            Set<String> src = sharedPreferences.getStringSet(key,null);
            checkImageSourceList(src);
        }else if (keyAdvice.equals(key)) {
            String data = sharedPreferences.getString(key,"");
            checkUserAdvice(data);
        } else if (keyAutoWallpaper.equals(key)) {
            boolean isAuto = sharedPreferences.getBoolean(keyAutoWallpaper, true);
            checkAutoWallpaperSetting(isAuto);
        } else if (keyWifiOnly.equals(key)) {
            checkWifiOnlyDownloadMode();
        } else if (keyWallpaperInterval.equals(key)) {
            checkWallpaperInterval();
        } else if (keyLockScreenWallpaper.equals(key)) {
            boolean isEnabled = sharedPreferences.getBoolean(key, true);
            checkLockScreenWallpaper(true);
        } else if (keyMaxImages.equals(key)) {
            int maxImages;
            try {
                maxImages = Integer.parseInt(SharedPrefUtil.getString(key, "2147483647"));
            } catch (Exception e) {
                maxImages = Integer.MAX_VALUE;
            }
            trimDataAsync(maxImages);
        }
    }

    private void checkImageSourceList(Set<String> src) {
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
    }

    //FIXME: if no network connected, need persist user advice
    private void checkUserAdvice(String data) {
        if (!TextUtils.isEmpty(data) && MiscUtil.isConnected(getActivity())) {
            uploadAdviceToCloud(data);
        } else {
            SharedPrefUtil.putString(getString(R.string.pref_give_your_advice), data);
        }
    }

    private void checkAutoWallpaperSetting(boolean isEnabled) {
        // for OS >= LL, use JobScheduler to do wallpaper setting
        if (Build.VERSION.SDK_INT >= 21) {
            if (isEnabled) {
                WallpaperUtils.startWallpaperSettingJob(getActivity(), MiscUtil.getJobId(MiscUtil.JOB_WALLPAPER_SET));
            } else {
                WallpaperUtils.cancelSchedulerJob(getActivity(), MiscUtil.getJobId(MiscUtil.JOB_WALLPAPER_SET));
            }
        } else {
            if (isEnabled) {
                WallpaperUtils.startAutoWallpaperAlarm(getActivity());
            } else {
                WallpaperUtils.cancelAutoWallpaperAlarm(getActivity());
            }
        }
    }

    private void checkWifiOnlyDownloadMode() {
        if (Build.VERSION.SDK_INT >= 21) {
            WallpaperUtils.startWallpaperUpdaterJob(getActivity(), MiscUtil.getJobId(MiscUtil.JOB_WALLPAPER_CACHE));
        } else {
            WallpaperUtils.startWallpaperCacheUpdaterAlarm(getActivity());
        }
    }

    private void checkWallpaperInterval() {
        if (Build.VERSION.SDK_INT >= 21) {
            WallpaperUtils.startWallpaperSettingJob(getActivity(), MiscUtil.getJobId(MiscUtil.JOB_WALLPAPER_SET));
        } else {
            WallpaperUtils.startAutoWallpaperAlarm(getActivity());
        }
    }

    private void checkLockScreenWallpaper(boolean isEnabled) {
        if (isEnabled) {
            WallpaperUtils.startLockScreenWallpaperService(getActivity());
            WallpaperUtils.startWallpaperUpdaterJob(getActivity(), MiscUtil.getJobId(MiscUtil.JOB_WALLPAPER_CACHE));
        } else {
            WallpaperUtils.cancelSchedulerJob(getActivity(), MiscUtil.getJobId(MiscUtil.JOB_WALLPAPER_CACHE));
            WallpaperUtils.stopLockScreenWallpaperService(getActivity());
        }
    }

    private void trimDataAsync(int maxImages) {
        HashMap<String, String> params = new HashMap<>();
        params.put("mIsUsed", Boolean.toString(Boolean.TRUE));
        params.put("mIsFavor", Boolean.toString(Boolean.FALSE));
        params.put("mIsWallpaper", Boolean.toString(Boolean.FALSE));
        mRealmApi.trimData(ImageRealm.class, params, maxImages);
    }

    private void uploadAdviceToCloud(String advice) {
        LeanCloudManager cloud = LeanCloudManager.getInstance();

        cloud.saveAdvice(advice);
    }
}
