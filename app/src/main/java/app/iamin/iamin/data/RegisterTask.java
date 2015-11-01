package app.iamin.iamin.data;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import app.iamin.iamin.R;
import app.iamin.iamin.data.event.RegisterEvent;

import static app.iamin.iamin.util.EndpointUtils.getEndpoint;
import static app.iamin.iamin.util.EndpointUtils.isOnline;
import static app.iamin.iamin.util.EndpointUtils.storeHeader;

/**
 * AsyncTask that wraps an OkHttpRequest. Designed to register a new user.
 */
public class RegisterTask extends AsyncTask<Context, Void, List<String>> {
    private static final String TAG = "RegisterTask";

    private String password;
    private String passwordConf;
    private String email;

    /**
     * Construct a new task with an email, a password and the users password confirmation.
     */
    public RegisterTask(String email, String password, String passwordConf) {
        this.password = password;
        this.passwordConf = passwordConf;
        this.email = email;
    }

    @Override
    protected List<String> doInBackground(Context ... contexts) {
        Context context = contexts[0];
        List<String> errors = new ArrayList<>();

        if (!isOnline(context)) {
            errors.add(context.getString(R.string.error_no_connection));
            return errors;
        }

        String url = getEndpoint(contexts[0]) + "auth/";
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
            String responseBody = response.body().string();
            Log.e(TAG, "RESPONSE: " + responseBody);

            if (response.isSuccessful()) {
                storeHeader(context, response.headers());
                return null;
            }

            JSONArray messages =  new JSONObject(responseBody)
                    .getJSONObject("errors")
                    .getJSONArray("full_messages");

            if (messages != null) {
                for (int i = 0; i < messages.length(); i++){
                    errors.add(messages.get(i).toString());
                    Log.e(TAG, "Error Message: " + messages.get(i).toString());
                }
            }

        } catch (IOException e) {
            errors.add(e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            errors.add(e.getMessage());
            e.printStackTrace();
        }

        return errors;
    }

    protected void onPostExecute(List<String> errors) {
        BusProvider.getInstance().post(new RegisterEvent(errors));
    }
}
