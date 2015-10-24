package app.iamin.iamin;

import android.content.Context;
import android.location.Address;

import java.util.Date;
import java.util.Locale;
import java.util.Random;

import app.iamin.iamin.model.Need;
import app.iamin.iamin.util.TimeUtils;

/**
 * Created by Markus on 23.10.15.
 */
public class Mock {

    // This will mock the /api/v1/needs endpoint.
    public static Need[] getNeeds(Context context, int size) {
        Need[] needs = new Need[size];
        for (int i = 0; i < size; i++) {
            if (i == 0) {
                // First need is customizable
                needs[i] = getCustomNeed(context);
            } else {
                Need need = new Need();
                need.setId(i);
                need.setCount(generateCount(1, 50));
                need.setCategory(generateCategory());
                need.setAddress(generateAddress());
                Date[] dates = generateStartEnd();
                need.setStart(dates[0]);
                need.setEnd(dates[1]);
                need.setDate(generateDate(context, dates[0], dates[1]));
                needs[i] = need;
            }
        }
        return needs;
    }

    private static Need getCustomNeed(Context context) {
        Need need = new Need();
        need.setId(0);
        need.setCount(100);
        need.setCategory(generateCategory());
        need.setAddress(generateAddress());
        Date[] dates = generateStartEnd();
        need.setStart(dates[0]);
        need.setEnd(dates[1]);
        need.setDate(generateDate(context, dates[0], dates[1]));
        return need;
    }

    private static int generateCount(int min, int max) {
        return new Random().nextInt(max - min) + min;
    }

    private static int generateCategory() {
        return new Random().nextInt(4);
    }

    private static String location[] = {
            "Hauptbahnhof",
            "Westbahnhof",
            "Hauptbahnof",
            "Hauptbahnof",
            "Hauptbahnof",
            "Bahnhof"
    };

    private static String city[] = {
            "Salzburg",
            "Wien",
            "Wien",
            "Linz",
            "Graz",
            "Rosenbach"
    };

    private static double lat[] = {
            47.8223972,
            48.196964,
            48.1852605,
            48.2943454,
            47.0728899,
            51.1258081
    };

    private static double lng[] = {
            13.0505488,
            16.339144,
            16.3765179,
            14.2944903,
            15.4170451,
            14.6821637
    };

    private static Address generateAddress() {
        Address address = new Address(Locale.GERMAN);
        int pos = new Random().nextInt(city.length);
        address.setAddressLine(0, city[pos] + " " + location[pos]);
        address.setLatitude(lat[pos]);
        address.setLongitude(lng[pos]);
        return address;
    }

    private static Date[] generateStartEnd() {
        Date start = new Date(System.currentTimeMillis() + (long) (new Random().nextDouble() * 300 * 60 * 60 * 60 * 1000));
        Date end = new Date(start.getTime() + new Random().nextInt(3 * 60 * 60 * 1000));
        return new Date[]{start, end};
    }

    private static String generateDate(Context context, Date start, Date end) {
        return TimeUtils.formatHumanFriendlyShortDate(context, start) + " " +
                TimeUtils.formatTimeOfDay(start) + " - " +
                TimeUtils.formatTimeOfDay(end) + " Uhr";
    }
}
