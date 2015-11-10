package app.iamin.iamin.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import app.iamin.iamin.R;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.data.model.User;
import app.iamin.iamin.data.service.DataService;
import app.iamin.iamin.ui.widget.CustomRecyclerView;
import app.iamin.iamin.util.DataUtils;
import app.iamin.iamin.util.UiUtils;
import io.realm.Realm;
import io.realm.RealmResults;

import static app.iamin.iamin.data.service.DataService.ACTION_REQUEST_BOOKINGS;
import static app.iamin.iamin.data.service.DataService.ACTION_REQUEST_NEEDS;
import static app.iamin.iamin.data.service.DataService.ACTION_SIGN_IN;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        FilterAdapter.FilterChangedListener {

    private static final String TAG = "MainActivity";

    private static final String STATE_UI = "uiState";
    private static final String STATE_FILTER = "filterState";

    private static final int UI_STATE_HOME = 0;
    private static final int UI_STATE_BOOKINGS = 1;

    private int mUiState = UI_STATE_HOME;
    private int mFilterState = 0;

    private CustomRecyclerView mNeedsList;
    private NeedFeedAdapter mAdapter;

    private ImageButton mRetryButton;
    private ProgressBar mProgressBar;
    private TextView mEmptyTextView;

    private DrawerLayout mDrawer;
    private RecyclerView mFiltersList;

    private Realm realm;
    private User user;

    private boolean hasUser = false;

    // private static final int PERMISSION_REQ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        realm = Realm.getInstance(this);
        realm.setAutoRefresh(false);

        user = DataUtils.getUser(this);
        hasUser = user.getEmail() != null;

        if (savedInstanceState != null) {
            mUiState = savedInstanceState.getInt(STATE_UI);
            mFilterState = savedInstanceState.getInt(STATE_FILTER);
        }

        Log.e(TAG, STATE_FILTER + ": " + mFilterState);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer);

        mAdapter = new NeedFeedAdapter(this);

/*        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);*/

        mNeedsList = (CustomRecyclerView) findViewById(R.id.recycler_view);
        mNeedsList.setEmptyView(findViewById(R.id.empty_view));
        //mNeedsList.addItemDecoration(itemDecoration);
        mNeedsList.setAdapter(mAdapter);

        mRetryButton = (ImageButton) findViewById(R.id.retry_button);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mEmptyTextView = (TextView) findViewById(R.id.empty_message);

        mFiltersList = (RecyclerView) findViewById(R.id.filters);
        mFiltersList.setAdapter(new FilterAdapter(this, mFilterState, this));

        setUiState(mUiState);

       /* // Check fine location permission has been granted
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
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        switch (mUiState) {
            case UI_STATE_HOME:
                menu.findItem(R.id.menu_filter).setVisible(true);
                menu.findItem(R.id.menu_login).setVisible(!hasUser);
                menu.findItem(R.id.menu_bookings).setVisible(hasUser);
                menu.findItem(R.id.menu_settings).setVisible(true);
                break;
            case UI_STATE_BOOKINGS:
                menu.findItem(R.id.menu_filter).setVisible(false);
                menu.findItem(R.id.menu_login).setVisible(false);
                menu.findItem(R.id.menu_bookings).setVisible(false);
                menu.findItem(R.id.menu_settings).setVisible(false);
                break;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setUiState(UI_STATE_HOME);
                return true;
            case R.id.menu_filter:
                mDrawer.openDrawer(GravityCompat.END);
                return true;
            case R.id.menu_bookings:
                setUiState(UI_STATE_BOOKINGS);
                return true;
            case R.id.menu_login:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivityForResult(intent, UiUtils.RC_LOGIN);
                return true;
            case R.id.menu_settings:
                UiUtils.fireSettingsIntent(MainActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setUiState(int state) {
        ActionBar ab = getSupportActionBar();
        switch (state) {
            case UI_STATE_HOME:
                if (ab != null) {
                    ab.setCustomView(R.layout.logo);
                    ab.setDisplayShowTitleEnabled(false);
                    ab.setDisplayShowCustomEnabled(true);
                    ab.setDisplayHomeAsUpEnabled(false);

                    mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

                    if (mUiState == UI_STATE_BOOKINGS) {
                        setCategoryFilter(mFilterState);
                    }
                }
                break;
            case UI_STATE_BOOKINGS:
                if (ab != null) {
                    ab.setTitle(getString(R.string.appointments));
                    ab.setDisplayShowCustomEnabled(false);
                    ab.setDisplayHomeAsUpEnabled(true);
                    ab.setDisplayShowTitleEnabled(true);

                    mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

                    setBookingsFilter();
                }
                break;
        }
        mUiState = state;
        invalidateOptionsMenu();
    }

    @Override
    public void onFilterChanged(View view, int position) {
        setCategoryFilter(position);
        mFiltersList.getAdapter().notifyDataSetChanged(); // update highlight
        mDrawer.closeDrawer(GravityCompat.END);
    }

    private void setCategoryFilter(int position) {
        if (position == 0) {
            mAdapter.setData(realm.where(Need.class).findAll());
        } else {
            mAdapter.setData(realm.where(Need.class)
                    .equalTo("category", position - 1).findAll());
        }
        // save state
        mFilterState = position;
    }

    private void setBookingsFilter() {
        RealmResults<Need> needs = realm.where(Need.class).equalTo("isAttending", true).findAll();
        if (needs.isEmpty()) {
            mProgressBar.setVisibility(View.GONE);
            mEmptyTextView.setVisibility(View.VISIBLE);
        }
        mAdapter.setData(needs);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case UiUtils.RC_LOGIN:
                if (resultCode == RESULT_OK) {
                    hasUser = true;
                    DataService.requestNeeds(this);
                    invalidateOptionsMenu();
                }
                break;
            case UiUtils.RC_DETAIL:
                if (resultCode == RESULT_OK) {
                    if (data.getBooleanExtra(UiUtils.EXTRA_BOOKING_CHANGED, false))
                        mAdapter.notifyDataSetChanged();
                    if (data.getBooleanExtra(UiUtils.EXTRA_NEW_USER, false))
                        hasUser = true;
                    invalidateOptionsMenu();
                }
                break;
        }
    }

    private void handleSignIn(String error) {
        if (error == null) {
            hasUser = true;
            DataService.requestNeeds(this);
            invalidateOptionsMenu();
        }
    }

    private BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String action = intent.getAction();
            String error = intent.getStringExtra(DataService.EXTRA_ERROR);
            if (ACTION_REQUEST_NEEDS.equals(action)) {
                handleNeedsResult(error);
            } else if (ACTION_REQUEST_BOOKINGS.equals(action)) {
                handleBookingsResult(error);
            } else if (ACTION_SIGN_IN.equals(action)) {
                handleSignIn(error);
            }
        }
    };

    private void handleNeedsResult(String error) {
        if (hasUser && error == null) {
            DataService.requestBookings(MainActivity.this);
        } else if (error == null) {
            notifyDatasetChanged();
        } else {
            // TODO: Handle errors in a better way
            Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
            mRetryButton.setVisibility(View.VISIBLE);
            mRetryButton.setEnabled(true);
        }
    }

    private void handleBookingsResult(String error) {
        if (error == null) {
            notifyDatasetChanged();
        } else if (error.equals("401")) {
            // Obtain a valid token by auto login user
            DataService.signIn(MainActivity.this, user.getEmail(), user.getPassword());
        } else {
            // TODO: Handle errors in a better way
            Toast.makeText(MainActivity.this, error, Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
            mRetryButton.setVisibility(View.VISIBLE);
            mRetryButton.setEnabled(true);
        }
    }

    private void notifyDatasetChanged() {
        realm.refresh();
        if (mUiState == UI_STATE_BOOKINGS) {
            setBookingsFilter();
        } else {
            setCategoryFilter(mFilterState);
        }
    }

    public void onRetry(View v) {
        mRetryButton.setEnabled(false);
        mRetryButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        DataService.requestNeeds(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDataReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mDataReceiver, DataService.getDataResultIntentFilter());
        //LocationService.requestLocation(this);
        DataService.requestNeeds(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(STATE_UI, mUiState);
        savedInstanceState.putInt(STATE_FILTER, mFilterState);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.END)) {
            mDrawer.closeDrawer(GravityCompat.END);
        } else if (mUiState == UI_STATE_BOOKINGS) {
            setUiState(UI_STATE_HOME);
        } else {
            super.onBackPressed();
        }
    }

    /*    *//**
     * Permissions request errors callback
     *//*
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

    *//**
     * Request the fine location permission from the user
     *//*
    private void requestFineLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQ);
    }

    *//**
     * Run when fine location permission has been granted
     *//*
    private void fineLocationPermissionGranted() {
        LocationService.requestLocation(this);
    }

    *//**
     * Show a permission explanation snackbar
     *//*
    private void showPermissionSnackbar() {
        // TODO: yell at user
*//*        Snackbar.make(
                findViewById(R.id.container), R.string.permission_explanation, Snackbar.LENGTH_LONG)
                .setAction(R.string.permission_explanation_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestFineLocationPermission();
                    }
                })
                .show();*//*
    }*/
}
