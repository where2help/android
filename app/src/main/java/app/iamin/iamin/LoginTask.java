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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import app.iamin.iamin.event.LoginEvent;
import app.iamin.iamin.model.User;

import static app.iamin.iamin.util.EndpointUtils.getEndpoint;
import static app.iamin.iamin.util.EndpointUtils.isOnline;
import static app.iamin.iamin.util.EndpointUtils.storeHeader;
import static app.iamin.iamin.util.EndpointUtils.storeUser;

/**
 * AsyncTask that wraps an OkHttpRequest. Designed to sign in a user.
 */
public class LoginTask extends AsyncTask<Context, Void, List<String>> {
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
    protected List<String> doInBackground(Context... contexts) {
        Context context = contexts[0];
        List<String> errors = new ArrayList<>();

        if (!isOnline(context)) {
            errors.add(context.getString(R.string.error_no_connection));
            return errors;
        }

        String url = getEndpoint(context) + "auth/sign_in";
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

            String responseBody = response.body().string();
            Log.e(TAG, "RESPONSE: " + responseBody);

            if (response.isSuccessful()) {
                JSONObject obj = new JSONObject(responseBody).getJSONObject("data");
                User user = new User().fromJSON(obj);
                storeUser(context, user);
                storeHeader(context, response.headers());
                return null;
            }

            JSONArray messages = new JSONObject(responseBody).getJSONArray("errors");
            if (messages != null) {
                for (int i = 0; i < messages.length(); i++) {
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
        } catch (ParseException e) {
            errors.add(e.getMessage());
            e.printStackTrace();
        }

        return errors;
    }

    protected void onPostExecute(List<String> errors) {
        BusProvider.getInstance().post(new LoginEvent(errors));
    }
}
