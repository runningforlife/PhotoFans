package jason.github.com.photofans.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jason.github.com.photofans.R;
import jason.github.com.photofans.model.ImageRealm;
import jason.github.com.photofans.utils.DisplayUtil;

/**
 * a gallery adapter to bind image data to recycleview
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.PhotoViewHolder>{
    private static final String TAG = "GalleryAdapter";

    private static final int DEFAULT_IMG_RESOLUTION = 512;
    private static final double DEFAULT_SCREEN_RATIO = 0.75;

    private static final int MB = 1024*1024;
    private static final int MEM_CACHE_SIZE = 3*MB;
    private static final int DISK_CACHE_SIZE = 50*MB;
    @SuppressWarnings("unchecked")
    private List<ImageRealm> mImageList = Collections.EMPTY_LIST;
    private LayoutInflater mInflater;
    private ItemClickListener mListener;
    private Picasso mPicasso;

    public interface ItemClickListener{
        void onItemClick(int pos);
    }

    public GalleryAdapter(Context context){
        mInflater = LayoutInflater.from(context);
        mPicasso = Picasso.with(context);
    }

    public GalleryAdapter(Context context, List<ImageRealm> imgList){
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mImageList = imgList;
        mPicasso = Picasso.with(context);
    }

    public void setImageList(List<ImageRealm> imgList){
        mImageList = imgList;
    }

    @Override
    public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View root = mInflater.inflate(R.layout.item_photo_layout,parent,false);

        return new PhotoViewHolder(root);
    }

    @Override
    public void onBindViewHolder(PhotoViewHolder vh, int position) {
        Log.v(TAG,"onBindViewHolder(): view position = " + position);
        // different device panel size may need different width and height
        double ratio = DisplayUtil.getScreenRatio();
        ratio = Double.compare(ratio,0) == 0 ? DEFAULT_SCREEN_RATIO : ratio;
        mPicasso.load(mImageList.get(position).getUrl())
                .resize(DEFAULT_IMG_RESOLUTION, (int) (DEFAULT_IMG_RESOLUTION*ratio))
                .centerCrop()
                .into(vh.img);
        vh.setPosition(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return mImageList.size();
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
                    if(mPos != -1 && mListener != null){
                        mListener.onItemClick(mPos);
                    }
                }
            });
        }

        public void setPosition(int pos){
            mPos = pos;
        }
    }
}
