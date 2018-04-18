package mugambbo.github.com.medmanager.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import mugambbo.github.com.medmanager.R;

public class NotificationHelper extends ContextWrapper {
    private NotificationManager mNotificationManager;
    private final String MY_CHANNEL = "med_manager_channel";
    int requestCode = 102;
    private final long[] vibrationScheme = new long[]{200, 400};

    /**
     * Registers notification channels, which can be used later by individual notifications.
     *
     * @param context The application context
     */
    public NotificationHelper(Context context, String medName) {
        super(context);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            // Create the channel object with the unique ID MY_CHANNEL
            NotificationChannel myChannel =
                    new NotificationChannel(
                            MY_CHANNEL,
                            medName,
                            NotificationManager.IMPORTANCE_DEFAULT);

            // Configure the channel's initial settings
            myChannel.setLightColor(getColor(R.color.colorPrimary));
            myChannel.setVibrationPattern(vibrationScheme);

            // Submit the notification channel object to the notification manager
            getNotificationManager().createNotificationChannel(myChannel);

        }
    }

    /**
     * Build you notification with desired configurations
     *
     */
    public NotificationCompat.Builder getNotificationBuilder(String title, String body, Intent intent) {

        Bitmap notificationLargeIconBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, requestCode, new Intent[]{intent}, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(getApplicationContext(), MY_CHANNEL)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(notificationLargeIconBitmap)
                .setContentTitle(title)
                .setContentText(body)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVibrate(vibrationScheme)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
    }

    public NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mNotificationManager;
    }

}