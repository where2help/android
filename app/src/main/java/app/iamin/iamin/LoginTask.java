package app.iamin.iamin;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.ParseException;

import app.iamin.iamin.event.LoginEvent;
import app.iamin.iamin.model.User;
import app.iamin.iamin.util.EndpointUtils;

/**
 * AsyncTask that wraps an OkHttpRequest. Designed to sign in a user.
 */
public class LoginTask extends AsyncTask<Context, Void, Integer> {
    private static final String TAG = "LoginTask";

    private String password;
    private String email;

    /**
     * Construct a new task with an email and the users password.
     */
    public LoginTask(String email, String password) {
        this.password = password;
        this.email = email;
    }

    @Override
    protected Integer doInBackground(Context ... contexts) {
        String url = EndpointUtils.getEndpoint(contexts[0], EndpointUtils.TASK_LOGIN);
        Log.d(TAG, url);

        RequestBody formBody = new FormEncodingBuilder()
                .add("email", email)
                .add("password", password)
                .build();

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) return response.code();

            EndpointUtils.storeHeader(contexts[0], response.headers());

            String responseBody = response.body().string();

            JSONObject obj = new JSONObject(responseBody).getJSONObject("data");
            User user = new User().fromJSON(obj);

            Log.d(TAG, "User id = " + user.getId());
            Log.d(TAG, "User email = " + user.getEmail());
            Log.d(TAG, "User first_name = " + user.getFirstName());
            Log.d(TAG, "User last_name = " + user.getLastName());
            Log.d(TAG, "User phone = " + user.getPhone());
            Log.d(TAG, "User admin = " + user.isAdmin());
            Log.d(TAG, "User ngo_admin = " + user.isNgoAdmin());
            Log.d(TAG, "User provider = " + user.getProvider());
            Log.d(TAG, "User uid = " + user.getUid());
            Log.d(TAG, "User name = " + user.getName());
            Log.d(TAG, "User nickname = " + user.getNickname());
            Log.d(TAG, "User image = " + user.getImage());

            Log.e(TAG, "Access-Token = " + response.headers().get("Access-Token"));
            Log.e(TAG, "Token-Type = " + response.headers().get("Token-Type"));
            Log.e(TAG, "Client = " + response.headers().get("Client"));
            Log.e(TAG, "Expiry = " + response.headers().get("Expiry"));
            Log.e(TAG, "Uid = " + response.headers().get("Uid"));

            EndpointUtils.storeUser(contexts[0], user);

            return response.code();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return HttpURLConnection.HTTP_NO_CONTENT;
    }

    protected void onPostExecute(Integer code) {
        Log.e(TAG, code + "");
        BusProvider.getInstance().post(new LoginEvent(code));
    }
}
