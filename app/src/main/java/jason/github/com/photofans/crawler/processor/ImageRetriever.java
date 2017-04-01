package jason.github.com.photofans.crawler.processor;

import java.util.List;

import jason.github.com.photofans.model.ImageRealm;
import us.codecraft.webmagic.Page;

/**
 * retrieve images from a given page
 */

public interface ImageRetriever {
    /*
     * retrieve images from a given page
     *
     * @param Page: downloaded pages
     * @return List<String> : a list of retrieved image urls
     */
    List<ImageRealm> retrieveImages(Page page);
}
