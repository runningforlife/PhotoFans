package jason.com.github.runningforlife.photosniffer.crawler.processor;

import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.github.runningforlife.photosniffer.crawler.OkHttpDownloader;
import com.github.runningforlife.photosniffer.crawler.processor.ImageRetrievePageProcessor;

import us.codecraft.webmagic.Spider;

import static com.github.runningforlife.photosniffer.crawler.processor.ImageSource.URL_MM;

/**
 * test image retrieveImages
 */

@RunWith(AndroidJUnit4.class)
public class ImageRetrieveTest {

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
                .addUrl(URL_MM)
                .run();
    }
}
