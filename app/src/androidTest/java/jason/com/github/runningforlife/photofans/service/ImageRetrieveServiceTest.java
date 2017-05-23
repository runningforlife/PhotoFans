package jason.com.github.runningforlife.photofans.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.core.deps.guava.net.InetAddresses;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.github.runningforlife.photofans.service.ImageRetrieveService;
import com.github.runningforlife.photofans.service.ServiceStatus;
import com.github.runningforlife.photofans.service.SimpleResultReceiver;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

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
