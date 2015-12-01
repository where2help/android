package app.iamin.iamin.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import app.iamin.iamin.R;
import app.iamin.iamin.data.BusProvider;
import app.iamin.iamin.data.DataManager;
import app.iamin.iamin.data.event.BookingsUpdatedEvent;
import app.iamin.iamin.data.event.ConnectedEvent;
import app.iamin.iamin.data.event.DisconnectedEvent;
import app.iamin.iamin.data.event.NeedFeedUpdatedEvent;
import app.iamin.iamin.data.event.NeedsUpdatedEvent;
import app.iamin.iamin.data.event.UserSignInEvent;
import app.iamin.iamin.data.event.UserSignOutEvent;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.ui.widget.CustomRecyclerView;
import app.iamin.iamin.util.UiUtils;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

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

    private CustomRecyclerView mNeedFeedRecyclerView;
    private NeedFeedAdapter mNeedFeedAdapter;

    private ProgressBar mProgressBar;
    private TextView mEmptyTextView;

    private DrawerLayout mDrawer;
    private RecyclerView mFiltersList;

    private Realm realm;
    private RealmChangeListener realmChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        BusProvider.getInstance().register(this);

        realm = Realm.getInstance(this);
        realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                Log.d(TAG, "RealmChangeListener: onChange()");
                notifyDataSetChanged();
            }
        };
        realm.addChangeListener(realmChangeListener);

        if (savedInstanceState != null) {
            mUiState = savedInstanceState.getInt(STATE_UI);
            mFilterState = savedInstanceState.getInt(STATE_FILTER);
        }

        Log.e(TAG, STATE_FILTER + ": " + mFilterState);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer);

        mNeedFeedAdapter = new NeedFeedAdapter(this);

/*        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);*/

        mNeedFeedRecyclerView = (CustomRecyclerView) findViewById(R.id.recycler_view);
        mNeedFeedRecyclerView.setEmptyView(findViewById(R.id.empty_view));
        //mNeedFeedRecyclerView.addItemDecoration(itemDecoration);
        mNeedFeedRecyclerView.setAdapter(mNeedFeedAdapter);

        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mEmptyTextView = (TextView) findViewById(R.id.empty_message);

        mFiltersList = (RecyclerView) findViewById(R.id.filters);
        mFiltersList.setAdapter(new FilterAdapter(this, mFilterState, this));

        setUiState(mUiState);
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
                boolean hasUser = DataManager.hasUser;
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
            mNeedFeedAdapter.setData(realm.where(Need.class).findAll());
        } else {
            mNeedFeedAdapter.setData(realm.where(Need.class)
                    .equalTo("category", position - 1).findAll());
        }
        mProgressBar.setVisibility(View.VISIBLE);
        mEmptyTextView.setVisibility(View.GONE);
        // save state
        mFilterState = position;
    }

    private void notifyDataSetChanged() {
        if (mUiState == UI_STATE_BOOKINGS) {
            setBookingsFilter();
        } else {
            setCategoryFilter(mFilterState);
        }
    }

    private void setBookingsFilter() {
        RealmResults<Need> needs = realm.where(Need.class).equalTo("isAttending", true).findAll();
        if (needs.isEmpty() && DataManager.hasBookings) {
            mEmptyTextView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        } else if (needs.isEmpty()) {
            mProgressBar.setVisibility(View.VISIBLE);
            mEmptyTextView.setVisibility(View.GONE);
        }
        mNeedFeedAdapter.setData(needs);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.getInstance().register(this);
        // It is possible that the user signed out vie SettingsActivity
        invalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        DataManager.getInstance().unregister(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
        realm.removeAllChangeListeners();
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

    @Subscribe
    public void onConnected(ConnectedEvent event) {
        Log.d(TAG, "onConnected");
    }

    @Subscribe
    public void onDisconnected(DisconnectedEvent event) {
        Log.d(TAG, "onDisconnected");
    }

    @Subscribe
    public void onUserSignIn(UserSignInEvent event) {
        Log.d(TAG, "onUserSignIn");
        if (event.error == null) {
            invalidateOptionsMenu();
        } else {
            // error
        }
    }

    @Subscribe
    public void onUserSignOut(UserSignOutEvent event) {
        Log.d(TAG, "onUserSignOut");
        if (event.error == null) {
            invalidateOptionsMenu();
        } else {
            // error
        }
    }

    @Subscribe
    public void onNeedFeedUpdated(NeedFeedUpdatedEvent event) {
        Log.d(TAG, "onNeedFeedUpdated");
        if (event.error == null) {
        } else {
            mNeedFeedAdapter.setData(null);
            onError(event.error);
        }
    }

    @Subscribe
    public void onNeedsUpdated(NeedsUpdatedEvent event) {
        Log.d(TAG, "onNeedsUpdated");
        if (event.error == null) {
        } else {
            mNeedFeedAdapter.setData(null);
            onError(event.error);
        }
    }

    @Subscribe
    public void onBookingsUpdated(BookingsUpdatedEvent event) {
        Log.d(TAG, "onBookingsUpdated");
        if (event.error == null) {
        } else {
            onError(event.error);
        }
    }

    public void onError(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        mProgressBar.setVisibility(View.VISIBLE);
    }
}
