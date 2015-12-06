package app.iamin.iamin.data.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import app.iamin.iamin.R;
import app.iamin.iamin.data.DataManager;
import app.iamin.iamin.data.api.AuthService;
import app.iamin.iamin.data.api.BookingsService;
import app.iamin.iamin.data.api.NeedsService;
import app.iamin.iamin.data.event.DataResultEvent;

/**
 * Created by Markus on 09.11.15.
 */
public class DataService extends IntentService {
    private static final String TAG = UtilityService.class.getSimpleName();

    public static final String ACTION_SIGN_UP = "where2help_sign_up";
    public static final String ACTION_SIGN_IN = "where2help_sign_in";
    public static final String ACTION_SIGN_OUT = "where2help_sign_out";
    public static final String ACTION_REQUEST_NEEDS = "where2help_request_needs";
    public static final String ACTION_REQUEST_BOOKINGS = "where2help_request_bookings";
    public static final String ACTION_CREATE_BOOKING = "where2help_create_booking";
    public static final String ACTION_CANCEL_BOOKING = "where2help_cancel_booking";

    public static final String EXTRA_EMAIL = "extra_email";
    public static final String EXTRA_PASSWORD = "extra_password";
    public static final String EXTRA_PASSWORD_CONF = "extra_password_conf";
    public static final String EXTRA_NEED_ID = "extra_need_id";
    public static final String EXTRA_VOLUNTEERING_ID = "extra_volunteering_id";


    public static void signUp(Context context, String email, String password, String passwordConf) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_SIGN_UP);
        intent.putExtra(EXTRA_EMAIL, email);
        intent.putExtra(EXTRA_PASSWORD, password);
        intent.putExtra(EXTRA_PASSWORD_CONF, passwordConf);
        context.startService(intent);
    }

    public static void signIn(Context context, String email, String password) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_SIGN_IN);
        intent.putExtra(EXTRA_EMAIL, email);
        intent.putExtra(EXTRA_PASSWORD, password);
        context.startService(intent);
    }

    public static void signOut(Context context) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_SIGN_OUT);
        context.startService(intent);
    }

    public static void requestNeeds(Context context) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_REQUEST_NEEDS);
        context.startService(intent);
    }

    public static void requestBookings(Context context) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_REQUEST_BOOKINGS);
        context.startService(intent);
    }

    public static void createBooking(Context context, int needId) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_CREATE_BOOKING);
        intent.putExtra(EXTRA_NEED_ID, needId);
        context.startService(intent);
    }

    public static void cancelBooking(Context context, int needId, int volunteeringId) {
        Intent intent = new Intent(context, DataService.class);
        intent.setAction(ACTION_CANCEL_BOOKING);
        intent.putExtra(EXTRA_NEED_ID, needId);
        intent.putExtra(EXTRA_VOLUNTEERING_ID, volunteeringId);
        context.startService(intent);
    }

    public DataService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isOnline(intent)) return;
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_SIGN_UP.equals(action)) {
            postResultFrom(intent, AuthService.signUp(this, intent));
        } else if (ACTION_SIGN_IN.equals(action)) {
            postResultFrom(intent, AuthService.signIn(this, intent));
        } else if (ACTION_SIGN_OUT.equals(action)) {
            postResultFrom(intent, AuthService.signOut(this));
        } else if (ACTION_REQUEST_NEEDS.equals(action)) {
            postResultFrom(intent, NeedsService.requestNeeds(this));
        } else if (ACTION_REQUEST_BOOKINGS.equals(action)) {
            postResultFrom(intent, BookingsService.requestBookings(this));
        } else if (ACTION_CREATE_BOOKING.equals(action)) {
            postResultFrom(intent,BookingsService.createBooking(this, intent));
        } else if (ACTION_CANCEL_BOOKING.equals(action)) {
            postResultFrom(intent, BookingsService.cancelBooking(this, intent));
        }
    }

    private void postResultFrom(Intent intent, String error) {
        int id = intent.getIntExtra(EXTRA_NEED_ID, -1);
        DataManager.getInstance().onDataResult(new DataResultEvent(intent.getAction(), id, error));
    }

    private boolean isOnline(Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) return true; // continue
        postResultFrom(intent, getString(R.string.error_no_connection));
        return false;
    }
}
