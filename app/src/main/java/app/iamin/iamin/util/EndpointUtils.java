package app.iamin.iamin.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

import app.iamin.iamin.R;

/**
 * Created by Markus on 14.10.15.
 */
public class EndpointUtils {

    public static final int TASK_NEEDS = 0;
    public static final int TASK_REGISTER = 1;

    private static final String URL_NEEDS = "http://where2help.herokuapp.com/api/v1/needs.json";
    private static final String URL_REGISTRATION = "http://where2help.herokuapp.com/api/v1/volunteerings/create";

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
        editor.putString(pos == 0 ? "URL_NEEDS" : "URL_REGISTRATION", url);
        editor.apply();
    }

    // Get endpoint
    public static String getEndpoint(Context context, int pos) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(
                pos == 0 ? "URL_NEEDS" : "URL_REGISTRATION",
                pos == 0 ? URL_NEEDS : URL_REGISTRATION);
    }
}
