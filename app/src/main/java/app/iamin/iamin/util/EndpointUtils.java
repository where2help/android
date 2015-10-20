package app.iamin.iamin.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import com.squareup.okhttp.Headers;

import app.iamin.iamin.R;
import app.iamin.iamin.model.User;

/**
 * Created by Markus on 14.10.15.
 */
public class EndpointUtils {

    public static final int TASK_NEEDS = 0;
    public static final int TASK_REGISTER = 1;
    public static final int TASK_LOGIN = 2;

    private static final String URL_NEEDS = "http://where2help.informatom.com/api/v1/needs";
    private static final String URL_REGISTRATION = "http://where2help.informatom.com/api/v1/auth/";
    private static final String URL_LOGIN = "http://where2help.informatom.com/api/v1/auth/sign_in";

    // Choose endpoint
    public static void showEndpointPicker(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Pick Endpoint")
                .setItems(R.array.endpoint, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        showEndpointInputPicker(context, which);
                    }
                });
        builder.show();
    }

    // Set new endpoint
    public static void showEndpointInputPicker(final Context context, final int which) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Change Endpoint");

        final EditText input = new EditText(context);
        input.setText(getEndpoint(context, which));
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int p) {
                storeEndpoint(context, input.getText().toString(), which);
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
    public static void storeEndpoint(Context context, String url, int pos) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        switch(pos) {
            case TASK_NEEDS: editor.putString("URL_NEEDS", url); break;
            case TASK_REGISTER: editor.putString("URL_REGISTRATION", url); break;
            case TASK_LOGIN: editor.putString("URL_LOGIN", url); break;
        }
        //editor.putString(pos == 0 ? "URL_NEEDS" : "URL_REGISTRATION", url);
        editor.apply();
    }

    // Get endpoint
    public static String getEndpoint(Context context, int pos) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        switch(pos) {
            case TASK_NEEDS: default: return prefs.getString("URL_NEEDS", URL_NEEDS);
            case TASK_REGISTER: return prefs.getString("URL_REGISTRATION", URL_REGISTRATION);
            case TASK_LOGIN: return prefs.getString("URL_LOGIN", URL_LOGIN);
        }
        //return prefs.getString(pos == 0 ? "URL_NEEDS" : "URL_REGISTRATION", pos == 0 ? URL_NEEDS : URL_REGISTRATION);
    }

    // Store headers
    public static void storeHeader(Context context, Headers headers) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
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
}
