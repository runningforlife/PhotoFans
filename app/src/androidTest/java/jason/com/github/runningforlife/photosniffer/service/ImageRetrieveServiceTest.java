package jason.com.github.runningforlife.photosniffer.service;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.github.runningforlife.photosniffer.service.ImageRetrieveService;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;

/**
 * ref: http://www.vogella.com/tutorials/AndroidTesting/article.html
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ImageRetrieveServiceTest {

    @Rule
    public ServiceTestRule serviceRule = new ServiceTestRule();

    @Test
    public void startService(){
        Intent intent = new Intent(InstrumentationRegistry.getTargetContext(),
                ImageRetrieveService.class);
        try {
            serviceRule.startService(intent);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }


}
