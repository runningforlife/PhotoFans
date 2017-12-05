package com.github.runningforlife.photosniffer.presenter;

import android.media.Image;
import android.util.Log;

import com.github.runningforlife.photosniffer.data.local.RealmApi;
import com.github.runningforlife.photosniffer.data.local.RealmApiImpl;
import com.github.runningforlife.photosniffer.data.local.RealmManager;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.ui.UI;

import java.util.HashMap;

import io.realm.OrderedCollectionChangeSet;
import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * base class for presenter
 */

abstract class PresenterBase implements Presenter {
    private static final String TAG = "PresenterBase";

    UI mView;

    RealmResults<ImageRealm> mImageList;
    RealmManager mRealmMgr;
    RealmApi mRealmApi;

    OrderReamChangeListener mOrderRealmChangeListener;

    PresenterBase(UI view) {
        mView = view;
        mRealmMgr = RealmManager.getInstance();
        mOrderRealmChangeListener = new OrderReamChangeListener();
        mRealmApi = RealmApiImpl.getInstance();
    }

    private final class OrderReamChangeListener implements OrderedRealmCollectionChangeListener<RealmResults<ImageRealm>> {

        @Override
        public void onChange(RealmResults<ImageRealm> images, OrderedCollectionChangeSet changeSet) {
            Log.v(TAG,"onChange(): size = " + images.size());
            //images.sort("mTimeStamp", Sort.DESCENDING);
            if (changeSet == null) {
                mImageList = images;
                mView.onDataSetChange(0, mImageList.size(), RealmOp.OP_REFRESH);
            } else {
                // For deletions, the adapter has to be notified in reverse order.
                OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
                for (int i = deletions.length - 1; i >= 0; i--) {
                    OrderedCollectionChangeSet.Range range = deletions[i];
                    mView.onDataSetChange(range.startIndex, range.length, RealmOp.OP_DELETE);
                }

                // insert
                OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
                for (OrderedCollectionChangeSet.Range range : insertions) {
                    mView.onDataSetChange(range.startIndex, range.length, RealmOp.OP_INSERT);
                }

                // modification
                OrderedCollectionChangeSet.Range[] modifications = changeSet.getChangeRanges();
                for (OrderedCollectionChangeSet.Range range : modifications) {
                    mView.onDataSetChange(range.startIndex, range.length, RealmOp.OP_MODIFY);
                }
            }
        }
    }

    void markAsWallpaper(String url) {
        Log.v(TAG,"markAsWallpaper()");
        // mark it as wall paper
        HashMap<String,String> params = new HashMap<>();
        HashMap<String, String> updated = new HashMap<>();

        params.put("mUrl", url);
        updated.put("mIsWallpaper", Boolean.toString(true));
        mRealmApi.updateAsync(ImageRealm.class, params, updated);
    }
}
