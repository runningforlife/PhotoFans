package jason.github.com.photofans.ui.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
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
import android.view.MenuItem;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import jason.github.com.photofans.R;
import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.ui.ImageDetailPresenter;
import jason.github.com.photofans.ui.ImageDetailPresenterImpl;
import jason.github.com.photofans.ui.ImageDetailView;
import jason.github.com.photofans.ui.adapter.ImageAdapterCallback;
import jason.github.com.photofans.ui.adapter.ImagePagerAdapter;
import jason.github.com.photofans.ui.adapter.PreviewAdapter;

/**
 * activity to show the detail of the images
 */

public class ImageDetailActivity extends AppCompatActivity implements ImageDetailView,
        ImageAdapterCallback {
    private static final String TAG = "ImageDetailActivity";

    //@BindView(R.id.iv_detail) ImageView mIvDetail;
    @BindView(R.id.rv_images) RecyclerView mLvImgPreview;
    @BindView(R.id.vp_image) ViewPager mImgPager;
    private ImagePagerAdapter mPagerAdapter;
    private ImageDetailPresenter mPresenter;
    private PreviewAdapter mAdapter;
    private int mCurrentImgIdx;
    private ActionBar mActionBar;

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
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(TAG,"onResume()");

        setTitle();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        mPresenter.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();
        if(id == android.R.id.home){
            if(Build.VERSION.SDK_INT >= 16) {
                NavUtils.navigateUpFromSameTask(this);
            }else{
                Intent intent = new Intent(this, GalleryActivity.class);
                NavUtils.navigateUpTo(this,intent);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initView(){
        mPresenter = new ImageDetailPresenterImpl(getApplicationContext(),ImageDetailActivity.this);
        mAdapter = new PreviewAdapter(getApplicationContext(),ImageDetailActivity.this);
        mPagerAdapter = new ImagePagerAdapter(getApplicationContext(),this);
        // may have null pointer exception
        mPresenter.init();

        // image pager
        //mImgPager = new UltraViewPager(this);
        //mImgPager.setAutoMeasureHeight(true);
        mImgPager.setAdapter(mPagerAdapter);
        mImgPager.addOnPageChangeListener(new ImagePageStateListener());
        LinearLayoutManager llMgr = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        mLvImgPreview.setAdapter(mAdapter);
        mLvImgPreview.setVisibility(View.VISIBLE);
        mLvImgPreview.setLayoutManager(llMgr);
        mLvImgPreview.setItemAnimator(new DefaultItemAnimator());

        Intent data = getIntent();
        mCurrentImgIdx = data.getIntExtra("image",0);
        Log.v(TAG,"initView(): current image index = " + mCurrentImgIdx);
        mImgPager.setCurrentItem(mCurrentImgIdx);
        mLvImgPreview.scrollToPosition(mCurrentImgIdx);
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

        // change title
        setTitle();
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
}
