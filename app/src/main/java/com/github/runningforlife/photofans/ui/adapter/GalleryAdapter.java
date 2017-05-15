package com.github.runningforlife.photofans.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.realm.RealmManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.github.runningforlife.photofans.loader.GlideLoader;
import com.github.runningforlife.photofans.realm.ImageRealm;
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
    private ItemSelectedCallback mCallback;
    private Context mContext;

    public interface ItemSelectedCallback {
        void onItemClick(int pos);
        int getItemCount();
        ImageRealm getItemAtPos(int pos);
        void removeItemAtPos(int pos);
    }

    public GalleryAdapter(Context context,ItemSelectedCallback callback){
        mCallback = callback;
        mInflater = LayoutInflater.from(context);
        // different device panel size may need different width and height
        double ratio = DisplayUtil.getScreenRatio();
        mContext = context;
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = mInflater.inflate(R.layout.item_photo_layout,parent,false);

        return new PhotoViewHolder(root);
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder vh, int position) {
        String url = mCallback.getItemAtPos(position).getUrl();
        Log.v(TAG,"onBindViewHolder(): image url = " + url);

        if(!TextUtils.isEmpty(url)) {
/*            PicassoLoader.load(mContext,new ImageTarget(vh.img,position),url,
                    DEFAULT_IMG_WIDTH,DEFAULT_IMG_HEIGHT);*/

            GlideLoader.load(mContext,url,vh.img,DEFAULT_IMG_WIDTH,DEFAULT_IMG_HEIGHT);
        }else if(getItemCount() > 0){
            // remove from the list
            RealmManager.getInstance().delete(mCallback.getItemAtPos(position));
            notifyItemChanged(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mCallback.getItemCount();
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
                        mCallback.onItemClick(getAdapterPosition());
                    }
                }
            });
        }
    }

    // picasso loader
    final class ImageTarget implements Target{

        private ImageView mImg;
        private int mPos;

        public ImageTarget(ImageView iv, int pos){
            mImg = iv;
            this.mPos = pos;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            //int imgSize = BitmapUtil.getBitmapSize(bitmap);
            int imgSize = bitmap.getByteCount();
            Log.v(TAG,"onBitmapLoaded(): from " + from + ",image size = " + imgSize/KB + "KB");
            int w = bitmap.getWidth();
            int h = bitmap.getHeight();
            // filter small size pictures
            if(imgSize < MIN_IMG_SIZE || (w < MIN_IMG_WIDTH || h < MIN_IMG_HEIGHT)){
                mCallback.removeItemAtPos(mPos);
            }else{
                // display it
                mImg.setImageBitmap(bitmap);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Log.v(TAG,"onBitmapFailed()");
            //just remove it
            //mCallback.removeItemAtPos(mPos);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }
}
