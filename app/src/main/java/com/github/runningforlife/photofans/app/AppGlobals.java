package com.github.runningforlife.photofans.app;

import android.app.Application;
import android.os.Environment;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * the entry point for the application
 */

public class AppGlobals extends Application{

    private static final String IMAGE_PATH = Environment.getExternalStorageState() +
            "/PhotoFans/Picture/";
    private static AppGlobals sInstance;

    public static AppGlobals getInstance(){
        return sInstance;
    }

    @Override
    public void onCreate(){
        super.onCreate();

        Realm.init(this);
        RealmConfiguration realmConfig = new RealmConfiguration.Builder()
                .name("PhotoFans")
                .build();
        Realm.setDefaultConfiguration(realmConfig);
        // it seems that we should init here
        sInstance = AppGlobals.this;
    }

    public String getImagePath(){
        return IMAGE_PATH;
    }
}
