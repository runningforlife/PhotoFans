package jason.github.com.photofans.loader;

import android.content.Context;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import jason.github.com.photofans.R;
import jason.github.com.photofans.ui.adapter.GalleryAdapter;

/**
 * user picasso to load images
 */

public class PicassoLoader{

    public static void load(Context context, ImageView iv, String url, int w, int h) {
        Picasso.with(context)
                .load(url)
                .placeholder(R.drawable.ic_android_black_150dp)
                .resize(w,h)
                .centerCrop()
                .resize(w,h)
                .into(iv);
    }

    public static void load(Context context, Target target, String url, int w, int h){
        Picasso.with(context)
                .load(url)
                .placeholder(R.drawable.ic_android_black_150dp)
                .resize(w,h)
                .centerCrop()
                .into(target);
    }
}
