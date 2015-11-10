package app.iamin.iamin.data.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import app.iamin.iamin.R;
import app.iamin.iamin.util.UiUtils;

/**
 * Created by Markus on 13.10.15.
 */
public class UtilityService extends IntentService {
    private static final String TAG = UtilityService.class.getSimpleName();

    private static final String ACTION_TRIGGER_NOTIFICATION = "trigger_notification";
    private static final String ACTION_CLEAR_NOTIFICATION = "clear_notification";

    public static final int MOBILE_NOTIFICATION_ID = 100;

    public UtilityService() {
        super(TAG);
    }

    public static void triggerNotification(Context context) {
        Intent intent = new Intent(context, UtilityService.class);
        intent.setAction(UtilityService.ACTION_TRIGGER_NOTIFICATION);
        // TODO: add need as extra
        context.startService(intent);
    }

    public static void clearNotification(Context context) {
        Intent intent = new Intent(context, UtilityService.class);
        intent.setAction(UtilityService.ACTION_CLEAR_NOTIFICATION);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent != null ? intent.getAction() : null;
        if (ACTION_TRIGGER_NOTIFICATION.equals(action)) {
            showNotification(intent);
        } else if (ACTION_CLEAR_NOTIFICATION.equals(action)) {
            clearNotificationInternal();
        }
    }

    /**
     * Clears the local device notification
     */
    private void clearNotificationInternal() {
        Log.v(TAG, ACTION_CLEAR_NOTIFICATION);
        NotificationManagerCompat.from(this).cancel(MOBILE_NOTIFICATION_ID);
    }

    /**
     * Show the notification.
     */
    private void showNotification(Intent intent) {
        intent = UiUtils.getDummyDetailIntent(this);
        // The intent to trigger when the notification is tapped
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Construct the main notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("2 Freiwillge benötigt!")
                .setContentText("Von 7:30 - 9:45 am Westbahnhof.")
                .setSmallIcon(R.drawable.ic_volunteer)
                .setContentIntent(pendingIntent)
                //.setDeleteIntent()
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setCategory(Notification.CATEGORY_RECOMMENDATION)
                .setAutoCancel(true);

        // Trigger the notification
        NotificationManagerCompat.from(this).notify(MOBILE_NOTIFICATION_ID, builder.build());
    }
}
