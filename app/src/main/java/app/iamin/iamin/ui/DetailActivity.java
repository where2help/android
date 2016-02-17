package app.iamin.iamin.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.NestedScrollView.OnScrollChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import app.iamin.iamin.R;
import app.iamin.iamin.data.BusProvider;
import app.iamin.iamin.data.DataManager;
import app.iamin.iamin.data.event.CancelBookingEvent;
import app.iamin.iamin.data.event.ConnectedEvent;
import app.iamin.iamin.data.event.CreateBookingEvent;
import app.iamin.iamin.data.event.DisconnectedEvent;
import app.iamin.iamin.data.event.PromptLoginEvent;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.ui.widget.CustomMapView;
import app.iamin.iamin.ui.widget.NeedView;
import app.iamin.iamin.util.TimeUtils;
import app.iamin.iamin.util.UiUtils;
import io.realm.Realm;
import io.realm.RealmChangeListener;

import static android.support.design.widget.Snackbar.LENGTH_INDEFINITE;
import static app.iamin.iamin.data.DataManager.ON_ERROR;
import static app.iamin.iamin.data.DataManager.ON_NEXT;

/**
 * Created by Markus on 10.10.15.
 */
public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";

    private static final int UI_MODE_DEFAULT = 0;
    private static final int UI_MODE_BOOKING = 1;
    private static final int UI_MODE_ATTENDING = 2;
    private static final int UI_MODE_EMPTY = 4;

    private int uiMode = 0;

    private ActionBar actionBar;

    private NestedScrollView container;

    private NeedView needView;

    private Button bookingButton;
    private ProgressBar progressBar;

    private TextView infoTextView;
    private TextView descTextView;
    private TextView organizationTextView;

    private DataManager dataManager;

    private Realm realm;
    private RealmChangeListener realmChangeListener;

    private Need need;

    private CustomMapView mapView;

    private OnScrollChangeListener scrollChangeListener = new OnScrollChangeListener() {
        @Override
        public void onScrollChange(NestedScrollView v, int x, int y, int oldX, int oldY) {
            if (mapView != null) mapView.setOffset(y);
        }
    };

    private Snackbar snackbar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mapView = (CustomMapView) findViewById(R.id.map);

        findViewById(R.id.dummy).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                mapView.dispatchTouchEvent(event);
                return true;
            }
        });

        needView = (NeedView) findViewById(R.id.need_view);
        infoTextView = (TextView) findViewById(R.id.info);

        bookingButton = (Button) findViewById(R.id.booking_button);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        descTextView = (TextView) findViewById(R.id.desc);
        organizationTextView = (TextView) findViewById(R.id.organization);

        container = (NestedScrollView) findViewById(R.id.container);
        container.setOnScrollChangeListener(scrollChangeListener);

        int needId = getIntent().getExtras().getInt("id");

        dataManager = DataManager.getInstance(this);

        realm = Realm.getDefaultInstance();
        need = realm.where(Need.class).equalTo("id", needId).findFirst();

        realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                Log.d(TAG, "RealmChangeListener: onChange()");
                if (need == null || !need.isValid()) {
                    setUiMode(UI_MODE_EMPTY);
                }
                setNeed();
            }
        };
        realm.addChangeListener(realmChangeListener);

        setNeed();

        findUiMode();
        setUiMode(uiMode);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mapView != null) {
                    mapView.create(); // ●～*
                }
            }
        }, 300);
    }

    private void setNeed() {
        if (need == null || !need.isValid()) {
            infoTextView.setText("Hoppla!\n\nDieser Termin wurde verlegt oder abgesagt.");
            return;
        }

        if (actionBar != null) {
            actionBar.setTitle(need.getOrganization());
        }

        mapView.setNeed(need);

        needView.setInDetail(true);
        needView.setNeed(need);

        infoTextView.setText(getString(R.string.message_thank_you,
                TimeUtils.formatTimeOfDay(need.getStart())));

        descTextView.setText(need.getDescription());

        organizationTextView.setText(getString(R.string.organization, need.getOrganization()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_share:
                UiUtils.fireShareIntent(DetailActivity.this, need);
                return true;
            case R.id.menu_calendar:
                UiUtils.fireCalendarIntent(DetailActivity.this, need);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void findUiMode() {
        if (need != null && need.isValid()) {
            if (dataManager.isBooking(need)) {
                uiMode = UI_MODE_BOOKING;
            } else if (need.isAttending() && dataManager.hasUser()) {
                uiMode = UI_MODE_ATTENDING;
            } else {
                uiMode = UI_MODE_DEFAULT;
            }
        } else {
            uiMode = UI_MODE_EMPTY;
        }
    }

    private void setUiMode(int mode) {
        switch (mode) {
            case UI_MODE_DEFAULT:
                mapView.setVisibility(View.VISIBLE);
                needView.setVisibility(View.VISIBLE);
                infoTextView.setVisibility(View.GONE);

                progressBar.setVisibility(View.GONE);

                bookingButton.setVisibility(View.VISIBLE);
                bookingButton.setEnabled(true);
                bookingButton.setText(R.string.action_help);

                descTextView.setVisibility(View.VISIBLE);
                organizationTextView.setVisibility(View.VISIBLE);
                break;
            case UI_MODE_BOOKING:
                if (need.isAttending()) {
                    // canceling for this need is ongoing
                    bookingButton.setText("");
                    bookingButton.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    // booking for this need is ongoing
                    bookingButton.setText("");
                    bookingButton.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                }
                break;
            case UI_MODE_ATTENDING:
                mapView.setVisibility(View.VISIBLE);
                needView.setVisibility(View.VISIBLE);
                infoTextView.setVisibility(View.VISIBLE);

                progressBar.setVisibility(View.GONE);

                bookingButton.setVisibility(View.VISIBLE);
                bookingButton.setText(R.string.action_cancel);
                bookingButton.setEnabled(true);

                descTextView.setVisibility(View.VISIBLE);
                organizationTextView.setVisibility(View.VISIBLE);
                break;
            case UI_MODE_EMPTY:
                mapView.setVisibility(View.GONE);
                needView.setVisibility(View.GONE);
                infoTextView.setVisibility(View.VISIBLE);
                bookingButton.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                descTextView.setVisibility(View.GONE);
                organizationTextView.setVisibility(View.GONE);
                break;
        }

        uiMode = mode;
    }

    public void onActionSubmit(View view) {
        switch (uiMode) {
            case UI_MODE_DEFAULT:
                setUiMode(UI_MODE_BOOKING);
                dataManager.createBooking(need);
                break;
            case UI_MODE_ATTENDING:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.cancel_dialog_title);
                builder.setMessage(R.string.cancel_dialog_message);
                builder.setNegativeButton(getString(R.string.action_no), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Do nothing
                    }
                });
                builder.setPositiveButton(getString(R.string.action_yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        setUiMode(UI_MODE_BOOKING);
                        dataManager.cancelBooking(need);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                break;
        }
    }

    @Subscribe
    public void onCreateBooking(CreateBookingEvent event) {
        if (need.getId() != event.id) return;
        Log.d(TAG, "onCreateBooking");

        switch (event.status) {
            case ON_NEXT:
                setUiMode(UI_MODE_ATTENDING);
                break;
            case ON_ERROR:
                Toast.makeText(DetailActivity.this, event.error, Toast.LENGTH_SHORT).show();
                setUiMode(UI_MODE_DEFAULT);
                break;
        }
    }

    @Subscribe
    public void onCancelBooking(CancelBookingEvent event) {
        if (need.getId() != event.id) return;
        Log.d(TAG, "onCancelBooking");

        switch (event.status) {
            case ON_NEXT:
                setUiMode(UI_MODE_DEFAULT);
                break;
            case ON_ERROR:
                Toast.makeText(DetailActivity.this, event.error, Toast.LENGTH_SHORT).show();
                setUiMode(UI_MODE_ATTENDING);
                break;
        }
    }

    @Subscribe
    public void onPromptLogin(PromptLoginEvent event) {
        Log.d(TAG, "onPromptLogin");

        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_CANCELED) {
            // prompt login canceled
            setUiMode(UI_MODE_DEFAULT);
        }
    }

    @Subscribe
    public void onConnected(ConnectedEvent event) {
        if (snackbar != null) {
            snackbar.dismiss();
        }
    }

    @Subscribe
    public void onDisconnected(DisconnectedEvent event) {
        snackbar = Snackbar.make(container, "Warte auf Verbindung ...", LENGTH_INDEFINITE);
        snackbar.getView().setBackgroundResource(R.color.colorPrimary);
        snackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        dataManager.register();
        mapView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
        dataManager.unregister();
        mapView.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.destroy();
        realm.removeChangeListener(realmChangeListener);
        realm.close();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.saveInstanceState();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.lowMemory();
    }
}
