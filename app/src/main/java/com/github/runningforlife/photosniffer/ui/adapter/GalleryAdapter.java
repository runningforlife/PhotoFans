package com.github.runningforlife.photosniffer.ui.adapter;

import android.content.Context;
import android.os.Build;
import android.renderscript.RenderScript;
import android.support.annotation.RequiresApi;
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
import com.github.runningforlife.photosniffer.model.ImageRealm;
import com.github.runningforlife.photosniffer.utils.MiscUtil;

import static com.github.runningforlife.photosniffer.loader.Loader.*;
import static com.github.runningforlife.photosniffer.ui.fragment.BaseFragment.*;

/**
 * a gallery adapter to bind image data to recycleview
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.PhotoViewHolder>{
    private static final String TAG = "GalleryAdapter";
    @SuppressWarnings("unchecked")
    private LayoutInflater mInflater;
    private GalleryAdapterCallback mCallback;
    private Context mContext;
    private int mWidth;
    private int mHeight;
    private String mLoader;
    private String mLayoutMgr;

    public GalleryAdapter(Context context,BaseAdapterCallback callback){
        mCallback = (GalleryAdapterCallback) callback;
        mInflater = LayoutInflater.from(context);
        // different device panel size may need different width and height
        mContext = context;

        mLoader = Loader.GLIDE;

        mLayoutMgr = GridManager;
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
        final ImageRealm img = mCallback.getItemAtPos(position);
        final String url = img.getUrl();
        Log.d(TAG,"onBindViewHolder(): pos = " + position);

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
            }else{
                //FIXME: some item is loaded very slowly
                GlideLoaderListener listener = new GlideLoaderListener(vh.img);
                if(mWidth > 0 && mHeight > 0 &&
                        mWidth != DEFAULT_IMG_WIDTH && mHeight != DEFAULT_IMG_HEIGHT) {
                    listener.setReqWidth(mWidth);
                    listener.setReqHeight(mHeight);
                }
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

    public class PhotoViewHolder extends RecyclerView.ViewHolder
            implements View.OnCreateContextMenuListener {
        @BindView(R.id.iv_photo) ImageView img;

        public PhotoViewHolder(View root) {
            super(root);

            ButterKnife.bind(this,root);
            // transition name
            if(Build.VERSION.SDK_INT >= 21) {
                String transitionName = mContext.getString(R.string.activity_image_transition)
                        + String.valueOf(getAdapterPosition());
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

            inflater.inflate(R.menu.menu_context_choice, menu);

            mCallback.onContextMenuCreated(getAdapterPosition(), TAG);
        }
    }
}
