package jason.github.com.photofans.ui.adapter;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.Image;
import android.support.v4.view.PagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;

import java.util.ArrayList;
import java.util.List;

import jason.github.com.photofans.R;
import jason.github.com.photofans.app.AppGlobals;
import jason.github.com.photofans.loader.GlideLoader;
import jason.github.com.photofans.loader.GlideLoaderListener;
import jason.github.com.photofans.loader.PicassoLoader;
import jason.github.com.photofans.utils.DisplayUtil;

/**
 * image pager adapter
 */

public class ImagePagerAdapter extends PagerAdapter{
    private static final String TAG = "ImagePageAdapter";

    private static final int DEFAULT_WIDTH = 1024;
    private static final int DEFAULT_HEIGHT = (int)(DEFAULT_WIDTH/DisplayUtil.getScreenRatio());
    private static final int MAX_HEIGHT = (int)(DEFAULT_WIDTH*1.5*DisplayUtil.getScreenRatio());

    private Context mContext;
    private ImageAdapterCallback mCallback;

    public ImagePagerAdapter(Context context, ImageAdapterCallback callback){
        mCallback = callback;
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
    public Object instantiateItem(ViewGroup parent, int position){
        Log.v(TAG,"instantiateItem(): position = " + position);

        ImageView view = (ImageView) LayoutInflater.from(mContext)
                .inflate(R.layout.item_image_detail,parent,false);
        // start loading
        mCallback.onImageLoadStart(position);

        GlideLoader.load(mContext,new ImageLoaderListener(view,position),mCallback.getItemAtPos(position).getUrl(),
                DEFAULT_WIDTH,DEFAULT_HEIGHT);

        parent.addView(view);

        return view;
    }

    @Override
    public void destroyItem(ViewGroup parent,int position, Object object){
        parent.removeView((ImageView)object);
    }

    private final class ImageLoaderListener implements RequestListener<String,GlideDrawable> {
        private static final String TAG = "GlideLoader";
        private ImageView image;
        private int pos;

        ImageLoaderListener(ImageView view, int pos){
            this.image = view;
            this.pos = pos;
        }

        @TargetApi(21)
        @Override
        public boolean onException(Exception e, String model, com.bumptech.glide.request.target.Target<GlideDrawable> target, boolean isFirstResource) {
            Log.v(TAG,"onException(): " + e);
            image.setImageDrawable(AppGlobals.getInstance().getDrawable(R.drawable.ic_mood_bad_grey_24dp));
            mCallback.onImageLoadDone(pos,false);
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, com.bumptech.glide.request.target.Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
            Log.v(TAG,"onResourceReady(): from memory = " + isFromMemoryCache);
            image.setImageDrawable(resource);
            mCallback.onImageLoadDone(pos,true);
            return false;
        }
    }
}
