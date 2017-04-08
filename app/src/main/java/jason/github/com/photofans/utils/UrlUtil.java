package jason.github.com.photofans.utils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * an utility class to process URLs
 */

public class UrlUtil {

    // get a root url of the give url
    public static String getRootUrl(String url) throws MalformedURLException{
        URL absUrl = new URL(url);
        String baseUrl = absUrl.getProtocol() + "://" + absUrl.getAuthority();

        return baseUrl;
    }
}
