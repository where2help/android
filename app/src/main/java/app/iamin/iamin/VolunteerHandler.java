package app.iamin.iamin;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import app.iamin.iamin.model.User;

import static app.iamin.iamin.util.EndpointUtils.getEndpoint;
import static app.iamin.iamin.util.EndpointUtils.getHeaders;
import static app.iamin.iamin.util.EndpointUtils.getUser;
import static app.iamin.iamin.util.EndpointUtils.isOnline;
import static app.iamin.iamin.util.EndpointUtils.storeHeader;

/**
 * This handles a users volunteering status for a given need.
 */
public class VolunteerHandler {
    private static final String TAG = "VolunteerHandler";

    private Context context;

    private static final int CREATE = 0;
    private static final int DELETE = 1;

    public VolunteerHandler(Context context) {
        this.context = context;
    }

    /**
     * Creates a volunteering.
     */
    public void create(int needId) {
        new VolunteerTask(CREATE, needId).execute(context);
    }

    /**
     * Deletes a volunteering.
     */
    public void delete(int needId) {
        new VolunteerTask(DELETE, needId).execute(context);
    }

    private class VolunteerTask extends AsyncTask<Context, Void, List<String>> {

        private final int action;
        private final int needId;

        public VolunteerTask(int action, int needId) {
            this.action = action;
            this.needId = needId;
        }

        private String formBody(int userId, int needId) {
            return "{ \"data\":{\"type\":\"volunteerings\",\"attributes\":{" +
                    "\"user-id\":\""+ userId + "\",\"need-id\":\"" + needId +"\"}}}";
        }

        private Request buildRequest(int action, String url, Headers headers, RequestBody requestBody) {
            switch (action) {
                default:
                case CREATE:
                    return new Request.Builder()
                            .headers(headers)
                            .url(url)
                            .post(requestBody)
                            .build();
                case DELETE:
                    return new Request.Builder()
                            .headers(headers)
                            .url(url)
                            .delete(requestBody).build();
            }
        }

        @Override
        protected List<String> doInBackground(Context... contexts) {
            Context context = contexts[0];
            List<String> errors = new ArrayList<>();
            MediaType JSON = MediaType.parse("application/vnd.api+json; charset=utf-8");

            User user = getUser(context);
            if (user.getEmail() == null) {
                errors.add(context.getString(R.string.error_user_not_found));
                return errors;
            }

            if (!isOnline(context)) {
                errors.add(context.getString(R.string.error_no_connection));
                return errors;
            }

            String url = getEndpoint(context) + "volunteerings";
            Log.e(TAG, "URL: " + url);
            String body = formBody(user.getId(), needId);
            Headers headers = getHeaders(context);
            RequestBody requestBody = RequestBody.create(JSON, body);

            try {
                OkHttpClient client = new OkHttpClient();
                Request request = buildRequest(action, url, headers, requestBody);
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();
                Log.e(TAG, "RESPONSE: " + responseBody);
                Log.e(TAG, "STATUS: " + response.message());
                Log.e(TAG, "STATUS CODE: " + response.code());

                if (response.isSuccessful()) {
                    storeHeader(context, response.headers());
                    return null;
                }

            } catch (IOException e) {
                errors.add(e.getMessage());
                e.printStackTrace();
            }

            return errors;
        }

        protected void onPostExecute(List<String> errors) {
            //TODO: handle errors
        }
    }
}
