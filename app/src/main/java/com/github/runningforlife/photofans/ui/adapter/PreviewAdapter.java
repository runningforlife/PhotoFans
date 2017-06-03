package com.github.runningforlife.photofans.ui.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.loader.GlideLoader;
import com.github.runningforlife.photofans.loader.GlideLoaderListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * preview adapter to show list of pictures
 */

public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.ImageViewHolder> {
    public static final String TAG = "PreviewAdapter";

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
        GlideLoader.load(mContext,url,new GlideLoaderListener(holder.preview),150,150);
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
