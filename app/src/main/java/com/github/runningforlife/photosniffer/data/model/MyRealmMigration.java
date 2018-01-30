package com.github.runningforlife.photosniffer.data.model;

import android.media.Image;
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

            schema.create(ImagePageInfo.class.getSimpleName())
                  .addField("mUrl", String.class)
                  .addField("mTimeStamp", Long.class)
                  .addField("mIsVisited", Boolean.class)
                  .removeField("mVisitTime");

            schema.get(ImageRealm.class.getSimpleName())
                  .addField("mIsCached", Boolean.class)
                  .addField("mHighResUrl", String.class);
        }

        if (oldVersion == 1) {
            ++oldVersion;

            schema.get(ImageRealm.class.getSimpleName())
                    .addField("mHighResUrl", String.class);
        }
    }
}
