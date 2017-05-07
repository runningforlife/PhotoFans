package com.github.runningforlife.photofans.ui.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.app.AppGlobals;
import com.github.runningforlife.photofans.model.ImageRealm;
import com.github.runningforlife.photofans.ui.ImageDetailPresenter;
import com.github.runningforlife.photofans.ui.ImageDetailPresenterImpl;
import com.github.runningforlife.photofans.ui.ImageDetailView;
import com.github.runningforlife.photofans.ui.adapter.ImageAdapterCallback;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.github.runningforlife.photofans.ui.adapter.ImagePagerAdapter;
import com.github.runningforlife.photofans.ui.adapter.PreviewAdapter;
import com.github.runningforlife.photofans.ui.fragment.ActionListDialogFragment;
import com.github.runningforlife.photofans.model.UserAction;
import com.github.runningforlife.photofans.utils.BitmapUtil;

import java.io.FileNotFoundException;
import java.util.Arrays;

import static com.github.runningforlife.photofans.model.UserAction.*;

/**
 * activity to show the detail of the images
 */

public class ImageDetailActivity extends AppCompatActivity implements ImageDetailView,
        ImageAdapterCallback,ActionListDialogFragment.ActionCallback {
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
    private static UserAction ACTION_SHARE = SHARE;
    private static UserAction ACTION_SAVE = SAVE;
    private static UserAction ACTION_FAVOR = FAVOR;
    private static UserAction ACTION_DELETE = DELETE;

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

        initPresenter();

        initView();

        initActionList();
        // keep it here to ensure data set is loaded
        mPresenter.onResume();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(TAG,"onResume()");

        mMainHandler = new EventHandler(Looper.myLooper());

        setTitle();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.v(TAG,"onDestroy()");
        mMainHandler = null;
        //FIXME: too late to call
        //mPresenter.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_detail,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();
        if(id == android.R.id.home){
            // call this to avoid close Realm when navigation is done
            mPresenter.onDestroy();
            Intent intent = new Intent(this, GalleryActivity.class);
            NavUtils.navigateUpTo(this,intent);
            return true;
        }else if(id == R.id.action_more){
            showActionDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDataSetChanged() {
        Log.v(TAG,"onDataSetChanged()");
        mPagerAdapter.notifyDataSetChanged();
        mAdapter.notifyDataSetChanged();

        mLvImgPreview.smoothScrollToPosition(mCurrentImgIdx);
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
    public void onItemClicked(int pos) {
        Log.v(TAG,"onItemClicked(): pos " + pos);
        mCurrentImgIdx = pos;
        // loading image
        mImgPager.setCurrentItem(pos);
        mLvImgPreview.scrollToPosition(pos);

        getPreviewScrollParams();
        // change title
        setTitle();
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
    public void onActionClick(String action, int pos) {
        Log.v(TAG,"onActionClick(): action = " + action);

        if(action.equals(ACTION_SAVE.action())){

        }else if(action.equals(ACTION_DELETE.action())){

        }else if(action.equals(ACTION_FAVOR.action())){

        }else if(action.equals(ACTION_SHARE.action())){

        }
    }

    private void initView(){
        Log.v(TAG,"initView()");
        mAdapter = new PreviewAdapter(getApplicationContext(),ImageDetailActivity.this);
        mPagerAdapter = new ImagePagerAdapter(getApplicationContext(),this);
        mImgPager.setAdapter(mPagerAdapter);
        mLvImgPreview.setAdapter(mAdapter);

        mImgPager.addOnPageChangeListener(new ImagePageStateListener());
        LinearLayoutManager llMgr = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        mLvImgPreview.setVisibility(View.VISIBLE);
        mLvImgPreview.setLayoutManager(llMgr);
        mLvImgPreview.setItemAnimator(new DefaultItemAnimator());

        Intent data = getIntent();
        mCurrentImgIdx = data.getIntExtra("image",0);
        Log.v(TAG,"initView(): current image index = " + mCurrentImgIdx);
        mImgPager.setCurrentItem(mCurrentImgIdx);
        //TODO: try to scroll to center
        mLvImgPreview.scrollToPosition(mCurrentImgIdx);

        getPreviewScrollParams();
    }

    private void initPresenter(){
        mPresenter = new ImageDetailPresenterImpl(getApplicationContext(),ImageDetailActivity.this);
        // may have null pointer exception
        mPresenter.init();
    }

    private void initActionList(){
        String share = getString(R.string.action_share);
        String save = getString(R.string.action_save);
        String delete = getString(R.string.action_delete);
        String favor = getString(R.string.action_favorite);

        ACTION_SHARE.setAction(share);
        ACTION_DELETE.setAction(delete);
        ACTION_FAVOR.setAction(favor);
        ACTION_SAVE.setAction(save);
    }

    private void setTitle(){
        String title = (mCurrentImgIdx + 1) + "/" + mPresenter.getItemCount();
        mActionBar.setWindowTitle(title);
    }

    private final class ImagePageStateListener implements ViewPager.OnPageChangeListener{

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // do nothing
        }

        @Override
        public void onPageSelected(int position) {
            Log.v(TAG,"onPageSelected()");
            mCurrentImgIdx = position;
            setTitle();
            View view = mLvImgPreview.getChildAt(position);
            if(view != null) {
                view.setBackgroundResource(R.drawable.rect_image_preview);
            }
            mLvImgPreview.scrollToPosition(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // do nothing
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
        String[] actions = getResources().getStringArray(R.array.detail_action_list);

        ActionListDialogFragment fragment = (ActionListDialogFragment) ActionListDialogFragment.newInstance(Arrays.asList(actions));
        fragment.setCallback(this);

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
          .add(fragment,"ActionList")
          .commit();
    }

    private void saveImage(){
        ImageView iv = (ImageView)mImgPager.getChildAt(mCurrentImgIdx);

        Drawable drawable = iv.getDrawable();
        if(drawable != null){
            final Bitmap bitmap = BitmapUtil.drawableToBitmap(drawable);

            final String path = AppGlobals.getInstance().getImagePath();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        BitmapUtil.saveToFile(bitmap,path);
                        // yes, job is done
                        Message msg = mMainHandler.obtainMessage(EventHandler.IMAGE_SAVE_COMPLETE);
                        msg.obj = null;
                    } catch (FileNotFoundException e) {
                        Log.v(TAG,"saveImage(): fail to save image");
                        e.printStackTrace();
                        Message msg = mMainHandler.obtainMessage(EventHandler.IMAGE_SAVE_COMPLETE);
                        msg.obj = e.getCause();
                    }
                }
            }).start();
        }
    }

    private class EventHandler extends Handler{

        static final int IMAGE_SAVE_COMPLETE = 1;
        static final int IMAGE_FAVOR_COMPLETE = 2;
        static final int IMAGE_DELETE_COMPLETE = 3;
        static final int IMAGE_SHARE_COMPLETE = 4;

        public EventHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg){
            int what = msg.what;

            switch (what){
                case IMAGE_SAVE_COMPLETE:
                    break;
                default:
                    break;
            }
        }
    }
}
