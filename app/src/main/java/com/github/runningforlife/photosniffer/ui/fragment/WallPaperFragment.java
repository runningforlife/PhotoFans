package com.github.runningforlife.photosniffer.ui.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

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
        implements ImageAdapterCallback, WallpaperView{
    public static final String TAG = "WallpaperFragment";
    public static final String ALARM_AUTO_WALLPAPER = "com.github.runningforlife.AUTO_WALLPAPER";

    @BindView(R.id.refresh)
    SwipeRefreshLayout mSrlRefresh;
    @BindView(R.id.rcv_img_list)
    RecyclerView mRcvWallpaper;

    private GalleryAdapter mAdapter;
    private WallpaperPresenter mPresenter;
    private ItemClickListener mListener;

    public static WallPaperFragment newInstance(){
        return new WallPaperFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedState){

        View root = inflater.inflate(R.layout.fragment_favor_image, parent, false);

        ButterKnife.bind(this, root);

        initView();

        initPresenter();

        //registerActionMenu();

        return root;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);

        Log.v(TAG,"onAttach()");
        try {
            mListener = (ItemClickListener)context;
            //mListener.onFragmentAttached();
        }catch (ClassCastException e){
            Log.e(TAG,"parent activity must implement ItemClickListener");
            mListener = null;
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        if(mListener != null){
            mListener.onFragmentAttached();
        }

        mPresenter.onStart();

        setTitle();

        registerActionMenu(mRcvWallpaper);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo info){
        super.onCreateContextMenu(menu, v, info);
        Log.v(TAG,"onCreateContextMenu()");
        MenuInflater inflater = getActivity().getMenuInflater();

        inflater.inflate(R.menu.menu_context_choice, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        Log.v(TAG,"onContextItemSelected()");
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();

        switch (item.getItemId()){
            case R.id.menu_save:
                break;
            case R.id.menu_wallpaper:
                break;
            case R.id.menu_delete:
                break;
            default:
                break;
        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        mPresenter.onDestroy();
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
        if(mListener != null){
            mListener.onItemClick(pos, mPresenter.getItemAtPos(pos).getUrl());
        }
    }

    @Override
    public void onItemLongClicked(int pos, String adapter) {
        Log.v(TAG,"onItemLongClicked(): pos " + pos);

        View view = mRcvWallpaper.getChildAt(pos);
        if(view != null){
            //registerActionMenu(view);
        }
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

    private void registerActionMenu(View view){
        Log.v(TAG,"registerActionMenu()");
        registerForContextMenu(view);
    }
}
