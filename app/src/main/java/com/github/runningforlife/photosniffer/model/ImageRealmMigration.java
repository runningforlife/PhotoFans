package com.github.runningforlife.photosniffer.model;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * image realm data migration
 */

public class ImageRealmMigration implements RealmMigration{
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        if(oldVersion == 0){
            schema.get("ImageRealm")
                    .addField("mIsWallpaper",Boolean.class);
            ++oldVersion;
        }

        if(oldVersion == 1){
            schema.create("QuoteRealm");
            ++oldVersion;
        }
    }
}
