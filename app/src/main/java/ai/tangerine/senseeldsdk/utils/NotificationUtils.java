package ai.tangerine.senseeldsdk.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import ai.tangerine.eldsdk.ELDSdk;
import ai.tangerine.senseeldsdk.R;

public class NotificationUtils {


    public static final int NOTIFICATION_ID = 1201;
    static String NOTIFICATION_CHANNEL = "connection_channel";

    public static Notification getConnectionNotification(Context context){
        initConnectionChannel(context);
        Intent notificationIntent;

        notificationIntent = getLaunchIntent(context);

        PendingIntent pendingIntent =
            PendingIntent.getActivity(context, 0, notificationIntent, 0);
        Notification notification =
            new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getText(R.string.notification_desc))
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setColor(ContextCompat.getColor(context, R.color.colorAccent))
                .setSmallIcon(ELDSdk.iNotificationIcon)
                .build();
        return notification;
    }

    private static void initConnectionChannel(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            String id = NOTIFICATION_CHANNEL;
            CharSequence name = context.getString(R.string.app_name);
            String description = NOTIFICATION_CHANNEL;
            NotificationChannel mChannel = new NotificationChannel(id, name, NotificationManager.IMPORTANCE_LOW);
            mChannel.setDescription(description);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(mChannel);
            }
        }
    }

    private static Intent getLaunchIntent(Context context){
        PackageManager pm = context.getPackageManager();
        return pm.getLaunchIntentForPackage(context.getPackageName());
    }


    /**
     * Called to infer whether the given notification is active
     * @param context Context
     * @param notificationID int notification id of the given notification
     * @return boolean true if notification is active
     */
    public static boolean isForegroundNotificationVisible(Context context, int notificationID) {
        NotificationManager mNotificationManager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            StatusBarNotification[] activeNotifications = mNotificationManager.getActiveNotifications();

            for (StatusBarNotification notification : activeNotifications) {
                if (notification.getId() == notificationID) {
                    return true;
                }
            }
        }

        return false;
    }
}
