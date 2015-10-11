package app.iamin.iamin;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Patterns;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by paul on 10/10/2015.
 */
public class PullNeedsActiveTask extends AsyncTask<Void, Integer, HelpRequest[]> {

    private URL url;
    private ListAdapter adapter;
    private Geocoder coder;
    private Context context;

    public PullNeedsActiveTask(Context context, URL url, ListAdapter adapter) {
        this.url = url;
        this.adapter = adapter;
        this.coder = new Geocoder(context);
        this.context = context;
    }

    @Override
    protected HelpRequest[] doInBackground(Void... params) {

        HelpRequest[] needs = null;

        try {
            //registerUser();

            InputStream is = this.url.openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            JSONObject root = loadJSON();
            // no time to look deeply into gson to parse this automatically
            JSONArray data = root.getJSONArray("data");

            needs = new HelpRequest[data.length()];
            for (int i = 0; i<data.length(); i++) {
                JSONObject obj = data.getJSONObject(i);
                needs[i] = new HelpRequest();
                needs[i].fromJSON(obj, coder);
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
    protected void onPostExecute(HelpRequest[] result) {
        adapter.setData(result);
    }

    private JSONObject loadJSON() throws IOException, JSONException {
        InputStream is = this.url.openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

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
