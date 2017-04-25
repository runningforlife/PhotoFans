package jason.github.com.photofans.ui.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import jason.github.com.photofans.R;
import jason.github.com.photofans.loader.GlideLoader;
import jason.github.com.photofans.loader.GlideLoaderListener;
import jason.github.com.photofans.loader.PicassoLoader;

/**
 * preview adapter to show list of pictures
 */

public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.ImageViewHolder> {
    private static final String TAG = "PreviewAdapter";

    private Context mContext;
    private ImageAdapterCallback mCallback;
    private HashMap<ImageView,Boolean> mChecked;
    // the last checked image view
    private ImageView mLastChecked;

    public PreviewAdapter(Context context, ImageAdapterCallback callback){
        mContext = context;
        mCallback = callback;
        mChecked = new HashMap<>();
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

/*        PicassoLoader.load(mContext,holder.preview,mCallback.getItemAtPos(position).getUrl(),
                150,150);*/
        GlideLoader.load(mContext,mCallback.getItemAtPos(position).getUrl(),
                holder.preview,150,150);
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
                public void onClick(View v) {
                    mCallback.onItemClicked(getAdapterPosition());

                    if(preview != mLastChecked){
                        preview.setBackgroundResource(R.drawable.rect_image_preview);
                        mLastChecked = preview;
                    }else{
                        // remove background
                        mLastChecked.setBackground(null);
                    }

/*                    mChecked.put(preview,Boolean.TRUE);
                    Iterator iterator = mChecked.entrySet().iterator();
                    while(iterator.hasNext()) {
                        Map.Entry entry = (Map.Entry)iterator.next();
                        ImageView view = (ImageView)entry.getKey();
                        if(view == preview){
                            preview.setBackgroundResource(R.drawable.rect_image_preview);
                        }else{
                            // remove background
                            preview.setBackground(null);
                        }
                    }*/
                }
            });
        }

    }
}
