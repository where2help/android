package app.iamin.iamin;

import android.location.Address;
import android.os.AsyncTask;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Locale;

/**
 * Created by paul on 10/10/2015.
 */
public class PullNeedsActiveTask extends AsyncTask<Void, Integer, HelpRequest[]> {

    private URL url;
    private ListAdapter adapter;

    public PullNeedsActiveTask(URL url, ListAdapter adapter) {
        this.url = url;
        this.adapter = adapter;
    }

    @Override
    protected HelpRequest[] doInBackground(Void... params) {

        HelpRequest[] needs = null;

        try {

            // load json
            //JSONObject entries = loadJSON();

            // parse json
            Gson gson = new Gson();
            InputStream is = this.url.openStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            JSONObject root = loadJSON();
            // no time to look deeply into gson to parse this automatically
            JSONArray data = root.getJSONArray("data");
            needs = new HelpRequest[data.length()];
            for (int i = 0; i<data.length(); i++) {
                JSONObject obj = data.getJSONObject(i);
                JSONObject attrs = obj.getJSONObject("attributes");
                needs[i] = new HelpRequest(attrs.getString("category"));
                needs[i].setId(obj.getInt("id"));
                needs[i].setStart(new Date(obj.getString("start-time")));
                needs[i].setEnd(new Date(obj.getString("end-time")));
                Address address = new Address(Locale.GERMAN);
                address.setFeatureName(attrs.getString("location"));
                address.setLocality(attrs.getString("city") + " " + attrs.getString("location"));
                needs[i].setAddress(address);
                needs[i].setStillOpen(attrs.getInt("volunteers-needed"));
            }
            //System.out.println(root.getJSONArray("data").toString());
            //needs = gson.fromJson(root.getJSONArray("data").toString(), HelpRequest[].class);
            //System.out.println(needs.length + " needs found");

        } catch (IOException e) {
            e.printStackTrace(); // TODO
        } catch (JSONException e) {
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
}
