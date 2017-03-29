package jason.github.com.photofans.crawler;

import android.util.Log;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.selector.PlainText;

import okhttp3.Request;

/**
 * Created by jason on 3/12/17.
 */

/*
 * a http downloader implemented by OkHttp3 <br>
 * @author JasonWang <br>
 * @since 2017-3-12
 *
 * and start crawl it for next time
 */

public class OkHttpDownloader extends AbstractDownloader {
    private static final String LOG_TAG = "OkHttpDownloader";

    private final static String REG_IMAGES = "(?m)(?s)<img\\s+(.*)src\\s*=\\s*\"([^\"]+)\"(.*)(\\.(gif|jpg|png))$";

    @Override
    public Page download(us.codecraft.webmagic.Request request, Task task) {
        Log.v(LOG_TAG,"download(): url = " + request.getUrl());

        if(isPossibleImageUrl(request.getUrl())){
            Page page = new Page();
            page.setRawText("");
            // bad request
            page.setStatusCode(400);
            return page;
        }

        Site site = null;
        if (task != null) {
            site = task.getSite();
        }

        int statusCode = 0;

        Request httpRequest = new Request.Builder()
                .url(request.getUrl())
                .build();

        try{
            Response response = handleRequest(site,httpRequest);
            statusCode = response.code();

            Log.v(LOG_TAG,"download(): status code = " + statusCode);

            if(response.isSuccessful()){
                onSuccess(request);
                request.putExtra(request.STATUS_CODE, statusCode);

                return handleResponse(request,response);
            }else{
                onError(request);
            }

        }catch(IOException e){
            e.printStackTrace();
            if(site.getCycleRetryTimes() > 0){
                return addToCycleRetry(request,site);
            }
        }

        onError(request);

        return null;
    }

    @Override
    public void onError(us.codecraft.webmagic.Request request){
        Log.v(LOG_TAG,"onError(): url = " + request.getUrl());
    }

    @Override
    public void setThread(int threadNum) {
    }

    private Response handleRequest(Site site,Request req) throws IOException{
        OkHttpClient client = OkHttpProxy.getClient(site, null);

        return client.newCall(req).execute();
    }

    private Page handleResponse(us.codecraft.webmagic.Request request, Response resp){
        String content;
        Page page = new Page();

        try {
            content = resp.body().string();

            Log.v(LOG_TAG,"handleResponse(): size = " + content.getBytes().length);

            page.setRawText(content);
            page.setRequest(request);
            page.setUrl(new PlainText(request.getUrl()));
            page.setStatusCode(resp.code());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return page;
    }

    private boolean isPossibleImageUrl(String url){
        Pattern pattern = Pattern.compile(REG_IMAGES);
        Matcher matcher = pattern.matcher(url);

        return matcher.matches();
    }
}
