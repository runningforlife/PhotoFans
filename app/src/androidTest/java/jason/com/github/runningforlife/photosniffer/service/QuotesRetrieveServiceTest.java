package jason.com.github.runningforlife.photosniffer.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.mock.MockContext;

import com.github.runningforlife.photosniffer.service.QuotesRetrieveService;
import com.github.runningforlife.photosniffer.service.ServiceStatus;
import com.github.runningforlife.photosniffer.service.SimpleResultReceiver;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.*;


import java.util.concurrent.TimeoutException;

/**
 * Created by jason on 8/27/17.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class QuotesRetrieveServiceTest {

    private SimpleResultReceiver receiver;

    @Rule
    public ServiceTestRule serviceTestRule = new ServiceTestRule();

    Context context;

    @Before
    public void setUp(){
        HandlerThread thread = new HandlerThread("QuotesRetrieveTest");
        thread.start();
        receiver = new SimpleResultReceiver(new Handler(thread.getLooper()));

        context = InstrumentationRegistry.getTargetContext();
    }

    @Test
    public void retrieveQuotesTest(){
        Intent intent = new Intent(context, QuotesRetrieveService.class);
        intent.putExtra("receiver", receiver);
        try {
            serviceTestRule.startService(intent);

            receiver.setReceiver(new SimpleResultReceiver.Receiver() {
                @Override
                public void onReceiveResult(int resultCode, Bundle data) {
                    switch (resultCode){
                        case ServiceStatus.RUNNING:
                            break;
                        case ServiceStatus.SUCCESS:
                            int quotes = data.getInt("result");
                            assertEquals("retrieve quotes success", true, quotes > 0);
                            break;
                        case ServiceStatus.ERROR:
                            break;
                        default:
                            break;
                    }
                }
            });

        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
