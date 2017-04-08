package jason.github.com.photofans.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import jason.github.com.photofans.R;
import jason.github.com.photofans.model.ImageRealm;

/**
 * Created by jason on 4/6/17.
 */

public class ImagePreviewAdapter extends BaseAdapter {
    private static final String TAG = "ImagePreviewAdapter";

    private Context mContext;
    private PreviewCallback mCallback;

    public ImagePreviewAdapter(Context context, PreviewCallback callback){
        mContext = context;
        mCallback = callback;
    }

    public interface PreviewCallback{
        int getCount();
        ImageRealm getItemAtPos(int pos);
        void onItemClicked(int pos);
    }

    @Override
    public int getCount() {
        return mCallback.getCount();
    }

    @Override
    public Object getItem(int position) {
        return mCallback.getItemAtPos(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.v(TAG,"getView(): position = " + position);
        View root = convertView;
        if(root == null){
            root = LayoutInflater.from(mContext)
                    .inflate(R.layout.item_image_preview,parent,false);
        }

        final ImageView iv = (ImageView)root.findViewById(R.id.iv_preview);
        String url = mCallback.getItemAtPos(position).getUrl();

        Picasso.with(mContext)
                .load(url)
                .centerCrop()
                .resize(120,120)
                .into(iv);

        // add click listener
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv.setBackgroundResource(R.drawable.rect_image_preview);
                mCallback.onItemClicked(position);
            }
        });

        return root;
    }
}
