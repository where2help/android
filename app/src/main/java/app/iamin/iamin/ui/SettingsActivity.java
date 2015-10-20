package app.iamin.iamin.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import app.iamin.iamin.BusProvider;
import app.iamin.iamin.R;
import app.iamin.iamin.model.User;
import app.iamin.iamin.service.UtilityService;
import app.iamin.iamin.util.EndpointUtils;

public class SettingsActivity extends AppCompatActivity {

    private TextView usernameTextView;
    private TextView emailTextView;

    private User user;
    private boolean hasUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        user = EndpointUtils.getUser(this);
        hasUser = user.getEmail() != null;

        usernameTextView = (TextView) findViewById(R.id.username);
        usernameTextView.setText(user.getFirstName() + " " + user.getLastName());

        emailTextView = (TextView) findViewById(R.id.email);
        emailTextView.setText(user.getEmail());

        findViewById(R.id.user).setVisibility(hasUser ? View.VISIBLE : View.GONE);
        findViewById(R.id.logout).setVisibility(hasUser ? View.VISIBLE : View.GONE);
        findViewById(R.id.communications).setVisibility(hasUser ? View.VISIBLE : View.GONE);
        findViewById(R.id.email_switch).setVisibility(hasUser ? View.VISIBLE : View.GONE);
        findViewById(R.id.divider1).setVisibility(hasUser ? View.VISIBLE : View.GONE);
        findViewById(R.id.divider2).setVisibility(hasUser ? View.VISIBLE : View.GONE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.action_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
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

    public void onEditEndpoint(View view) {
        EndpointUtils.showEndpointPicker(SettingsActivity.this);
    }

    public void onFireMissile(View view) {
        UtilityService.triggerNotification(SettingsActivity.this);
    }
}
