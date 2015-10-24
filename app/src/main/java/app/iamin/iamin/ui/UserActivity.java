package app.iamin.iamin.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import app.iamin.iamin.BusProvider;
import app.iamin.iamin.R;
import app.iamin.iamin.model.User;
import app.iamin.iamin.util.EndpointUtils;
import app.iamin.iamin.util.UiUtils;

public class UserActivity extends AppCompatActivity {

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

        TextView titleTextView = (TextView) findViewById(R.id.header_title);
        titleTextView.setText(user.getFirstName());

        TextView descTextView = (TextView) findViewById(R.id.header_desc);
        descTextView.setText(R.string.empty_message);
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
