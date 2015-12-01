package app.iamin.iamin.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.NestedScrollView.OnScrollChangeListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import app.iamin.iamin.R;
import app.iamin.iamin.data.BusProvider;
import app.iamin.iamin.data.DataManager;
import app.iamin.iamin.data.event.BookingCanceledEvent;
import app.iamin.iamin.data.event.BookingCreatedEvent;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.data.model.User;
import app.iamin.iamin.ui.widget.CustomMapView;
import app.iamin.iamin.ui.widget.NeedViewNew;
import app.iamin.iamin.util.DataUtils;
import app.iamin.iamin.util.NeedUtils;
import app.iamin.iamin.util.TimeUtils;
import app.iamin.iamin.util.UiUtils;
import io.realm.Realm;

import static app.iamin.iamin.util.UiUtils.EXTRA_BOOKING_CHANGED;
import static app.iamin.iamin.util.UiUtils.EXTRA_NEW_USER;

/**
 * Created by Markus on 10.10.15.
 */
public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "DetailActivity";

    private boolean isAttending = false;
    private boolean hasUser = false;
    private boolean hasNewUser = false;
    private boolean hasChanged = false;

    private LinearLayout btnBarLayout;
    private Button submitButton;
    private Button cancelButton;
    private Button calendarButton;

    private TextView submitInfoTextView;
    private TextView webTextView;

    private NestedScrollView container;

    private Need need;
    private NeedViewNew needView;

    private User user;

    private Realm realm;

    private CustomMapView mapView;

    private OnScrollChangeListener scrollChangeListener = new OnScrollChangeListener() {
        @Override
        public void onScrollChange(NestedScrollView v, int sX, int sY, int oldSX, int oldSY) {
            if (mapView != null) mapView.setOffset(sY);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        BusProvider.getInstance().register(this);

        realm = Realm.getInstance(this);

        if (savedInstanceState != null) {
            hasChanged = savedInstanceState.getBoolean(EXTRA_BOOKING_CHANGED);
            hasNewUser = savedInstanceState.getBoolean(EXTRA_NEW_USER);
        }

        need = NeedUtils.createNeedfromIntent(getIntent());

        user = DataUtils.getUser(this);
        hasUser = user.getEmail() != null;

        isAttending = need.isAttending() && DataManager.hasUser;

        container = (NestedScrollView) findViewById(R.id.container);
        container.setOnScrollChangeListener(scrollChangeListener);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(NeedUtils.getCategoryPlural(need.getCategory()) + " - " + need.getLocation());
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(this);

        mapView = (CustomMapView) findViewById(R.id.map);
        mapView.setNeed(need);
        mapView.onCreate(null);

        needView = (NeedViewNew) findViewById(R.id.need_view);
        needView.setInDetail(true);
        needView.setNeed(need);

        webTextView = (TextView) findViewById(R.id.web);
        webTextView.setText("Delete Token");
        webTextView.setOnClickListener(this);

        submitInfoTextView = (TextView) findViewById(R.id.info);

        btnBarLayout = (LinearLayout) findViewById(R.id.btnBar);

        submitButton = (Button) findViewById(R.id.submit);
        submitButton.setOnClickListener(this);

        setUiMode(isAttending);
    }

    private void setUiMode(boolean isAttending) {
        if (isAttending) {
            submitButton.setText(R.string.action_share);
            submitButton.setEnabled(true);

            submitInfoTextView.setText(getString(R.string.thank_you_message, TimeUtils.formatTimeOfDay(need.getStart())));
            submitInfoTextView.setVisibility(View.VISIBLE);

            calendarButton = (Button) findViewById(R.id.calendar);
            calendarButton.setOnClickListener(this);

            cancelButton = (Button) findViewById(R.id.cancel);
            cancelButton.setText(R.string.action_iamout);
            cancelButton.setEnabled(true);
            cancelButton.setOnClickListener(this);

            btnBarLayout.setVisibility(View.VISIBLE);
        } else {
            submitButton.setEnabled(true);
            submitButton.setText(R.string.action_iamin);

            submitInfoTextView.setVisibility(View.GONE);
            btnBarLayout.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.getInstance().register(this);
        // TODO: update need (data) when user comes back
    }

    @Override
    public void onPause() {
        super.onPause();

        DataManager.getInstance().unregister(this);
    }

    private void handleBookingsRequest(String error) {
        if (error == null && hasUser) {
            onActionCreateBooking();
        } else {
            Toast.makeText(DetailActivity.this, error, Toast.LENGTH_SHORT).show();
            setUiMode(isAttending);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default: // Back button hack
                onBackPressed();
                break;
            case R.id.submit:
                if (!isAttending) {
                    onActionCreateBooking();
                } else {
                    UiUtils.fireShareIntent(DetailActivity.this, need);
                }
                break;
            case R.id.cancel:
                onActionCancelBooking();
                break;
            case R.id.calendar:
                UiUtils.fireCalendarIntent(DetailActivity.this, need);
                break;
            case R.id.web:
                //UiUtils.fireWebIntent(DetailActivity.this, need);
                DataUtils.clearToken(this);
                Toast.makeText(this, "Token deleted!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void onActionCreateBooking() {
        submitButton.setText(R.string.register_status);
        submitButton.setEnabled(false);
        DataManager.getInstance().createBooking(need.getId());
    }

    private void onActionCancelBooking() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.cancel_message));
        builder.setPositiveButton(getString(R.string.cancel_positive), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing
            }
        });
        builder.setNegativeButton(getString(R.string.cancel_negative), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //new DeleteVolunteeringTask(need.getVolunteeringId()).execute(DetailActivity.this);
                Log.wtf("onActionCancel", "volunteeringId = " + need.getVolunteeringId());
                cancelButton.setText("Absagen...");
                cancelButton.setEnabled(false);
                DataManager.getInstance().cancelBooking(need.getVolunteeringId());
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean(EXTRA_BOOKING_CHANGED, hasChanged);
        outState.putBoolean(EXTRA_NEW_USER, hasNewUser);
    }

    @Override
    protected void onDestroy() {
        BusProvider.getInstance().unregister(this);
        realm.close();
        super.onDestroy();
    }

    public void onError(String message) {
        Toast.makeText(DetailActivity.this, message, Toast.LENGTH_SHORT).show();
        setUiMode(isAttending);
    }

    @Subscribe
    public void onBookingCreated(BookingCreatedEvent event) {
        if (need.getId() != event.id) return;
        Log.d(TAG, "onBookingCreated");

        if (event.error == null || event.error.equals("422")) {
            isAttending = true;
            hasChanged = !hasChanged;
            realm.refresh();
            need = realm.where(Need.class).equalTo("id", need.getId()).findFirst(); //update need
            Log.wtf("handleBookingCreation", "volunteeringId = " + need.getVolunteeringId());
            needView.setNeeded(need.getNeeded());
            setUiMode(isAttending);
        } else {
            // error
            onError(event.error);
        }
    }

    @Subscribe
    public void onBookingCanceled(BookingCanceledEvent event) {
        if (need.getVolunteeringId() != event.id) return;
        Log.d(TAG, "onBookingCanceled");

        if (event.error == null) {
            hasChanged = !hasChanged;
            isAttending = false;
            realm.refresh();
            need = realm.where(Need.class).equalTo("id", need.getId()).findFirst(); //update need
            needView.setNeeded(need.getNeeded());
            setUiMode(isAttending);
        } else {
            // error
            onError(event.error);
        }
    }
}
