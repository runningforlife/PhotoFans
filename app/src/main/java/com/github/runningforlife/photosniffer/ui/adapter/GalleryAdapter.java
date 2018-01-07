package com.github.runningforlife.photosniffer.ui.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.MenuRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.github.runningforlife.photosniffer.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.iwf.photopicker.utils.AndroidLifecycleUtils;

import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * a gallery adapter to bind image data to recycleview
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.PhotoViewHolder>{
    private static final String TAG = "GalleryAdapter";

    @SuppressWarnings("unchecked")
    private LayoutInflater mInflater;
    private GalleryAdapterCallback mCallback;
    private Context mContext;
    private @MenuRes int mContextMenId;
    private List<String> mSelectedImages;
    private boolean mIsBatchEditEnabled;

    public GalleryAdapter(Context context,BaseAdapterCallback callback) {
        mCallback = (GalleryAdapterCallback) callback;
        mInflater = LayoutInflater.from(context);
        // different device panel size may need different width and height
        mContext = context;

        mContextMenId = R.menu.menu_context_default;

        mSelectedImages = new ArrayList<>();
    }

    public List<String> getSelectedImages() {
        return new ArrayList<>(mSelectedImages);
    }

    public void enableBatchEdit(boolean isEnabled) {
        mIsBatchEditEnabled = isEnabled;
        mSelectedImages.clear();
        notifyDataSetChanged();
    }

    public void setContextMenuRes(@MenuRes int id){
        mContextMenId = id;
    }


    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = mInflater.inflate(R.layout.item_photo_layout,parent,false);

        return new PhotoViewHolder(root);
    }

    @Override
    public void onBindViewHolder(final PhotoViewHolder vh, final int position) {
        final ImageRealm img = (ImageRealm)mCallback.getItemAtPos(position);
        if (img != null) {
            final String url = img.getUrl();
            Log.d(TAG, "onBindViewHolder(): pos = " + position);
            if (!TextUtils.isEmpty(url)) {
                // preload image
                MiscUtil.preloadImage(vh.img);
                if (AndroidLifecycleUtils.canLoadImage(mContext)) {
                    mCallback.loadImageIntoView(position, vh.img, Priority.HIGH,
                            800, 800, ImageView.ScaleType.CENTER_CROP);
                }

                if (mIsBatchEditEnabled) {
                    vh.img.setSelected(isSelected(url));
                    vh.selectedView.setVisibility(View.VISIBLE);
                    vh.selectedView.setSelected(isSelected(url));

                    vh.selectedView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!mSelectedImages.contains(url)) {
                                mSelectedImages.add(url);
                            } else {
                                mSelectedImages.remove(url);
                            }
                            notifyItemChanged(position);
                            mCallback.onItemSelected(mSelectedImages.size());
                        }
                    });
                } else {
                    vh.selectedView.setVisibility(View.GONE);
                    vh.img.setSelected(false);
                }

            } else if (getItemCount() > 0) {
                // remove from the list
                mCallback.removeItemAtPos(position);
            }
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

    private boolean isSelected(String url) {
        return url != null && mSelectedImages.contains(url);
    }

    final class PhotoViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener {
        @BindView(R.id.iv_photo) ImageView img;
        @BindView(R.id.iv_selected) View selectedView;

        PhotoViewHolder(View root) {
            super(root);

            ButterKnife.bind(this,root);
            // transition name
            if(Build.VERSION.SDK_INT >= 21) {
                String transitionName = mContext.getString(R.string.activity_image_transition);
                img.setTransitionName(transitionName);
            }
            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mCallback != null){
                        mCallback.onItemClicked(img,getAdapterPosition(),TAG);
                    }
                }
            });

            root.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            Log.v(TAG,"onCreateContextMenu()");
            MenuInflater inflater = ((AppCompatActivity)mContext).getMenuInflater();

            inflater.inflate(mContextMenId, menu);

            if(mCallback != null) {
                mCallback.onContextMenuCreated(getAdapterPosition(), TAG);
            }
        }
    }
}
