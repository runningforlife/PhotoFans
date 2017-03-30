package jason.github.com.photofans.crawler.processor;

import android.support.test.runner.AndroidJUnit4;
import android.test.mock.MockContext;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.realm.Realm;
import jason.github.com.photofans.crawler.OkHttpDownloader;
import us.codecraft.webmagic.Spider;

/**
 * test image retrieve
 */

@RunWith(AndroidJUnit4.class)
public class ImageRetrieveTest {

    private final static String URL_FREE_JPG = "http://en.freejpg.com.ar/free/images/";
    private final static String URL_PIXELS = "https://www.pexels.com/";
    private final static String URL_ALBUM = "http://albumarium.com/";

    @Before
    public void initRealm(){
/*        MockContext context = new MockContext();
        Realm.init(context);*/
    }

    @Test
    public void downloadPages(){
        ImageRetrievePageProcessor processor = new ImageRetrievePageProcessor(20);
        Spider.create(processor)
                .setDownloader(new OkHttpDownloader())
                .addUrl(URL_FREE_JPG);
    }
}
