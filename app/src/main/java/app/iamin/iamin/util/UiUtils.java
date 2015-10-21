package app.iamin.iamin.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.IntentCompat;

import app.iamin.iamin.model.Need;
import app.iamin.iamin.ui.DetailActivity;
import app.iamin.iamin.ui.LoginActivity;
import app.iamin.iamin.ui.MainActivity;
import app.iamin.iamin.ui.SettingsActivity;
import app.iamin.iamin.ui.UserActivity;

/**
 * Created by Markus on 15.10.15.
 */
public class UiUtils {

    public static void fireMainIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        context.startActivity(intent);
    }

    public static void fireLoginIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static Intent getDetailIntent(Context context, Need need) {
        Intent intent = new Intent();
        intent.setClass(context, DetailActivity.class);
        intent.putExtra("id", need.getId());
        intent.putExtra("category", need.getCategory());

        intent.putExtra("address", need.getAddress().getAddressLine(0));
        intent.putExtra("latitude", need.getAddress().getLatitude());
        intent.putExtra("longitude", need.getAddress().getLongitude());

        intent.putExtra("start", need.getStart().getTime());
        intent.putExtra("end", need.getEnd().getTime());

        intent.putExtra("count", need.getCount());
        intent.putExtra("selfLink", need.getSelfLink());
        return intent;
    }

    // TODO: remove some day
    public static Intent getDummyDetailIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, DetailActivity.class);
        intent.putExtra("id", 0);
        intent.putExtra("category", Need.CATEGORY_VOLUNTEER);

        intent.putExtra("address", "Westbahnhof");
        intent.putExtra("latitude", 0);
        intent.putExtra("longitude", 0);

        intent.putExtra("start", System.currentTimeMillis());
        intent.putExtra("end", System.currentTimeMillis());

        intent.putExtra("count", 2);
        intent.putExtra("selfLink", "www.google.at");
        return intent;
    }

    public static void fireUserIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, UserActivity.class);
        context.startActivity(intent);
    }

    public static void fireSettingsIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, SettingsActivity.class);
        context.startActivity(intent);
    }

    public static void fireCalendarIntent(Context context, Need need) {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setType("vnd.android.cursor.item/event")
                .putExtra("beginTime", need.getStart())
                .putExtra("endTime", need.getEnd())
                .putExtra("allDay", false)
                .putExtra("title", "Where2Help - " + need.getCategoryPlural())
                .putExtra("description", "Where2Help - " + need.getCategoryPlural() + " für " +
                        TimeUtils.getDuration(need.getStart(), need.getEnd()) + ".")
                .putExtra("eventLocation", need.getAddress().getAddressLine(0));
        context.startActivity(intent);
    }

    public static void fireShareIntent(Context context, Need need) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Where2Help braucht noch " + need.getCategoryPlural() + " am " +
                TimeUtils.formatHumanFriendlyShortDate(context, need.getStart()) + " " +
                TimeUtils.formatTimeOfDay(need.getStart()) + " - " + TimeUtils.formatTimeOfDay(need.getEnd()) + " Uhr" + " für " + TimeUtils.getDuration(need.getStart(), need.getEnd()) + " am " +
                need.getAddress().getAddressLine(0) + ". (" + need.getSelfLink() + ")");
        context.startActivity(intent);
    }

    public static void fireWebIntent(Context context, Need need) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(need.getSelfLink()));
        context.startActivity(intent);
    }
}
