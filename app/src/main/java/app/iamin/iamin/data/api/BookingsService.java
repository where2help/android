package app.iamin.iamin.data.api;

import android.content.Context;
import android.content.Intent;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

import app.iamin.iamin.R;
import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.data.model.User;
import io.realm.Realm;

import static app.iamin.iamin.data.service.DataService.EXTRA_BOOKING_ID;
import static app.iamin.iamin.data.service.DataService.EXTRA_NEED_ID;
import static app.iamin.iamin.util.DataUtils.getEndpoint;
import static app.iamin.iamin.util.DataUtils.getHeaders;
import static app.iamin.iamin.util.DataUtils.getUser;
import static app.iamin.iamin.util.DataUtils.storeHeader;

/**
 * Created by Markus on 08.11.15.
 */
public class BookingsService {

    public static String requestBookings(final Context context) {
        User user = getUser(context);
        if (user.getEmail() == null) {
            return context.getString(R.string.error_user_not_found);
        }

        String url = getEndpoint(context) + "users/" + user.getId() + "/volunteerings";
        Headers headers = getHeaders(context);
        Request request = new Request.Builder().headers(headers).url(url).build();
        try {
            Response response = new OkHttpClient().newCall(request).execute();

            if (response.isSuccessful()) {
                storeHeader(context, response.headers());
                storeBookings(context, response.body().string());
                return null;
            }
            return String.valueOf(response.code());
        } catch (IOException e) {
            return e.getMessage();
        } catch (ParseException e) {
            return e.getMessage();
        } catch (JSONException e) {
            return e.getMessage();
        }
    }

    public static String createBooking(Context context, Intent intent) {
        User user = getUser(context);
        if (user.getEmail() == null) {
            return context.getString(R.string.error_user_not_found);
        }

        int needId = intent.getIntExtra(EXTRA_NEED_ID, 0);

        MediaType contentType = MediaType.parse("application/vnd.api+json; charset=utf-8");
        String content = "{ \"data\":{\"type\":\"volunteerings\",\"attributes\":{" +
                "\"user-id\":\"" + user.getId() + "\",\"need-id\":\"" + needId + "\"}}}";

        RequestBody requestBody = RequestBody.create(contentType, content);

        String url = getEndpoint(context) + "volunteerings";
        Headers headers = getHeaders(context);

        Request request = new Request.Builder().headers(headers).url(url).post(requestBody).build();
        try {
            Response response = new OkHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {
                storeHeader(context, response.headers());
                storeBookingCreation(context, response.body().string(), needId);
                return null;
            }
            return String.valueOf(response.code());
        } catch (IOException e) {
            return e.getMessage();
        } catch (ParseException e) {
            return e.getMessage();
        } catch (JSONException e) {
            return e.getMessage();
        }
    }

    public static String cancelBooking(Context context, Intent intent) {
        User user = getUser(context);
        if (user.getEmail() == null) {
            return context.getString(R.string.error_user_not_found);
        }

        int volunteeringId = intent.getIntExtra(EXTRA_BOOKING_ID, 0);
        String url = getEndpoint(context) + "volunteerings/" + volunteeringId;
        Headers headers = getHeaders(context);

        Request request = new Request.Builder().headers(headers).url(url).delete().build();
        try {
            Response response = new OkHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {
                storeHeader(context, response.headers());
                storeBookingCancellation(context, volunteeringId);
                return null;
            }
            return String.valueOf(response.code());
        } catch (IOException e) {
            return e.getMessage();
        } catch (ParseException e) {
            return e.getMessage();
        } catch (JSONException e) {
            return e.getMessage();
        }
    }

    private static void storeBookings(Context context, String responseBody) throws JSONException, ParseException {
        JSONArray data = new JSONObject(responseBody).getJSONArray("data");

        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();
        for (int i = 0; i < data.length(); i++) {

            JSONObject item = data.getJSONObject(i);
            int volunteeringId = item.getInt("id");

            JSONObject attributes = item.getJSONObject("attributes");
            int needId = attributes.getInt("need-id");

            Need need = realm.where(Need.class).equalTo("id", needId).findFirst();
            if (need != null) {
                need.setIsAttending(true);
                need.setVolunteeringId(volunteeringId);
            }
        }
        realm.commitTransaction();
        realm.close();
    }

    private static void storeBookingCreation(Context context, String responseBody, int needId) throws JSONException, ParseException {
        JSONObject data = new JSONObject(responseBody).getJSONObject("data");
        int volunteeringId = data.getInt("id");

        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();

        Need need = realm.where(Need.class).equalTo("id", needId).findFirst();
        if (need != null) {
            need.setIsAttending(true);
            need.setVolunteeringId(volunteeringId);
            int needed = need.getNeeded() - 1;
            need.setNeeded(needed);
        }

        realm.commitTransaction();
        realm.close();
    }

    private static void storeBookingCancellation(Context context, int volunteeringId) throws JSONException, ParseException {
        Realm realm = Realm.getInstance(context);
        realm.beginTransaction();

        Need need = realm.where(Need.class).equalTo("volunteeringId", volunteeringId).findFirst();
        if (need != null) {
            need.setIsAttending(false);
            int needed = need.getNeeded() + 1;
            need.setNeeded(needed);
        }
        realm.commitTransaction();
        realm.close();
    }
}
