package com.github.runningforlife.photosniffer.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * user picasso to load images
 */

public class PicassoLoader{
    private static final String TAG = "PicassoLoader";

    public static void load(Context context, Target target, String url) {
        Picasso.with(context)
                .load(url)
                .config(Bitmap.Config.ARGB_8888)
                .into(target);
    }

    public static void load(Context context, Target target, String url, int w, int h){
        Picasso.Builder builder = new Picasso.Builder(context);
        Picasso picasso = builder.listener(new ErrorListener()).build();

        picasso.load(url)
                .centerCrop()
                .resize(w,h)
                .into(target);
    }

    private static class ErrorListener implements Picasso.Listener{

        @Override
        public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
            Log.v("PicassoLoader","onImageLoadFailed(): url = " + uri + ", error = " + exception);
        }
    }

}
