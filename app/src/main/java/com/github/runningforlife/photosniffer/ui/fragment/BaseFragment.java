package com.github.runningforlife.photosniffer.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.presenter.NetState;
import com.github.runningforlife.photosniffer.presenter.Presenter;
import com.github.runningforlife.photosniffer.presenter.RealmOp;
import com.github.runningforlife.photosniffer.ui.UI;
import com.github.runningforlife.photosniffer.ui.activity.Refresh;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapterCallback;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import io.realm.RealmObject;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * a abstract fragment class implemented by child
 */

public abstract class BaseFragment extends Fragment implements Refresh, UI, GalleryAdapterCallback {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({LinearManager,GridManager})
    public @interface RecycleLayout{}
    public static final String LinearManager = "linearManager";
    public static final String GridManager = "GridManager";
    // current context menu item view position
    protected int mCurrentPos = -1;
    protected FragmentCallback mCallback;
    private Presenter mPresenter;

    public interface FragmentCallback {
        void onItemClick(View view, int pos, String url);
        void onFragmentAttached();
        void showToast(String toast);
    }

    @Override
    public void onCreate(Bundle savedState){
        super.onCreate(savedState);
        //setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.v(TAG,"onAttach()");
        try {
            mCallback = (FragmentCallback)context;
        }catch (ClassCastException e){
            Log.e(TAG,"parent activity must implement FragmentCallback");
            throw new IllegalStateException("refresh callback must be implemented");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG,"onResume()");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mPresenter.onDestroy();
    }

    @Override
    public boolean isRefreshing() {
        return false;
    }

    @Override
    public void setRefreshing(boolean enable) {
        // empty
    }

    protected void setPresenter(@NonNull Presenter presenter) {
        mPresenter = presenter;
    }

    protected void setTitle(String title){
        Activity activity = getActivity();
        if(activity != null){
            activity.setTitle(title);
        }
    }

    @Override
    public void onNetworkState(@NetState String state) {
        Log.v(TAG,"onNetworkState():state = " + state);
        if (mCallback != null) {
            switch (state) {
                case NetState.STATE_DISCONNECT:
                    mCallback.showToast(getString(R.string.network_not_connected));
                    break;
                case NetState.STATE_HUNG:
                    mCallback.showToast(getString(R.string.hint_network_state_hung));
                    break;
                case NetState.STATE_SLOW:
                    mCallback.showToast(getString(R.string.hint_network_state_slow));
                    break;
                default:
                    break;
            }
        }
    }


    @Override
    public void onImageSaveDone(String path) {
        Log.v(TAG,"onImageSaveDone(): isOk = " + !TextUtils.isEmpty(path));
        if(!TextUtils.isEmpty(path)) {
            mCallback.showToast(getString(R.string.save_image_Success) + path);
        }else{
            mCallback.showToast(getString(R.string.save_image_fail));
        }
    }

    @Override
    public void onWallpaperSetDone(boolean isOk) {
        Log.v(TAG,"onWallpaperSetDone()");
        if(isOk){
            mCallback.showToast(getString(R.string.set_wallpaper_success));
        }else{
            mCallback.showToast(getString(R.string.set_wallpaper_fail));
        }
    }

    @Override
    public int getCount() {
        checkPresenter();
        return mPresenter.getItemCount();
    }

    @Override
    public RealmObject getItemAtPos(int pos) {
        checkPresenter();
        return mPresenter.getItemAtPos(pos);
    }

    @Override
    public void removeItemAtPos(int pos) {
        checkPresenter();
        mPresenter.removeItemAtPos(pos);
    }

    @Override
    public void loadImageIntoView(int pos, ImageView iv, Priority priority, int w, int h, ImageView.ScaleType scaleType) {
        checkPresenter();
        mPresenter.loadImageIntoView(pos, iv, priority, w, h, scaleType);
    }

    private void checkPresenter() {
        if (mPresenter == null) {
            throw new IllegalArgumentException("presenter should not be null");
        }
    }
}
