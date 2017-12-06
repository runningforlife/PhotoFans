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
import com.github.runningforlife.photosniffer.loader.GlideLoaderListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.github.runningforlife.photosniffer.loader.GlideLoader;
import com.github.runningforlife.photosniffer.loader.Loader;
import com.github.runningforlife.photosniffer.loader.PicassoLoader;
import com.github.runningforlife.photosniffer.loader.PicassoLoaderListener;
import com.github.runningforlife.photosniffer.data.model.ImageRealm;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import java.io.IOException;

import static com.github.runningforlife.photosniffer.loader.Loader.*;
import static com.github.runningforlife.photosniffer.ui.fragment.BaseFragment.*;

/**
 * a gallery adapter to bind image data to recycleview
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.PhotoViewHolder>{
    private static final String TAG = "GalleryAdapter";

    // if network error count is larger than 10, network is bad
    private static final int NETWORK_HUNG_ERROR_COUNT = 10;
    private static final int NETWORK_SLOW_ERROR_COUNT = 5;

    @SuppressWarnings("unchecked")
    private LayoutInflater mInflater;
    private GalleryAdapterCallback mCallback;
    private Context mContext;
    private String mLoader;
    private String mLayoutMgr;
    private @MenuRes int mContextMenId;
    private int mNetworkErrorCount;

    public GalleryAdapter(Context context,BaseAdapterCallback callback){
        mCallback = (GalleryAdapterCallback) callback;
        mInflater = LayoutInflater.from(context);
        // different device panel size may need different width and height
        mContext = context;

        mLoader = Loader.GLIDE;

        mLayoutMgr = GridManager;

        mContextMenId = R.menu.menu_context_default;

        mNetworkErrorCount = 0;
    }


    public void setContextMenuRes(@MenuRes int id){
        mContextMenId = id;
    }

    public void setLayoutManager(@RecycleLayout String layoutManager){
        mLayoutMgr = layoutManager;
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
        final ImageRealm img = (ImageRealm)mCallback.getItemAtPos(position);
        final String url = img.getUrl();
        Log.d(TAG,"onBindViewHolder(): pos = " + position + ",url = " + url);

        if(!TextUtils.isEmpty(url)) {
            if(LinearManager.equals(mLayoutMgr)){
                vh.img.setMinimumHeight((int) mContext.getResources().
                        getDimension(R.dimen.list_view_image_min_height));
            }
            // preload image
            MiscUtil.preloadImage(vh.img);
            if (Loader.PICASSO.equals(mLoader)) {
                PicassoLoader.load(mContext, new PicassoLoaderListener(vh.img), url,
                        DEFAULT_IMAGE_MEDIUM_WIDTH,DEFAULT_IMAGE_MEDIUM_HEIGHT);
            } else {
                GlideLoaderListener listener = new GlideLoaderListener(vh.img);
                listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
                    @Override
                    public void onImageLoadDone(Object o) {
                        Log.v(TAG,"onImageLoadDone(): " + o);
                        // 404 or socket time out
                        if (o instanceof IOException) {
                            //check network state
                            if (MiscUtil.isConnected(mContext)) {
                                // network is slow
                                ++mNetworkErrorCount;
                                if (mNetworkErrorCount >= NETWORK_SLOW_ERROR_COUNT && mNetworkErrorCount < NETWORK_HUNG_ERROR_COUNT) {
                                    mCallback.onNetworkState(STATE_SLOW);
                                } else if (mNetworkErrorCount >= NETWORK_HUNG_ERROR_COUNT) {
                                    mCallback.onNetworkState(STATE_HUNG);
                                }
                            } else {
                                mCallback.onNetworkState(STATE_DISCONNECT);
                            }

                        }
                    }
                });
                GlideLoader.load(mContext,url,listener, Priority.HIGH,
                        DEFAULT_IMAGE_MEDIUM_WIDTH, DEFAULT_IMAGE_MEDIUM_HEIGHT);
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

    final class PhotoViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener {
        @BindView(R.id.iv_photo) ImageView img;

        public PhotoViewHolder(View root) {
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

            inflater.inflate(R.menu.menu_context_default, menu);

            if(mCallback != null) {
                mCallback.onContextMenuCreated(getAdapterPosition(), TAG);
            }
        }
    }
}
