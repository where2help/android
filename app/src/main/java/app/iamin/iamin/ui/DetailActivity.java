package app.iamin.iamin.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
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
import app.iamin.iamin.data.event.CancelBookingEvent;
import app.iamin.iamin.data.event.ConnectedEvent;
import app.iamin.iamin.data.event.CreateBookingEvent;
import app.iamin.iamin.data.event.DisconnectedEvent;
import app.iamin.iamin.data.event.PromptLoginEvent;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.ui.widget.CustomMapView;
import app.iamin.iamin.ui.widget.NeedView;
import app.iamin.iamin.util.DataUtils;
import app.iamin.iamin.util.NeedUtils;
import app.iamin.iamin.util.TimeUtils;
import app.iamin.iamin.util.UiUtils;
import io.realm.Realm;
import io.realm.RealmChangeListener;

import static android.support.design.widget.Snackbar.LENGTH_INDEFINITE;
import static app.iamin.iamin.data.DataManager.ERROR;
import static app.iamin.iamin.data.DataManager.NEXT;
import static app.iamin.iamin.data.DataManager.START;

/**
 * Created by Markus on 10.10.15.
 */
public class DetailActivity extends AppCompatActivity {

    private static final String TAG = "DetailActivity";

    private boolean isAttending = false;

    private LinearLayout btnBarLayout;
    private Button submitButton;
    private Button cancelButton;

    private TextView submitInfoTextView;
    private TextView descTextView;
    private TextView webTextView;

    private NestedScrollView container;

    private Need need;

    private NeedView needView;

    private Realm realm;
    private RealmChangeListener realmChangeListener;

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

        int needId = getIntent().getExtras().getInt("id");

        realm = Realm.getInstance(this);
        need = realm.where(Need.class).equalTo("id", needId).findFirst();

        realmChangeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                Log.d(TAG, "RealmChangeListener: onChange()");
                if (needView != null) {
                    needView.setNeed(need);
                }
                // update map
            }
        };
        realm.addChangeListener(realmChangeListener);

        isAttending = need.isAttending() && DataManager.hasUser();

        container = (NestedScrollView) findViewById(R.id.container);
        container.setOnScrollChangeListener(scrollChangeListener);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(NeedUtils.getCategoryPlural(need.getCategory()) + " - " + need.getLocation());
        toolbar.setNavigationIcon(R.drawable.ic_action_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mapView = (CustomMapView) findViewById(R.id.map);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mapView != null) {
                    mapView.setNeed(need);
                    mapView.onCreate(null);
                }
            }
        }, 300);

        needView = (NeedView) findViewById(R.id.need_view);
        needView.setNeed(need);
        needView.setInDetail(true);

        webTextView = (TextView) findViewById(R.id.link);
        webTextView.setText("Delete Token");

        descTextView = (TextView) findViewById(R.id.desc);
        descTextView.setText(need.getDescription());

        submitInfoTextView = (TextView) findViewById(R.id.info);

        btnBarLayout = (LinearLayout) findViewById(R.id.btnBar);

        submitButton = (Button) findViewById(R.id.submit);
        cancelButton = (Button) findViewById(R.id.cancel);

        setUiMode(isAttending);

        if (DataManager.getInstance().isBooking(need)) {
            if (isAttending) {
                // canceling for this need is ongoing
                cancelButton.setText("Absagen...");
                cancelButton.setEnabled(false);
            } else {
                // booking for this need is ongoing
                submitButton.setText(R.string.register_status);
                submitButton.setEnabled(false);
            }
        }
    }

    private void setUiMode(boolean isAttending) {
        if (isAttending) {
            submitButton.setText(R.string.action_share);
            submitButton.setEnabled(true);

            submitInfoTextView.setText(getString(R.string.thank_you_message, TimeUtils.formatTimeOfDay(need.getStart())));
            submitInfoTextView.setVisibility(View.VISIBLE);

            cancelButton.setText(R.string.action_iamout);
            cancelButton.setEnabled(true);

            btnBarLayout.setVisibility(View.VISIBLE);
        } else {
            submitButton.setEnabled(true);
            submitButton.setText(R.string.action_iamin);

            submitInfoTextView.setVisibility(View.GONE);
            btnBarLayout.setVisibility(View.GONE);
        }
    }

    public void onActionSubmit(View view) {
        if (!isAttending) {
            DataManager.getInstance().createBooking(need);
        } else {
            UiUtils.fireShareIntent(DetailActivity.this, need);
        }
    }

    public void onActionCancel(View view) {
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
                DataManager.getInstance().cancelBooking(need);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onActionCalendar(View view) {
        UiUtils.fireCalendarIntent(DetailActivity.this, need);
    }

    public void onActionLink(View view) {
        //UiUtils.fireWebIntent(DetailActivity.this, need);
        DataUtils.clearToken(this);
        Toast.makeText(this, "Token deleted!", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void onCreateBooking(CreateBookingEvent event) {
        if (need.getId() != event.id) return;
        Log.d(TAG, "onCreateBooking");

        switch (event.status) {
            case START:
                submitButton.setText(R.string.register_status);
                submitButton.setEnabled(false);
                break;
            case NEXT:
                isAttending = true;
                setUiMode(true);
                break;
            case ERROR:
                Toast.makeText(DetailActivity.this, event.error, Toast.LENGTH_SHORT).show();
                setUiMode(isAttending);
                break;
        }
    }

    @Subscribe
    public void onCancelBooking(CancelBookingEvent event) {
        if (need.getId() != event.id) return;
        Log.d(TAG, "onCancelBooking");

        switch (event.status) {
            case NEXT:
                isAttending = false;
                setUiMode(false);
                break;
            case ERROR:
                Toast.makeText(DetailActivity.this, event.error, Toast.LENGTH_SHORT).show();
                setUiMode(isAttending);
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
            setUiMode(isAttending);
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
        snackbar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        DataManager.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
        DataManager.getInstance().unregister(this);
    }

    @Override
    protected void onDestroy() {
        realm.removeChangeListener(realmChangeListener);
        realm.close();
        super.onDestroy();
    }
}
