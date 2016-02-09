package app.iamin.iamin.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.otto.Bus;

import java.util.LinkedList;

import app.iamin.iamin.data.api.BookingHandler;
import app.iamin.iamin.data.event.CancelBookingEvent;
import app.iamin.iamin.data.event.ConnectedEvent;
import app.iamin.iamin.data.event.CreateBookingEvent;
import app.iamin.iamin.data.event.DisconnectedEvent;
import app.iamin.iamin.data.event.ErrorEvent;
import app.iamin.iamin.data.event.PromptLoginEvent;
import app.iamin.iamin.data.event.RefreshEvent;
import app.iamin.iamin.data.event.RequestBookingsEvent;
import app.iamin.iamin.data.event.RequestNeedsEvent;
import app.iamin.iamin.data.event.UserSignInEvent;
import app.iamin.iamin.data.event.UserSignOutEvent;
import app.iamin.iamin.data.event.UserSignUpEvent;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.data.model.User;
import app.iamin.iamin.data.service.DataService;
import app.iamin.iamin.util.DataUtils;

import okhttp3.Headers;
import okhttp3.OkHttpClient;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

/**
 * Created by Markus on 11.11.15.
 */
public class DataManager {

    private static final String TAG = "DataManager";

    public static final int ON_START = 0;
    public static final int ON_NEXT = 1;
    public static final int ON_ERROR = 2;

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
    public static final String EXTRA_RETRY_COUNT = "extra_retry_count";

    private static final String ERROR_UNAUTHORIZED = "401";
    private static final String ERROR_UNCHANGED = "422";

    private Context mContext;

    private IntentFilter connectivityFilter = new IntentFilter(CONNECTIVITY_ACTION);

    private NetworkReceiver networkReceiver = new NetworkReceiver();

    private OkHttpClient client = new OkHttpClient();

    private LinkedList<Intent> QUEUE = new LinkedList<>();

    private Bus BUS = BusProvider.getInstance();

    // Used to halt the queue for an in between action (e.g. prompted login)
    private boolean isPaused = false;

    private boolean hasUser = false;

    private boolean hasNeeds = false;

    private boolean hasBookings = false;

    private static volatile DataManager instance = null;

    public static synchronized DataManager getInstance(Context context) {
        if (instance == null) {
            instance = new DataManager(context.getApplicationContext());
        }
        return instance;
    }

    private DataManager(Context context) {
        mContext = context;
        hasUser = hasUserInternal();
    }

    /*
    * Call register to connect with where2help. This will start the NetworkReceiver which
    * will update local needs as well as the user's booking information if a network connection
    * is available, else disconnected() will be called.
    * */
    public void register() {
        mContext.registerReceiver(networkReceiver, connectivityFilter);
    }

    public void unregister() {
        mContext.unregisterReceiver(networkReceiver);
    }

    public void signUp(String email, String password, String passwordConf) {
        BUS.post(new UserSignUpEvent(ON_START, null));

        Intent intent = new Intent();
        intent.setAction(ACTION_SIGN_UP);
        intent.putExtra(EXTRA_EMAIL, email);
        intent.putExtra(EXTRA_PASSWORD, password);
        intent.putExtra(EXTRA_PASSWORD_CONF, passwordConf);
        intent.putExtra(EXTRA_RETRY_COUNT, 0);

        QUEUE.add(intent);
        startService();
    }

    public void signIn(String email, String password) {
        BUS.post(new UserSignInEvent(ON_START, null));

        Intent intent = new Intent();
        intent.setAction(ACTION_SIGN_IN);
        intent.putExtra(EXTRA_EMAIL, email);
        intent.putExtra(EXTRA_PASSWORD, password);
        intent.putExtra(EXTRA_RETRY_COUNT, 0);

        QUEUE.addFirst(intent); // addFirst() so auth pending requests can be completed.
        startService();
    }

    public void signOut() {
        BUS.post(new UserSignOutEvent(ON_START, null));

        Intent intent = new Intent();
        intent.setAction(ACTION_SIGN_OUT);
        intent.putExtra(EXTRA_RETRY_COUNT, 0);

        QUEUE.add(intent);
        startService();
    }

    public void requestNeeds() {
        BUS.post(new RequestNeedsEvent(ON_START, null));
        BUS.post(new RefreshEvent(true));

        Intent intent = new Intent();
        intent.setAction(ACTION_REQUEST_NEEDS);
        intent.putExtra(EXTRA_RETRY_COUNT, 0);

        QUEUE.add(intent);
        startService();
    }

    public void requestBookings() {
        BUS.post(new RequestBookingsEvent(ON_START, null));
        BUS.post(new RefreshEvent(true));

        Intent intent = new Intent();
        intent.setAction(ACTION_REQUEST_BOOKINGS);
        intent.putExtra(EXTRA_RETRY_COUNT, 0);

        QUEUE.add(intent);

        if (isAuthenticated()) {
            Log.d(TAG, "requestBookings");
            startService();
        } else {
            Log.d(TAG, "pendingRequest: ACTION_REQUEST_BOOKINGS");
            connect(false);
        }
    }

    public void createBooking(Need need) {
        BUS.post(new CreateBookingEvent(ON_START, need.getId(), null));

        Intent intent = new Intent();
        intent.setAction(ACTION_CREATE_BOOKING);
        intent.putExtra(EXTRA_NEED_ID, need.getId());
        intent.putExtra(EXTRA_RETRY_COUNT, 0);

        QUEUE.add(intent);

        if (isAuthenticated()) {
            startService();
        } else {
            Log.d(TAG, "pending on auth: ACTION_CREATE_BOOKING");
            connect(true);
        }
    }

    public void cancelBooking(Need need) {
        BUS.post(new CancelBookingEvent(ON_START, need.getId(), null));

        Intent intent = new Intent();
        intent.setAction(ACTION_CANCEL_BOOKING);
        intent.putExtra(EXTRA_NEED_ID, need.getId());
        intent.putExtra(EXTRA_VOLUNTEERING_ID, need.getVolunteeringId());
        intent.putExtra(EXTRA_RETRY_COUNT, 0);

        QUEUE.add(intent);

        if (isAuthenticated()) {
            startService();
        } else {
            Log.d(TAG, "pending on auth: ACTION_CANCEL_BOOKING");
            connect(false);
        }
    }

    private void connect(boolean promptLogin) {
        if (hasUser) {
            Log.d(TAG, "connect - hasUser");
            User user = DataUtils.getUser(mContext);
            signIn(user.getEmail(), user.getPassword());
        } else if (promptLogin) {
            Log.d(TAG, "connect - promptLogin");
            isPaused = true;
            BUS.post(new PromptLoginEvent());
        } else {
            Log.d(TAG, "connect - user not found");
            BUS.post(new ErrorEvent("User not found, need to login?"));
        }
    }

    private void startService() {
        mContext.startService(new Intent(mContext, DataService.class));
    }

    public void onServiceResult(Intent intent, String error) {
        String action = intent != null ? intent.getAction() : null;

        if (ACTION_SIGN_UP.equals(action)) {
            Log.d(TAG, "ACTION_SIGN_UP");
            handleSignUp(intent, error);
        } else if (ACTION_SIGN_IN.equals(action)) {
            Log.d(TAG, "ACTION_SIGN_IN");
            handleSignIn(intent, error);
        } else if (ACTION_SIGN_OUT.equals(action)) {
            Log.d(TAG, "ACTION_SIGN_OUT");
            handleSignOut(intent, error);
        } else if (ACTION_REQUEST_NEEDS.equals(action)) {
            Log.d(TAG, "ACTION_REQUEST_NEEDS");
            handleNeedsRequest(intent, error);
        } else if (ACTION_REQUEST_BOOKINGS.equals(action)) {
            Log.d(TAG, "ACTION_REQUEST_BOOKINGS");
            handleBookingsRequest(intent, error);
        } else if (ACTION_CREATE_BOOKING.equals(action)) {
            Log.d(TAG, "ACTION_CREATE_BOOKING");
            handleBookingCreation(intent, error);
        } else if (ACTION_CANCEL_BOOKING.equals(action)) {
            Log.d(TAG, "ACTION_CANCEL_BOOKING");
            handleBookingCancellation(intent, error);
        }
    }

    private void handleSignUp(Intent intent, String error) {
        if (error == null) {
            BUS.post(new UserSignUpEvent(ON_NEXT, null));
        } else {
            BUS.post(new UserSignUpEvent(ON_ERROR, error));
        }
    }

    private void handleSignIn(Intent intent, String error) {
        isPaused = false;

        if (error == null) {
            hasUser = true;
            BUS.post(new UserSignInEvent(ON_NEXT, null));
            requestBookings(); // Request bookings if needed
        } else {
            BUS.post(new UserSignInEvent(ON_ERROR, error));
        }
    }

    private void handleSignOut(Intent intent, String error) {
        hasUser = false;
        DataUtils.clearUser(mContext);
        BookingHandler.clearBookings(mContext);
        BUS.post(new UserSignOutEvent(ON_NEXT, null));
    }

    private void handleNeedsRequest(Intent intent, String error) {
        hasNeeds = error == null;

        if (error == null && hasUser) {
            requestBookings();
        } else if (error == null) {
            BUS.post(new RequestNeedsEvent(ON_NEXT, null));
            BUS.post(new RefreshEvent(false));
        } else {
            BUS.post(new RequestNeedsEvent(ON_ERROR, error));
            BUS.post(new RefreshEvent(false));
        }
    }

    private void handleBookingsRequest(Intent intent, String error) {
        hasBookings = error == null;

        if (error == null) {
            BUS.post(new RequestBookingsEvent(ON_NEXT, null));
            BUS.post(new RefreshEvent(false));
        } else if (error.equals(ERROR_UNAUTHORIZED)) {
            Log.d(TAG, "pendingRequest: ACTION_REQUEST_BOOKINGS");
            if (retry(intent)) {
                QUEUE.addFirst(intent);
                connect(true);
            } else {
                BUS.post(new RequestBookingsEvent(ON_ERROR, error));
                BUS.post(new RefreshEvent(false));
            }
        } else {
            BUS.post(new RequestBookingsEvent(ON_ERROR, error));
            BUS.post(new RefreshEvent(false));
        }
    }

    private void handleBookingCreation(Intent intent, String error) {
        int id = intent.getIntExtra(EXTRA_NEED_ID, 0);

        if (error == null || error.equals(ERROR_UNCHANGED)) {
            BUS.post(new CreateBookingEvent(ON_NEXT, id, null));
        } else if (error.equals(ERROR_UNAUTHORIZED)) {
            Log.d(TAG, "pendingRequest: ACTION_CREATE_BOOKINGS");
            if (retry(intent)) {
                QUEUE.addFirst(intent);
                connect(true);
            } else {
                BUS.post(new CreateBookingEvent(ON_ERROR, id, error));
            }
        } else {
            BUS.post(new CreateBookingEvent(ON_ERROR, id, error));
        }
    }

    private void handleBookingCancellation(Intent intent, String error) {
        int id = intent.getIntExtra(EXTRA_NEED_ID, 0);

        if (error == null || error.equals(ERROR_UNCHANGED)) {
            BUS.post(new CancelBookingEvent(ON_NEXT, id, null));
        } else if (error.equals(ERROR_UNAUTHORIZED)) {
            Log.d(TAG, "pendingRequest: ACTION_CANCEL_BOOKINGS");
            if (retry(intent)) {
                QUEUE.addFirst(intent);
                connect(true);
            } else {
                BUS.post(new CancelBookingEvent(ON_ERROR, id, error));
            }
        } else {
            BUS.post(new CancelBookingEvent(ON_ERROR, id, error));
        }
    }

    private boolean isAuthenticated() {
        User user = DataUtils.getUser(mContext);
        Headers headers = DataUtils.getHeaders(mContext);
        return (!TextUtils.isEmpty(user.getEmail()) &&
                !TextUtils.isEmpty(user.getPassword()) &&
                !TextUtils.isEmpty(headers.get("Access-Token")) &&
                !TextUtils.isEmpty(headers.get("Expiry")) &&
                System.currentTimeMillis() < (Long.parseLong(headers.get("Expiry")) * 1000L));
    }

    private boolean hasUserInternal() {
        User user = DataUtils.getUser(mContext);
        return (!TextUtils.isEmpty(user.getEmail()) &&
                !TextUtils.isEmpty(user.getPassword()));
    }

    private boolean retry(Intent intent) {
        int count = intent.getIntExtra(EXTRA_RETRY_COUNT, 0) + 1;
        if (count < 2) {
            intent.putExtra(EXTRA_RETRY_COUNT, count);
            return true;
        }
        return false;
    }

    public LinkedList<Intent> getQueue() {
        return QUEUE;
    }

    public OkHttpClient getClient() {
        return client;
    }

    public boolean hasUser() {
        return hasUser;
    }

    public boolean hasBookings() {
        return hasBookings;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public boolean isBooking(Need need) {
        if (QUEUE.isEmpty()) return false;

        for (Intent intent : QUEUE) {
            if (intent.getIntExtra(EXTRA_NEED_ID, 0) == need.getId() &&
                    (intent.getAction().equals(ACTION_CREATE_BOOKING) ||
                            intent.getAction().equals(ACTION_CANCEL_BOOKING))) {
                return true;
            }
        }
        return false;
    }

    public boolean isRefreshing() {
        if (QUEUE.isEmpty()) return false;

        for (Intent intent : QUEUE) {
            if (intent.getAction().equals(ACTION_REQUEST_NEEDS) ||
                            intent.getAction().equals(ACTION_REQUEST_BOOKINGS)) {
                return true;
            }
        }
        return false;
    }

    private class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context c, Intent i) {
            ConnectivityManager cm = (ConnectivityManager) c.getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo != null) {
                BUS.post(new ConnectedEvent());
                if (!hasNeeds) {
                    requestNeeds();
                }
            } else {
                BUS.post(new DisconnectedEvent());
            }
        }
    }
}