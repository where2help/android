package app.iamin.iamin;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import app.iamin.iamin.event.LogoutEvent;

import static app.iamin.iamin.util.EndpointUtils.getEndpoint;
import static app.iamin.iamin.util.EndpointUtils.getHeaders;
import static app.iamin.iamin.util.EndpointUtils.storeHeader;

/**
 * AsyncTask that wraps an OkHttpRequest. Designed to log out a user.
 */
public class LogoutTask extends AsyncTask<Context, Void, List<String>> {
    private static final String TAG = "LogoutTask";

    public LogoutTask() {
    }

    @Override
    protected List<String> doInBackground(Context ... contexts) {
        List<String> errors = new ArrayList<>();
        Context context = contexts[0];
        String url = getEndpoint(context) + "auth/sign_out";
        Log.d(TAG, url);

        Headers headers = getHeaders(context);

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .headers(headers)
                    .url(url)
                    .delete()
                    .build();
            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                storeHeader(context, response.headers());
            }

            String responseBody = response.body().string();
            Log.e(TAG, responseBody);


        } catch (IOException e) {
            errors.add(e.getMessage());
            e.printStackTrace();
        }

        return errors;
    }

    protected void onPostExecute(List<String> errors) {
        BusProvider.getInstance().post(new LogoutEvent(errors));
    }
}
