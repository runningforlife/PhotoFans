package jason.github.com.photofans.crawler;

/*
* a factory class to generate http client　<br>
* @author JasonWang
* @date 2017-3-12
*/

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.proxy.Proxy;

public class OkHttpProxy {

    private static final int CONNECT_TIMEOUT = 5000;

    private final static OkHttpClient.Builder builder = new OkHttpClient.Builder();

    protected static OkHttpClient getClient(Site site, Proxy proxy){
        OkHttpClient client = builder
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
                //.readTimeout(site.getTimeOut(), TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();
        return client;
    }
}
