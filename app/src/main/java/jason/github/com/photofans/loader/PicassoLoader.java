package jason.github.com.photofans.loader;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
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
                .into(iv);
    }

    public static void load(Context context, Target target, String url, int w, int h){
        Picasso.Builder builder = new Picasso.Builder(context);
        Picasso picasso = builder.listener(new ErrorListener()).build();

        picasso.load(url)
                .placeholder(R.drawable.ic_android_black_150dp)
                .error(R.drawable.ic_mood_bad_grey_24dp)
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
