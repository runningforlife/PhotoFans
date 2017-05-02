package com.github.runningforlife.photofans.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.model.RealmHelper;
import com.github.runningforlife.photofans.model.VisitedPageInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmResults;

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

        if(key.equals(keyImgSrc)) {
            Set<String> src = sharedPreferences.getStringSet(key,null);
            if(src != null) {
                Realm realm = Realm.getDefaultInstance();
                RealmResults<VisitedPageInfo> visited = realm.where(VisitedPageInfo.class)
                        .equalTo("mIsVisited", true)
                        .findAll();

                RealmHelper helper = RealmHelper.getInstance();

                Iterator it = src.iterator();
                while(it.hasNext()){
                    String url = (String) it.next();
                    if(!visited.contains(url)){
                        helper.writeAsync(new VisitedPageInfo(url));
                    }
                }
            }
        }
    }
}
