package com.github.runningforlife.photofans.app;

import android.app.Application;
import android.os.Environment;
import android.text.TextUtils;

import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.model.ImageRealmMigration;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * the entry point for the application
 */

public class AppGlobals extends Application{

    private static final String PATH_NAME = "photos";
    private static AppGlobals sInstance;
    private static String sImagePath;

    public static AppGlobals getInstance(){
        return sInstance;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        Realm.init(this);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name("PhotoFans")
                .migration(new ImageRealmMigration())
                .build();
        Realm.setDefaultConfiguration(realmConfig);
        // it seems that we should init here
        sInstance = AppGlobals.this;

        String appName = getString(R.string.app_name);
        sImagePath = appName + File.separator + appName;
        File file = new File(sImagePath);
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    public String getImagePath(){
        if(TextUtils.isEmpty(sImagePath)){
            return "PhotoFans" + File.separator + PATH_NAME;
        }
        return sImagePath;
    }
}
