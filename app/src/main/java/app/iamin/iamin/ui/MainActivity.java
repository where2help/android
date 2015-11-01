package app.iamin.iamin.ui;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.squareup.otto.Subscribe;

import app.iamin.iamin.BusProvider;
import app.iamin.iamin.PullNeedsTask;
import app.iamin.iamin.PullVolunteeringsTask;
import app.iamin.iamin.R;
import app.iamin.iamin.event.NeedsEvent;
import app.iamin.iamin.event.VolunteeringsEvent;
import app.iamin.iamin.model.Need;
import app.iamin.iamin.model.User;
import app.iamin.iamin.service.LocationService;
import app.iamin.iamin.util.EndpointUtils;
import app.iamin.iamin.util.NeedUtils;
import app.iamin.iamin.util.UiUtils;
import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback {

    private NeedsRecyclerView mNeedsView;
    private RecyclerView.LayoutManager mLayoutManager;
    private NeedsAdapter mAdapter;

    private ImageButton mRetryButton;
    private ProgressBar mProgressBar;

    private Realm realm;
    private User user;

    // private static final int PERMISSION_REQ = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getInstance(this);
        user = EndpointUtils.getUser(this);
        if (user.getEmail() == null) {
            // If we don't have a user create one
            UiUtils.fireLoginIntent(this);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else {
            setContentView(R.layout.activity_main);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.addView(LayoutInflater.from(this).inflate(R.layout.logo, toolbar, false));
            setSupportActionBar(toolbar);

            new PullNeedsTask(this).execute();
            // new PullNeedsTaskMock(this).execute();

            mAdapter = new NeedsAdapter(this);
            mLayoutManager = new LinearLayoutManager(this);

            RecyclerView.ItemDecoration itemDecoration = new
                    DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);

            mNeedsView = (NeedsRecyclerView) findViewById(R.id.recycler_view);
            mNeedsView.setLayoutManager(mLayoutManager);
            mNeedsView.setEmptyView(findViewById(R.id.empty_view));
            mNeedsView.addItemDecoration(itemDecoration);
            mNeedsView.setAdapter(mAdapter);

            mRetryButton = (ImageButton) findViewById(R.id.retry_button);
            mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);


            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            final PopupMenu popup = new PopupMenu(this, fab);
            popup.inflate(R.menu.menu_filter);
            popup.setGravity(GravityCompat.END);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.all_categories:
                            RealmResults<Need> needs = realm.where(Need.class).findAll();
                            mAdapter.setData(needs);
                            break;
                        case R.id.general:
                            mAdapter.setData(realm.where(Need.class)
                                    .equalTo("category", NeedUtils.CATEGORY_VOLUNTEER).findAll());
                            break;
                        case R.id.legal:
                            mAdapter.setData(realm.where(Need.class)
                                    .equalTo("category", NeedUtils.CATEGORY_LAWYER).findAll());
                            break;
                        case R.id.medical:
                            mAdapter.setData(realm.where(Need.class)
                                    .equalTo("category", NeedUtils.CATEGORY_DOCTOR).findAll());
                            break;
                        case R.id.translation:
                            mAdapter.setData(realm.where(Need.class)
                                    .equalTo("category", NeedUtils.CATEGORY_INTERPRETER).findAll());
                            break;
                    }
                    item.setChecked(true);
                    return true;
                }
            });

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popup.show();
                }
            });
            //more.setOnTouchListener(popup.getDragToOpenListener());

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_user:
                UiUtils.fireUserIntent(MainActivity.this);
                overridePendingTransition(R.anim.enter_left, R.anim.leave_right);
                return true;
            case R.id.action_settings:
                UiUtils.fireSettingsIntent(MainActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when fetching new needs is done. If successful this starts a PullVolunteeringsTask.
     */
    @Subscribe
    public void onNeedsUpdate(NeedsEvent event) {
        if (event.getErrors().size() == 0) {
            // All local needs are up to date
            new PullVolunteeringsTask(this).execute();
        } else {
            mProgressBar.setVisibility(View.GONE);
            mRetryButton.setVisibility(View.VISIBLE);
            mRetryButton.setEnabled(true);
        }
    }

    @Subscribe
    public void onVolunteeringsUpdate(VolunteeringsEvent event) {
        if (event.getErrors().size() == 0) {
            // All local needs have the correct isAttending status
            RealmResults<Need> needs = realm.where(Need.class).findAll();
            mAdapter.setData(needs);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mRetryButton.setVisibility(View.VISIBLE);
            mRetryButton.setEnabled(true);
        }
    }

    public void onRetry(View v) {
        mRetryButton.setEnabled(false);
        mRetryButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        new PullNeedsTask(this).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        LocationService.requestLocation(this);
        // TODO: update needs
        // new PullNeedsTask(this).execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
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
