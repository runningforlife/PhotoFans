package com.github.runningforlife.photosniffer.ui.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.presenter.ImageDetailPresenter;
import com.github.runningforlife.photosniffer.presenter.ImageDetailPresenterImpl;
import com.github.runningforlife.photosniffer.ui.ImageDetailView;
import com.github.runningforlife.photosniffer.ui.adapter.BaseAdapterCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmObject;

import com.github.runningforlife.photosniffer.ui.adapter.ImagePagerAdapter;
import com.github.runningforlife.photosniffer.ui.adapter.PageAdapterCallback;
import com.github.runningforlife.photosniffer.ui.adapter.PreviewAdapter;
import com.github.runningforlife.photosniffer.ui.fragment.ActionListDialogFragment;
import com.github.runningforlife.photosniffer.model.UserAction;
import com.github.runningforlife.photosniffer.utils.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import static com.github.runningforlife.photosniffer.model.UserAction.*;

/**
 * activity to show the detail of the images
 *
 * FIXME:
 * java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid item position 0(offset:98).state:98
 */

public class ImageDetailActivity extends AppCompatActivity implements ImageDetailView,
        PageAdapterCallback,ActionListDialogFragment.ActionCallback {
    private static final String TAG = "ImageDetailActivity";

    @BindView(R.id.rv_images) RecyclerView mLvImgPreview;
    @BindView(R.id.vp_image) ViewPager mImgPager;
    @BindView(R.id.cpv_load) CircularProgressView mCpvLoad;

    private ImagePagerAdapter mPagerAdapter;
    private ImageDetailPresenter mPresenter;
    private PreviewAdapter mAdapter;
    private int mCurrentImgIdx;
    private ActionBar mActionBar;
    private Handler mMainHandler;

    /**
     * user action to do operations allowed at the images
     */
    private List<String> mUserActionList;
    private static UserAction ACTION_SHARE = SHARE;
    private static UserAction ACTION_SAVE = SAVE;
    private static UserAction ACTION_WALLPAPER = WALLPAPER;
    private static UserAction ACTION_FAVOR = FAVOR;
    private static UserAction ACTION_DELETE = DELETE;

    private static final int PREVIEW_HIDE_COUNT_DOWN = 5000; // 5s

    @Override
    public void onCreate(Bundle savedSate){
        super.onCreate(savedSate);

        setContentView(R.layout.activity_image_detail);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mActionBar = getSupportActionBar();
        if(mActionBar != null){
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeAsUpIndicator(R.drawable.ic_back_white_24dp);
        }

        initView();

        initActionList();

        mMainHandler = new EventHandler(Looper.getMainLooper());
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(TAG,"onResume()");
        initPresenter();
        mPresenter.onStart();

        setTitle();
    }

    @Override
    public void onPause(){
        super.onPause();

        GlideLoader.pauseRequest(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v(TAG,"onDestroy()");
        //mMainHandler = null;
        mPresenter.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail,menu);

        return true;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();

        if(mCpvLoad.isActivated()){
            mCpvLoad.stopAnimation();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();
        if(id == android.R.id.home){
            if(Build.VERSION.SDK_INT >= 21){
                finishAfterTransition();
            }else {
                finish();
            }
/*            Intent intent = new Intent(this, GalleryActivity.class);
            NavUtils.navigateUpTo(this,intent);*/
            return true;
        }else if(id == R.id.action_more){
            showActionDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataSetRangeChange(int start, int count) {
        Log.v(TAG,"onDataSetRangeChange()");
        if(mImgPager.getAdapter() == null) {
            mImgPager.setAdapter(mPagerAdapter);
            mLvImgPreview.setAdapter(mAdapter);
        }
        if(start == 0 && count > 0){
            mPagerAdapter.notifyDataSetChanged();
            mAdapter.notifyItemRangeInserted(0, count);
        // item deleted
        }else if(start >= 0 && count < 0){
            mPagerAdapter.notifyDataSetChanged();
            mAdapter.notifyItemRangeRemoved(start, (-1)*count);
        }else{
            mPagerAdapter.notifyDataSetChanged();
            mAdapter.notifyDataSetChanged();
        }
        // current index is changed
        mImgPager.setCurrentItem(mCurrentImgIdx);
        mLvImgPreview.smoothScrollToPosition(mCurrentImgIdx);
    }

    @Override
    public void onWallpaperSetDone(boolean isOk) {
        if(isOk){
            ToastUtil.showToast(this,
                    getString(R.string.set_wallpaper_success));
        }else{
            ToastUtil.showToast(this,
                    getString(R.string.set_wallpaper_fail));
        }
    }

    @Override
    public int getCount() {
        return mPresenter.getItemCount();
    }

    @Override
    public RealmObject getItemAtPos(int pos) {
        return mPresenter.getItemAtPos(pos);
    }

    @Override
    public void onItemClicked(View view, int pos, String adapter) {
        Log.v(TAG,"onItemClicked(): pos " + pos);
        mCurrentImgIdx = pos;
        // loading image
        mImgPager.setCurrentItem(pos);
        mLvImgPreview.scrollToPosition(pos);

        if(ImagePagerAdapter.TAG.equals(adapter)){
            int visible = mLvImgPreview.getVisibility();
            if(visible != View.VISIBLE){
                mLvImgPreview.setVisibility(View.VISIBLE);
                //after given time, preview should be hidden
                Message msg = mMainHandler.obtainMessage(EventHandler.EVENT_HIDE_PREVIEW);
                mMainHandler.sendMessageDelayed(msg,PREVIEW_HIDE_COUNT_DOWN);
            }
        }

        getPreviewScrollParams();
        // change title
        setTitle();
    }

    @Override
    public void onItemLongClicked(int pos, String adapter) {
        Log.v(TAG,"onItemLongClicked()");
        showActionDialog();
    }

    @Override
    public void onImageLoadStart(int pos) {
        Log.v(TAG,"onImageLoadStart()");

        mCpvLoad.setVisibility(View.VISIBLE);
        mCpvLoad.startAnimation();
    }

    @Override
    public void onImageLoadDone(int pos, boolean isSuccess) {
        Log.v(TAG,"onImageLoadDone()");

        mCpvLoad.stopAnimation();
        mCpvLoad.setVisibility(View.INVISIBLE);
    }

    @Override
    public void removeItemAtPos(final int pos) {
        mPresenter.removeItemAtPos(pos);

        mCurrentImgIdx = pos;

        final View obj = mImgPager.getChildAt(pos);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(obj, "alpha", 1f, 0f);
        alpha.setDuration(500);
        alpha.start();

        mImgPager.removeViewAt(pos);
        mPagerAdapter.destroyItem(mImgPager, pos,obj);
        mPagerAdapter.notifyDataSetChanged();

        mImgPager.setCurrentItem(pos + 1);
        mLvImgPreview.smoothScrollToPosition(pos + 1);

        //mImgPager.removeView(obj);
    }

    @Override
    public void onActionClick(String action, int pos) {
        Log.v(TAG,"onActionClick(): action = " + action);

        mCurrentImgIdx = mImgPager.getCurrentItem();

        if(action.equals(ACTION_SAVE.action())){
            // save image
            saveImage(mCurrentImgIdx);
        }else if(action.equals(ACTION_DELETE.action())){
            // remove image
           removeItemAtPos(mCurrentImgIdx);
            // refresh data at once
            //mPresenter.onStart();
        }else if(action.equals(ACTION_FAVOR.action())){
            // favor this image
            mPresenter.favorImageAtPos(mCurrentImgIdx);
        }else if(action.equals(ACTION_SHARE.action())){

        }else if(action.equals(ACTION_WALLPAPER.action())){
            setWallpaper(mCurrentImgIdx);
        }

        // hide fragment
        FragmentManager ft = getSupportFragmentManager();

        ft.beginTransaction()
          .detach(ft.findFragmentByTag("ActionList"))
          .commit();
    }

    private void initView(){
        Log.v(TAG,"initView()");
        mAdapter = new PreviewAdapter(getApplicationContext(),ImageDetailActivity.this);
        mPagerAdapter = new ImagePagerAdapter(getApplicationContext(),this);

        mImgPager.addOnPageChangeListener(new ImagePageStateListener());
        LinearLayoutManager llMgr = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        mLvImgPreview.setVisibility(View.INVISIBLE);
        mLvImgPreview.setLayoutManager(llMgr);
        mLvImgPreview.setItemAnimator(new DefaultItemAnimator());

        Intent data = getIntent();
        mCurrentImgIdx = data.getIntExtra("image",0);
        Log.v(TAG,"initView(): current image index = " + mCurrentImgIdx);
        mImgPager.setCurrentItem(mCurrentImgIdx);

        mLvImgPreview.scrollToPosition(mCurrentImgIdx);
        mLvImgPreview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                Log.d(TAG,"onScrollStateChanged(): new state " + newState);
                if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
                    // dragging state, no hide
                    if(mMainHandler != null && mMainHandler.hasMessages(EventHandler.EVENT_HIDE_PREVIEW)){
                        mMainHandler.removeMessages(EventHandler.EVENT_HIDE_PREVIEW);
                    }
                }else if(newState == RecyclerView.SCROLL_STATE_IDLE){
                    if (mMainHandler != null &&
                            !mMainHandler.hasMessages(EventHandler.EVENT_HIDE_PREVIEW)) {
                        Message msg = mMainHandler.obtainMessage(EventHandler.EVENT_HIDE_PREVIEW);
                        mMainHandler.sendMessageDelayed(msg,PREVIEW_HIDE_COUNT_DOWN);
                    }
                }
            }
        });

        getPreviewScrollParams();
    }

    private void initPresenter(){
        mPresenter = new ImageDetailPresenterImpl(getApplicationContext(),ImageDetailActivity.this);
        // may have null pointer exception
        mPresenter.init();
        // set adapter to bind data
        mImgPager.setAdapter(mPagerAdapter);
        mLvImgPreview.setAdapter(mAdapter);
    }

    private void initActionList(){
        mUserActionList = new ArrayList<>();
        String share = getString(R.string.action_share);
        String save = getString(R.string.action_save);
        String wallpaper = getString(R.string.action_wallpaper);
        String delete = getString(R.string.action_delete);
        String favor = getString(R.string.action_favorite);

        //mUserActionList.add(share);
        mUserActionList.add(save);
        mUserActionList.add(favor);
        mUserActionList.add(wallpaper);
        mUserActionList.add(delete);

        ACTION_SHARE.setAction(share);
        ACTION_DELETE.setAction(delete);
        ACTION_FAVOR.setAction(favor);
        ACTION_SAVE.setAction(save);
        ACTION_WALLPAPER.setAction(wallpaper);
    }

    private void setTitle(){
        String title = (mCurrentImgIdx + 1) + "/" + mPresenter.getItemCount();
        mActionBar.setWindowTitle(title);
    }

    @Override
    public void onImageSaveDone(String path) {
        Log.v(TAG,"onImageSaveDone(): isOk = " + !TextUtils.isEmpty(path));

        if(!TextUtils.isEmpty(path)) {
            Message msg = mMainHandler.obtainMessage(EventHandler.IMAGE_SAVE_COMPLETE,path);
            msg.sendToTarget();
        }else{
            Message msg = mMainHandler.obtainMessage(EventHandler.IMAGE_SAVE_FAIL);
            msg.sendToTarget();
        }
    }

    private final class ImagePageStateListener implements ViewPager.OnPageChangeListener{

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // do nothing
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onPageSelected(int position) {
            Log.v(TAG,"onPageSelected()");
            mCurrentImgIdx = position;
            setTitle();
            if(mLvImgPreview.isShown()) {
                mLvImgPreview.scrollToPosition(position);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // FIXME:java.lang.IllegalStateException: The application's PagerAdapter changed the adapter's contents
            //without calling PagerAdapter#notifyDataSetChanged!
            //Expected adapter item count: 101, found: 99
            if(state == ViewPager.SCROLL_STATE_IDLE){
                mPagerAdapter.notifyDataSetChanged();
            }
        }
    }

    private int getPreviewScrollParams(){
        int hOffset = mLvImgPreview.computeHorizontalScrollOffset();
        int hRange = mLvImgPreview.computeHorizontalScrollRange();
        int hExtent = mLvImgPreview.computeHorizontalScrollExtent();

        Log.v(TAG,"getPreviewScrollParams(): horizontal offset = " + hOffset + ", range = " + hRange + ", extent = " + hExtent);

        return mLvImgPreview.computeHorizontalScrollRange();
    }

    private void showActionDialog(){
        ActionListDialogFragment fragment = (ActionListDialogFragment)
                ActionListDialogFragment.newInstance(mUserActionList);
        fragment.setCallback(this);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
          .add(fragment,"ActionList")
          .commit();
    }

    private void hidePreview(){
        mLvImgPreview.setVisibility(View.INVISIBLE);
    }

    private void saveImage(int pos){
        Log.v(TAG,"saveImage()");
        mPresenter.saveImageAtPos(pos);
    }

    private void setWallpaper(int pos){
        Log.v(TAG,"setWallpaper()");
        mPresenter.setWallpaper(pos);
    }

    private class EventHandler extends Handler{

        static final int IMAGE_SAVE_COMPLETE = 1;
        static final int IMAGE_SAVE_FAIL = 2;
        static final int IMAGE_FAVOR_COMPLETE = 3;
        static final int IMAGE_DELETE_COMPLETE = 4;
        static final int IMAGE_SHARE_COMPLETE = 5;
        static final int EVENT_HIDE_PREVIEW = 6;

        EventHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg){
            int what = msg.what;

            switch (what){
                case IMAGE_SAVE_COMPLETE:
                    String toast = getString(R.string.save_image_Success) + msg.obj;
                    ToastUtil.showToast(ImageDetailActivity.this,toast);
                    break;
                case IMAGE_SAVE_FAIL:
                    String toast1 = getString(R.string.save_image_fail);
                    ToastUtil.showToast(ImageDetailActivity.this,toast1);
                case EVENT_HIDE_PREVIEW:
                    hidePreview();
                    break;
                default:
                    break;
            }
        }
    }
}
