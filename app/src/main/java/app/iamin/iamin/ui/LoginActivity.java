package app.iamin.iamin.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Pattern;

import app.iamin.iamin.R;
import app.iamin.iamin.data.service.DataService;

import static app.iamin.iamin.data.service.DataService.ACTION_SIGN_IN;
import static app.iamin.iamin.data.service.DataService.ACTION_SIGN_UP;
import static app.iamin.iamin.data.service.DataService.EXTRA_ERROR;

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

        passwordInput = (TextInputLayout) findViewById(R.id.input_password);
        passwordEditText = (EditText) findViewById(R.id.password);

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
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mDataReceiver, DataService.getDataResultIntentFilter());
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDataReceiver);
    }

    private BroadcastReceiver mDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            String action = intent.getAction();
            String error = intent.getStringExtra(EXTRA_ERROR);
            if (ACTION_SIGN_UP.equals(action)) {
                handleSignUp(error);
            } else if (ACTION_SIGN_IN.equals(action)) {
                handleSignIn(error);
            }
        }
    };

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
            DataService.signIn(this, email, password);
        } else {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String passwordConf = passwordConfEditText.getText().toString();
            DataService.signUp(this, email, password, passwordConf);
        }
        setUiMode(UI_MODE_PROGRESS);
    }

    private void handleSignUp(String error) {
        if (error == null) {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            DataService.signIn(this, email, password);
        } else {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            setUiMode(UI_MODE_SIGN_UP);
        }
    }

    private void handleSignIn(String error) {
        if (error == null) {
            setResult(Activity.RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            setUiMode(UI_MODE_SIGN_IN);
        }
    }

    private void dismiss(View view) {
        finish();
    }
}