package app.iamin.iamin.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import com.squareup.okhttp.Headers;

import app.iamin.iamin.model.User;

/**
 * Created by Markus on 14.10.15.
 */
public class EndpointUtils {

    public static final int TASK_ENDPOINT = 100;
    public static final int TASK_NEEDS = 0;
    public static final int TASK_REGISTER = 1;
    public static final int TASK_LOGIN = 2;
    public static final int TASK_LOGOUT = 3;
    public static final int TASK_VOLUNTEERING = 4;

    private static final String URL_ENDPOINT = "http://staging-where2help.herokuapp.com/api/v1/";

    private static final String URL_NEEDS = "needs";
    private static final String URL_REGISTRATION = "auth/";
    private static final String URL_LOGIN = "auth/sign_in";
    private static final String URL_LOGOUT = "auth/sign_out";
    private static final String URL_VOLUNTEERING = "volunteerings";

    // Set new endpoint
    public static void showEndpointInputPicker(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Change Endpoint");

        final EditText input = new EditText(context);
        input.setText(getEndpoint(context, TASK_ENDPOINT));
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int p) {
                storeEndpoint(context, input.getText().toString());
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int p) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    // Store endpoint
    public static void storeEndpoint(Context context, String url) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("URL_ENDPOINT", url);
        editor.apply();
    }

    // Get endpoint
    public static String getEndpoint(Context context, int pos) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String endpoint = prefs.getString("URL_ENDPOINT", URL_ENDPOINT);
        switch(pos) {
            case TASK_ENDPOINT: return endpoint;
            case TASK_NEEDS: default: return endpoint + URL_NEEDS;
            case TASK_REGISTER: return endpoint + URL_REGISTRATION;
            case TASK_LOGIN: return endpoint + URL_LOGIN;
            case TASK_LOGOUT: return endpoint + URL_LOGOUT;
            case TASK_VOLUNTEERING: return endpoint + URL_VOLUNTEERING;
        }
    }

    // Store headers
    public static void storeHeader(Context context, Headers headers) {
        SharedPreferences prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("Access-Token", headers.get("Access-Token"));
        editor.putString("Token-Type", headers.get("Token-Type"));
        editor.putString("Client", headers.get("Client"));
        editor.putString("Expiry", headers.get("Expiry"));
        editor.putString("Uid", headers.get("Uid"));
        editor.apply();
    }

    // Get headers
    public static Headers getHeaders(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        return new Headers.Builder()
                .add("Access-Token", prefs.getString("Access-Token", ""))
                .add("Token-Type", prefs.getString("Token-Type", ""))
                .add("Client", prefs.getString("Client", ""))
                .add("Expiry", prefs.getString("Expiry", ""))
                .add("Uid", prefs.getString("Uid", ""))
                .build();
    }

    // Store user
    public static void storeUser(Context context, User user) {
        SharedPreferences prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("User_id", user.getId());
        editor.putString("User_email", user.getEmail());
        editor.putString("User_first_name", user.getFirstName());
        editor.putString("User_last_name", user.getLastName());
        editor.putString("User_phone", user.getPhone());
        editor.putBoolean("User_admin", user.isAdmin());
        editor.putBoolean("User_ngo_admin", user.isNgoAdmin());
        editor.putString("User_provider", user.getProvider());
        editor.putString("User_uid", user.getUid());
        editor.putString("User_name", user.getName());
        editor.putString("User_nickname", user.getNickname());
        editor.putString("User_image", user.getImage());
        editor.apply();
    }

    // Get user
    public static User getUser(Context context) {
        User user = new User();
        SharedPreferences prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.setId(prefs.getInt("User_id", 0));
        user.setEmail(prefs.getString("User_email", null));
        user.setFirstName(prefs.getString("User_first_name", null));
        user.setLastName(prefs.getString("User_last_name", null));
        user.setPhone(prefs.getString("User_phone", null));
        user.setAdmin(prefs.getBoolean("User_admin", false));
        user.setNgoAdmin(prefs.getBoolean("User_ngo_admin", false));
        user.setProvider(prefs.getString("User_provider", null));
        user.setUid(prefs.getString("User_uid", null));
        user.setName(prefs.getString("User_name", null));
        user.setNickname(prefs.getString("User_nickname", null));
        user.setImage(prefs.getString("User_image", null));
        return user;
    }

    public static void clearUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
