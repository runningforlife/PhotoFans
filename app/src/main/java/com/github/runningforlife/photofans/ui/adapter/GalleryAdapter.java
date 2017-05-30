package com.github.runningforlife.photofans.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.loader.GlideLoaderListener;
import com.github.runningforlife.photofans.model.RealmManager;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.github.runningforlife.photofans.loader.GlideLoader;
import com.github.runningforlife.photofans.model.ImageRealm;
import com.github.runningforlife.photofans.utils.DisplayUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    // decode bitmap
    private ExecutorService mExecutor;

    public interface ItemSelectedCallback {
        void onItemClick(int pos);
        int getItemCount();
        ImageRealm getItemAtPos(int pos);
        void removeItemAtPos(int pos);
        void saveImage(int pos, Bitmap bitmap);
    }

    public GalleryAdapter(Context context,ItemSelectedCallback callback){
        mCallback = callback;
        mInflater = LayoutInflater.from(context);
        // different device panel size may need different width and height
        double ratio = DisplayUtil.getScreenRatio();
        mContext = context;
        mExecutor = Executors.newFixedThreadPool(3);
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

        if(img.getData() != null) {
            mExecutor.submit(new DecodeRunnable(img.getData(), new DecodeCallback() {
                @Override
                public void onDecodeDone(Bitmap bitmap) {
                    if(bitmap != null) {
                        vh.img.setImageBitmap(bitmap);
                    }else{
                        GlideLoader.load(mContext,url,new GlideLoaderListener(vh.img),DEFAULT_IMG_WIDTH,
                                DEFAULT_IMG_HEIGHT);
                    }
                }
            }));
        }else if(!TextUtils.isEmpty(url)) {
            GlideLoaderListener listener = new GlideLoaderListener(vh.img);
            listener.addCallback(new GlideLoaderListener.ImageLoadCallback() {
                @Override
                public void onImageLoadDone(Bitmap bitmap) {
                    // save image
                    mCallback.saveImage(vh.getAdapterPosition(),bitmap);
                }
            });
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

    public class DecodeRunnable implements Runnable{
        private byte[] data;
        private DecodeCallback callback;

        public DecodeRunnable(byte[] data, DecodeCallback callback){
            this.data = data;
            this.callback = callback;
        }

        @Override
        public void run() {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.outWidth = DEFAULT_IMG_WIDTH;
            options.outHeight = DEFAULT_IMG_HEIGHT;

            Bitmap bitmap = BitmapFactory.decodeByteArray(data,0,data.length,options);
            callback.onDecodeDone(bitmap);
        }
    }

    private interface DecodeCallback{
        void onDecodeDone(Bitmap bitmap);
    }
}
