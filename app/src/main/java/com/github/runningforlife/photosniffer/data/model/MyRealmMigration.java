package com.github.runningforlife.photosniffer.data.model;

import android.util.Log;

import io.realm.DynamicRealm;
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

        if (oldVersion == 0) {
            ++oldVersion;

            schema.create("ImagePageInfo")
                  .addField("mUrl", String.class)
                  .addField("mTimeStamp", Long.class)
                  .addField("mIsVisited", Boolean.class);

            schema.get("ImageRealm")
                  .addField("mIsCached", Boolean.class)
                  .addField("mHighResUrl", String.class);

            schema.get("ImagePageInfo")
                  .removeField("mVisitTime")
                  .addField("mTimeStamp", Long.class);
        }

        if (oldVersion == 1) {
            ++oldVersion;

            schema.get("ImageRealm")
                    .addField("mHighResUrl", String.class);
        }

        if (oldVersion == 2) {
            ++oldVersion;

            schema.get("ImageRealm")
                    .addField("mHighResUrl", String.class);
        }
    }
}
