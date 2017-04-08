package jason.github.com.photofans.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import jason.github.com.photofans.R;
import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.ui.GalleryPresenter;
import jason.github.com.photofans.ui.GalleryPresenterImpl;
import jason.github.com.photofans.ui.GalleryView;
import jason.github.com.photofans.ui.activity.ImageDetailActivity;
import jason.github.com.photofans.ui.adapter.GalleryAdapter;

/**
 * a fragment to display all pictures
 */

public class AllPicturesFragment extends BaseFragment implements GalleryView,
            GalleryAdapter.ItemSelectedCallback{
    public static final String TAG = "AllPicturesFragment";

    @BindView(R.id.rcv_gallery) RecyclerView mRvImgList;
    @BindView(R.id.srl_refresh) SwipeRefreshLayout mRefresher;
    private GalleryPresenter mPresenter;
    private GalleryAdapter mAdapter;
    private RefreshCallback mCallback;

    public interface RefreshCallback {
        void onRefreshDone(boolean success);
    }

    public static Fragment newInstance(){
        return new AllPicturesFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedState){
        View root = inflater.inflate(R.layout.fragment_photos_gallery,parent,false);
        ButterKnife.bind(this,root);

        initView(getContext());

        return root;
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        Log.v(TAG,"onAttach()");
        //initView(context);
        if(!(context instanceof RefreshCallback)){
            throw new IllegalStateException("refresh callback must be implemented");
        }

        mCallback = (RefreshCallback)context;
    }

    @Override
    public void onResume(){
        super.onResume();
        mPresenter.loadAllDataAsync();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        mPresenter.onDestroy();
    }

    @Override
    public boolean isRefreshing(){
        return mRefresher.isRefreshing();
    }

    @Override
    public void setRefreshing(boolean enable){
        mRefresher.setRefreshing(enable);
    }

    private void initView(Context context){
        Log.v(TAG,"initView()");
        mPresenter = new GalleryPresenterImpl(context,this);
        mPresenter.init();

        GridLayoutManager gridLayoutMgr = new GridLayoutManager(context,2);
        mRvImgList.setLayoutManager(gridLayoutMgr);

        mAdapter = new GalleryAdapter(context,this);
        mRvImgList.setAdapter(mAdapter);

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

    @Override
    public void notifyDataChanged() {
        Log.v(TAG,"notifyDataChanged()");
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRefreshDone(boolean isSuccess) {
        Log.v(TAG,"onRefreshDone()");

        if(mRefresher.isRefreshing()){
            mRefresher.setRefreshing(false);
        }

        mCallback.onRefreshDone(isSuccess);
    }

    @Override
    public void onItemClick(int pos) {
        Log.v(TAG,"onItemClick(): pos = " + pos);

        Intent intent = new Intent(getContext(),ImageDetailActivity.class);
        intent.putExtra("image",pos);
        startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return mPresenter.getItemCount();
    }

    @Override
    public ImageRealm getItemAtPos(int pos) {
        return mPresenter.getItemAtPos(pos);
    }

    @Override
    public void removeItemAtPos(int pos) {
        mPresenter.removeItemAtPos(pos);
    }
}
