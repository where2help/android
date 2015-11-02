
package app.iamin.iamin.data;

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

import app.iamin.iamin.data.model.NeedOld;
import app.iamin.iamin.data.model.User;

import static app.iamin.iamin.util.EndpointUtils.getEndpoint;
import static app.iamin.iamin.util.EndpointUtils.getHeaders;
import static app.iamin.iamin.util.EndpointUtils.getUser;
import static app.iamin.iamin.util.EndpointUtils.isOnline;
import static app.iamin.iamin.util.EndpointUtils.storeHeader;

/**
 * Created by paul on 10/10/2015.
 */
public class PullAppointmentsTask extends AsyncTask<Void, Integer, NeedOld[]> {
    private static final String TAG = "PullAppointmentsTask";

    private Context context;

    public PullAppointmentsTask(Context context) {
        this.context = context;
    }

    @Override
    protected NeedOld[] doInBackground(Void... params) {

        if (!isOnline(context)) return null;

        NeedOld[] needs = null;
        User user = getUser(context);

        if (user.getEmail() == null) return null;

        String url = getEndpoint(context) + "users/" + user.getId() + "/appointments";
        Log.d(TAG, url);

        Headers headers = getHeaders(context);

        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .headers(headers)
                    .url(url)
                    .build();

            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                storeHeader(context, response.headers());

                String result = response.body().string();
                Log.e(TAG, "RESULT: " + result);

                JSONArray data = new JSONObject(result).getJSONArray("data");
                needs = new NeedOld[data.length()];
                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);
                    needs[i] = new NeedOld().fromJSON(context, obj);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return needs;
    }

    @Override
    protected void onPostExecute(NeedOld[] needs) {
       // BusProvider.getInstance().post(new VolunteeringsEvent(needs));
    }
}