package com.github.runningforlife.photosniffer.presenter;

import android.content.Context;
import android.util.Log;

import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.ui.FavorPictureView;

import java.util.HashMap;

import io.realm.RealmResults;

/**
 * presenter to get favor image list
 */

public class FavorImagePresenterImpl extends PresenterBase {
    private static final String TAG = "FavorImagePresenter";

    public FavorImagePresenterImpl(Context context, FavorPictureView view){
        super(context, view);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onStart() {
        Log.v(TAG,"onStart()");
        HashMap<String,String> params = new HashMap<>();
        params.put("mIsUsed", Boolean.toString(true));
        params.put("mIsFavor", Boolean.toString(true));
        params.put("mIsWallpaper", Boolean.toString(false));
        mImageList = (RealmResults<ImageRealm>) mRealmApi.queryAsync(ImageRealm.class, params);
        mImageList.addChangeListener(mOrderRealmChangeListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG,"onDestroy()");
        //mRealmMgr.onDestroy();
    }
}
