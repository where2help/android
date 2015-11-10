package app.iamin.iamin.data.api;

import android.content.Context;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.util.NeedUtils;
import app.iamin.iamin.util.TimeUtils;
import io.realm.Realm;

import static app.iamin.iamin.util.DataUtils.getEndpoint;

/**
 * Created by Markus on 08.11.15.
 */
public class NeedsService {

    public static String requestNeeds(Context context) {
        String url = getEndpoint(context) + "needs";
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = new OkHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {
                storeNeeds(context, response.body().string());
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

        // First clear database
        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();
        realm.where(Need.class).findAll().clear();

        // Write into database
        for (int i = 0; i < data.length(); i++) {
            JSONObject obj = data.getJSONObject(i);

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
        realm.commitTransaction();
        realm.close();
    }
}
