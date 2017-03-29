package jason.github.com.photofans.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import jason.github.com.photofans.model.ImageItem;
import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.repository.RealmHelper;
import jason.github.com.photofans.service.ImageRetrieveService;
import jason.github.com.photofans.service.ServiceStatus;
import jason.github.com.photofans.service.SimpleResultReceiver;

/**
 * a presenter to bridge UI and data repository
 */

public class GalleryPresenterImpl implements GalleryPresenter,SimpleResultReceiver.Receiver{
    private static final String TAG = "GalleryPresenter";

    private Context mContext;
    private GalleryView mView;
    private List<ImageRealm> mImgList;
    // whether user is refreshing data
    private boolean mIsRefreshing;
    // to receive result from service
    private SimpleResultReceiver mReceiver;

    public GalleryPresenterImpl(Context context,GalleryView view){
        mView = view;
        mContext = context;
        mImgList = new ArrayList<>();
        mIsRefreshing = false;

        mReceiver = new SimpleResultReceiver(new Handler(Looper.myLooper()));
        mReceiver.setReceiver(this);
    }

    @Override
    public List<ImageRealm> loadAllData() {
        // blocking call
        return RealmHelper.getInstance().queryAll();
    }

    @Override
    public void loadAllDataAsync() {
        Log.v(TAG,"loadAllDataAsync()");
        RealmHelper.getInstance().queryAllAsync();
    }

    @Override
    public void refresh() {
        Log.v(TAG,"refresh()");
        mIsRefreshing = true;
        Intent intent = new Intent(mContext,ImageRetrieveService.class);
        intent.putExtra("receiver",mReceiver);
        intent.putExtra(ImageRetrieveService.EXTRA_MAX_IMAGES,15);
        mContext.startService(intent);
    }

    @Override
    public void init() {
        //RealmHelper.getInstance().onStart();
        RealmHelper.getInstance().addListener(this);
    }

    @Override
    public void onDestroy() {
        RealmHelper.getInstance().removeListener(this);
        mImgList = null;
        //mView = null;
        RealmHelper.getInstance().onDestroy();
        mReceiver.setReceiver(null);
    }

    @Override
    public void onRealmDataChange(RealmResults<ImageRealm> data) {
        Log.v(TAG,"onRealmDataChange(): data size = " + data.size());
        for(ImageRealm info : data){
            if(!mImgList.contains(info)) {
                mImgList.add(info);
            }
        }

        if(mIsRefreshing){
            mView.onRefreshDone(true);
            mIsRefreshing = false;
        }

        Collections.sort(mImgList);
        mView.notifyDataChanged(mImgList);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle data) {
        switch (resultCode){
            case ServiceStatus.RUNNING:
                Log.v(TAG,"image retrieve starting");
                break;
            case ServiceStatus.ERROR:
                mView.onRefreshDone(false);
                break;
            case ServiceStatus.SUCCESS:
                Log.v(TAG,"image retrieve success");
                mView.onRefreshDone(true);
                // save to realm
                break;
        }
    }
}
