package app.iamin.iamin;

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
import java.util.ArrayList;

/**
 * Created by paul on 10/10/2015.
 */
public class PullNeedsActiveTask extends AsyncTask<Void, Integer, HelpRequest[]> {

    private URL url;
    private MainActivity activity;

    public PullNeedsActiveTask(URL url, MainActivity activity) {
        this.url = url;
        this.activity = activity;
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
            String jsonText = readAll(rd);
            needs = gson.fromJson(jsonText, HelpRequest[].class);

        } catch (IOException e) {
            e.printStackTrace(); // TODO
        }

        return needs;
    }

    @Override
    protected void onPostExecute(HelpRequest[] result) {
        activity.updateNeeds(result);
    }
/*
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
    }*/

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }
}
