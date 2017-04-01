package jason.github.com.photofans.ui;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmResults;
import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.model.RealmHelper;
import jason.github.com.photofans.service.ImageRetrieveService;
import jason.github.com.photofans.service.ServiceStatus;
import jason.github.com.photofans.service.SimpleResultReceiver;

/**
 * a presenter to bridge UI and data repository
 */

//FIXME: images are duplicated in recycle view
public class GalleryPresenterImpl implements GalleryPresenter,SimpleResultReceiver.Receiver{
    private static final String TAG = "GalleryPresenter";

    private static final int DEFAULT_RETRIEVED_IMAGES = 10;

    private Context mContext;
    private GalleryView mView;
    private Set<ImageRealm> mUnUsedImages;
    private LinkedList<ImageRealm> mImageList;
    // whether user is refreshing data
    private boolean mIsRefreshing;
    // to receive result from service
    private SimpleResultReceiver mReceiver;

    @SuppressWarnings("unchecked")
    public GalleryPresenterImpl(Context context,GalleryView view){
        mView = view;
        mContext = context;
        mUnUsedImages = new HashSet<>();
        mImageList = new LinkedList<>();
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

        if(mUnUsedImages.size() <= DEFAULT_RETRIEVED_IMAGES) {

            mIsRefreshing = true;
            Intent intent = new Intent(mContext, ImageRetrieveService.class);
            intent.putExtra("receiver", mReceiver);
            intent.putExtra(ImageRetrieveService.EXTRA_MAX_IMAGES, DEFAULT_RETRIEVED_IMAGES);
            mContext.startService(intent);
        }else{
            // add to the list
            List<ImageRealm> removed = new ArrayList<>();
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            int cn = 0;
            Iterator iter = mUnUsedImages.iterator();
            while(iter.hasNext() && ++cn <= DEFAULT_RETRIEVED_IMAGES){
                ImageRealm item = (ImageRealm)iter.next();
                item.setUsed(true);
                mImageList.add(item);
                removed.add(item);
            }
            realm.commitTransaction();
            // notify UI to refresh
            mView.onRefreshDone(true);
            // remove from set
            for(ImageRealm item : removed){
                mUnUsedImages.remove(item);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mImageList.size();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        return mImageList.get(pos);
    }

    @Override
    public void removeItemAtPos(int pos) {
        Log.v(TAG,"removeItemAtPos(): position = " + pos);
        RealmHelper.getInstance().delete(mImageList.get(pos));
        mImageList.remove(pos);
    }

    @Override
    public void init() {
        RealmHelper.getInstance().onStart();
        RealmHelper.getInstance().addListener(this);
    }

    @Override
    public void onDestroy() {
        RealmHelper.getInstance().removeListener(this);
        mUnUsedImages = null;
        mImageList = null;
        //mView = null;
        RealmHelper.getInstance().onDestroy();
        mReceiver.setReceiver(null);
    }

    @Override
    public void onRealmDataChange(RealmResults<ImageRealm> data) {
        Log.v(TAG,"onRealmDataChange(): data size = " + data.size());

        if(data.get(0).getUsed()) {
            for (ImageRealm info : data) {
                if (!mImageList.contains(info)) {
                    mImageList.addFirst(info);
                }
            }

            if (mIsRefreshing) {
                mView.onRefreshDone(true);
                mIsRefreshing = false;
            }

            //Collections.sort(mImgList);
            mView.notifyDataChanged();
        }else{
            Log.v(TAG,"onRealmDataChange(): unused url size = " + data.size());
            for(ImageRealm img : data){
                mUnUsedImages.add(img);
            }
        }
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
