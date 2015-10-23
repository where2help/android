package app.iamin.iamin.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.net.HttpURLConnection;

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

    EditText emailEditText;
    EditText passwordEditText;

    LinearLayout userScreen;
    LinearLayout progressScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userScreen = (LinearLayout) findViewById(R.id.login_screen);
        progressScreen = (LinearLayout) findViewById(R.id.progress_screen);
        emailEditText = (EditText) findViewById(R.id.email);
        passwordEditText = (EditText) findViewById(R.id.password);

        setUiMode(UI_MODE_LOGIN);
    }

    private void setUiMode(int uiMode) {
        switch(uiMode) {
            case UI_MODE_LOGIN:
                userScreen.setVisibility(View.VISIBLE);
                progressScreen.setVisibility(View.GONE);
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
        switch(event.status) {
            case HttpURLConnection.HTTP_OK:
                findViewById(R.id.btn_register).setVisibility(View.GONE);
                Toast.makeText(this, "Willkommen! Bitte melde Dich an!", Toast.LENGTH_LONG).show();
                setUiMode(UI_MODE_LOGIN);
                break;
            case HttpURLConnection.HTTP_FORBIDDEN:
                Toast.makeText(this, "Überprüfe Deine Angaben!", Toast.LENGTH_SHORT).show();
                // TODO: Give user more info!
                setUiMode(UI_MODE_LOGIN);
                break;
            case HttpURLConnection.HTTP_NO_CONTENT:
                Toast.makeText(this, "Keine Verbindung!", Toast.LENGTH_SHORT).show();
                setUiMode(UI_MODE_LOGIN);
                break;
            default:
                Toast.makeText(this, "Registrieren fehlgeschlagen!", Toast.LENGTH_SHORT).show();
                setUiMode(UI_MODE_LOGIN);
                break;
        }
    }

    @Subscribe
    public void onLoginUpdate(LoginEvent event) {
        switch(event.status) {
            case HttpURLConnection.HTTP_OK:
                UiUtils.fireMainIntent(this);
                finish();
                break;
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                Toast.makeText(this, "Überprüfe Deine Angaben!", Toast.LENGTH_SHORT).show();
                setUiMode(UI_MODE_LOGIN);
                break;
            case HttpURLConnection.HTTP_NO_CONTENT:
                Toast.makeText(this, "Keine Verbindung!", Toast.LENGTH_SHORT).show();
                setUiMode(UI_MODE_LOGIN);
                break;
            default:
                Toast.makeText(this, "Login fehlgeschlagen!", Toast.LENGTH_SHORT).show();
                setUiMode(UI_MODE_LOGIN);
                break;
        }
    }
}