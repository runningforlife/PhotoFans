package jason.github.com.photofans.crawler.processor;

import android.content.RestrictionEntry;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * source to retrieve images
 */

public interface ImageSource {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({URL_FREE_JPG,URL_PIXELS,URL_ALBUM,URL_ILLUSION,
            URL_VISUAL_HUNT,URL_1X,URL_PIXBABY,URL_PUBLIC_ARCHIVE,
            URL_VISUAL_CHINA})
    @interface IMAGE_SOURCE{}
    String URL_FREE_JPG = "http://en.freejpg.com.ar";
    String URL_PIXELS = "https://www.pexels.com";
    String URL_ALBUM = "http://albumarium.com";
    String URL_ILLUSION = "http://illusion.scene360.com";
    String URL_VISUAL_HUNT = "https://visualhunt.com";
    String URL_1X = "https://1x.com";
    String URL_PIXBABY = "https://pixabay.com";
    String URL_PUBLIC_ARCHIVE = "http://publicdomainarchive.com";
    String URL_VISUAL_CHINA = "http://www.vcg.com";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({REG_FREE_JPG,REG_PIXELS,REG_ALBUM,REG_ILLUSION,
            REG_VISUAL_HUNG,REG_1X,REG_PEXBABY,REG_PUBLIC_ARCHIVE,
            REG_VISUAL_CHINA})
    @interface REG_RETRIEVER{}

    String REG_FREE_JPG = "img[src$=.jpg]";
    String REG_PIXELS = "img";
    String REG_ALBUM = "img";
    String REG_ILLUSION = "img";
    String REG_VISUAL_HUNG = "img[src$=.jpg]";
    String REG_1X = "img";
    String REG_PEXBABY = "img";
    String REG_PUBLIC_ARCHIVE = "img";
    String REG_VISUAL_CHINA = "img";
}
