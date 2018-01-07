package com.github.runningforlife.photosniffer.ui.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.R;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.iwf.photopicker.utils.AndroidLifecycleUtils;

import static com.github.runningforlife.photosniffer.loader.Loader.*;


/**
 * image pager adapter
 */

public class ImagePagerAdapter extends PagerAdapter {
    public static final String TAG = "ImagePageAdapter";

    private Context mContext;
    private PageAdapterCallback mCallback;

    public ImagePagerAdapter(Context context, @Nonnull BaseAdapterCallback callback) {
        mCallback = (PageAdapterCallback) callback;
        mContext = context;
    }


    @Override
    public int getCount() {
        return mCallback.getCount();
    }

    @Override
    public int getItemPosition(Object object) {
        // try to refresh all data set
        return POSITION_NONE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup parent, final int position) {
        Log.v(TAG,"instantiateItem(): position = " + position);

        final View view = LayoutInflater.from(mContext)
                .inflate(R.layout.item_image_detail,parent,false);
        ImageHolder imageHolder = new ImageHolder(view);

        if (AndroidLifecycleUtils.canLoadImage(mContext)) {
            // preload image
            ImageView iv = imageHolder.imageView;
            //MiscUtil.preloadImage(iv);
            //view.setAnimation(AnimationUtils.loadAnimation(mContext, R.anim.anim_scale_in));
            mCallback.loadImageIntoView(position, iv, Priority.IMMEDIATE,
                    DEFAULT_IMG_WIDTH, DEFAULT_IMG_HEIGHT, ImageView.ScaleType.FIT_CENTER);

        }
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
    public void destroyItem(ViewGroup parent,int position, Object object) {
        parent.removeView((View)object);
    }

    final class ImageHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_image_detail) ImageView imageView;

        ImageHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}
