
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

import app.iamin.iamin.event.NeedsEvent;
import app.iamin.iamin.model.Need;

import static app.iamin.iamin.util.EndpointUtils.*;

/**
 * Created by paul on 10/10/2015.
 */
public class PullNeedsTask extends AsyncTask<Void, Integer, Need[]> {
    private static final String TAG = "PullNeedsTask";

    private Context context;

    public PullNeedsTask(Context context) {
        this.context = context;
    }

    @Override
    protected Need[] doInBackground(Void... params) {

        if (!isOnline(context)) return null;

        Need[] needs = null;
        String url = getEndpoint(context) + "needs";
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
                JSONArray data = new JSONObject(result).getJSONArray("data");
                needs = new Need[data.length()];
                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);
                    needs[i] = new Need().fromJSON(context, obj);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        return needs;
    }

    @Override
    protected void onPostExecute(Need[] needs) {
        BusProvider.getInstance().post(new NeedsEvent(needs));
    }
}