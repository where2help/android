package app.iamin.iamin.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import app.iamin.iamin.BusProvider;
import app.iamin.iamin.PullAppointmentsTask;
import app.iamin.iamin.R;
import app.iamin.iamin.event.AppointmentsEvent;
import app.iamin.iamin.model.Need;
import app.iamin.iamin.model.User;
import app.iamin.iamin.util.EndpointUtils;
import app.iamin.iamin.util.UiUtils;

public class UserActivity extends AppCompatActivity {

    private NeedsRecyclerView mNeedsView;
    private RecyclerView.LayoutManager mLayoutManager;
    private NeedsAdapter mAdapter;

    private ImageButton mRetryButton;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        User user = EndpointUtils.getUser(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        new PullAppointmentsTask(this).execute();

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

        TextView titleTextView = (TextView) findViewById(R.id.header_title);
        titleTextView.setText(user.getFirstName());
    }

    @Subscribe
    public void onNeedsUpdate(AppointmentsEvent event) {
        Need[] needs = event.getNeeds();
        if (needs != null) {
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
        new PullAppointmentsTask(this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.action_settings:
                UiUtils.fireSettingsIntent(UserActivity.this);
                overridePendingTransition(R.anim.enter_left, R.anim.leave_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_right, R.anim.leave_left);
    }
}
