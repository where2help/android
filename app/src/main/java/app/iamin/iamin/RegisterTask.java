package app.iamin.iamin;

import android.os.AsyncTask;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by paul on 10/11/2015.
 */
public class RegisterTask extends AsyncTask<Void, Integer, Integer> {

    int needId;
    String email;
    DetailActivity activity;

    public RegisterTask(DetailActivity activity, int needId, String email) {
        this.needId = needId;
        this.email = email;
        this.activity = activity;
    }

    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    @Override
    protected Integer doInBackground(Void... params) {
        String json = "{ \"email\": \"" + email + "\", \"need-id\":\"" + needId + "\"}";

        try {
            OkHttpClient client = new OkHttpClient();
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .addHeader("Content-Type", "application/json")
                    .url(new URL("http://where2help.herokuapp.com/api/v1/volunteerings/create"))
                    .post(body)
                    .build();
            Response response = client.newCall(request).execute();
            String respJSON = response.body().string();

            System.out.println(respJSON);

            JSONObject obj = new JSONObject(respJSON);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0; // should actually return volunteering-id

    }

    protected void onPostExecute(Integer result) {
        activity.onRegisterSuccess();
    }
}
