package jason.com.github.runningforlife.photosniffer.cloud;

import android.support.test.runner.AndroidJUnit4;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.SaveCallback;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
/**
 * Created by jason on 6/12/17.
 */

@RunWith(AndroidJUnit4.class)

public class LeanCloudApiTest {

    @Test
    public void testUpload(){
        final AVObject object = new AVObject("test");
        object.put("date", new Date());
        object.put("content", "something is happening");

        object.saveInBackground(new SaveCallback() {
            @Override
            public void done(AVException e) {
                assertNull(e);
                assertNotNull(object.getObjectId());
            }
        });
    }

}
