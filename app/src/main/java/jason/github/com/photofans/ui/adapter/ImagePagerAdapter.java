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
import jason.github.com.photofans.loader.PicassoLoader;
import jason.github.com.photofans.utils.DisplayUtil;

/**
 * image pager adapter
 */

public class ImagePagerAdapter extends PagerAdapter{
    private static final String TAG = "ImagePageAdapter";

    private static final int DEFAULT_WIDTH = 1024;
    private Context mContext;
    private ImageAdapterCallback mCallback;
    private int mPageHeight;

    public ImagePagerAdapter(Context context, ImageAdapterCallback callback){
        mCallback = callback;
        mContext = context;
    }

    public void setPageHeight(int h){
        Log.v(TAG,"setPageHeight(): page height = " + h);
        if(h != 0){
            mPageHeight = DisplayUtil.getScreenDimen().widthPixels/h * DEFAULT_WIDTH;
        }
        //mPageHeight = (DisplayUtil.getScreenDimen().widthPixels/h*DEFAULT_WIDTH);
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
        // get the height of the page
        if(mPageHeight == 0){
            int h = parent.getMeasuredHeight();
            mPageHeight = DEFAULT_WIDTH*DisplayUtil.getScreenDimen().widthPixels/h;
            Log.v(TAG,"instantiateItem(): page height = " + mPageHeight);
        }

        ImageView iv = (ImageView) LayoutInflater.from(mContext)
                .inflate(R.layout.item_image_detail,parent,false);

        PicassoLoader.load(mContext,iv,mCallback.getItemAtPos(position).getUrl(),
                DEFAULT_WIDTH,mPageHeight);

        parent.addView(iv);

        return iv;
    }

    @Override
    public void destroyItem(ViewGroup parent,int position, Object object){
        parent.removeView((ImageView)object);
    }
}
