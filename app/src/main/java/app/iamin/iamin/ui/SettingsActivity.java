package app.iamin.iamin.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
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
import app.iamin.iamin.data.event.UserSignOutEvent;
import app.iamin.iamin.data.model.User;
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

        setUiMode(DataManager.getInstance(this).hasUser());

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
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
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);

    }

    public void onEditEndpoint(View view) {
        DataUtils.showEndpointInputPicker(SettingsActivity.this);
    }

    public void onDeleteToken(View view) {
        DataUtils.clearToken(SettingsActivity.this);
        Toast.makeText(this, "Token deleted!", Toast.LENGTH_SHORT).show();
    }

    public void onActionSignOut(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.confirm);
        builder.setMessage(R.string.sign_out_message);
        builder.setPositiveButton(R.string.action_yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                DataManager.getInstance(SettingsActivity.this).signOut();
            }
        });
        builder.setNegativeButton(R.string.action_no, new DialogInterface.OnClickListener() {
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
        setUiMode(false);
    }

    public void onActionTerms(View view) {
        Intent intent = new Intent();
        intent.setClass(this, TermsActivity.class);
        startActivity(intent);
    }
}
