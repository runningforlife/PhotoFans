package jason.github.com.photofans.crawler.processor;

import android.content.RestrictionEntry;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * source to retrieve images
 */

public interface ImageSource {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({URL_FREE_JPG,URL_PIXELS,URL_ALBUM, URL_VISUAL_HUNT,
            URL_PIXBABY,URL_PUBLIC_ARCHIVE, URL_VISUAL_CHINA,URL_YOUWU,URL_MM})
    @interface IMAGE_SOURCE{}
    String URL_FREE_JPG = "http://en.freejpg.com.ar";
    String URL_PIXELS = "https://www.pexels.com";
    String URL_ALBUM = "http://albumarium.com";
    String URL_VISUAL_HUNT = "https://visualhunt.com";
    String URL_PIXBABY = "https://pixabay.com";
    String URL_PUBLIC_ARCHIVE = "http://publicdomainarchive.com";
    String URL_VISUAL_CHINA = "http://www.vcg.com";
    String URL_YOUWU = "http://www.youwu.cc";
    String URL_MM = "http://www.mmjpg.com";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({REG_FREE_JPG,REG_PIXELS,REG_ALBUM, REG_VISUAL_HUNG,REG_PEXBABY,REG_PUBLIC_ARCHIVE,
            REG_VISUAL_CHINA,REG_YOUWU})
    @interface REG_RETRIEVER{}

    String REG_FREE_JPG = "img[src$=.jpg]";
    String REG_PIXELS = "img"; // FIXME: still has problem
    String REG_ALBUM = "img";
    String REG_VISUAL_HUNG = "img[src$=.jpg]";
    String REG_PEXBABY = "img[src$=.jpg]";
    String REG_PUBLIC_ARCHIVE = "img[src$=.jpg]";
    String REG_VISUAL_CHINA = "img[src$=.jpg";
    String REG_YOUWU = "img[src$=.jpg]";
    String REG_MM = "img[src$=.jpg]";


    String PIXELS_IMAGE_START = "https://images.pexels.com/photos/";
    String ALBUM_IMAGE_START = "http://albumarium.com/media/";
    String PIXABAY_IMAGE_START = "https://cdn.pixabay.com/photo/";
    String PDN_IMAGE_START = "http://publicdomainarchive.com/wp-content/";
    String VC_IMAGE_START = "https//goss.vcg.com/html/images/";
    String YW_IMAGE_START = "http://www.youwu.cc/uploads/allimg/";
    String MM_IMAGE_START = "http://www.mmjpg.com/mm/";
    String FREEJPG_IMAGE_START = "http://en.freejpg.com.ar/asset/";
    String VH_IMAGE_START = "https://visualhunt.com/photos/";

    String[] ALL_URLS = {URL_FREE_JPG,URL_PIXELS,URL_ALBUM, URL_VISUAL_HUNT,
            URL_PIXBABY,URL_PUBLIC_ARCHIVE, URL_VISUAL_CHINA,URL_YOUWU,URL_MM};
}
