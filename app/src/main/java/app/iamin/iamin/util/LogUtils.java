package app.iamin.iamin.util;

import android.util.Log;

import com.squareup.okhttp.Headers;
import com.squareup.okhttp.Response;

import app.iamin.iamin.data.model.User;

/**
 * Created by Markus on 29.10.15.
 */
public class LogUtils {

    public static void logLocalHeaders(String TAG, Headers headers){
        Log.e(TAG, "LOCAL Access-Token = " + headers.get("Access-Token"));
        Log.e(TAG, "LOCAL Token-Type = " + headers.get("Token-Type"));
        Log.e(TAG, "LOCAL Client = " + headers.get("Client"));
        Log.e(TAG, "LOCAL Expiry = " + headers.get("Expiry"));
        Log.e(TAG, "LOCAL Uid = " + headers.get("Uid"));
    }

    public static void logHeaders(String TAG, Response response){
        Log.e(TAG, "Access-Token = " + response.headers().get("Access-Token"));
        Log.e(TAG, "Token-Type = " + response.headers().get("Token-Type"));
        Log.e(TAG, "Client = " + response.headers().get("Client"));
        Log.e(TAG, "Expiry = " + response.headers().get("Expiry"));
        Log.e(TAG, "Uid = " + response.headers().get("Uid"));
    }

    public static void logUser(String TAG, User user) {
        Log.d(TAG, "User id = " + user.getId());
        Log.d(TAG, "User email = " + user.getEmail());
        Log.d(TAG, "User first_name = " + user.getFirstName());
        Log.d(TAG, "User last_name = " + user.getLastName());
        Log.d(TAG, "User phone = " + user.getPhone());
        Log.d(TAG, "User admin = " + user.isAdmin());
        Log.d(TAG, "User ngo_admin = " + user.isNgoAdmin());
        Log.d(TAG, "User provider = " + user.getProvider());
        Log.d(TAG, "User uid = " + user.getUid());
        Log.d(TAG, "User name = " + user.getName());
        Log.d(TAG, "User nickname = " + user.getNickname());
        Log.d(TAG, "User image = " + user.getImage());
    }
}
