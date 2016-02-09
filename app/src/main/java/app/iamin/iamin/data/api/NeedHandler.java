package app.iamin.iamin.data.api;

import android.content.Context;
import android.support.annotation.WorkerThread;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;

import app.iamin.iamin.data.model.Booking;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.util.LogUtils;
import app.iamin.iamin.util.NeedUtils;
import app.iamin.iamin.util.TimeUtils;
import io.realm.Realm;
import io.realm.RealmResults;

import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static app.iamin.iamin.util.DataUtils.getEndpoint;
import static app.iamin.iamin.util.DataUtils.getHeaders;

/**
 * Created by Markus on 08.11.15.
 */
public class NeedHandler {

    private static final String TAG = "NeedsService";

    @WorkerThread
    public static String requestNeeds(Context context, OkHttpClient client) {
        String url = getEndpoint(context) + "needs";
        Headers headers = getHeaders(context);

        LogUtils.logLocalHeaders(TAG, headers);

        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            String responseBody = response.body().string();

            LogUtils.logHeaders(TAG, response);
            Log.d(TAG, responseBody);

            if (response.isSuccessful()) {
                storeNeeds(context, responseBody);
                return null;
            }
            return response.message();
        } catch (IOException e) {
            return e.getMessage();
        } catch (ParseException e) {
            return e.getMessage();
        } catch (JSONException e) {
            return e.getMessage();
        }
    }

    private static void storeNeeds(Context context, String responseBody) throws JSONException, ParseException {
        JSONArray data = new JSONObject(responseBody).getJSONArray("data");

        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();

        ArrayList<Integer> ids = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            JSONObject obj = data.getJSONObject(i);
            ids.add(obj.getInt("id"));
        }

        RealmResults<Need> localNeeds = realm.where(Need.class).findAll();
        for (int i = 0; i < localNeeds.size(); i++) {
            Need need = localNeeds.get(i);
            if (!ids.contains(Integer.valueOf(need.getId()))) {
                need.removeFromRealm();
                // Maybe inform the user that a need got canceled
            }
        }

        for (int i = 0; i < data.length(); i++) {
            JSONObject obj = data.getJSONObject(i);
            JSONObject attrs = obj.getJSONObject("attributes");

            Need need = new Need();

            need.setId(obj.getInt("id"));
            need.setSelfLink(obj.getJSONObject("links").getString("self"));

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

            need.setDescription(attrs.getString("description"));
            need.setOrganization(attrs.getString("organization-name"));

            // Get booking info
            Booking booking = realm.where(Booking.class).equalTo("needId", need.getId()).findFirst();
            if (booking != null) {
                need.setIsAttending(true);
                need.setVolunteeringId(booking.getId());
            }

            // This will update a existing Need with the same id or create a new one instead
            realm.copyToRealmOrUpdate(need);
        }

        realm.commitTransaction();
        realm.close();
    }
}
