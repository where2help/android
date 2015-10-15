package app.iamin.iamin.util;

import android.content.Context;
import android.content.Intent;

import app.iamin.iamin.model.Need;
import app.iamin.iamin.ui.DetailActivity;

/**
 * Created by Markus on 15.10.15.
 */
public class UiUtils {

    public static Intent createDetailIntent(Context context, Need need) {
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
    public static Intent createDummyDetailIntent(Context context) {
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
}
