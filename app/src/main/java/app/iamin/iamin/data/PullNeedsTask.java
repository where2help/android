
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
import java.util.ArrayList;
import java.util.List;

import app.iamin.iamin.R;
import app.iamin.iamin.data.event.NeedsEvent;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.util.LogUtils;
import app.iamin.iamin.util.NeedUtils;
import app.iamin.iamin.util.TimeUtils;
import io.realm.Realm;

import static app.iamin.iamin.util.EndpointUtils.getEndpoint;
import static app.iamin.iamin.util.EndpointUtils.getHeaders;
import static app.iamin.iamin.util.EndpointUtils.isOnline;
import static app.iamin.iamin.util.EndpointUtils.storeHeader;

/**
 * Created by paul on 10/10/2015.
 */
public class PullNeedsTask extends AsyncTask<Void, Integer, List<String>> {
    private static final String TAG = "PullNeedsTask";

    private Context context;

    public PullNeedsTask(Context context) {
        this.context = context;
    }

    @Override
    protected List<String> doInBackground(Void... params) {

        List<String> errors = new ArrayList<>();

        if (!isOnline(context)) {
            errors.add(context.getString(R.string.error_no_connection));
            return errors;
        }

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

            LogUtils.logHeaders(TAG, response);
            Log.e(TAG, "STATUS: " + response.message());

            if (response.isSuccessful()) {
                storeHeader(context, response.headers());

                String result = response.body().string();
                JSONArray data = new JSONObject(result).getJSONArray("data");

                // First clear database
                Realm realm = Realm.getInstance(context);
                realm.beginTransaction();
                realm.where(Need.class).findAll().clear();
                realm.commitTransaction();

                // Write into database
                realm.beginTransaction();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject obj = data.getJSONObject(i);
                    saveNeed(realm, obj);
                }
                realm.commitTransaction();
                realm.close();
            }

        } catch (IOException e) {
            errors.add(e.getMessage());
            e.printStackTrace();
        } catch (JSONException e) {
            errors.add(e.getMessage());
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
            errors.add(e.getMessage());
        }

        return errors;
    }

    @Override
    protected void onPostExecute(List<String> errors) {
        BusProvider.getInstance().post(new NeedsEvent(errors));
    }

    private void saveNeed(Realm realm, JSONObject obj) throws JSONException, IOException, ParseException{
        Need need = realm.createObject(Need.class);

        need.setId(obj.getInt("id"));
        need.setSelfLink(obj.getJSONObject("links").getString("self"));

        JSONObject attrs = obj.getJSONObject("attributes");

        need.setCategory(NeedUtils.getCategory(attrs.getString("category")));

        need.setCity(attrs.getString("city"));
        need.setLocation(attrs.getString("location"));
        need.setLat(attrs.getString("lat").equals("null") ? 0 : attrs.getDouble("lat"));
        need.setLng(attrs.getString("lng").equals("null") ? 0 : attrs.getDouble("lng"));

        need.setStart(TimeUtils.FORMAT_API.parse(attrs.getString("start-time")));
        need.setEnd(TimeUtils.FORMAT_API.parse(attrs.getString("end-time")));
        need.setDate(TimeUtils.formatHumanFriendlyShortDate(context, need.getStart()) + " " +
                TimeUtils.formatTimeOfDay(need.getStart()) + " - " +
                TimeUtils.formatTimeOfDay(need.getEnd()) + " Uhr");

        need.setNeeded(attrs.getInt("volunteers-needed"));
        need.setCount(attrs.getInt("volunteers-count"));

        need.setIsAttending(false);
    }
}