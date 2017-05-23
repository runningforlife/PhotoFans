package jason.com.github.runningforlife.photofans.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import com.github.runningforlife.photofans.R;
import com.github.runningforlife.photofans.utils.SharedPrefUtil;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.*;
/**
 * Created by jason on 5/23/17.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class SharedPrefUtilTest {

    Context context = InstrumentationRegistry.getTargetContext();

    @Mock
    SharedPreferences.Editor editor;

    @Before
    public void setup(){
        editor = PreferenceManager.getDefaultSharedPreferences(context).edit();

        editor.putBoolean(context.getString(R.string.pref_wifi_download),true);
        editor.putInt(context.getString(R.string.pref_max_reserved_images),100);
        editor.apply();
    }

    @Test
    public void testWifiDownloadPref(){
        String key = context.getString(R.string.pref_wifi_download);
        assertNotNull("checking wifi download key",key);

        boolean isWifiMode = SharedPrefUtil.getWifiDownloadMode(context,key);
        assertEquals("checking wifi download mode",isWifiMode,true);
    }

    @Test
    public void testMaxReservedPref(){
        String key = context.getString(R.string.pref_max_reserved_images);
        assertNotNull("checking max reserved images", key);

        int val = SharedPrefUtil.getMaxReservedImages(context,key,101);
        assertNotEquals("checking max reserved images number",val,101);
    }

}
