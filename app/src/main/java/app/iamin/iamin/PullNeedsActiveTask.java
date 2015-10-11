package app.iamin.iamin;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;

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
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * Created by paul on 10/10/2015.
 */
public class PullNeedsActiveTask extends AsyncTask<Void, Integer, HelpRequest[]> {

    private URL url;
    private ListAdapter adapter;
    private Geocoder coder;

    public PullNeedsActiveTask(Context context, URL url, ListAdapter adapter) {
        this.url = url;
        this.adapter = adapter;
        this.coder = new Geocoder(context);
    }

    @Override
    protected HelpRequest[] doInBackground(Void... params) {

        HelpRequest[] needs = null;

        try {
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
}
