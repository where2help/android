package app.iamin.iamin.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import com.squareup.okhttp.Headers;

import app.iamin.iamin.data.model.User;

/**
 * Created by Markus on 14.10.15.
 */
public class DataUtils {
    /**
     * Default endpoint URL
     */
    private static final String URL_ENDPOINT = "http://staging-where2help.herokuapp.com/api/v1/";

    /**
     * Shows a dialog for changing the endpoint. (Developer Settings)
     */
    public static void showEndpointInputPicker(final Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Change Endpoint");

        final EditText input = new EditText(context);
        input.setText(getEndpoint(context));
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

    /**
     * Store the endpoint in the app preferences. (Developer Settings)
     */
    public static void storeEndpoint(Context context, String url) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("URL_ENDPOINT", url);
        editor.apply();
    }

    /**
     * Fetch the endpoint from app preferences. (Developer Settings)
     */
    public static String getEndpoint(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString("URL_ENDPOINT", URL_ENDPOINT);
    }

    /**
     * Deletes the Access-Token. (Developer Settings)
     */
    public static void clearToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("Access-Token", null);
        editor.apply();
    }

    /**
     * Store the headers in the app preferences.
     */
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

    /**
     * Fetch the headers from app preferences.
     */
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

    /**
     * Store the user in the app preferences.
     */
    public static void storeUser(Context context, User user) {
        SharedPreferences prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("User_id", user.getId());
        editor.putString("User_email", user.getEmail());
        editor.putString("User_password", user.getPassword());
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

    /**
     * Fetch the user from app preferences.
     */
    public static User getUser(Context context) {
        User user = new User();
        SharedPreferences prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        user.setId(prefs.getInt("User_id", 0));
        user.setEmail(prefs.getString("User_email", null));
        user.setPassword(prefs.getString("User_password", null));
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

    /**
     * Deletes all user related data stored in the app preferences.
     * This will also delete stored headers.
     */
    public static void clearUser(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    /**
     * Returns true if user has network connectivity.
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

/*    private static boolean isTokenValid(Headers headers) {
        if (!TextUtils.isEmpty(headers.get("Access-Token")) ||
                !TextUtils.isEmpty(headers.get("Expiry"))) {
            if (System.currentTimeMillis() > (Long.parseLong(headers.get("Expiry")) * 1000L)) {
                return true;
            }
        }
        return false;
    }*/
}
