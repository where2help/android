package app.iamin.iamin.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.IntentCompat;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import app.iamin.iamin.data.model.Need;
import app.iamin.iamin.data.model.NeedOld;
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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        context.startActivity(intent);
    }

    public static Intent getDetailIntent(Context context, Need need) {
        Intent intent = new Intent();
        intent.setClass(context, DetailActivity.class);
        intent.putExtra("id", need.getId());
        intent.putExtra("category", need.getCategory());

        intent.putExtra("city", need.getCity());
        intent.putExtra("location", need.getLocation());
        intent.putExtra("latitude", need.getLat());
        intent.putExtra("longitude", need.getLng());

        intent.putExtra("start", need.getStart().getTime());
        intent.putExtra("end", need.getEnd().getTime());
        intent.putExtra("date", need.getDate());

        intent.putExtra("needed", need.getNeeded());
        intent.putExtra("count", need.getCount());
        intent.putExtra("selfLink", need.getSelfLink());

        intent.putExtra("attending", need.isAttending());
        return intent;
    }

    // TODO: remove some day
    public static Intent getDummyDetailIntent(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, DetailActivity.class);
        intent.putExtra("id", 0);
        intent.putExtra("category", NeedOld.CATEGORY_VOLUNTEER);

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
        Intent intent = new Intent(Intent.ACTION_EDIT)
                .setType("vnd.android.cursor.item/event")
                .putExtra("beginTime", need.getStart().getTime())
                .putExtra("endTime", need.getEnd().getTime())
                .putExtra("allDay", false)
                .putExtra("title", "Where2Help - " + NeedUtils.getCategoryPlural(need.getCategory()))
                .putExtra("description", "Where2Help - " + NeedUtils.getCategoryPlural(need.getCategory()) + " für " +
                        TimeUtils.getDuration(need.getStart(), need.getEnd()) + ".")
                .putExtra("eventLocation", need.getCity()+ " " + need.getLocation());
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, "No Calendar found", Toast.LENGTH_SHORT).show();
        }
    }

    public static void fireShareIntent(Context context, Need need) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "Where2Help braucht noch " + NeedUtils.getCategoryPlural(need.getCategory()) + " am " +
                TimeUtils.formatHumanFriendlyShortDate(context, need.getStart()) + " " +
                TimeUtils.formatTimeOfDay(need.getStart()) + " - " + TimeUtils.formatTimeOfDay(need.getEnd()) + " Uhr" + " für " + TimeUtils.getDuration(need.getStart(), need.getEnd()) + " am " +
                need.getCity()+ " " + need.getLocation() + ". (" + need.getSelfLink() + ")");
        context.startActivity(intent);
    }

    public static void fireWebIntent(Context context, NeedOld need) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(need.getSelfLink()));
        context.startActivity(intent);
    }

    public static void fireMapIntent(Context context, Need need) {
        LatLng location = NeedUtils.getLocation(need);
        String geo = location.latitude + "," + location.longitude;
        Uri gmmIntentUri = Uri.parse("geo:" + geo + "?q=" + need.getCity()+ " " + need.getLocation());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(mapIntent);
        }
    }
}
