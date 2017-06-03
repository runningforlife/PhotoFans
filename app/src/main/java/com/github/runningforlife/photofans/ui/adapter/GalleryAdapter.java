package com.github.runningforlife.photofans.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.loader.GlideLoaderListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.github.runningforlife.photofans.loader.GlideLoader;
import com.github.runningforlife.photofans.model.ImageRealm;
import com.github.runningforlife.photofans.utils.DisplayUtil;

/**
 * a gallery adapter to bind image data to recycleview
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.PhotoViewHolder>{
    private static final String TAG = "GalleryAdapter";

    private static final int DEFAULT_IMG_WIDTH = 1024;
    private static final int DEFAULT_IMG_HEIGHT = (int)(DEFAULT_IMG_WIDTH*DisplayUtil.getScreenRatio());

    private static final int KB = 1024;
    private static final int MIN_IMG_SIZE = KB * 60;
    private static final int MIN_IMG_WIDTH = 150;
    private static final int MIN_IMG_HEIGHT = 150;

    @SuppressWarnings("unchecked")
    private LayoutInflater mInflater;
    private ImageAdapterCallback mCallback;
    private Context mContext;

    public GalleryAdapter(Context context,ImageAdapterCallback callback){
        mCallback = callback;
        mInflater = LayoutInflater.from(context);
        // different device panel size may need different width and height
        mContext = context;
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
        Log.d(TAG,"onBindViewHolder(): image url = " + url);

        if(!TextUtils.isEmpty(url)) {
            GlideLoaderListener listener = new GlideLoaderListener(vh.img);
            GlideLoader.load(mContext,url,listener,DEFAULT_IMG_WIDTH,DEFAULT_IMG_HEIGHT);
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
