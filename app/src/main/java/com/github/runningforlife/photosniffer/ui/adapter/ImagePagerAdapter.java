package com.github.runningforlife.photosniffer.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.runningforlife.photosniffer.R;

import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import static com.github.runningforlife.photosniffer.loader.Loader.*;


/**
 * image pager adapter
 */

public class ImagePagerAdapter extends PagerAdapter{
    public static final String TAG = "ImagePageAdapter";

    private Context mContext;
    private PageAdapterCallback mCallback;

    public ImagePagerAdapter(Context context, BaseAdapterCallback callback){
        mCallback = (PageAdapterCallback) callback;
        mContext = context;
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

        final ImageView view = (ImageView) LayoutInflater.from(mContext)
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
        view.setAnimation(AnimationUtils.loadAnimation(mContext,R.anim.anim_scale_out));
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
        parent.removeView((ImageView)object);
    }

    public final class ImageLoaderListener implements RequestListener<String,Bitmap> {
        private static final String TAG = "GlideLoader";
        private ImageView image;
        private int pos;

        ImageLoaderListener(ImageView view, int pos){
            this.image = view;
            this.pos = pos;
        }

        @Override
        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
            Log.v(TAG,"onException(): " + e);
            image.setImageResource(R.drawable.ic_photo_grey_24dp);
            mCallback.onImageLoadDone(pos,false);
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
            Log.v(TAG,"onResourceReady(): from memory = " + isFromMemoryCache);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            //image.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.anim_scale_out));
            image.setImageBitmap(resource);
            mCallback.onImageLoadDone(pos,true);
            return false;
        }
    }
}
