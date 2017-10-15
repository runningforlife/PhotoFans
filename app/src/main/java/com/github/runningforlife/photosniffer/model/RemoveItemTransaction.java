package com.github.runningforlife.photosniffer.model;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 *  item to be removed from realm
 */
public class RemoveItemTransaction implements Realm.Transaction{
    private RealmObject removedTarget;

    @Override
    public void execute(Realm realm) {
        removedTarget.deleteFromRealm();
    }
}
