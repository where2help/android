package app.iamin.iamin.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
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
import app.iamin.iamin.util.UiUtils;

public class LoginActivity extends AppCompatActivity {

    private static final int UI_MODE_LOGIN = 0;
    private static final int UI_MODE_PROGRESS = 1;

    private int uiMode = UI_MODE_LOGIN;

    TextView titleTextView;
    TextView descTextView;

    EditText emailEditText;
    EditText passwordEditText;

    LinearLayout userScreen;
    LinearLayout progressScreen;
    LinearLayout btnBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
            case UI_MODE_PROGRESS:
                userScreen.setVisibility(View.GONE);
                progressScreen.setVisibility(View.VISIBLE);
                break;
        }
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
            // TODO: Launch MainActivity
            UiUtils.fireMainIntent(this);
            // overridePendingTransition(R.anim.enter_right, R.anim.leave_left);
            finish();
        }
    }
}