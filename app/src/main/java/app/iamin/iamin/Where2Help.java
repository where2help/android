package app.iamin.iamin;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Markus on 16.01.16.
 */
public class Where2Help extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        LeakCanary.install(this);
        Realm.setDefaultConfiguration(getRealmConfiguration());
    }

    /**
     * Default RealmConfiguration with RealmMigration.
     */
    private RealmConfiguration getRealmConfiguration() {
        return new RealmConfiguration.Builder(this)
                .deleteRealmIfMigrationNeeded()
                .build();
    }
}
