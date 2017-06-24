package com.github.runningforlife.photosniffer.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.model.ImageRealm;
import static com.github.runningforlife.photosniffer.model.UserAction.*;

import com.github.runningforlife.photosniffer.model.UserAction;
import com.github.runningforlife.photosniffer.presenter.WallpaperPresenter;
import com.github.runningforlife.photosniffer.presenter.WallpaperPresenterImpl;
import com.github.runningforlife.photosniffer.ui.WallpaperView;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapter;
import com.github.runningforlife.photosniffer.ui.adapter.ImageAdapterCallback;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * fragment to manager wallpaper
 */

public class WallPaperFragment extends BaseFragment
        implements ImageAdapterCallback, WallpaperView,
        FullScreenImageFragment.ItemLongClickedListener,
        ActionListDialogFragment.ActionCallback{
    public static final String TAG = "WallpaperFragment";
    public static final String ALARM_AUTO_WALLPAPER = "com.github.runningforlife.AUTO_WALLPAPER";

    @BindView(R.id.srl_refresh)
    SwipeRefreshLayout mSrlRefresh;
    @BindView(R.id.rcv_gallery)
    RecyclerView mRcvWallpaper;

    private GalleryAdapter mAdapter;
    private WallpaperPresenter mPresenter;

    private List<String> mUserActionList;
    private int mCurrentPos;
    private static UserAction ACTION_SAVE = SAVE;
    private static UserAction ACTION_FAVOR = FAVOR;
    private static UserAction ACTION_WALLPAPER = WALLPAPER;
    private static UserAction ACTION_DELETE = DELETE;

    public static WallPaperFragment newInstance(){
        return new WallPaperFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedState){

        View root = inflater.inflate(R.layout.fragment_photos_gallery, parent, false);

        ButterKnife.bind(this, root);

        initView();

        initPresenter();

        initActionList();

        return root;
    }

    @Override
    public void onResume(){
        super.onResume();

        mPresenter.onStart();

        setTitle();
    }

    @Override
    public int getCount() {
        return mPresenter.getItemCount();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        return mPresenter.getItemAtPos(pos);
    }

    @Override
    public void onItemClicked(int pos, String adapter) {
        Log.v(TAG,"onItemClicked(): pos = " + pos);
        mCurrentPos = pos;
        //showFullScreenImage(pos);
    }

    @Override
    public void onItemLongClicked(int pos, String adapter) {
        Log.v(TAG,"onItemLongClicked(): pos " + pos);
        mCurrentPos = pos;
        showActionListFragment();
    }

    @Override
    public void onImageLoadStart(int pos) {

    }

    @Override
    public void onImageLoadDone(int pos, boolean isSuccess) {

    }

    @Override
    public void removeItemAtPos(int pos) {
        mPresenter.removeItemAtPos(pos);
    }

    @Override
    public void onDataSetChanged() {
        Log.v(TAG,"onDataSetChanged()");
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefreshDone(boolean isSuccess) {
        if(mSrlRefresh.isRefreshing()){
            mSrlRefresh.setRefreshing(false);
        }
    }

    @Override
    public void onImageSaveDone(String path) {

    }

    private void initView(){
        StaggeredGridLayoutManager sgm = new StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.VERTICAL);
        mRcvWallpaper.setLayoutManager(sgm);

        mAdapter = new GalleryAdapter(getContext(), this);
        mRcvWallpaper.setAdapter(mAdapter);

        mSrlRefresh.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_orange_dark);
        mSrlRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.v(TAG,"onRefresh()");
                mPresenter.refresh();
            }
        });
    }

    private void initPresenter(){
        mPresenter = new WallpaperPresenterImpl(getContext());
        mPresenter.setView(this);
        mPresenter.init();
    }

    private void setTitle(){
        String title = getString(R.string.set_wallpaper);
        Activity activity = getActivity();
        if(activity != null) {
            activity.setTitle(title);
        }
    }


    private void showFullScreenImage(int pos){
        String url = mPresenter.getItemAtPos(pos).getUrl();
        FullScreenImageFragment fragment = FullScreenImageFragment.newInstance(url);

        FragmentManager fragmentMgr = getActivity().getSupportFragmentManager();
        fragmentMgr.beginTransaction()
                .replace(android.R.id.content, fragment, FullScreenImageFragment.TAG)
                .commit();
    }

    private void showActionListFragment(){
        FragmentManager fragmentMgr = getActivity().getSupportFragmentManager();

        ActionListDialogFragment fragment = (ActionListDialogFragment) ActionListDialogFragment.newInstance(mUserActionList);

        fragmentMgr.beginTransaction()
                .replace(R.id.fragment_container, fragment, AllPicturesFragment.TAG)
                .commit();
    }

    @Override
    public void onImageLongClicked(String url) {
        Log.v(TAG,"onImageLongClicked()");

        showActionListFragment();
    }

    private void initActionList(){
        mCurrentPos = -1;

        mUserActionList = new ArrayList<>();
        //String share = getString(R.string.action_share);
        String save = getString(R.string.action_save);
        String wallpaper = getString(R.string.action_wallpaper);
        String delete = getString(R.string.action_delete);
        String favor = getString(R.string.action_favorite);

        //mUserActionList.add(share);
        mUserActionList.add(save);
        mUserActionList.add(wallpaper);
        mUserActionList.add(delete);
        mUserActionList.add(favor);

        ACTION_DELETE.setAction(delete);
        ACTION_FAVOR.setAction(favor);
        ACTION_SAVE.setAction(save);
        ACTION_WALLPAPER.setAction(wallpaper);
    }

    @Override
    public void onActionClick(String action, int pos) {
        Log.v(TAG,"onActionClick(): action = " + action);

        if(action.equals(ACTION_SAVE.action())){
            // save image
            if(mCurrentPos != -1) {
                mPresenter.saveImageAtPos(mCurrentPos);
            }
        }else if(action.equals(ACTION_DELETE.action())){
            // remove image
            if(mCurrentPos != -1) {
                mPresenter.removeItemAtPos(mCurrentPos);
                // refresh data at once
                mPresenter.onStart();
            }
        }else if(action.equals(ACTION_FAVOR.action())){
            // favor this image
        }else if(action.equals(ACTION_WALLPAPER.action())){
            if(mCurrentPos != -1){
                mPresenter.setWallpaperAtPos(mCurrentPos);
            }
        }
    }
}
