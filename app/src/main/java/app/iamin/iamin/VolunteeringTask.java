package app.iamin.iamin;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;


import java.io.IOException;

import app.iamin.iamin.util.EndpointUtils;

/**
 * AsyncTask that wraps an OkHttpRequest. Designed to create a volunteering.
 */
public class VolunteeringTask extends AsyncTask<Context, Void, Response> {
    private static final String TAG = "LoginTask";

    private int needId;
    private Exception savedException;

    /**
     * Construct a new task with the ID of the need.
     */
    public VolunteeringTask(int needId) {
        this.needId = needId;
    }

    @Override
    protected Response doInBackground(Context ... contexts) {
        String url = EndpointUtils.getEndpoint(contexts[0], EndpointUtils.TASK_VOLUNTEERING);
        Log.d(TAG, url);

        RequestBody formBody = new FormEncodingBuilder()
                .add("need_id", String.valueOf(needId))
                .build();

        Headers headers = EndpointUtils.getHeaders(contexts[0]);

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .headers(headers)
                    .addHeader("Content-Type", "application/vnd.api+json")
                    .url(url)
                    .post(formBody)
                    .build();
            Response response = client.newCall(request).execute();
            EndpointUtils.storeHeader(contexts[0], response.headers());

            String responseBody = response.body().string();

            Log.e(TAG, responseBody);

            return response;

        } catch (IOException e) {
            this.savedException = e;
            cancel(true);
        }

        return null;
    }

    protected void onPostExecute(Response response) {
        // TODO: Do something with response
        Log.e(TAG, "Access-Token = " + response.headers().get("Access-Token"));
        Log.e(TAG, "Token-Type = " + response.headers().get("Token-Type"));
        Log.e(TAG, "Client = " + response.headers().get("Client"));
        Log.e(TAG, "Expiry = " + response.headers().get("Expiry"));
        Log.e(TAG, "Uid = " + response.headers().get("Uid"));

        //BusProvider.getInstance().post(new VolunteeringEvent(true));
    }

    @Override
    protected void onCancelled() {
        // TODO: Do something with this.savedException
        this.savedException.printStackTrace();

        //BusProvider.getInstance().post(new VolunteeringEvent(false));
    }
}
