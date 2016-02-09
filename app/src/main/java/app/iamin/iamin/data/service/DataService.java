package app.iamin.iamin.data.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import app.iamin.iamin.R;
import app.iamin.iamin.data.DataManager;
import app.iamin.iamin.data.api.AuthHandler;
import app.iamin.iamin.data.api.BookingHandler;
import app.iamin.iamin.data.api.NeedHandler;
import app.iamin.iamin.util.LogUtils;
import okhttp3.OkHttpClient;

import static app.iamin.iamin.data.DataManager.ACTION_CANCEL_BOOKING;
import static app.iamin.iamin.data.DataManager.ACTION_CREATE_BOOKING;
import static app.iamin.iamin.data.DataManager.ACTION_REQUEST_BOOKINGS;
import static app.iamin.iamin.data.DataManager.ACTION_REQUEST_NEEDS;
import static app.iamin.iamin.data.DataManager.ACTION_SIGN_IN;
import static app.iamin.iamin.data.DataManager.ACTION_SIGN_OUT;
import static app.iamin.iamin.data.DataManager.ACTION_SIGN_UP;

/**
 * Created by Markus on 09.11.15.
 */
public class DataService extends Service {
    private static final String TAG = DataService.class.getSimpleName();

    private static final Handler MAIN_THREAD = new Handler(Looper.getMainLooper());

    private boolean running;

    private DataManager dataManager;

    private OkHttpClient client;

    @Override
    public void onCreate() {
        super.onCreate();
        dataManager = DataManager.getInstance(this);
        client = dataManager.getClient();
        Log.i(TAG, "Service starting!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        executeNext();
        return START_STICKY;
    }

    private void executeNext() {
        if (running) return; // Only one task at a time.

        Intent intent = dataManager.getQueue().peek();
        if (intent != null) {
            running = true;
            execute(intent);
        } else {
            Log.i(TAG, "Service stopping!");
            stopSelf(); // No more tasks are present. Stop.
        }
    }

    private void execute(final Intent intent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!isOnline(intent)) return;
                Context context = getApplicationContext();
                String action = intent != null ? intent.getAction() : null;
                if (ACTION_SIGN_UP.equals(action)) {
                    post(intent, AuthHandler.signUp(context, client, intent));
                } else if (ACTION_SIGN_IN.equals(action)) {
                    post(intent, AuthHandler.signIn(context, client, intent));
                } else if (ACTION_SIGN_OUT.equals(action)) {
                    post(intent, AuthHandler.signOut(context, client));
                } else if (ACTION_REQUEST_NEEDS.equals(action)) {
                    post(intent, NeedHandler.requestNeeds(context, client));
                } else if (ACTION_REQUEST_BOOKINGS.equals(action)) {
                    post(intent, BookingHandler.requestBookings(context, client));
                } else if (ACTION_CREATE_BOOKING.equals(action)) {
                    post(intent, BookingHandler.createBooking(context, client, intent));
                } else if (ACTION_CANCEL_BOOKING.equals(action)) {
                    post(intent, BookingHandler.cancelBooking(context, client, intent));
                }
            }
        }).start();
    }

    private void post(final Intent intent, final String error) {
        MAIN_THREAD.post(new Runnable() {
            @Override
            public void run() {
                running = false;
                dataManager.getQueue().remove();
                dataManager.onServiceResult(intent, error);
                LogUtils.logQueue(TAG, dataManager.getQueue());
                if (!dataManager.isPaused()) executeNext();
            }
        });
    }

    private boolean isOnline(Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) return true; // continue
        post(intent, getString(R.string.error_no_connection));
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
