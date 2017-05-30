package com.github.runningforlife.photofans.model;

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
            // add new field
            schema.get("ImageRealm")
                    .addField("mData",byte[].class);
            ++oldVersion;
        }
    }
}
