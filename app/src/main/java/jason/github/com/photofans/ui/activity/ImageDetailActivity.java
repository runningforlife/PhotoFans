package jason.github.com.photofans.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import jason.github.com.photofans.R;
import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.ui.ImageDetailPresenter;
import jason.github.com.photofans.ui.ImageDetailPresenterImpl;
import jason.github.com.photofans.ui.ImageDetailView;
import jason.github.com.photofans.ui.adapter.ImagePreviewAdapter;
import jason.github.com.photofans.utils.DisplayUtil;

/**
 * Created by jason on 4/5/17.
 */

public class ImageDetailActivity extends AppCompatActivity implements ImageDetailView,
        ImagePreviewAdapter.PreviewCallback{
    private static final String TAG = "ImageDetailActivity";

    @BindView(R.id.toolbar) Toolbar mActionBar;
    @BindView(R.id.iv_detail) ImageView mIvDetail;
    @BindView(R.id.lv_container) LinearLayout mLvContainer;
    @BindView(R.id.lv_images) ListView mLvImgPreview;
    private ImageDetailPresenter mPresenter;
    private ImagePreviewAdapter mAdapter;
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
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        mPresenter.onDestroy();
    }

    private void initView(){
        //mLvImgPreview = (ListView)mLvContainer.findViewById(R.id.lv_images);
        mPresenter = new ImageDetailPresenterImpl(getApplicationContext(),ImageDetailActivity.this);
        mAdapter = new ImagePreviewAdapter(getApplicationContext(),ImageDetailActivity.this);
        // may have null pointer exception
        mPresenter.init();
        mLvImgPreview.setAdapter(mAdapter);
        mLvImgPreview.setVisibility(View.VISIBLE);
        Intent data = getIntent();
        mCurrentImgIdx = data.getIntExtra("image",0);
        Log.v(TAG,"initView(): current image index = " + mCurrentImgIdx);

    }

    @Override
    public void onDataSetChanged() {
        Log.v(TAG,"onDataSetChanged()");
        mAdapter.notifyDataSetChanged();

        Picasso.with(this)
                .load(getItemAtPos(mCurrentImgIdx).getUrl())
                .resize(1024,(int)(1024* DisplayUtil.getScreenRatio()))
                .into(mIvDetail);
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
        Log.v(TAG,"onItemClicked()");
        mCurrentImgIdx = pos;
        Picasso.with(this)
                .load(getItemAtPos(mCurrentImgIdx).getUrl())
                .centerCrop()
                .resize(1024,(int)(1024* DisplayUtil.getScreenRatio()))
                .into(mIvDetail);
    }
}
