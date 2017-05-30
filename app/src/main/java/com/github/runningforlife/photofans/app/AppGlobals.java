package com.github.runningforlife.photofans.app;

import android.app.Application;
import android.os.Environment;

import com.github.runningforlife.photofans.model.ImageRealmMigration;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * the entry point for the application
 */

public class AppGlobals extends Application{

    private static final String IMAGE_PATH = Environment.getExternalStorageDirectory() +
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
                .migration(new ImageRealmMigration())
                .build();
        Realm.setDefaultConfiguration(realmConfig);
        // it seems that we should init here
        sInstance = AppGlobals.this;

        File file = new File(IMAGE_PATH);
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    public String getImagePath(){
        return IMAGE_PATH;
    }
}
