package app.iamin.iamin.util;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import app.iamin.iamin.R;

/**
 * Created by Markus on 14.10.15.
 */
public class TimeUtils {

    public static final int SECOND = 1000;
    public static final int MINUTE = 60 * SECOND;
    public static final int HOUR = 60 * MINUTE;
    public static final int DAY = 24 * HOUR;

    private static final SimpleDateFormat FORMAT_SHORT_DATE = new SimpleDateFormat("dd. MMM");
    private static final SimpleDateFormat FORMAT_TIME_OF_DAY = new SimpleDateFormat("HH:mm");

    /**
     * Returns "Today", "Tomorrow", "Yesterday", or a short date format.
     */
    public static String formatHumanFriendlyShortDate(Context context, Date date) {
        long localTimestamp, localTime;
        long timestamp = date.getTime();
        long now = System.currentTimeMillis();

        TimeZone tz = TimeZone.getDefault();
        localTimestamp = timestamp + tz.getOffset(timestamp);
        localTime = now + tz.getOffset(now);

        long dayOrd = localTimestamp / 86400000L;
        long nowOrd = localTime / 86400000L;

        if (dayOrd == nowOrd) {
            return context.getString(R.string.day_title_today);
        } else if (dayOrd == nowOrd - 1) {
            return context.getString(R.string.day_title_yesterday);
        } else if (dayOrd == nowOrd + 1) {
            return context.getString(R.string.day_title_tomorrow);
        } else {
            return formatShortDate(new Date(timestamp));
        }
    }

    public static String formatShortDate(Date date) {
        return FORMAT_SHORT_DATE.format(date);
    }

    public static String formatTimeOfDay(Date date) {
        return FORMAT_TIME_OF_DAY.format(date);
    }
}
