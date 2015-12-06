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

import java.util.ArrayList;

import app.iamin.iamin.data.api.BookingsService;
import app.iamin.iamin.data.event.CancelBookingEvent;
import app.iamin.iamin.data.event.ConnectedEvent;
import app.iamin.iamin.data.event.CreateBookingEvent;
import app.iamin.iamin.data.event.DataResultEvent;
import app.iamin.iamin.data.event.DisconnectedEvent;
import app.iamin.iamin.data.event.ErrorEvent;
import app.iamin.iamin.data.event.PromptLoginEvent;
import app.iamin.iamin.data.event.RequestBookingsEvent;
import app.iamin.iamin.data.event.RequestNeedsEvent;
import app.iamin.iamin.data.event.UserSignInEvent;
import app.iamin.iamin.data.event.UserSignOutEvent;
import app.iamin.iamin.data.event.UserSignUpEvent;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.data.model.User;
import app.iamin.iamin.data.service.DataService;
import app.iamin.iamin.util.DataUtils;

import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;
import static app.iamin.iamin.data.service.DataService.ACTION_REQUEST_BOOKINGS;
import static app.iamin.iamin.data.service.DataService.ACTION_REQUEST_NEEDS;

/**
 * Created by Markus on 11.11.15.
 */
public class DataManager {

    public static final int START = 0;
    public static final int NEXT = 1;
    public static final int ERROR = 2;

    private static final String TAG = DataManager.class.getSimpleName();

    private Context mContext;

    private Bus mBus = BusProvider.getInstance();

    private IntentFilter mConnectivityFilter = new IntentFilter(CONNECTIVITY_ACTION);

    private NetworkReceiver mNetworkReceiver = new NetworkReceiver();

    private PendingRequest pendingRequest;

    private ArrayList<Integer> ongoingBookings = new ArrayList<>();

    private static boolean hasUser = false;

    private boolean hasNeeds = false;

    private static boolean hasBookings = false;

    public boolean isConnected = false;

    private boolean isBooking = false;

    private static volatile DataManager dataManager = null;

    public DataManager() {

    }

    public static synchronized DataManager getInstance() {
        if (dataManager == null) {
            dataManager = new DataManager();
        }

        return dataManager;
    }

    public void register(Context context) {
        mContext = context;
        mContext.registerReceiver(mNetworkReceiver, mConnectivityFilter);
        hasUser = hasUserInternal();
    }

    public void unregister(Context context) {
        context.unregisterReceiver(mNetworkReceiver);
    }

    public void signUp(String email, String password, String passwordConf) {
        mBus.post(new UserSignUpEvent(START, null));
        DataService.signUp(mContext, email, password, passwordConf);
    }

    public void signIn(String email, String password) {
        mBus.post(new UserSignInEvent(START, null));
        DataService.signIn(mContext, email, password);
    }

    public void signOut() {
        mBus.post(new UserSignOutEvent(START, null));
        DataService.signOut(mContext);
    }

    private void requestNeeds() {
        mBus.post(new RequestNeedsEvent(START, null));
        DataService.requestNeeds(mContext);
    }

    public void requestBookings() {
        mBus.post(new RequestBookingsEvent(START, null));
        if (isAuthenticated()) {
            Log.d(TAG, "requestBookings");
            isBooking = true;
            DataService.requestBookings(mContext);
        }
    }

    public void createBooking(Need need) {
        int needId = need.getId();
        mBus.post(new CreateBookingEvent(START, needId, null));
        if (isAuthenticated()) {
            isBooking = true;
            ongoingBookings.add(needId);
            DataService.createBooking(mContext, needId);
        } else {
            Log.d(TAG, "addedPendingIntent: ACTION_CREATE_BOOKING");
            pendingRequest = new PendingRequest();
            pendingRequest.setNeed(need);
            pendingRequest.setAction(DataService.ACTION_CREATE_BOOKING);
            connect(true);
        }
    }

    public void cancelBooking(Need need) {
        int needId = need.getId();
        int volunteeringId = need.getVolunteeringId();
        mBus.post(new CancelBookingEvent(START, needId, null));
        if (isAuthenticated()) {
            isBooking = true;
            ongoingBookings.add(need.getId());
            DataService.cancelBooking(mContext, needId, volunteeringId);
        } else {
            Log.d(TAG, "addedPendingIntent: ACTION_CANCEL_BOOKING");
            pendingRequest = new PendingRequest();
            pendingRequest.setNeed(need);
            pendingRequest.setAction(DataService.ACTION_CANCEL_BOOKING);
            connect(false); // prompt login should not be possible
        }
    }

    public static boolean hasUser() {
        return hasUser;
    }

    public static boolean hasBookings() {
        return hasBookings;
    }

    public boolean isBooking(Need need) {
        return ongoingBookings.contains(Integer.valueOf(need.getId()));
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
        } else {
            Log.d(TAG, "connect - user not found");
            mBus.post(new ErrorEvent("User not found, need to login?"));
        }
    }

    private void handleSignUp(String error) {
        if (error == null) {
            mBus.post(new UserSignUpEvent(NEXT, null));
        } else {
            mBus.post(new UserSignUpEvent(ERROR, error));
        }
    }

    private void handleSignIn(String error) {
        if (error == null) {
            hasUser = true;
            handlePendingRequests();
            requestBookings();
            mBus.post(new UserSignInEvent(NEXT, null));
        } else {
            mBus.post(new UserSignInEvent(ERROR, error));
        }
    }

    private void handleSignOut(String error) {
        if (error == null) {
            BookingsService.clearBookings(mContext);
            mBus.post(new UserSignOutEvent(NEXT, null));
        } else {
            mBus.post(new UserSignOutEvent(ERROR, error));
        }
    }

    private void handleNeedsRequest(String error) {
        Log.d(TAG, "handleNeedsRequest");
        hasNeeds = error == null;
        if (error == null && hasUser) {
            requestBookings();
        } else if (error == null) {
            mBus.post(new RequestNeedsEvent(NEXT, error));
        } else {
            mBus.post(new RequestNeedsEvent(ERROR, error));
        }
    }

    private void handleBookingsRequest(String error) {
        Log.d(TAG, "handleBookingsRequest");
        hasBookings = error == null;
        if (error == null) {
            mBus.post(new RequestBookingsEvent(NEXT, null));
        } else {
            mBus.post(new RequestBookingsEvent(ERROR, error));
        }

    }

    private void handleBookingCreation(int id, String error) {
        Log.d(TAG, "handleBookingCreation");
        if (error == null || error.equals("422")) {
            mBus.post(new CreateBookingEvent(NEXT, id, null));
        } else {
            mBus.post(new CreateBookingEvent(ERROR, id, error));
        }
        ongoingBookings.remove(Integer.valueOf(id));
        isBooking = false;
    }

    private void handleBookingCancellation(int id, String error) {
        Log.d(TAG, "handleBookingCreation");
        if (error == null || error.equals("422")) {
            mBus.post(new CancelBookingEvent(NEXT, id, null));
        } else {
            mBus.post(new CancelBookingEvent(ERROR, id, error));
        }
        ongoingBookings.remove(Integer.valueOf(id));
        isBooking = false;
    }

    private void handlePendingRequests() {
        if (pendingRequest != null) {
            if (DataService.ACTION_CREATE_BOOKING.equals(pendingRequest.getAction())) {
                Log.d(TAG, "handlePendingRequests: ACTION_CREATE_BOOKING");
                createBooking(pendingRequest.getNeed());
            } else if (DataService.ACTION_CANCEL_BOOKING.equals(pendingRequest.getAction())) {
                Log.d(TAG, "handlePendingRequests: ACTION_CANCEL_BOOKING");
                cancelBooking(pendingRequest.getNeed());
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

    private boolean hasUserInternal() {
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
            }
        }
    }

    private class PendingRequest {
        private Need need;
        private String action;

        public Need getNeed() {
            return need;
        }

        public void setNeed(Need need) {
            this.need = need;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }
    }
}
