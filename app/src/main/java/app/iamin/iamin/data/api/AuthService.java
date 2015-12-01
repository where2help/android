package app.iamin.iamin.data.api;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.WorkerThread;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.Headers;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;

import app.iamin.iamin.data.model.User;
import app.iamin.iamin.util.DataUtils;

import static app.iamin.iamin.data.service.DataService.EXTRA_EMAIL;
import static app.iamin.iamin.data.service.DataService.EXTRA_PASSWORD;
import static app.iamin.iamin.data.service.DataService.EXTRA_PASSWORD_CONF;
import static app.iamin.iamin.util.DataUtils.getEndpoint;
import static app.iamin.iamin.util.DataUtils.getHeaders;
import static app.iamin.iamin.util.DataUtils.storeHeader;

/**
 * Created by Markus on 08.11.15.
 */
public class AuthService {

    @WorkerThread
    public static String signUp(Context context, Intent intent) {
        String email = intent.getStringExtra(EXTRA_EMAIL);
        String password = intent.getStringExtra(EXTRA_PASSWORD);
        String passwordConf = intent.getStringExtra(EXTRA_PASSWORD_CONF);

        String url = getEndpoint(context) + "auth/";
        RequestBody requestBody = new FormEncodingBuilder()
                .add("email", email)
                .add("password", password)
                .add("password_confirmation", passwordConf)
                .add("first_name", "null")
                .add("last_name", "null")
                .add("phone", "null")
                .build();
        Request request = new Request.Builder().url(url).post(requestBody).build();

        try {
            Response response = new OkHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {
                storeHeader(context, response.headers());
                storeUser(context, response.body().string(), password);
                return null;
            }
            return parseErrorSignUp(response.body().string());
        } catch (IOException e) {
            return e.getMessage();
        } catch (ParseException e) {
            return e.getMessage();
        } catch (JSONException e) {
            return e.getMessage();
        }
    }

    @WorkerThread
    public static String signIn(Context context, Intent intent) {

        String email = intent.getStringExtra(EXTRA_EMAIL);
        String password = intent.getStringExtra(EXTRA_PASSWORD);

        String url = getEndpoint(context) + "auth/sign_in";
        RequestBody requestBody = new FormEncodingBuilder()
                .add("email", email)
                .add("password", password)
                .build();
        Request request = new Request.Builder().url(url).post(requestBody).build();

        try {
            Response response = new OkHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {
                storeHeader(context, response.headers());
                storeUser(context, response.body().string(), password);
                return null;
            }
            return parseErrorSignIn(response.body().string());
        } catch (IOException e) {
            return e.getMessage();
        } catch (ParseException e) {
            return e.getMessage();
        } catch (JSONException e) {
            return e.getMessage();
        }
    }

    @WorkerThread
    public static String signOut(Context context) {
        String url = getEndpoint(context) + "auth/sign_out";
        Headers headers = getHeaders(context);
        Request request = new Request.Builder().url(url).headers(headers).delete().build();

        try {
            Response response = new OkHttpClient().newCall(request).execute();
            if (response.isSuccessful()) {
                storeHeader(context, response.headers());
                return null;
            }
            return response.message();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private static void storeUser(Context context, String responseBody, String password) throws JSONException, ParseException, IOException {
        JSONObject obj = new JSONObject(responseBody).getJSONObject("data");
        User user = new User();
        user.setId(obj.getInt("id"));
        user.setEmail(obj.getString("email"));
        user.setPassword(password);
        user.setFirstName(obj.getString("first_name"));
        user.setLastName(obj.getString("last_name"));
        user.setPhone(obj.getString("phone"));
        // TODO: wait for fix
        //user.setAdmin(obj.getBoolean("admin"));
        //user.setNgoAdmin(obj.getBoolean("ngo_admin"));
        user.setProvider(obj.getString("provider"));
        user.setUid(obj.getString("uid"));
        user.setName(obj.getString("name"));
        user.setNickname(obj.getString("nickname"));

        DataUtils.storeUser(context, user);
    }

    private static String parseErrorSignUp(String responseBody) throws JSONException {
        String error = "";
        JSONArray messages = new JSONObject(responseBody)
                .getJSONObject("errors")
                .getJSONArray("full_messages");

        if (messages != null) {
            for (int i = 0; i < messages.length(); i++) {
                String br = (i != messages.length() - 1) ? ".\n" : ".";
                error += messages.get(i).toString() + br;
            }
        }
        return error;
    }

    private static String parseErrorSignIn(String responseBody) throws JSONException {
        String error = "";
        JSONArray messages = new JSONObject(responseBody).getJSONArray("errors");

        if (messages != null) {
            for (int i = 0; i < messages.length(); i++) {
                String br = (i != messages.length() - 1) ? ".\n" : "";
                error += messages.get(i).toString() + br;
            }
        }
        return error;
    }
}
