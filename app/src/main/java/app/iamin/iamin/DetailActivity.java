package app.iamin.iamin;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.regex.Pattern;

/**
 * Created by Markus on 10.10.15.
 */
public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback,
        View.OnClickListener {

    private LinearLayout btnBarLayout;
    private Button submitButton;
    private Button cancelButton;
    private Button calendarButton;

    private TextView titleTextView;
    private TextView addressTextView;
    private TextView distanceTextView;
    private TextView durationTextView;
    private TextView dateTextView;
    private TextView submitInfoTextView;
    private TextView webTextView;

    private CountView countView;

    private LatLng mLatestLocation;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getType() + " - " + getAddress());
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        GoogleMapOptions options = new GoogleMapOptions().liteMode(true);

        SupportMapFragment mMapFragment = SupportMapFragment.newInstance(options);
        mMapFragment.getMapAsync(DetailActivity.this);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.map, mMapFragment);
        ft.commit();

        titleTextView = (TextView) findViewById(R.id.title);
        titleTextView.setText(getType());

        addressTextView = (TextView) findViewById(R.id.address);
        addressTextView.setText(getAddress());

        distanceTextView = (TextView) findViewById(R.id.distance);
        distanceTextView.setVisibility(View.GONE);

        durationTextView = (TextView) findViewById(R.id.duration);
        durationTextView.setText(getDuration());

        dateTextView = (TextView) findViewById(R.id.date);
        dateTextView.setText(getDate());

        webTextView = (TextView) findViewById(R.id.web);
        webTextView.setText("www.google.at");
        webTextView.setOnClickListener(this);

        submitInfoTextView = (TextView) findViewById(R.id.info);
        submitInfoTextView.setText("Bitte komm um " + getDateStartForm() + "!");
        btnBarLayout = (LinearLayout) findViewById(R.id.btnBar);

        calendarButton = (Button) findViewById(R.id.calendar);
        calendarButton.setOnClickListener(this);

        cancelButton = (Button) findViewById(R.id.cancel);
        cancelButton.setOnClickListener(this);

        submitButton = (Button) findViewById(R.id.submit);
        submitButton.setOnClickListener(this);

        countView = (CountView) findViewById(R.id.count);
        countView.setCount(getStillOpen());
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mBroadcastReceiver, LocationService.getLocationUpdatedIntentFilter());
        LocationService.requestLocation(this);
        // TODO: also update need (data) when user comes back
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location =
                    intent.getParcelableExtra(FusedLocationProviderApi.KEY_LOCATION_CHANGED);
            if (location != null) {
                mLatestLocation = new LatLng(location.getLatitude(), location.getLongitude());
                String distance = LocationUtils.formatDistanceBetween(getLocation(), mLatestLocation);
                distanceTextView.setText(distance);
                distanceTextView.setVisibility(View.VISIBLE);
            }
        }
    };

    @Override
    public void onMapReady(GoogleMap map) {
        map.getUiSettings().setMapToolbarEnabled(false);
        LatLng sydney = getLocation();

        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 13));

        map.addMarker(new MarkerOptions()
                .title(getAddress())
                .position(sydney));
    }

    public void registerSuccess() {
        countView.setCount(getStillOpen() - 1); // TODO: cheat ! ;-)
        submitButton.setEnabled(true);
        submitInfoTextView.setVisibility(View.VISIBLE);
        btnBarLayout.setVisibility(View.VISIBLE);
        submitButton.setText("Weitersagen!");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            default: break;
            case R.id.submit:
                if (submitInfoTextView.getVisibility() != View.VISIBLE) {
                    onActionSubmit();
                } else {
                    onActionShare();
                }
                break;
            case R.id.cancel:
                onActionCancel();
                break;
            case R.id.calendar:
                onActionCalendar();
                break;
            case R.id.web:
                onActionWeb();
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

        new RegisterTask(this, getIntent().getExtras().getInt("id"), email).execute();
    }

    private void onActionCancel() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Wir brauchen dich! Willst du wirklich absagen?");
        builder.setPositiveButton("Natürlich nicht!", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // FIRE ZE MISSILES!
            }
        });
        builder.setNegativeButton("Ja...", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void onActionCalendar() {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setType("vnd.android.cursor.item/event")
                .putExtra("beginTime", getDateStart())
                .putExtra("endTime", getDateEnd())
                .putExtra("allDay", false)
                .putExtra("title", "Where2Help - " + getType())
                .putExtra("description", "Where2Help - " + getType() + " für " + "mind. 2h.")
                .putExtra("eventLocation", getAddress());
        startActivity(intent);
    }

    public void onActionShare() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Where2Help braucht noch " +
                getType() + " (" + getStillOpen() + ") am " + getDate() + " für " + "mind. 2h" + " am " + getAddress() + ". (" + getSelfLink() + ")");
        startActivity(sendIntent);
    }

    private void onActionWeb() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse("http://" + webTextView.getText().toString()));
        startActivity(i);
    }

    private LatLng getLocation() {
        return new LatLng(getIntent().getExtras().getDouble("latitude"), getIntent().getExtras().getDouble("longitude"));
    }

    private String getType() {
        return getIntent().getExtras().getString("type");
    }

    private String getAddress() {
        return getIntent().getExtras().getString("address");
    }

    private String getDuration() {
        long diffHours = (getDateEnd() - getDateStart()) / (1000l * 60l * 60l);
        return "mind " + diffHours + " h";
    }

    private int getStillOpen() {
        return getIntent().getExtras().getInt("stillOpen");
    }

    private String getDate() {
        return getIntent().getExtras().getString("date");
    }

    private String getDateStartForm() {
        return getIntent().getExtras().getString("dateStartForm");
    }

    private long getDateStart() {
        return getIntent().getExtras().getLong("dateStart");
    }

    private long getDateEnd() {
        return getIntent().getExtras().getLong("dateEnd");
    }

    private String getSelfLink() {
        return getIntent().getExtras().getString("selfLink");
    }
}
