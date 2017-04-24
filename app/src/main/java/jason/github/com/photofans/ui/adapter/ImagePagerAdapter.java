package jason.github.com.photofans.ui.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import jason.github.com.photofans.R;
import jason.github.com.photofans.loader.GlideLoader;
import jason.github.com.photofans.loader.GlideLoaderListener;
import jason.github.com.photofans.loader.PicassoLoader;
import jason.github.com.photofans.utils.DisplayUtil;

/**
 * image pager adapter
 */

public class ImagePagerAdapter extends PagerAdapter{
    private static final String TAG = "ImagePageAdapter";

    private static final int DEFAULT_WIDTH = 1024;
    private static final int DEFAULT_HEIGHT = (int)(DEFAULT_WIDTH*DisplayUtil.getScreenRatio());
    private static final int MAX_HEIGHT = (int)(DEFAULT_WIDTH*1.5*DisplayUtil.getScreenRatio());

    private Context mContext;
    private ImageAdapterCallback mCallback;

    public ImagePagerAdapter(Context context, ImageAdapterCallback callback){
        mCallback = callback;
        mContext = context;
    }

    @Override
    public int getCount() {
        return mCallback.getCount();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup parent, int position){
        Log.v(TAG,"instantiateItem(): position = " + position);

        ImageView iv = (ImageView) LayoutInflater.from(mContext)
                .inflate(R.layout.item_image_detail,parent,false);

        //iv.setMaxHeight(MAX_HEIGHT);
/*        PicassoLoader.load(mContext,iv,mCallback.getItemAtPos(position).getUrl(),
                DEFAULT_WIDTH,DEFAULT_HEIGHT);*/
        GlideLoader.load(mContext,new GlideLoaderListener(iv),mCallback.getItemAtPos(position).getUrl(),
                DEFAULT_WIDTH,DEFAULT_HEIGHT);
        //GlideLoader.load(mContext,mCallback.getItemAtPos(position).getUrl(),iv);
        parent.addView(iv);

        return iv;
    }

    @Override
    public void destroyItem(ViewGroup parent,int position, Object object){
        parent.removeView((ImageView)object);
    }
}
