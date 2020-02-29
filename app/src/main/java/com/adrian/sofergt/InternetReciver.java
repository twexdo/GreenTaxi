package com.adrian.sofergt;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;

import java.util.Objects;


public class InternetReciver extends BroadcastReceiver {

    Context context;
    private boolean isUsed = false;

    public InternetReciver(Context context) {
        this.context = context;


    }

    public InternetReciver() {

    }

    @Override
    public void onReceive(final Context context, final Intent intent) {


        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = Objects.requireNonNull(connMgr)
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (!wifi.isConnected() && mobile.isConnected()) {

            Log.d("Network Available ", "Flag No 1");

            try {
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Objects.requireNonNull(mNotificationManager).cancel(0);
            } catch (Exception ignored) {
            }

            isUsed = false;
        } else {
//            if (!isUsed) {
//                isUsed = true;
//                Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
////            Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), notification);
////            r.play();
//                //  Toast.makeText(context, "NO INTERNET ", Toast.LENGTH_SHORT).show();
//                NotificationManager mNotificationManager;
//
//                NotificationCompat.Builder mBuilder =
//                        new NotificationCompat.Builder(context.getApplicationContext(), "notify_001");
//                Intent ii = new Intent(context.getApplicationContext(), MainActivity.class);
//                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, ii, 0);
//
//                NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
//                bigText.bigText("Activeaza datele si opreste wifi.");
//                bigText.setBigContentTitle("Nu se detecteaza internet!");
//                bigText.setSummaryText("");
//
//                mBuilder.setContentIntent(pendingIntent);
//                mBuilder.setSmallIcon(R.drawable.ic_warning);
//                mBuilder.setContentTitle("Your Title");
//                mBuilder.setContentText("Your text");
//                mBuilder.setPriority(Notification.PRIORITY_MAX);
//                mBuilder.setStyle(bigText);
//                mBuilder.setSound(alarm);
//
//                mNotificationManager =
//                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    String channelId = "Your_channel_id";
//                    NotificationChannel channel = new NotificationChannel(
//                            channelId,
//                            "Channel human readable title",
//                            NotificationManager.IMPORTANCE_HIGH);
//                    Objects.requireNonNull(mNotificationManager).createNotificationChannel(channel);
//                    mBuilder.setChannelId(channelId);
//                }
//
//                Objects.requireNonNull(mNotificationManager).notify(1, mBuilder.build());
//            }
        }
    }
}