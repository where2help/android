package app.iamin.iamin;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Markus on 16.01.16.
 */
public class Where2Help extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
    }
}
