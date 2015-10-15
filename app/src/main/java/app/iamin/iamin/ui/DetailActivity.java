package app.iamin.iamin.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import java.util.regex.Pattern;

import app.iamin.iamin.BusProvider;
import app.iamin.iamin.model.Need;
import app.iamin.iamin.util.LocationUtils;
import app.iamin.iamin.R;
import app.iamin.iamin.RegisterTask;
import app.iamin.iamin.event.LocationEvent;
import app.iamin.iamin.service.LocationService;
import app.iamin.iamin.util.TimeUtils;
import app.iamin.iamin.util.UiUtils;

/**
 * Created by Markus on 10.10.15.
 */
public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener {

    private boolean isAttending = false; // TODO: set value based on registration !

    private LinearLayout btnBarLayout;
    private Button submitButton;
    private Button cancelButton;
    private Button calendarButton;

    private TextView typeTextView;
    private TextView addressTextView;
    private TextView dateTextView;
    private TextView submitInfoTextView;
    private TextView webTextView;
    private TextView countTextView;

    private ImageView typeImageView;

    private SupportMapFragment mMapFragment;

    private Need need;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        need = new Need().fromIntent(getIntent());

        if (savedInstanceState != null) {
            // Restore value from saved state
            isAttending = savedInstanceState.getBoolean("isAttending");
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(need.getCategoryPlural() + " - " + need.getAddress().getAddressLine(0));
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(this);

        typeTextView = (TextView) findViewById(R.id.type);

        typeImageView = (ImageView) findViewById(R.id.type_icon);
        typeImageView.setImageResource(need.getCategoryIcon());

        addressTextView = (TextView) findViewById(R.id.address);
        addressTextView.setText(need.getAddress().getAddressLine(0));

        dateTextView = (TextView) findViewById(R.id.date);
        dateTextView.setText(
                TimeUtils.formatHumanFriendlyShortDate(this, need.getStart()) + " " +
                TimeUtils.formatTimeOfDay(need.getStart()) + " - " +
                TimeUtils.formatTimeOfDay(need.getEnd()) + " Uhr" + " (" +
                TimeUtils.getDuration(need.getStart(), need.getEnd()) + ")");

        webTextView = (TextView) findViewById(R.id.web);
        webTextView.setText("www.google.at");
        webTextView.setOnClickListener(this);

        submitInfoTextView = (TextView) findViewById(R.id.info);
        submitInfoTextView.setText("Danke! Bitte komm um " + TimeUtils.formatTimeOfDay(need.getStart()) + "!");
        btnBarLayout = (LinearLayout) findViewById(R.id.btnBar);

        calendarButton = (Button) findViewById(R.id.calendar);
        calendarButton.setOnClickListener(this);

        cancelButton = (Button) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(this);

        submitButton = (Button) findViewById(R.id.submit);
        submitButton.setOnClickListener(this);

        countTextView = (TextView) findViewById(R.id.count);

        setUiMode(isAttending);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mMapFragment == null) {
            GoogleMapOptions options = new GoogleMapOptions().liteMode(true)
                    .camera(CameraPosition.fromLatLngZoom(need.getLocation(), 13));

            mMapFragment = SupportMapFragment.newInstance(options);
            mMapFragment.getMapAsync(DetailActivity.this);

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.map, mMapFragment);
            ft.commit();
        }
    }

    private void setUiMode(boolean isAttending) {
        if (isAttending) {
            countTextView.setText("" + (need.getCount() - 1)); // TODO: cheat ! ;-)
            submitButton.setEnabled(true);
            submitInfoTextView.setVisibility(View.VISIBLE);
            btnBarLayout.setVisibility(View.VISIBLE);
            submitButton.setText("Weitersagen!");
            typeTextView.setText((need.getCount() - 1) == 1 ? need.getCategorySingular() : need.getCategoryPlural());
        } else {
            countTextView.setText("" + (need.getCount())); // TODO: cheat ! ;-)
            submitInfoTextView.setVisibility(View.GONE);
            btnBarLayout.setVisibility(View.GONE);
            submitButton.setText("I'm In!");
            typeTextView.setText((need.getCount()) == 1 ? need.getCategorySingular() : need.getCategoryPlural());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        LocationService.requestLocation(this);
        // TODO: also update need (data) when user comes back
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onLocationUpdate(LocationEvent event) {
        LatLng userLocation = event.getLocation();
        if (userLocation != null) {
            String distance = LocationUtils.formatDistanceBetween(need.getLocation(), userLocation);
            addressTextView.setText(need.getAddress().getAddressLine(0) + " (" + distance + ")");
        } else {
            addressTextView.setText(need.getAddress().getAddressLine(0));
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.getUiSettings().setMapToolbarEnabled(false);
        map.addMarker(new MarkerOptions()
                .title(need.getAddress().getAddressLine(0))
                .position(need.getLocation()));
    }

    public void onRegisterSuccess() {
        isAttending = true;
        setUiMode(isAttending);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default: // Back button hack
                finish();
                break;
            case R.id.submit:
                if (submitInfoTextView.getVisibility() != View.VISIBLE) {
                    onActionSubmit();
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
        submitButton.setText("Registriere...");
        submitButton.setEnabled(false);

        String email = null;
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                email = account.name;
                break;
            }
        }
        Log.d("onActionSubmit", email);
        //TODO: If email is null ask user for email or phone number
        new RegisterTask(this, getIntent().getExtras().getInt("id"), email).execute();
    }

    private void onActionCancel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Wir brauchen dich! Willst du wirklich absagen?");
        builder.setPositiveButton("Natürlich nicht!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing
            }
        });
        builder.setNegativeButton("Ja...", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                isAttending = false;
                setUiMode(isAttending);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current state
        savedInstanceState.putBoolean("isAttending", isAttending);
        super.onSaveInstanceState(savedInstanceState);
    }
}
