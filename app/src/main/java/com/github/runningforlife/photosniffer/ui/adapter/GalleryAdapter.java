package com.github.runningforlife.photosniffer.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.loader.PicassoLoader;
import com.github.runningforlife.photosniffer.loader.PicassoLoaderListener;
import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.utils.DisplayUtil;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

/**
 * a gallery adapter to bind image data to recycleview
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.PhotoViewHolder>{
    private static final String TAG = "GalleryAdapter";

    private static final int DEFAULT_IMG_WIDTH = 1024;
    private static final int DEFAULT_IMG_HEIGHT = (int)(DEFAULT_IMG_WIDTH*DisplayUtil.getScreenRatio());

    @SuppressWarnings("unchecked")
    private LayoutInflater mInflater;
    private ImageAdapterCallback mCallback;
    private Context mContext;
    private int mWidth;
    private int mHeight;
    private String mLoader;

    public GalleryAdapter(Context context,ImageAdapterCallback callback){
        mCallback = callback;
        mInflater = LayoutInflater.from(context);
        // different device panel size may need different width and height
        mContext = context;

        mWidth = DEFAULT_IMG_WIDTH;
        mHeight = DEFAULT_IMG_HEIGHT;
        mLoader = Loader.GLIDE;
    }

    public void setImageWidth(int w){
        mWidth = w > 0 ? w : DEFAULT_IMG_WIDTH;
    }

    public void setImageHeight(int h){
        mHeight = h > 0 ? h : DEFAULT_IMG_HEIGHT;
    }

    public void setImageLoader(@Loader.LOADER String loader){
        mLoader = loader;
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = mInflater.inflate(R.layout.item_photo_layout,parent,false);

        return new PhotoViewHolder(root);
    }

    @Override
    public void onBindViewHolder(final PhotoViewHolder vh, final int position) {
        final ImageRealm img = mCallback.getItemAtPos(position);
        final String url = img.getUrl();
        Log.d(TAG,"onBindViewHolder(): pos = " + position);

        if(!TextUtils.isEmpty(url)) {
            // preload image
            MiscUtil.preloadImage(vh.img);
            if (Loader.PICASSO.equals(mLoader)) {
                PicassoLoader.load(mContext, new PicassoLoaderListener(vh.img), url, mWidth,mHeight);
            }else{
                //FIXME: some item is loaded very slowly
                GlideLoaderListener listener = new GlideLoaderListener(vh.img);
                //Glide.clear(vh.img);
                GlideLoader.load(mContext,url,listener,mWidth,mHeight);
            }

        }else if(getItemCount() > 0){
            // remove from the list
            mCallback.removeItemAtPos(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mCallback.getCount();
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.iv_photo) ImageView img;

        public PhotoViewHolder(View root) {
            super(root);

            ButterKnife.bind(this,root);

            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mCallback != null){
                        mCallback.onItemClicked(getAdapterPosition(),TAG);
                    }
                }
            });
        }
    }
}
