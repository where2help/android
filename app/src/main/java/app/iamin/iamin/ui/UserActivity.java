package app.iamin.iamin.ui;

import android.content.Intent;
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

import app.iamin.iamin.R;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.data.model.User;
import app.iamin.iamin.ui.widget.CustomRecyclerView;
import app.iamin.iamin.util.DataUtils;
import app.iamin.iamin.util.UiUtils;
import io.realm.Realm;
import io.realm.RealmResults;

public class UserActivity extends AppCompatActivity {

    private CustomRecyclerView mNeedsView;
    private RecyclerView.LayoutManager mLayoutManager;
    private NeedFeedAdapter mAdapter;

    private ImageButton mRetryButton;
    private ProgressBar mProgressBar;
    private TextView mEmptyTextView;

    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        User user = DataUtils.getUser(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mAdapter = new NeedFeedAdapter(this);
        mLayoutManager = new LinearLayoutManager(this);

        realm = Realm.getInstance(this);
        RealmResults<Need> needs = realm.where(Need.class).equalTo("isAttending", true).findAll();
        mAdapter.setData(needs);

        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);

        mNeedsView = (CustomRecyclerView) findViewById(R.id.recycler_view);
        mNeedsView.setLayoutManager(mLayoutManager);
        mNeedsView.setEmptyView(findViewById(R.id.empty_view));
        mNeedsView.addItemDecoration(itemDecoration);
        mNeedsView.setAdapter(mAdapter);

        mRetryButton = (ImageButton) findViewById(R.id.retry_button);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mEmptyTextView = (TextView) findViewById(R.id.empty_message);

        TextView titleTextView = (TextView) findViewById(R.id.header_title);
        titleTextView.setText(user.getFirstName());
    }

    public void onRetry(View v) {
        mRetryButton.setEnabled(false);
        mRetryButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case UiUtils.RC_DETAIL:
                if (resultCode == RESULT_OK) {
                    if (data.getBooleanExtra(UiUtils.EXTRA_BOOKING_CHANGED, false))
                        mAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

/*    public void onVolunteeringsUpdate(CreateVolunteeringEvent event) {
        if (event.getErrors().size() == 0) {
            RealmResults<Need> needs = realm.where(Need.class).equalTo("isAttending", true).findAll();
            if (needs.isEmpty()) {
                mProgressBar.setVisibility(View.GONE);
                mEmptyTextView.setVisibility(View.VISIBLE);
            } else {
                mAdapter.setData(needs);
            }
        } else {
            mProgressBar.setVisibility(View.GONE);
            mRetryButton.setVisibility(View.VISIBLE);
            mRetryButton.setEnabled(true);
        }
    }*/

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_right, R.anim.leave_left);
    }
}
