package com.github.runningforlife.photosniffer.data.model;

import android.media.Image;
import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.RealmObjectSchema;
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

            RealmObjectSchema pageSchema = schema.get(ImagePageInfo.class.getSimpleName());
            if (pageSchema != null) {
                if (!pageSchema.hasField("mTimeStamp")) {
                    pageSchema.addField("mTimeStamp", Long.class);
                }
                if (pageSchema.hasField("mVisitTime")) {
                    pageSchema.removeField("mVisitTime");
                }
            }

            RealmObjectSchema imageSchema = schema.get(ImageRealm.class.getSimpleName());
            if (imageSchema != null) {
                if (!imageSchema.hasField("mIsWallpaper")) {
                    imageSchema.addField("mIsWallpaper", Boolean.class);
                }
                if (!imageSchema.hasField("mIsCached")) {
                    imageSchema.addField("mIsCached", Boolean.class);
                }
                if (!imageSchema.hasField("mHighResUrl")) {
                    imageSchema.addField("mHighResUrl", String.class);
                }
            }

        }
    }
}
