package app.iamin.iamin.data;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.Headers;
import com.squareup.otto.Bus;

import app.iamin.iamin.data.api.BookingsService;
import app.iamin.iamin.data.api.NeedsService;
import app.iamin.iamin.data.event.BookingCanceledEvent;
import app.iamin.iamin.data.event.BookingCreatedEvent;
import app.iamin.iamin.data.event.BookingsUpdatedEvent;
import app.iamin.iamin.data.event.ConnectedEvent;
import app.iamin.iamin.data.event.DataResultEvent;
import app.iamin.iamin.data.event.DisconnectedEvent;
import app.iamin.iamin.data.event.ErrorEvent;
import app.iamin.iamin.data.event.NeedFeedUpdatedEvent;
import app.iamin.iamin.data.event.NeedsUpdatedEvent;
import app.iamin.iamin.data.event.PromptLoginEvent;
import app.iamin.iamin.data.event.UserSignInEvent;
import app.iamin.iamin.data.event.UserSignOutEvent;
import app.iamin.iamin.data.event.UserSignUpEvent;
import app.iamin.iamin.data.model.User;
import app.iamin.iamin.data.service.DataService;
import app.iamin.iamin.ui.LoginActivity;
import app.iamin.iamin.util.DataUtils;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static app.iamin.iamin.data.service.DataService.ACTION_REQUEST_BOOKINGS;
import static app.iamin.iamin.data.service.DataService.ACTION_REQUEST_NEEDS;
import static app.iamin.iamin.data.service.DataService.ACTION_REQUEST_NEED_FEED;
import static app.iamin.iamin.data.service.DataService.EXTRA_ID;

/**
 * Created by Markus on 11.11.15.
 */
public class DataManager {

    private static final String TAG = DataManager.class.getSimpleName();

    private Context mContext;

    private Bus mBus = BusProvider.getInstance();

    private IntentFilter mConnectivityFilter = new IntentFilter(CONNECTIVITY_ACTION);

    private NetworkReceiver mNetworkReceiver = new NetworkReceiver();

    private Intent pendingRequest = null;

    public static boolean hasUser = false;

    private boolean hasNeeds = false;

    public static boolean hasBookings = false;

    public boolean isConnected = false;

    private boolean isBooking = false;

    private static volatile DataManager instance = null;

    public DataManager() {

    }

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }

        return instance;
    }

    public void register(Context context) {
        mContext = context;
        mContext.registerReceiver(mNetworkReceiver, mConnectivityFilter);
        hasUser = hasUser();
    }

    public void unregister(Context context) {
        context.unregisterReceiver(mNetworkReceiver);
    }

    public void signUp(String email, String password, String passwordConf) {
        DataService.signUp(mContext, email, password, passwordConf);
    }

    public void signIn(String email, String password) {
        DataService.signIn(mContext, email, password);
    }

    public void signOut() {
        DataService.signOut(mContext);
    }

    private void requestNeeds() {
        DataService.requestNeeds(mContext);
    }

    public void requestBookings() {
        if (isAuthenticated()) {
            Log.d(TAG, "requestBookings");
            isBooking = true;
            DataService.requestBookings(mContext);
        }
    }

    public void createBooking(int needId) {
        if (isAuthenticated()) {
            isBooking = true;
            DataService.createBooking(mContext, needId);
        } else {
            Log.d(TAG, "addedPendingIntent: ACTION_CREATE_BOOKING");
            pendingRequest = new Intent();
            pendingRequest.setAction(DataService.ACTION_CREATE_BOOKING);
            pendingRequest.putExtra(EXTRA_ID, needId);
            connect(true);
        }
    }

    public void cancelBooking(int volunteeringId) {
        if (isAuthenticated()) {
            DataService.cancelBooking(mContext, volunteeringId);
        } else {
            Log.d(TAG, "addedPendingIntent: ACTION_CANCEL_BOOKING");
            pendingRequest = new Intent();
            pendingRequest.setAction(DataService.ACTION_CANCEL_BOOKING);
            pendingRequest.putExtra(EXTRA_ID, volunteeringId);
            connect(false); // prompt login should not be possible
        }
    }

    public void onDataResult(DataResultEvent event) {
        String action = event.action;
        String error = event.error;

        if (DataService.ACTION_SIGN_UP.equals(action)) {
            Log.d(TAG, "ACTION_SIGN_UP");
            handleSignUp(error);
        } else if (DataService.ACTION_SIGN_IN.equals(action)) {
            Log.d(TAG, "ACTION_SIGN_IN");
            handleSignIn(error);
        } else if (DataService.ACTION_SIGN_OUT.equals(action)) {
            Log.d(TAG, "ACTION_SIGN_OUT");
            handleSignOut(error);
        } else if (ACTION_REQUEST_NEED_FEED.equals(action)) {
            Log.d(TAG, "ACTION_REQUEST_NEED_FEED");
            handleNeedFeedRequest(error);
        } else if (ACTION_REQUEST_NEEDS.equals(action)) {
            Log.d(TAG, "ACTION_REQUEST_NEEDS");
            handleNeedsRequest(error);
        } else if (ACTION_REQUEST_BOOKINGS.equals(action)) {
            Log.d(TAG, "ACTION_REQUEST_BOOKINGS");
            handleBookingsRequest(error);
        } else if (DataService.ACTION_CREATE_BOOKING.equals(action)) {
            Log.d(TAG, "ACTION_CREATE_BOOKING");
            handleBookingCreation(event.id, error);
        } else if (DataService.ACTION_CANCEL_BOOKING.equals(action)) {
            Log.d(TAG, "ACTION_CANCEL_BOOKING");
            handleBookingCancellation(event.id, error);
        }
    }

    private void connect(boolean promptLogin) {
        if (hasUser) {
            Log.d(TAG, "connect - hasUser");
            User user = DataUtils.getUser(mContext);
            signIn(user.getEmail(), user.getPassword());
        } else if (promptLogin) {
            Log.d(TAG, "connect - promptLogin");
            mBus.post(new PromptLoginEvent());

            Intent intent = new Intent(mContext, LoginActivity.class);
            mContext.startActivity(intent);
        } else {
            Log.d(TAG, "connect - user not found");
            mBus.post(new ErrorEvent("User not found, need to login?"));
        }
    }

    private void handleSignUp(String error) {
        mBus.post(new UserSignUpEvent(error));
    }

    private void handleSignIn(String error) {
        if (error == null) {
            hasUser = true;
            handlePendingRequests();
            requestBookings();
        }
        mBus.post(new UserSignInEvent(error));
    }

    private void handleSignOut(String error) {
        hasUser = error == null;
        BookingsService.clearBookings(mContext);
        mBus.post(new UserSignOutEvent(error));
    }

    private void handleNeedFeedRequest(String error) {
        Log.d(TAG, "handleNeedFeedRequest");
        hasBookings = error == null;
        hasNeeds = error == null;
        mBus.post(new NeedFeedUpdatedEvent(error));
    }

    private void handleNeedsRequest(String error) {
        Log.d(TAG, "handleNeedsRequest");
        hasNeeds = error == null;
        if (error == null && hasUser) {
            requestBookings();
        } else {
            mBus.post(new NeedsUpdatedEvent(error));
        }
    }

    private void handleBookingsRequest(String error) {
        Log.d(TAG, "handleBookingsRequest");
        hasBookings = error == null;
        mBus.post(new BookingsUpdatedEvent(error));
    }

    private void handleBookingCreation(int id, String error) {
        Log.d(TAG, "handleBookingCreation");
        mBus.post(new BookingCreatedEvent(id, error));
        isBooking = false;
    }

    private void handleBookingCancellation(int id, String error) {
        Log.d(TAG, "handleBookingCreation");
        mBus.post(new BookingCanceledEvent(id, error));
        isBooking = false;
    }

    private void handlePendingRequests() {
        if (pendingRequest != null) {
            String action = pendingRequest.getAction();
            if (DataService.ACTION_CREATE_BOOKING.equals(action)) {
                Log.d(TAG, "handlePendingRequests: ACTION_CREATE_BOOKING");
                createBooking(pendingRequest.getIntExtra(EXTRA_ID, 0));
            } else if (DataService.ACTION_CANCEL_BOOKING.equals(action)) {
                Log.d(TAG, "handlePendingRequests: ACTION_CANCEL_BOOKING");
                cancelBooking(pendingRequest.getIntExtra(EXTRA_ID, 0));
            }
            // clear
            pendingRequest = null;
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

    private boolean hasUser() {
        User user = DataUtils.getUser(mContext);
        return (!TextUtils.isEmpty(user.getEmail()) &&
                !TextUtils.isEmpty(user.getPassword()));
    }

    private class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ConnectivityManager connMgr =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            isConnected = networkInfo != null;

            if (isConnected) {
                mBus.post(new ConnectedEvent());
                if (!isBooking && !hasNeeds) {
                    requestNeeds();
                }
            } else {
                mBus.post(new DisconnectedEvent());
                NeedsService.clearNeeds(mContext); // Clear outdated data
                // NOTE: Bookings won't be offline accessible because of clearNeeds()
            }
        }
    }
}
