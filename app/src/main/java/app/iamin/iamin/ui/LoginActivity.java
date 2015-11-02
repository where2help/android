package app.iamin.iamin.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import java.util.List;
import java.util.regex.Pattern;

import app.iamin.iamin.R;
import app.iamin.iamin.data.BusProvider;
import app.iamin.iamin.data.LoginTask;
import app.iamin.iamin.data.RegisterTask;
import app.iamin.iamin.data.event.LoginEvent;
import app.iamin.iamin.data.event.RegisterEvent;
import app.iamin.iamin.util.UiUtils;

public class LoginActivity extends AppCompatActivity {

    private static final int UI_MODE_SIGN_IN = 0;
    private static final int UI_MODE_SIGN_UP = 1;
    private static final int UI_MODE_PROGRESS = 2;

    private int currentUiMode = UI_MODE_SIGN_IN;

    private TextInputLayout emailInput;
    private TextInputLayout passwordInput;
    private TextInputLayout passwordConfInput;

    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText passwordConfEditText;

    private Button posBtn;
    private Button negBtn;

    private View buttonBar;
    private View loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        buttonBar = findViewById(R.id.btn_bar);
        loading = findViewById(R.id.loading);

        emailInput = (TextInputLayout) findViewById(R.id.input_email);
        emailEditText = (EditText) findViewById(R.id.email);
        emailEditText.setText("android_user@example.com");

        passwordInput = (TextInputLayout) findViewById(R.id.input_password);
        passwordEditText = (EditText) findViewById(R.id.password);
        //passwordEditText.setText("supersecret");

        passwordConfInput = (TextInputLayout) findViewById(R.id.input_password_conf);
        passwordConfEditText = (EditText) findViewById(R.id.password_conf);

        posBtn = (Button) findViewById(R.id.btn_pos);
        negBtn = (Button) findViewById(R.id.btn_neg);

        setUiMode(UI_MODE_SIGN_IN);
    }

    private String getUserMail() {
        String email = null;
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                email = account.name;
                break;
            }
        }
        Log.d("onActionSubmit", email);
        return email;
    }

    private void setUiMode(int uiMode) {
        currentUiMode = uiMode;
        switch (uiMode) {
            case UI_MODE_SIGN_IN:
                posBtn.setText(getString(R.string.sign_in));
                negBtn.setText(getString(R.string.sign_up));
                emailInput.setVisibility(View.VISIBLE);
                passwordInput.setVisibility(View.VISIBLE);
                passwordConfInput.setVisibility(View.GONE);
                buttonBar.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
                break;
            case UI_MODE_SIGN_UP:
                posBtn.setText(getString(R.string.sign_up));
                negBtn.setText(getString(R.string.back));
                emailInput.setVisibility(View.VISIBLE);
                passwordInput.setVisibility(View.VISIBLE);
                passwordConfInput.setVisibility(View.VISIBLE);
                buttonBar.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
                break;
            case UI_MODE_PROGRESS:
                emailInput.setVisibility(View.GONE);
                passwordInput.setVisibility(View.GONE);
                passwordConfInput.setVisibility(View.GONE);
                buttonBar.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
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
        switch (currentUiMode) {
            case UI_MODE_SIGN_UP:
                setUiMode(UI_MODE_SIGN_IN);
                break;
            default:
                super.onBackPressed();
        }
    }

    public void onActionNegative(View view) {
        if (currentUiMode == UI_MODE_SIGN_IN){
            setUiMode(UI_MODE_SIGN_UP);
        } else {
            setUiMode(UI_MODE_SIGN_IN);
        }
    }

    public void onActionPositive(View view) {
        if (currentUiMode == UI_MODE_SIGN_IN) {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            new LoginTask(email, password).execute(this);
        } else {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String passwordConf = passwordConfEditText.getText().toString();
            new RegisterTask(email, password, passwordConf).execute(this);
        }
        setUiMode(UI_MODE_PROGRESS);
    }

    @Subscribe
    public void onRegisterUpdate(RegisterEvent event) {
        List<String> errors = event.errors;
        if (errors == null) {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            new LoginTask(email, password).execute(this);
        } else {
            showErrorMessage(errors);
            setUiMode(UI_MODE_SIGN_UP);
        }
    }

    @Subscribe
    public void onLoginUpdate(LoginEvent event) {
        List<String> errors = event.errors;
        if (errors == null) {
            UiUtils.fireMainIntent(this);
            finish();
        } else {
            showErrorMessage(errors);
            setUiMode(UI_MODE_SIGN_IN);
        }
    }

    private void showErrorMessage(List<String> errors) {
        String message = "";
        for (String error : errors) {
            message += error + " ";
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void dismiss(View view) {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}