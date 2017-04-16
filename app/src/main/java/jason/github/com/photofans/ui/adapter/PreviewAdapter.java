package jason.github.com.photofans.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import jason.github.com.photofans.R;
import jason.github.com.photofans.loader.PicassoLoader;

/**
 * preview adapter to show list of pictures
 */

public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.ImageViewHolder> {
    private static final String TAG = "PreviewAdapter";

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

        holder.setPosition(position);
        PicassoLoader.load(mContext,holder.preview,
                mCallback.getItemAtPos(position).getUrl(),150,150);
    }

    @Override
    public int getItemCount() {
        return mCallback.getCount();
    }

    final class ImageViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.iv_preview) ImageView preview;
        int pos = -1;

        public ImageViewHolder(View root) {
            super(root);

            ButterKnife.bind(this,root);

            root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(pos > -1) {
                        mCallback.onItemClicked(pos);
                    }
                }
            });
        }

        void setPosition(int pos){
            this.pos = pos;
        }
    }
}
