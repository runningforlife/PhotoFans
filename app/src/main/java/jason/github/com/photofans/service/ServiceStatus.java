package jason.github.com.photofans.service;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by jason on 3/19/17.
 */

public interface ServiceStatus {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({RUNNING,SUCCESS,ERROR})
    @interface STATUS {}

    int RUNNING = 0x01;
    int SUCCESS = 0x02;
    int ERROR = 0x03;
}
