package jason.github.com.photofans.loader;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;

import jason.github.com.photofans.R;

/**
 * image loader using Glide
 */

public class GlideLoader {

    public static void load(Context context, RequestListener<String,GlideDrawable> listener, String url, int w, int h) {
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_android_black_150dp)
                .error(R.drawable.ic_mood_bad_grey_24dp)
                .listener(listener)
                .crossFade()
                .into(w,h);
    }

    public static void load(Context context, String url, ImageView target, int w, int h){
        Glide.with(context)
             .load(url)
             .listener(new GlideLoaderListener(target))
             .placeholder(R.drawable.ic_android_black_150dp)
             .error(R.drawable.ic_mood_bad_grey_24dp)
             .centerCrop()
             .crossFade()
             .into(w,h);
    }
}
