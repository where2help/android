package app.iamin.iamin;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.HttpURLConnection;

import app.iamin.iamin.event.RegisterEvent;
import app.iamin.iamin.util.EndpointUtils;

/**
 * AsyncTask that wraps an OkHttpRequest. Designed to register a new user.
 */
public class RegisterTask extends AsyncTask<Context, Void, Integer> {
    private static final String TAG = "RegisterTask";

    private String password;
    private String passwordConf;
    private String email;

    /**
     * Construct a new task with an email, a password and the users password confirmation.
     */
    public RegisterTask(String email, String password, String passwordConf) {
        this.password = password;
        this.passwordConf = passwordConf; // TODO: Request pw confirmation!
        this.email = email;
    }

    @Override
    protected Integer doInBackground(Context ... contexts) {
        String url = EndpointUtils.getEndpoint(contexts[0], EndpointUtils.TASK_REGISTER);
        Log.d(TAG, url);

        RequestBody formBody = new FormEncodingBuilder()
                .add("email", email)
                .add("password", password)
                .add("password_confirmation", passwordConf)
                .add("first_name", "null")
                .add("last_name", "null")
                .add("phone", "null")
                .build();

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("Content-Type", "application/json")
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) return response.code();

            Log.e(TAG, "Access-Token = " + response.headers().get("Access-Token"));
            Log.e(TAG, "Token-Type = " + response.headers().get("Token-Type"));
            Log.e(TAG, "Client = " + response.headers().get("Client"));
            Log.e(TAG, "Expiry = " + response.headers().get("Expiry"));
            Log.e(TAG, "Uid = " + response.headers().get("Uid"));

            EndpointUtils.storeHeader(contexts[0], response.headers());

            return response.code();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return HttpURLConnection.HTTP_NO_CONTENT;
    }

    protected void onPostExecute(Integer code) {
        Log.e(TAG, "STATUS: " + code);
        BusProvider.getInstance().post(new RegisterEvent(code));
    }
}
