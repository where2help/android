package app.iamin.iamin.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import app.iamin.iamin.BusProvider;
import app.iamin.iamin.LogoutTask;
import app.iamin.iamin.R;
import app.iamin.iamin.event.LogoutEvent;
import app.iamin.iamin.model.User;
import app.iamin.iamin.service.UtilityService;
import app.iamin.iamin.util.EndpointUtils;
import app.iamin.iamin.util.UiUtils;

public class SettingsActivity extends AppCompatActivity {

    private TextView usernameTextView;
    private TextView emailTextView;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        user = EndpointUtils.getUser(this);

        usernameTextView = (TextView) findViewById(R.id.username);
        usernameTextView.setText(user.getFirstName() + " " + user.getLastName());

        emailTextView = (TextView) findViewById(R.id.email);
        emailTextView.setText(user.getEmail());

        setUiMode(user.getEmail() != null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUiMode(boolean hasUser) {
        findViewById(R.id.user).setVisibility(hasUser ? View.VISIBLE : View.GONE);
        findViewById(R.id.logout).setVisibility(hasUser ? View.VISIBLE : View.GONE);
        findViewById(R.id.communications).setVisibility(hasUser ? View.VISIBLE : View.GONE);
        findViewById(R.id.email_switch).setVisibility(hasUser ? View.VISIBLE : View.GONE);
        findViewById(R.id.divider1).setVisibility(hasUser ? View.VISIBLE : View.GONE);
        findViewById(R.id.divider2).setVisibility(hasUser ? View.VISIBLE : View.GONE);
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
        EndpointUtils.showEndpointInputPicker(SettingsActivity.this);
    }

    public void onFireMissile(View view) {
        UtilityService.triggerNotification(SettingsActivity.this);
    }

    public void onActionLogout(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Do you really want to sign out?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                new LogoutTask().execute(SettingsActivity.this);
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Subscribe
    public void onLogoutUpdate(LogoutEvent event) {
        if (event.isSuccsess()) {
            EndpointUtils.clearUser(this);
            UiUtils.fireLoginIntent(this);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        } else {
            // TODO: Handle errors
            Toast.makeText(this, "Error! Try again!", Toast.LENGTH_SHORT).show();
        }
    }
}
