package app.iamin.iamin.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import app.iamin.iamin.BusProvider;
import app.iamin.iamin.LoginTask;
import app.iamin.iamin.R;
import app.iamin.iamin.RegisterTask;
import app.iamin.iamin.event.LoginEvent;
import app.iamin.iamin.event.RegisterEvent;
import app.iamin.iamin.model.User;
import app.iamin.iamin.util.EndpointUtils;
import app.iamin.iamin.util.UiUtils;

public class UserActivity extends AppCompatActivity {

    private static final int UI_MODE_LOGIN = 0;
    private static final int UI_MODE_USER = 1;
    private static final int UI_MODE_PROGRESS = 2;

    private int uiMode = 0;

    TextView titleTextView;
    TextView descTextView;

    EditText emailEditText;
    EditText passwordEditText;

    LinearLayout userScreen;
    LinearLayout progressScreen;
    LinearLayout btnBar;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        user = EndpointUtils.getUser(this);
        uiMode = user.getEmail() == null ? UI_MODE_LOGIN : UI_MODE_USER;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userScreen = (LinearLayout) findViewById(R.id.login_screen);
        progressScreen = (LinearLayout) findViewById(R.id.progress_screen);
        titleTextView = (TextView) findViewById(R.id.header_title);
        descTextView = (TextView) findViewById(R.id.header_desc);
        emailEditText = (EditText) findViewById(R.id.email);
        passwordEditText = (EditText) findViewById(R.id.password);
        btnBar = (LinearLayout) findViewById(R.id.btnBar);

        setUiMode(uiMode);
    }

    private void setUiMode(int uiMode) {
        switch(uiMode) {
            case UI_MODE_LOGIN:
                userScreen.setVisibility(View.VISIBLE);
                progressScreen.setVisibility(View.GONE);
                emailEditText.setVisibility(View.VISIBLE);
                passwordEditText.setVisibility(View.VISIBLE);
                btnBar.setVisibility(View.VISIBLE);

                titleTextView.setText(R.string.welcome);
                descTextView.setText(R.string.login_message);
                break;
            case UI_MODE_USER:
                userScreen.setVisibility(View.VISIBLE);
                progressScreen.setVisibility(View.GONE);
                emailEditText.setVisibility(View.GONE);
                passwordEditText.setVisibility(View.GONE);
                btnBar.setVisibility(View.GONE);

                titleTextView.setText(user.getFirstName());
                descTextView.setText(R.string.empty_message);
                break;
            case UI_MODE_PROGRESS:
                userScreen.setVisibility(View.GONE);
                progressScreen.setVisibility(View.VISIBLE);
                break;
        }
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

    public void onActionRegister(View view) {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            new RegisterTask(email, password, password).execute(this);
            setUiMode(UI_MODE_PROGRESS);
        }
    }

    public void onActionLogin(View view) {
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            new LoginTask(email, password).execute(this);
            setUiMode(UI_MODE_PROGRESS);
        }
    }

    @Subscribe
    public void onRegisterUpdate(RegisterEvent event) {
        boolean isSuccsess = event.isSuccsess(); // TODO: Handle errors
        if (isSuccsess) {
            Toast.makeText(this, "SUCCSESS... LOG IN!", Toast.LENGTH_LONG).show();
            setUiMode(UI_MODE_LOGIN);
        }
    }

    @Subscribe
    public void onLoginUpdate(LoginEvent event) {
        boolean isSuccsess = event.isSuccsess(); // TODO: Handle errors
        if (isSuccsess) {
            user = EndpointUtils.getUser(this);
            setUiMode(UI_MODE_USER);
        }
    }
}
