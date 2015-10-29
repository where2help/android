
package app.iamin.iamin;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

import app.iamin.iamin.event.AppointmentsEvent;
import app.iamin.iamin.model.Need;
import app.iamin.iamin.model.User;
import app.iamin.iamin.util.EndpointUtils;

/**
 * Created by paul on 10/10/2015.
 */
public class PullAppointmentsTask extends AsyncTask<Void, Integer, Need[]> {
    private static final String TAG = "PullAppointmentsTask";

    private Context context;

    public PullAppointmentsTask(Context context) {
        this.context = context;
    }

    @Override
    protected Need[] doInBackground(Void... params) {

        User user = EndpointUtils.getUser(context);
        if (user.getEmail() == null) return null;


        Need[] needs = null;
        String url = EndpointUtils.getEndpoint(context);
        Log.d(TAG, url);

        Headers headers = EndpointUtils.getHeaders(context);
/*
        Log.e(TAG, "LOCAL Access-Token = " + headers.get("Access-Token"));
        Log.e(TAG, "LOCAL Token-Type = " + headers.get("Token-Type"));
        Log.e(TAG, "LOCAL Client = " + headers.get("Client"));
        Log.e(TAG, "LOCAL Expiry = " + headers.get("Expiry"));
        Log.e(TAG, "LOCAL Uid = " + headers.get("Uid"));
*/
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .headers(headers)
                    .url(url + "users/" + user.getId() + "/appointments")
            .build();

            Response response = client.newCall(request).execute();
/*
            Log.e(TAG, "Access-Token = " + response.headers().get("Access-Token"));
            Log.e(TAG, "Token-Type = " + response.headers().get("Token-Type"));
            Log.e(TAG, "Client = " + response.headers().get("Client"));
            Log.e(TAG, "Expiry = " + response.headers().get("Expiry"));
            Log.e(TAG, "Uid = " + response.headers().get("Uid"));
*/
            EndpointUtils.storeHeader(context, response.headers());

            String result = response.body().string();
            Log.e(TAG, "USER ID: " + user.getId());
            Log.e(TAG, "RESULT: " + result);

            JSONArray data = new JSONObject(result).getJSONArray("data");

            needs = new Need[data.length()];
            for (int i = 0; i < data.length(); i++) {
                JSONObject obj = data.getJSONObject(i);
                needs[i] = new Need().fromJSON(context, obj);
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return needs;
    }

    @Override
    protected void onPostExecute(Need[] needs) {
        BusProvider.getInstance().post(new AppointmentsEvent(needs));
    }
}