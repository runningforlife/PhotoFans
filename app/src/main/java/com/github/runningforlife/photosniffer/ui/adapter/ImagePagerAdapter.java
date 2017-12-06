package com.github.runningforlife.photosniffer.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.runningforlife.photosniffer.R;

import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.runningforlife.photosniffer.loader.Loader.*;


/**
 * image pager adapter
 */

public class ImagePagerAdapter extends PagerAdapter {
    public static final String TAG = "ImagePageAdapter";

    // if network error count is larger than 10, network is bad
    private static final int NETWORK_HUNG_ERROR_COUNT = 8;
    private static final int NETWORK_SLOW_ERROR_COUNT = 3;

    private Context mContext;
    private PageAdapterCallback mCallback;
    // check network state
    private int mNetworkErrorCount;

    public ImagePagerAdapter(Context context, BaseAdapterCallback callback){
        mCallback = (PageAdapterCallback) callback;
        mContext = context;

        mNetworkErrorCount = 0;
    }

    @Override
    public int getCount() {
        return mCallback.getCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup parent, final int position){
        Log.v(TAG,"instantiateItem(): position = " + position);

        final View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_image_detail,parent,false);
        // transition name
        if(Build.VERSION.SDK_INT >= 21) {
            String transitionName = mContext.getString(R.string.activity_image_transition)
                    + String.valueOf(position);
            view.setTransitionName(transitionName);
        }
        // start loading
        mCallback.onImageLoadStart(position);
        // preload image
        //MiscUtil.preloadImage(view);
        view.setAnimation(AnimationUtils.loadAnimation(mContext,R.anim.anim_scale_in));
        GlideLoader.load(mContext,new ImageLoaderListener(view,position),
                ((ImageRealm)mCallback.getItemAtPos(position)).getUrl(),
                Priority.IMMEDIATE,DEFAULT_IMG_WIDTH,DEFAULT_IMG_HEIGHT);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallback != null) {
                    mCallback.onItemClicked(view,position, TAG);
                }
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(mCallback != null) {
                    mCallback.onItemLongClicked(position, TAG);
                }
                return true;
            }
        });
        parent.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup parent,int position, Object object){
        parent.removeView((View)object);
    }

    public final class ImageLoaderListener implements RequestListener<String,Bitmap> {
        private static final String TAG = "GlideLoader";
        @BindView(R.id.iv_image_detail) ImageView image;
        private int pos;

        ImageLoaderListener(View view, int pos){
            ButterKnife.bind(this, view);
            this.pos = pos;
        }

        @Override
        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
            Log.v(TAG,"onException(): " + e);
            image.setImageResource(R.drawable.ic_photo_grey_24dp);
            mCallback.onImageLoadDone(pos,false);
            // check network state
            if (MiscUtil.isConnected(mContext)) {
                if(++mNetworkErrorCount >= NETWORK_SLOW_ERROR_COUNT && mNetworkErrorCount < NETWORK_HUNG_ERROR_COUNT) {
                    mCallback.onNetworkState(NetworkStateCallback.STATE_SLOW);
                } else if (mNetworkErrorCount > NETWORK_HUNG_ERROR_COUNT) {
                    mCallback.onNetworkState(NetworkStateCallback.STATE_HUNG);
                }
            } else {
                mCallback.onNetworkState(NetworkStateCallback.STATE_DISCONNECT);
            }


            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
            Log.v(TAG,"onResourceReady(): from memory = " + isFromMemoryCache);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            //image.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.anim_scale_in));
            image.setImageBitmap(resource);
            mCallback.onImageLoadDone(pos,true);
            if(Build.VERSION.SDK_INT >= 23){
                RippleDrawable rd = (RippleDrawable)image.getForeground();
                if(rd != null) {
                    Palette palette = Palette.from(resource).generate();
                    int dc = palette.getDominantColor(Color.DKGRAY);
                    ColorStateList csl = ColorStateList.valueOf(dc);
                    rd.setColor(csl);
                    //rd.setColorFilter(dc, PorterDuff.Mode.DST_IN);
                }
            }
            return false;
        }
    }
}
