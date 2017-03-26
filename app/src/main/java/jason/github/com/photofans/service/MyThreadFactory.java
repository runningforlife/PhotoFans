package jason.github.com.photofans.service;

import java.util.concurrent.ThreadFactory;

/**
 * Created by jason on 3/26/17.
 */

public class MyThreadFactory implements ThreadFactory{

    private static class InstanceHolder{
        private static MyThreadFactory instance = new MyThreadFactory();
    }

    public static MyThreadFactory getInstance(){
        return InstanceHolder.instance;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r);
    }
}
