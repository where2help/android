
package app.iamin.iamin;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import app.iamin.iamin.event.NeedsEvent;
import app.iamin.iamin.model.Need;

public class PullNeedsTaskMock extends AsyncTask<Void, Integer, Need[]> {

    private Context context;

    public PullNeedsTaskMock(Context context) {
        this.context = context;
    }

    @Override
    protected Need[] doInBackground(Void... params) {

        Need[] needs = null;

        try {
            InputStream inputStream = context.getAssets().open("mock.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String result = new String(buffer, "UTF-8");

            JSONArray data = new JSONObject(result).getJSONArray("data");

            needs = new Need[data.length()];
            for (int i = 0; i < data.length(); i++) {
                JSONObject obj = data.getJSONObject(i);
                needs[i] = new Need().fromJSON(context, obj);
            }

        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
            return null;
        }

        return needs;
    }

    @Override
    protected void onPostExecute(Need[] needs) {
        BusProvider.getInstance().post(new NeedsEvent(needs));
    }
}