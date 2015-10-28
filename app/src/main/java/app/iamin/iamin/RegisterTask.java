package app.iamin.iamin;

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

import app.iamin.iamin.event.RegisterEvent;
import app.iamin.iamin.util.EndpointUtils;

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
        List<String> errors = new ArrayList<>();

        if (!EndpointUtils.isOnline(contexts[0])) {
            errors.add("Registrierung nicht möglich. Überprüfen Sie Ihre Netzwerkverbindung.");
            return errors;
        }

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
            String responseBody = response.body().string();
            Log.e(TAG, "RESPONSE: " + responseBody);

            if (response.isSuccessful()) return null;

            JSONArray messages =  new JSONObject(responseBody)
                    .getJSONObject("errors")
                    .getJSONArray("full_messages");

            if (messages != null) {
                for (int i=0;i<messages.length();i++){
                    errors.add(messages.get(i).toString());
                    Log.e(TAG, "Error Message: " + messages.get(i).toString());
                }
            }

/*          Log.e(TAG, "Access-Token = " + response.headers().get("Access-Token"));
            Log.e(TAG, "Token-Type = " + response.headers().get("Token-Type"));
            Log.e(TAG, "Client = " + response.headers().get("Client"));
            Log.e(TAG, "Expiry = " + response.headers().get("Expiry"));
            Log.e(TAG, "Uid = " + response.headers().get("Uid"));*/

            EndpointUtils.storeHeader(contexts[0], response.headers());

        } catch (IOException e) {
            errors.add(e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            errors.add(e.getMessage());
            e.printStackTrace();
        }

        return errors;
    }

    protected void onPostExecute(List<String> result) {
        BusProvider.getInstance().post(new RegisterEvent(result));
    }
}
