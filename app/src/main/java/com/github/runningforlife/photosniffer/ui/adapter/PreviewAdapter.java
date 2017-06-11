package com.github.runningforlife.photosniffer.ui.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.github.runningforlife.photosniffer.R;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;
import com.github.runningforlife.photosniffer.utils.DisplayUtil;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.runningforlife.photosniffer.loader.Loader.*;


/**
 * preview adapter to show list of pictures
 */

public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.ImageViewHolder> {
    public static final String TAG = "PreviewAdapter";

    private static final int DEFAULT_IMAGE_WIDTH = DEFAULT_IMG_WIDTH/8;
    private static final int DEFAULT_IMAGE_HEIGHT = (int)(200*DisplayUtil.getScreenRatio());

    private Context mContext;
    private ImageAdapterCallback mCallback;

    public PreviewAdapter(Context context, ImageAdapterCallback callback){
        mContext = context;
        mCallback = callback;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = LayoutInflater.from(mContext)
                .inflate(R.layout.item_image_preview,parent,false);
        return new ImageViewHolder(root);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        Log.v(TAG,"onBindViewHolder(): position = " + position);

        String url = mCallback.getItemAtPos(position).getUrl();
        // preload
        MiscUtil.preloadImage(holder.preview);
        holder.preview.setAnimation(AnimationUtils.loadAnimation(mContext,R.anim.view_scale_out));
        GlideLoaderListener listener = new GlideLoaderListener(holder.preview);
        listener.setReqWidth(DEFAULT_IMAGE_WIDTH);
        listener.setReqHeight(DEFAULT_IMAGE_HEIGHT);
        GlideLoader.load(mContext,url, listener,
                DEFAULT_IMG_WIDTH,DEFAULT_IMG_HEIGHT);
    }

    @Override
    public int getItemCount() {
        return mCallback.getCount();
    }

    final class ImageViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.iv_preview) ImageView preview;

        public ImageViewHolder(View root) {
            super(root);

            ButterKnife.bind(this,root);

            root.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                // FIXME: there is exception when item is clicked
                public void onClick(View v) {
                    if(mCallback != null) {
                        mCallback.onItemClicked(getAdapterPosition(), TAG);
                    }
                }
            });
        }

    }
}