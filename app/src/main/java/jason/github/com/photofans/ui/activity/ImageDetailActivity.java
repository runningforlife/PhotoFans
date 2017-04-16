package jason.github.com.photofans.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;
import com.tmall.ultraviewpager.UltraViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;
import jason.github.com.photofans.R;
import jason.github.com.photofans.loader.PicassoLoader;
import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.ui.ImageDetailPresenter;
import jason.github.com.photofans.ui.ImageDetailPresenterImpl;
import jason.github.com.photofans.ui.ImageDetailView;
import jason.github.com.photofans.ui.adapter.ImageAdapterCallback;
import jason.github.com.photofans.ui.adapter.ImagePagerAdapter;
import jason.github.com.photofans.ui.adapter.PreviewAdapter;
import jason.github.com.photofans.utils.DisplayUtil;

/**
 * activity to show the detail of the images
 */

public class ImageDetailActivity extends AppCompatActivity implements ImageDetailView,
        ImageAdapterCallback {
    private static final String TAG = "ImageDetailActivity";

    @BindView(R.id.toolbar) Toolbar mActionBar;
    //@BindView(R.id.iv_detail) ImageView mIvDetail;
    @BindView(R.id.rv_images) RecyclerView mLvImgPreview;
    @BindView(R.id.uvp_image) UltraViewPager mImgPager;
    private ImagePagerAdapter mPagerAdapter;
    private ImageDetailPresenter mPresenter;
    private PreviewAdapter mAdapter;
    private int mCurrentImgIdx;

    @Override
    public void onCreate(Bundle savedSate){
        super.onCreate(savedSate);

        setContentView(R.layout.activity_image_detail);

        ButterKnife.bind(this);

        setSupportActionBar(mActionBar);

        initView();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.v(TAG,"onResume()");
        // for adjust w/h ratio
        mPagerAdapter.setPageHeight(mImgPager.getHeight());
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        mPresenter.onDestroy();
    }

    private void initView(){
        mPresenter = new ImageDetailPresenterImpl(getApplicationContext(),ImageDetailActivity.this);
        mAdapter = new PreviewAdapter(getApplicationContext(),ImageDetailActivity.this);
        // may have null pointer exception
        mPresenter.init();

        // image pager
        //mImgPager = new UltraViewPager(this);
        mImgPager.setScrollMode(UltraViewPager.ScrollMode.HORIZONTAL);
        //mImgPager.setAutoMeasureHeight(true);
        mPagerAdapter = new ImagePagerAdapter(getApplicationContext(),this);
        mImgPager.setAdapter(mPagerAdapter);

        LinearLayoutManager llMgr = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        mLvImgPreview.setAdapter(mAdapter);
        mLvImgPreview.setVisibility(View.VISIBLE);
        mLvImgPreview.setLayoutManager(llMgr);

        Intent data = getIntent();
        mCurrentImgIdx = data.getIntExtra("image",0);
        Log.v(TAG,"initView(): current image index = " + mCurrentImgIdx);
        mImgPager.setCurrentItem(mCurrentImgIdx);
        mLvImgPreview.scrollToPosition(mCurrentImgIdx);
    }

    @Override
    public void onDataSetChanged() {
        Log.v(TAG,"onDataSetChanged()");
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
    }
}
