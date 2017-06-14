package com.github.runningforlife.photosniffer.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.util.Log;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.model.RealmManager;
import com.github.runningforlife.photosniffer.model.VisitedPageInfo;
import com.github.runningforlife.photosniffer.remote.LeanCloudManager;

import java.util.Iterator;
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
        String keyAdvice = getString(R.string.pref_give_your_advice);

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
                        helper.writeAsync(new VisitedPageInfo(url));
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
    }

    private void uploadAdviceToCloud(String advice){
        LeanCloudManager cloud = LeanCloudManager.getInstance();

        cloud.saveAdvice(advice);
    }
}
