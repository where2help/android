
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
import java.util.ArrayList;
import java.util.List;

import app.iamin.iamin.event.VolunteeringsEvent;
import app.iamin.iamin.model.Need;
import app.iamin.iamin.model.User;
import io.realm.Realm;
import io.realm.RealmResults;

import static app.iamin.iamin.util.EndpointUtils.getEndpoint;
import static app.iamin.iamin.util.EndpointUtils.getHeaders;
import static app.iamin.iamin.util.EndpointUtils.getUser;
import static app.iamin.iamin.util.EndpointUtils.isOnline;
import static app.iamin.iamin.util.EndpointUtils.storeHeader;

/**
 * Created by paul on 10/10/2015.
 */
public class PullVolunteeringsTask extends AsyncTask<Void, Integer, List<String>> {
    private static final String TAG = "PullAppointmentsTask";

    private Context context;

    public PullVolunteeringsTask(Context context) {
        this.context = context;
    }

    @Override
    protected List<String> doInBackground(Void... params) {

        List<String> errors = new ArrayList<>();

        if (!isOnline(context)) {
            errors.add(context.getString(R.string.error_no_connection));
            return errors;
        }

        // This are all ids from needs the user is attending
        List<Integer> ids = new ArrayList<>();
        Realm realm = Realm.getInstance(context);
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
                for (int i = 0; i < data.length(); i++) {
                    realm.beginTransaction();

                    JSONObject obj = data.getJSONObject(i);
                    int id = obj.getInt("id");

                    RealmResults<Need> need = realm.where(Need.class).equalTo("id", id).findAll();
                    need.first().setIsAttending(true);

                    realm.commitTransaction();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } catch (JSONException e) {
            e.printStackTrace();
            realm.cancelTransaction();
        }

        realm.close();
        return errors;
    }

    @Override
    protected void onPostExecute(List<String> errors) {
        BusProvider.getInstance().post(new VolunteeringsEvent(errors));
    }
}