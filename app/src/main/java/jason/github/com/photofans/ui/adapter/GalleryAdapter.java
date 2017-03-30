package jason.github.com.photofans.ui.adapter;

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

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jason.github.com.photofans.R;
import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.model.RealmHelper;
import jason.github.com.photofans.utils.DisplayUtil;

/**
 * a gallery adapter to bind image data to recycleview
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.PhotoViewHolder>{
    private static final String TAG = "GalleryAdapter";

    private static final int DEFAULT_IMG_RESOLUTION = 512;
    private static final double DEFAULT_SCREEN_RATIO = 0.75;

    private static final int KB = 1024;
    private static final int MIN_IMG_SIZE = KB * 60;
    private static final int MIN_IMG_WIDTH = 150;
    private static final int MIN_IMG_HEIGHT = 150;

    @SuppressWarnings("unchecked")
    private List<ImageRealm> mImageList = Collections.EMPTY_LIST;
    private LayoutInflater mInflater;
    private ItemSelectedCallback mCallback;
    private Picasso mPicasso;
    private double mScreenRatio;

    public interface ItemSelectedCallback {
        void onItemClick(int pos);
        int getItemCount();
        ImageRealm getItemAtPos(int pos);
        void removeItemAtPos(int pos);
    }

    public GalleryAdapter(Context context,ItemSelectedCallback callback){
        mCallback = callback;
        mInflater = LayoutInflater.from(context);
        mPicasso = Picasso.with(context);
        // different device panel size may need different width and height
        double ratio = DisplayUtil.getScreenRatio();
        mScreenRatio = Double.compare(ratio,0) == 0 ? DEFAULT_SCREEN_RATIO : ratio;
    }

    public GalleryAdapter(Context context, List<ImageRealm> imgList){
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageList = imgList;
        mPicasso = Picasso.with(context);
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = mInflater.inflate(R.layout.item_photo_layout,parent,false);

        return new PhotoViewHolder(root);
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder vh, int position) {
        Log.v(TAG,"onBindViewHolder(): view position = " + position);
        String url = mCallback.getItemAtPos(position).getUrl();

        if(!TextUtils.isEmpty(url)) {
            mPicasso.load(url)
                    .resize(DEFAULT_IMG_RESOLUTION, (int) (DEFAULT_IMG_RESOLUTION * mScreenRatio))
                    .centerCrop()
                    .into(new ImageTarget(vh.img,position));
            vh.setPosition(position);
        }else if(getItemCount() > 0){
            // remove from the list
            RealmHelper.getInstance().delete(mCallback.getItemAtPos(position));
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
        public @BindView(R.id.iv_photo) ImageView img;
        private int mPos = -1;

        public PhotoViewHolder(View root) {
            super(root);

            ButterKnife.bind(this,root);

            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mPos != -1 && mCallback != null){
                        mCallback.onItemClick(mPos);
                    }
                }
            });
        }

        public void setPosition(int pos){
            mPos = pos;
        }
    }

    private class ImageTarget implements Target{

        private ImageView mImg;
        private int mPos;

        public ImageTarget(ImageView iv, int pos){
            mImg = iv;
            mPos = pos;
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
            mCallback.removeItemAtPos(mPos);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }
}
