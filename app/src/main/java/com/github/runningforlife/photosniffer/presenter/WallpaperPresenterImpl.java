package com.github.runningforlife.photosniffer.presenter;

import android.content.Context;
import android.util.Log;

import com.bumptech.glide.RequestManager;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.ui.WallpaperView;
import com.github.runningforlife.photosniffer.ui.fragment.WallPaperFragment;

import java.util.HashMap;

import io.realm.RealmResults;

/**
 * wallpaper presenter to get UI data
 */

public class WallpaperPresenterImpl extends PresenterBase {
    private static final String TAG = "WallpaperPresenter";

    public WallpaperPresenterImpl(RequestManager requestManager, Context context, WallpaperView view){
        super(requestManager, context, view);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onStart() {
        Log.v(TAG,"onStart()");
        HashMap<String,String> params = new HashMap<>();
        params.put("mIsUsed", Boolean.toString(true));
        params.put("mIsFavor", Boolean.toString(false));
        params.put("mIsWallpaper", Boolean.toString(true));
        //params.put("mIsCached", Boolean.toString(true));
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
