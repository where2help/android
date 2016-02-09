package app.iamin.iamin.ui;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import app.iamin.iamin.R;
import app.iamin.iamin.data.BusProvider;
import app.iamin.iamin.data.DataManager;
import app.iamin.iamin.data.event.ConnectedEvent;
import app.iamin.iamin.data.event.DisconnectedEvent;
import app.iamin.iamin.data.event.RefreshEvent;
import app.iamin.iamin.data.event.RequestBookingsEvent;
import app.iamin.iamin.data.event.RequestNeedsEvent;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.ui.widget.CustomRecyclerView;
import app.iamin.iamin.ui.widget.CustomSwipeRefreshLayout;
import app.iamin.iamin.util.UiUtils;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import static app.iamin.iamin.data.DataManager.ON_ERROR;
import static app.iamin.iamin.data.DataManager.ON_NEXT;
import static app.iamin.iamin.data.DataManager.ON_START;

public class MainActivity extends AppCompatActivity implements
        ActivityCompat.OnRequestPermissionsResultCallback,
        FilterAdapter.FilterChangedListener,
        SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "MainActivity";

    private static final String STATE_UI = "uiState";
    private static final String STATE_CATEGORY = "categoryFilterState";
    private static final String STATE_CITY = "cityFilterState";

    private static final int UI_STATE_HOME = 0;
    private static final int UI_STATE_BOOKINGS = 1;

    private int mUiState = UI_STATE_HOME;
    private int mFilterCategory = 0;
    private String mFilterCity;

    private boolean hasUser;

    private CustomRecyclerView mRecyclerView;

    private NeedFeedAdapter mNeedFeedAdapter;

    private TextView mEmptyTextView;

    private DrawerLayout mDrawer;

    private FilterAdapter mFilterAdapter;

    private RecyclerView mFiltersList;

    private CustomSwipeRefreshLayout mSwipeRefreshLayout;

    private DataManager mDataManager;

    private Realm mRealm;

    private RealmChangeListener mRealmChangeListener;

    private Snackbar mSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        mDataManager = DataManager.getInstance(this);
        hasUser = mDataManager.hasUser();

        mRealm = Realm.getInstance(this);
        mRealmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                notifyDataSetChanged();
            }
        };
        mRealm.addChangeListener(mRealmChangeListener);

        if (savedInstanceState != null) {
            mUiState = savedInstanceState.getInt(STATE_UI);
            mFilterCategory = savedInstanceState.getInt(STATE_CATEGORY);
            mFilterCity = savedInstanceState.getString(STATE_CITY);
        }

        mDrawer = (DrawerLayout) findViewById(R.id.drawer);

        mNeedFeedAdapter = new NeedFeedAdapter(this);

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);

        mSwipeRefreshLayout = (CustomSwipeRefreshLayout) findViewById(R.id.swiperefresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (CustomRecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setEmptyView(findViewById(R.id.empty_view));
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setAdapter(mNeedFeedAdapter);

        mEmptyTextView = (TextView) findViewById(R.id.empty_message);

        mFilterAdapter = new FilterAdapter(this, mFilterCategory, this);

        mFiltersList = (RecyclerView) findViewById(R.id.filters);
        mFiltersList.setAdapter(mFilterAdapter);

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
                UiUtils.fireLoginIntent(MainActivity.this);
                return true;
            case R.id.menu_refresh:
                onRefresh();
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

                    setFilter();
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

    private void notifyDataSetChanged() {
        Log.d(TAG, "notifyDataSetChanged() ");
        if (mUiState == UI_STATE_BOOKINGS) {
            setBookingsFilter();
        } else {
            setFilter();
        }
    }

    @Override
    public void onFilterCategoryChanged(View view, int category) {
        mFilterCategory = category;
        setFilter();
        mFiltersList.getAdapter().notifyDataSetChanged(); // update highlight
        mDrawer.closeDrawer(GravityCompat.END);
    }

    @Override
    public void onFilterCityChanged(View view, String city) {
        mFilterCity = city;
        setFilter();
        mDrawer.closeDrawer(GravityCompat.END);
    }

    private void setFilter() {
        RealmQuery<Need> query = mRealm.where(Need.class);

        if (mFilterCategory != 0) {
            query.equalTo("category", mFilterCategory - 1);
        }

        if (mFilterCity != null && !mFilterCity.isEmpty()) {
            query.equalTo("city", mFilterCity);
        }

        RealmResults<Need> needs = query.findAll();

        if (needs.isEmpty()) {
            Log.d(TAG, "setCategoryFilter(): needs.isEmpty()");
            mEmptyTextView.setVisibility(View.GONE);
        }

        mFilterAdapter.setCityList(getCities(needs));
        mNeedFeedAdapter.setData(needs);
    }

    private String[] getCities(RealmResults<Need> needs) {
        List<String> cities = new ArrayList<>();

        for (Need need : needs) {
            cities.add(need.getCity());
        }

        Set<String> set = new HashSet<>(cities);
        return set.toArray(new String[set.size()]);
    }

    private void setBookingsFilter() {
        RealmResults<Need> needs = mRealm.where(Need.class).equalTo("isAttending", true).findAll();
        if (needs.isEmpty() && mDataManager.hasBookings()) {
            mEmptyTextView.setVisibility(View.VISIBLE);
        } else if (needs.isEmpty()) {
            mEmptyTextView.setVisibility(View.GONE);
        }
        mNeedFeedAdapter.setData(needs);
    }

    @Subscribe
    public void onRequestNeeds(RequestNeedsEvent event) {
        Log.d(TAG, "onNeedsUpdated");

        switch (event.status) {
            case ON_START:
                break;
            case ON_NEXT:
                break;
            case ON_ERROR:
                onError(event.error);
                break;
        }
    }

    @Subscribe
    public void onRequestBookings(RequestBookingsEvent event) {
        Log.d(TAG, "onBookingsUpdated");

        switch (event.status) {
            case ON_START:
                break;
            case ON_NEXT:
                break;
            case ON_ERROR:
                onError(event.error);
                break;
        }
    }

    @Subscribe
    public void onRefreshEvent(RefreshEvent event) {
        mSwipeRefreshLayout.setRefreshing(event.refresh);
    }

    @Override
    public void onRefresh() {
        mDataManager.requestNeeds();
    }

    public void onError(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
    }

    @Subscribe
    public void onConnected(ConnectedEvent event) {
        Log.d(TAG, "onConnected");
        if (mSnackbar != null) {
            mSnackbar.dismiss();
        }
    }

    @Subscribe
    public void onDisconnected(DisconnectedEvent event) {
        Log.d(TAG, "onDisconnected");
        mSnackbar = Snackbar.make(mRecyclerView, "Warte auf Verbindung...", Snackbar.LENGTH_INDEFINITE);
        mSnackbar.getView().setBackgroundResource(R.color.colorPrimary);
        mSnackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        mDataManager.register();

        // It's possible that the user signed in or out
        if (hasUser != mDataManager.hasUser()) {
            hasUser = mDataManager.hasUser();
            invalidateOptionsMenu();
            notifyDataSetChanged();
        }

        if (mDataManager.isRefreshing() && !mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
        mDataManager.unregister();
        mSwipeRefreshLayout.setRefreshing(false);
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(STATE_UI, mUiState);
        savedInstanceState.putInt(STATE_CATEGORY, mFilterCategory);
        savedInstanceState.putString(STATE_CITY, mFilterCity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.removeChangeListener(mRealmChangeListener);
        mRealm.close();
    }
}
