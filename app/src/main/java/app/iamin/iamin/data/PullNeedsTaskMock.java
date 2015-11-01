
package app.iamin.iamin.data;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import app.iamin.iamin.data.event.NeedsEvent;
import app.iamin.iamin.data.model.NeedOld;

public class PullNeedsTaskMock extends AsyncTask<Void, Integer, NeedOld[]> {

    private Context context;

    public PullNeedsTaskMock(Context context) {
        this.context = context;
    }

    @Override
    protected NeedOld[] doInBackground(Void... params) {

        NeedOld[] needs = null;

        try {
            InputStream inputStream = context.getAssets().open("mock.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            String result = new String(buffer, "UTF-8");

            JSONArray data = new JSONObject(result).getJSONArray("data");

            needs = new NeedOld[data.length()];
            for (int i = 0; i < data.length(); i++) {
                JSONObject obj = data.getJSONObject(i);
                needs[i] = new NeedOld().fromJSON(context, obj);
            }

            // TODO: Write to realm

        } catch (IOException | JSONException | ParseException e) {
            e.printStackTrace();
            return null;
        }

        return needs;
    }

    @Override
    protected void onPostExecute(NeedOld[] needs) {
        BusProvider.getInstance().post(new NeedsEvent(null));
    }
}