package com.github.runningforlife.photofans.ui.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.app.AppGlobals;
import com.github.runningforlife.photofans.utils.DisplayUtil;

import com.github.runningforlife.photofans.loader.GlideLoader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * image pager adapter
 */

public class ImagePagerAdapter extends PagerAdapter{
    public static final String TAG = "ImagePageAdapter";

    private static final int DEFAULT_WIDTH = 1024;
    private static final int DEFAULT_HEIGHT = (int)(DEFAULT_WIDTH/ DisplayUtil.getScreenRatio());
    private List<ImageView> mAllImages;
    private Context mContext;
    private ImageAdapterCallback mCallback;

    public ImagePagerAdapter(Context context, ImageAdapterCallback callback){
        mCallback = callback;
        mContext = context;
        mAllImages = new ArrayList<>();
    }

    //get view at index
    public View getViewAtPos(int pos){
        return mAllImages.get(pos);
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

        ImageView view = (ImageView) LayoutInflater.from(mContext)
                .inflate(R.layout.item_image_detail,parent,false);
        // start loading
        mCallback.onImageLoadStart(position);

        GlideLoader.load(mContext,new ImageLoaderListener(view,position),mCallback.getItemAtPos(position).getUrl(),
                DEFAULT_WIDTH,DEFAULT_HEIGHT);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mCallback != null) {
                    mCallback.onItemClicked(position, TAG);
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
        // save it for later use
        mAllImages.add(position,view);
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
            image.setImageDrawable(AppGlobals.getInstance().getResources().getDrawable(R.drawable.ic_mood_bad_grey_24dp));
            mCallback.onImageLoadDone(pos,false);
            return false;
        }

        @Override
        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
            Log.v(TAG,"onResourceReady(): from memory = " + isFromMemoryCache);
            image.setImageBitmap(resource);
            mCallback.onImageLoadDone(pos,true);
            return false;
        }
    }
}
