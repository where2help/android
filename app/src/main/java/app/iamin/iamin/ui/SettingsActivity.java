package app.iamin.iamin.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import app.iamin.iamin.R;
import app.iamin.iamin.data.model.User;
import app.iamin.iamin.data.service.DataService;
import app.iamin.iamin.data.service.UtilityService;
import app.iamin.iamin.util.DataUtils;

import static app.iamin.iamin.data.service.DataService.ACTION_SIGN_OUT;

public class SettingsActivity extends AppCompatActivity {

    private TextView usernameTextView;
    private TextView emailTextView;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        user = DataUtils.getUser(this);

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
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mDataReceiver, DataService.getDataResultIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDataReceiver);
    }

    public void onEditEndpoint(View view) {
        DataUtils.showEndpointInputPicker(SettingsActivity.this);
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
                DataService.signOut(SettingsActivity.this);
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

    private BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String action = intent.getAction();
            String error = intent.getStringExtra(DataService.EXTRA_ERROR);
            if (ACTION_SIGN_OUT.equals(action)) {
                handleSignOut(error);
            }
        }
    };

    private void handleSignOut(String error) {
        if (error == null) {
            DataUtils.clearUser(this);
            setUiMode(false);
            Toast.makeText(this, "ByeBye!", Toast.LENGTH_SHORT).show();
        } else {
            // TODO: Handle errors
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        }
    }

    public void onDeleteToken(View view) {
        DataUtils.clearToken(SettingsActivity.this);
        Toast.makeText(this, "Token deleted!", Toast.LENGTH_SHORT).show();
    }
}
