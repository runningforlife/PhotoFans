package com.github.runningforlife.photosniffer.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.presenter.AllPicturesPresenterImpl;
import com.github.runningforlife.photosniffer.presenter.RealmOp;
import com.github.runningforlife.photosniffer.ui.AllPictureView;
import com.github.runningforlife.photosniffer.ui.adapter.GalleryAdapter;
import com.github.runningforlife.photosniffer.utils.SharedPrefUtil;

import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.runningforlife.photosniffer.presenter.ImageType.IMAGE_GALLERY;

/**
 * a fragment to display all pictures
 */

public class AllPicturesFragment extends BaseFragment implements AllPictureView {
    public static final String TAG = "AllPicturesFragment";

    @BindView(R.id.rcv_gallery) RecyclerView mRvImgList;
    @BindView(R.id.srl_refresh) SwipeRefreshLayout mRefresher;
    private AllPicturesPresenterImpl mPresenter;

    public static AllPicturesFragment newInstance(int type){
        Bundle args = new Bundle();
        args.putInt(ARGS_IMAGE_TYPE, type);
        AllPicturesFragment fragment = new AllPicturesFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedState){
        Log.v(TAG,"onCreateView()");
        View root = inflater.inflate(R.layout.fragment_photos_gallery,parent,false);
        ButterKnife.bind(this,root);

        initView();
        // need to call it here for onStart is too late
        initPresenter();

        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.v(TAG,"onAttach()");
        //initView(context);
        if(!(context instanceof FragmentCallback)){
            throw new IllegalStateException("refresh callback must be implemented");
        }

        mCallback = (FragmentCallback)context;
        // show tool bar
        mCallback.onFragmentAttached();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG,"onResume()");
        //setTitle(getString(R.string.app_name));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRvImgList != null) {
            mRvImgList.setAdapter(null);
        }
    }

    @Override
    public boolean isRefreshing(){
        return mRefresher.isRefreshing();
    }

    @Override
    public void setRefreshing(boolean enable){
        mRefresher.setRefreshing(enable);
    }

    @Override
    public void onRefreshDone(boolean isSuccess) {
        Log.v(TAG,"onRefreshDone()");

        if (mRefresher.isRefreshing()) {
            mRefresher.setRefreshing(false);
        }

        if (isAdded()) {
            if (isSuccess) {
                mCallback.showToast(getString(R.string.refresh_success));
            } else {
                mCallback.showToast(getString(R.string.refresh_error));
            }
        }
    }

    @Override
    public void onNetworkDisconnect() {
        Log.v(TAG,"onNetworkDisconnect()");
        if(mRefresher.isRefreshing()){
            mRefresher.setRefreshing(false);
        }
        if (isAdded() && mCallback != null) {
            mCallback.showToast(getString(R.string.network_not_connected));
        }
    }

    @Override
    public void onMobileConnected() {
        Log.d(TAG,"onMobileConnected()");
        if(mRefresher.isRefreshing()){
            mRefresher.setRefreshing(false);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle(R.string.network_mobile_connected)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // nothing to do
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mRefresher.setRefreshing(true);
                        mPresenter.refreshAnyway();
                    }
                }).show();
    }

    @Override
    public void onImageSourceEmpty() {
        if (isAdded() && mCallback != null) {
            mCallback.showToast(getString(R.string.default_image_source_empty));
        }
    }

    @Override
    public void onDataSetChange(int start, int len, RealmOp op) {
        Log.v(TAG,"onDataSetChange(): op = " + op);
        if (op == RealmOp.OP_INSERT) {
            mAdapter.notifyItemRangeInserted(start,  len);
            mRvImgList.scrollToPosition(0);
        } else if (op == RealmOp.OP_DELETE) {
            mAdapter.notifyItemRangeRemoved(start, len);
        } else if (op == RealmOp.OP_MODIFY) {
            mAdapter.notifyItemRangeChanged(start, len);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onContextMenuCreated(int pos, String adapter) {
        Log.v(TAG,"onContextMenuCreated()");

        mCurrentPos = pos;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.v(TAG,"onContextItemSelected()");
        switch (item.getItemId()) {
            case R.id.menu_save:
                mPresenter.saveImageAtPos(mCurrentPos);
                break;
            case R.id.menu_favor:
                mPresenter.favorImageAtPos(mCurrentPos);
                break;
            case R.id.menu_delete:
                mPresenter.removeItemAtPos(mCurrentPos);
                break;
            case R.id.menu_wallpaper:
                mPresenter.setWallpaperAtPos(mCurrentPos);
                break;
            default:
                return super.onContextItemSelected(item);
        }

        return true;
    }

    @Override
    public void onItemClicked(View view, int pos,String adapter) {
        Log.v(TAG,"onItemClicked(): pos = " + pos);
        if (mRefresher.isRefreshing()) {
            mRefresher.setRefreshing(false);
        }
        if (isAdded() && mCallback != null) {
            mCallback.onItemClick(view, pos, IMAGE_GALLERY);
            if (mMenu != null) {
                mMenu.setGroupVisible(R.id.group_usage, true);
                mMenu.setGroupVisible(R.id.group_batch_edit, false);
            }
            SharedPrefUtil.putArrayList(ARG_BATCH_SELECTED_IMAGES, new HashSet<>(mAdapter.getSelectedImages()));
            SharedPrefUtil.putBoolean(ARG_BATCH_EDIT_MODE, mIsBatchEditMode);
        }
    }

    @Override
    public void onImageSaveDone(String path) {
        Log.v(TAG,"onImageSaveDone(): isOk = " + !TextUtils.isEmpty(path));
        if (isAdded()) {
            if (!TextUtils.isEmpty(path)) {
                mCallback.showToast(getString(R.string.save_image_Success) + path);
            } else {
                mCallback.showToast(getString(R.string.save_image_fail));
            }
        }
    }

    @Override
    public void onWallpaperSetDone(boolean isOk) {
        if (isAdded()) {
            if (isOk) {
                mCallback.showToast(getString(R.string.set_wallpaper_success));
            } else {
                mCallback.showToast(getString(R.string.set_wallpaper_fail));
            }
        }
    }

    @Override
    public RecyclerView getRecycleView() {
        return mRvImgList;
    }


    private void initView() {
        Log.v(TAG,"initView()");

        mImageType = getArguments().getInt(ARGS_IMAGE_TYPE);

        //LinearLayoutManager llMgr = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        GridLayoutManager gridLayoutMgr = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        gridLayoutMgr.setSmoothScrollbarEnabled(true);
        mRvImgList.setHasFixedSize(true);
        mRvImgList.setLayoutManager(gridLayoutMgr);
        mRvImgList.setItemAnimator(new DefaultItemAnimator());
        mRvImgList.setBackgroundResource(R.color.colorLightGrey);

        mAdapter = new GalleryAdapter(getActivity(),this);
        mAdapter.setContextMenuRes(R.menu.menu_context_gallery);

        mRvImgList.setAdapter(mAdapter);
        //mAdapter.setHasStableIds(true);
        mRefresher.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_orange_dark);
        mRefresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.v(TAG,"onRefresh()");
                mPresenter.refresh();
            }
        });
    }

    private void initPresenter() {
        mPresenter = new AllPicturesPresenterImpl( Glide.with(this), getContext(),this);
        setPresenter(mPresenter);
        mPresenter.onStart();
    }
}
