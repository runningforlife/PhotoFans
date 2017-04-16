package jason.github.com.photofans.loader;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import jason.github.com.photofans.R;

/**
 * image loader using Glide
 */

public class GlideLoader {

    public static void load(Context context, ImageView iv, String url, int w, int h) {
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.ic_android_black_150dp)
                .centerCrop()
                .into(iv);
    }
}
