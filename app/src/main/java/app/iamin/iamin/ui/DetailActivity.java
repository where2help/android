package app.iamin.iamin.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.NestedScrollView.OnScrollChangeListener;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import app.iamin.iamin.R;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.data.model.User;
import app.iamin.iamin.data.service.DataService;
import app.iamin.iamin.ui.widget.CustomMapView;
import app.iamin.iamin.ui.widget.NeedViewNew;
import app.iamin.iamin.util.DataUtils;
import app.iamin.iamin.util.NeedUtils;
import app.iamin.iamin.util.TimeUtils;
import app.iamin.iamin.util.UiUtils;

import static app.iamin.iamin.data.service.DataService.ACTION_CANCEL_BOOKING;
import static app.iamin.iamin.data.service.DataService.ACTION_CREATE_BOOKING;
import static app.iamin.iamin.util.UiUtils.EXTRA_BOOKING_CHANGED;
import static app.iamin.iamin.util.UiUtils.EXTRA_NEW_USER;

/**
 * Created by Markus on 10.10.15.
 */
public class DetailActivity extends AppCompatActivity implements View.OnClickListener {

    private boolean isAttending = false;
    private boolean hasChanged = false;
    private boolean newUser = false;

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

        if (savedInstanceState != null) {
            hasChanged = savedInstanceState.getBoolean(EXTRA_BOOKING_CHANGED);
            newUser = savedInstanceState.getBoolean(EXTRA_NEW_USER);
        }

        need = NeedUtils.createNeedfromIntent(getIntent());

        user = DataUtils.getUser(this);

        isAttending = need.isAttending();

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
        webTextView.setText("www.google.at");
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
        // LocationService.requestLocation(this);
        // TODO: update need (data) when user comes back

       LocalBroadcastManager.getInstance(this)
                .registerReceiver(mDataReceiver, DataService.getDataResultIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDataReceiver);
    }

    private BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String action = intent.getAction();
            String error = intent.getStringExtra(DataService.EXTRA_ERROR);
            if (ACTION_CREATE_BOOKING.equals(action)) {
                handleBookingCreation(error);
            } else if (ACTION_CANCEL_BOOKING.equals(action)) {
                handleBookingCancellation(error);
            }
        }
    };

    private void handleBookingCreation(String error) {
        if (error == null) {
            isAttending = true;
            hasChanged = !hasChanged;
            setUiMode(isAttending);
            needView.setNeeded(need.getNeeded() - 1); //update needed
        } else {
            Toast.makeText(DetailActivity.this, "Error. Try again.", Toast.LENGTH_SHORT).show();
            setUiMode(isAttending);
        }
    }

    private void handleBookingCancellation(String error) {
        if (error == null) {
            hasChanged = !hasChanged;
            isAttending = false;
            setUiMode(isAttending);
            needView.setNeeded(need.getNeeded() + 1); //update needed
        } else {
            Toast.makeText(DetailActivity.this, "Error. Try again.", Toast.LENGTH_SHORT).show();
            setUiMode(isAttending);
        }
    }

    /*   public void onLocationUpdate() {
        LatLng userLocation = event.getLocation();
        if (userLocation != null) {
            String distance = LocationUtils.formatDistanceBetween(need.getLocation(), userLocation);
            needView.setDistance(distance);
        }
    }*/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default: // Back button hack
                onBackPressed();
                break;
            case R.id.submit:
                if (!isAttending) {
                    if (user.getEmail() != null) {
                        onActionSubmit();
                    } else {
                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivityForResult(intent, UiUtils.RC_LOGIN);
                    }
                } else {
                    UiUtils.fireShareIntent(DetailActivity.this, need);
                }
                break;
            case R.id.cancel:
                onActionCancel();
                break;
            case R.id.calendar:
                UiUtils.fireCalendarIntent(DetailActivity.this, need);
                break;
            case R.id.web:
                UiUtils.fireWebIntent(DetailActivity.this, need);
                break;
        }
    }

    private void onActionSubmit() {
        submitButton.setText(R.string.register_status);
        submitButton.setEnabled(false);
        //new CreateVolunteeringTask(need.getId()).execute(this);
        DataService.createBooking(this, need.getId());
    }

    private void onActionCancel() {
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
                DataService.cancelBooking(DetailActivity.this, need.getVolunteeringId());
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case UiUtils.RC_LOGIN:
                if (resultCode == RESULT_OK) {
                    newUser = true;
                    user = DataUtils.getUser(this);
                    onActionSubmit();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra(EXTRA_BOOKING_CHANGED, hasChanged);
        result.putExtra(EXTRA_NEW_USER, newUser);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean(EXTRA_BOOKING_CHANGED, hasChanged);
        outState.putBoolean(EXTRA_NEW_USER, newUser);
    }
}
