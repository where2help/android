package app.iamin.iamin.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import app.iamin.iamin.BusProvider;
import app.iamin.iamin.HelpRequest;
import app.iamin.iamin.LocationUtils;
import app.iamin.iamin.PullNeedsActiveTask;
import app.iamin.iamin.R;
import app.iamin.iamin.event.NeedsEvent;
import app.iamin.iamin.service.LocationService;
import app.iamin.iamin.service.UtilityService;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    private CustomRecyclerView mRecyclerView;
    private ListAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ImageButton retryButton;
    private ProgressBar progressBar;

    private static final int PERMISSION_REQ = 0;

    private static final String URL_NEEDS = "http://where2help.herokuapp.com/api/v1/needs.json";
    private static final String URL_REGISTRATION = "http://where2help.herokuapp.com/api/v1/volunteerings/create";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Click on logo to show endpoint picker
        findViewById(R.id.logo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEndpointPicker();
            }
        });

        findViewById(R.id.logo_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showEndpointPicker();
                UtilityService.triggerNotification(MainActivity.this);
            }
        });

        mAdapter = new ListAdapter(this);
        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView = (CustomRecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setEmptyView(findViewById(R.id.empty_view));
        mRecyclerView.setAdapter(mAdapter);

        retryButton = (ImageButton) findViewById(R.id.retry_button);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        new PullNeedsActiveTask(this, getEndpoint(this, 0)).execute();

        // Check fine location permission has been granted
        if (!LocationUtils.checkFineLocationPermission(this)) {
            // See if user has denied permission in the past
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show a simple snackbar explaining the request instead
                showPermissionSnackbar();
            } else {
                // Otherwise request permission from user
                if (savedInstanceState == null) {
                    requestFineLocationPermission();
                }
            }
        } else {
            // Otherwise permission is granted (which is always the case on pre-M devices)
            fineLocationPermissionGranted();
        }
    }

    @Subscribe
    public void onNeedsUpdate(NeedsEvent event) {
        HelpRequest[] needs = event.getNeeds();
        if (needs != null) {
            mAdapter.setData(needs);
        } else {
            progressBar.setVisibility(View.GONE);
            retryButton.setVisibility(View.VISIBLE);
            retryButton.setEnabled(true);
        }
    }

    public void onRetry(View v) {
        retryButton.setEnabled(false);
        retryButton.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        new PullNeedsActiveTask(this, getEndpoint(this, 0)).execute();
    }

    // Choose endpoint
    private void showEndpointPicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Endpoint for")
                .setItems(R.array.endpoint, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showEndpointInputPicker(which);
                    }
                });
        builder.show();
    }

    // Set new endpoint
    private void showEndpointInputPicker(final int which) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Change Endpoint");

        final EditText input = new EditText(MainActivity.this);
        input.setText(getEndpoint(MainActivity.this, which));
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int p) {
                storeEndpoint(MainActivity.this, input.getText().toString(), which);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int p) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    // Store endpoint
    public static void storeEndpoint(Context context, String url, int pos) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(pos == 0 ? "URL_NEEDS" : "URL_REGISTRATION", url);
        editor.apply();
    }

    // Get endpoint
    public static String getEndpoint(Context context, int pos) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                pos == 0 ? "URL_NEEDS" : "URL_REGISTRATION",
                pos == 0 ? URL_NEEDS : URL_REGISTRATION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        LocationService.requestLocation(this);
        // TODO: update needs
        // new PullNeedsActiveTask(this, mAdapter, getEndpoint(this, 0)).execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    /**
     * Permissions request result callback
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQ:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fineLocationPermissionGranted();
                }
        }
    }

    /**
     * Request the fine location permission from the user
     */
    private void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQ);
    }

    /**
     * Run when fine location permission has been granted
     */
    private void fineLocationPermissionGranted() {
        LocationService.requestLocation(this);
    }

    /**
     * Show a permission explanation snackbar
     */
    private void showPermissionSnackbar() {
        // TODO: yell at user
/*        Snackbar.make(
                findViewById(R.id.container), R.string.permission_explanation, Snackbar.LENGTH_LONG)
                .setAction(R.string.permission_explanation_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestFineLocationPermission();
                    }
                })
                .show();*/
    }
}
