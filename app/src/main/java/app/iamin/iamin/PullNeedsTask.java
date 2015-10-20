package app.iamin.iamin;

import android.content.Context;
import android.location.Geocoder;
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
import java.net.URL;
import java.text.ParseException;

import app.iamin.iamin.event.NeedsEvent;
import app.iamin.iamin.model.Need;
import app.iamin.iamin.util.EndpointUtils;

/**
 * Created by paul on 10/10/2015.
 */
public class PullNeedsTask extends AsyncTask<Void, Integer, Need[]> {

    private Geocoder coder;
    private Context context;

    public PullNeedsTask(Context context) {
        this.coder = new Geocoder(context);
        this.context = context;
    }

    @Override
    protected Need[] doInBackground(Void... params) {

        Need[] needs = null;
        String url = EndpointUtils.getEndpoint(context, EndpointUtils.TASK_NEEDS);
        Log.d("PullNeedsActiveTask", url);

        Headers headers = EndpointUtils.getHeaders(context);

        try {
            //registerUser();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .headers(headers)
                    .url(new URL(url))
                    .build();

            Response response = client.newCall(request).execute();
            EndpointUtils.storeHeader(context, response.headers());

            String result =  response.body().string();

            Log.e("PullNeedsActiveTask", result);

            JSONArray data = new JSONObject(result).getJSONArray("data");

            needs = new Need[data.length()];
            for (int i = 0; i<data.length(); i++) {
                JSONObject obj = data.getJSONObject(i);
                needs[i] = new Need().fromJSON(obj, coder);
            }

        } catch (IOException e) {
            e.printStackTrace(); // TODO
        } catch (JSONException e) {
            e.printStackTrace(); // TODO
        } catch (ParseException e) {
            e.printStackTrace(); // TODO
        }

        return needs;
    }

    @Override
    protected void onPostExecute(Need[] result) {
        BusProvider.getInstance().post(new NeedsEvent(result));
    }

/*
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");*/

    private void registerUser() throws IOException {
        /*SharedPreferences settings;
        settings = context.getSharedPreferences("IAMIN", 0);
        if (settings.contains("token")) {
            return;
        }

        // get phone number
        TelephonyManager tMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        String phoneNumber = tMgr.getLine1Number();
        // get email
        String email = null;
        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(context).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                email = account.name;
                break;
            }
        }

        String json = "{ \"email\": \"" + email + "\", \"phone\":\"" + phoneNumber + "\"}";

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .addHeader("Content-Type", "application/json")
                .url(new URL("http://where2help.herokuapp.com/api/v1/sessions/create"))
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        String respJSON = response.body().string();

        System.out.println("response to session: " + respJSON);*/

    }
}
