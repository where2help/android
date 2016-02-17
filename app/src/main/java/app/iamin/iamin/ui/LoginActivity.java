package app.iamin.iamin.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;

import app.iamin.iamin.R;
import app.iamin.iamin.data.BusProvider;
import app.iamin.iamin.data.DataManager;
import app.iamin.iamin.data.event.UserSignInEvent;
import app.iamin.iamin.data.event.UserSignUpEvent;
import butterknife.Bind;
import butterknife.ButterKnife;

import static app.iamin.iamin.data.DataManager.ON_ERROR;
import static app.iamin.iamin.data.DataManager.ON_NEXT;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private static final int UI_MODE_SIGN_IN = 0;
    private static final int UI_MODE_SIGN_UP = 1;
    private static final int UI_MODE_PROGRESS = 2;

    private int currentUiMode = UI_MODE_SIGN_IN;

    @Bind(R.id.header_title) TextView title;

    @Bind(R.id.input_email) TextInputLayout emailInput;
    @Bind(R.id.input_password) TextInputLayout passwordInput;
    @Bind(R.id.input_password_conf) TextInputLayout passwordConfInput;

    @Bind(R.id.email) EditText emailEditText;
    @Bind(R.id.password) EditText passwordEditText;
    @Bind(R.id.password_conf) EditText passwordConfEditText;

    @Bind(R.id.terms) CheckBox termsCheckbox;

    @Bind(R.id.btn_pos) Button posBtn;
    @Bind(R.id.btn_neg) Button negBtn;

    @Bind(R.id.btn_bar) View buttonBar;
    @Bind(R.id.loading) View loading;

    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        BusProvider.getInstance().register(this);

        dataManager = DataManager.getInstance(this);

        emailEditText.setText("normal_user@example.com");

        passwordInput.setTypeface(emailInput.getTypeface());

        passwordEditText.setTypeface(emailEditText.getTypeface());
        passwordEditText.setText("supersecret");

        passwordConfInput.setTypeface(emailInput.getTypeface());

        passwordConfEditText.setTypeface(emailEditText.getTypeface());

        termsCheckbox.setText(Html.fromHtml("Ich akzeptiere die <a href='app.iamin.iamin.ui.terms://'>Nutzungsbedingungen</a>"));
        termsCheckbox.setMovementMethod(LinkMovementMethod.getInstance());

        setUiMode(UI_MODE_SIGN_IN);
    }

    private void setUiMode(int uiMode) {
        currentUiMode = uiMode;
        switch (uiMode) {
            case UI_MODE_SIGN_IN:
                title.setText(getString(R.string.action_sign_in));
                posBtn.setText(getString(R.string.action_sign_in));
                negBtn.setText(getString(R.string.action_sign_up));
                emailInput.setVisibility(View.VISIBLE);
                passwordInput.setVisibility(View.VISIBLE);
                passwordConfInput.setVisibility(View.GONE);
                termsCheckbox.setVisibility(View.GONE);
                buttonBar.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
                break;
            case UI_MODE_SIGN_UP:
                title.setText(getString(R.string.action_sign_up));
                posBtn.setText(getString(R.string.action_sign_up));
                negBtn.setText(getString(R.string.action_back));
                emailInput.setVisibility(View.VISIBLE);
                passwordInput.setVisibility(View.VISIBLE);
                passwordConfInput.setVisibility(View.VISIBLE);
                termsCheckbox.setVisibility(View.VISIBLE);
                buttonBar.setVisibility(View.VISIBLE);
                loading.setVisibility(View.GONE);
                break;
            case UI_MODE_PROGRESS:
                emailInput.setVisibility(View.GONE);
                passwordInput.setVisibility(View.GONE);
                passwordConfInput.setVisibility(View.GONE);
                termsCheckbox.setVisibility(View.GONE);
                buttonBar.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void onActionNegative(View view) {
        if (currentUiMode == UI_MODE_SIGN_IN) {
            setUiMode(UI_MODE_SIGN_UP);
        } else {
            setUiMode(UI_MODE_SIGN_IN);
        }
    }

    public void onActionPositive(View view) {
        if (currentUiMode == UI_MODE_SIGN_IN) {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            dataManager.signIn(email, password);
            setUiMode(UI_MODE_PROGRESS);
        } else {
            String email = emailEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String passwordConf = passwordConfEditText.getText().toString();
            if (termsCheckbox.isChecked()) {
                dataManager.signUp(email, password, passwordConf);
                setUiMode(UI_MODE_PROGRESS);
            } else {
                Toast.makeText(this, "Nutzungsbedingungen müssen akzeptiert werden", Toast.LENGTH_SHORT).show();
            }
        }
    }

        @Subscribe
        public void onUserSignIn (UserSignInEvent event){
            Log.d(TAG, "onUserSignIn");

            switch (event.status) {
                case ON_NEXT:
                    setResult(Activity.RESULT_OK);
                    finish();
                    break;
                case ON_ERROR:
                    Toast.makeText(this, event.error, Toast.LENGTH_LONG).show();
                    setUiMode(UI_MODE_SIGN_IN);
                    break;
            }
        }

        @Subscribe
        public void onUserSignUp (UserSignUpEvent event){
            Log.d(TAG, "onUserSignUp");

            switch (event.status) {
                case ON_NEXT:
                    String email = emailEditText.getText().toString();
                    String password = passwordEditText.getText().toString();
                    dataManager.signIn(email, password);
                    break;
                case ON_ERROR:
                    Toast.makeText(this, event.error, Toast.LENGTH_LONG).show();
                    setUiMode(UI_MODE_SIGN_UP);
                    break;
            }
        }

    public void dismiss(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onBackPressed() {
        switch (currentUiMode) {
            case UI_MODE_SIGN_UP:
                setUiMode(UI_MODE_SIGN_IN);
                break;
            default:
                dismiss(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }
}