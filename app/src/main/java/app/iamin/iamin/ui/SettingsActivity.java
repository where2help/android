package app.iamin.iamin.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import app.iamin.iamin.R;
import app.iamin.iamin.data.BusProvider;
import app.iamin.iamin.data.DataManager;
import app.iamin.iamin.data.event.ErrorEvent;
import app.iamin.iamin.data.event.UserSignOutEvent;
import app.iamin.iamin.data.model.User;
import app.iamin.iamin.data.service.UtilityService;
import app.iamin.iamin.util.DataUtils;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    private TextView usernameTextView;
    private TextView emailTextView;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        BusProvider.getInstance().register(this);

        user = DataUtils.getUser(this);

        usernameTextView = (TextView) findViewById(R.id.username);
        usernameTextView.setText(user.getFirstName() + " " + user.getLastName());

        emailTextView = (TextView) findViewById(R.id.email);
        emailTextView.setText(user.getEmail());

        setUiMode(DataManager.hasUser());

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
        DataManager.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        DataManager.getInstance().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public void onEditEndpoint(View view) {
        DataUtils.showEndpointInputPicker(SettingsActivity.this);
    }

    public void onFireMissile(View view) {
        UtilityService.triggerNotification(SettingsActivity.this);
    }

    public void onDeleteToken(View view) {
        DataUtils.clearToken(SettingsActivity.this);
        Toast.makeText(this, "Token deleted!", Toast.LENGTH_SHORT).show();
    }

    public void onActionSignOut(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Do you really want to sign out?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DataManager.getInstance().signOut();
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
    public void onUserSignOut(UserSignOutEvent event) {
        Log.d(TAG, "onUserSignOut");
        DataUtils.clearUser(this);
        setUiMode(false);
        Toast.makeText(this, "ByeBye!", Toast.LENGTH_SHORT).show();

        //TODO: Notify MainActivity!
    }

    @Subscribe
    public void onError(ErrorEvent event) {
        Toast.makeText(this, event.error, Toast.LENGTH_SHORT).show();
    }
}
