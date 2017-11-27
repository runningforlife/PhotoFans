package com.github.runningforlife.photosniffer.model;

import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * image realm data migration
 */

public class MyRealmMigration implements io.realm.RealmMigration {
    private static final String TAG = "MyRealmMigration";
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();
        Log.v(TAG, "migrate realm from version " + oldVersion + " to version " + newVersion);
    }
}
