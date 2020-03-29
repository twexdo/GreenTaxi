package com.adrian.sofergt.services;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.adrian.sofergt.Comanda;
import com.adrian.sofergt.ComenziActivity;
import com.adrian.sofergt.MainActivity;
import com.adrian.sofergt.R;
import com.adrian.sofergt.communication.Action;
import com.adrian.sofergt.ui.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Objects;
import java.util.Random;

public class FMessagingService extends FirebaseMessagingService {
    private static final String TAG ="FCM:DevDebug" ;
    String receiverid="",id="",msg="",myPhoneNumber="";
    double x,y;
    PendingIntent pendingIntent;
    DatabaseReference dbr;
    int notificationId;
    NotificationCompat.Builder notificationBuilder;
    Notification notification;
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        dbr= FirebaseDatabase.getInstance().getReference("soferi");
        Log.d(TAG,"We recived data...");
        if(remoteMessage.getData().size()>0) {
            Log.d(TAG,"data is bigger than 0...");


            receiverid = remoteMessage.getData().get("n_to");

            Log.d(TAG,"to:"+receiverid);
            Log.d(TAG,"myphone:"+getMyPhoneNumber());
            if (receiverid != null && receiverid.equals(getMyPhoneNumber())) {

                id = remoteMessage.getData().get("n_from");
                int type = Integer.parseInt(Objects.requireNonNull(remoteMessage.getData().get("n_type")));
                if (type == 0) {
                    x = Double.parseDouble(Objects.requireNonNull(remoteMessage.getData().get("n_x")));
                    y = Double.parseDouble(Objects.requireNonNull(remoteMessage.getData().get("n_y")));
                    msg = remoteMessage.getData().get("n_content");
                    Log.e(TAG,"type: "+type+
                                    "\n to: "+receiverid+
                                    "\n from: "+id+
                                    "\n content: "+msg+
                                    "\n x/y:"+x+" / "+y
                    );

                    Intent notificationIntent = new Intent(getApplicationContext(), Action.class);

                    notificationIntent.putExtra("from", id);
                    notificationIntent.putExtra("myId", receiverid);
                    notificationIntent.putExtra("content", msg);
                    notificationIntent.putExtra("x", x);
                    notificationIntent.putExtra("y", y);
                    notificationIntent.putExtra("smsid", remoteMessage.getData().get("n_childkey"));

                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                            0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                } else if (type == 2) {
                    Log.e(TAG,"n_acceptat : "+remoteMessage.getData().get("n_acceptat"));
                    if (Boolean.parseBoolean(remoteMessage.getData().get("n_acceptat"))) {
                        msg = "Clientul  a acceptat comanda.";
                        Intent notificationIntent = new Intent(getApplicationContext(), ComenziActivity.class);
                        notificationIntent.putExtra("smsid", remoteMessage.getData().get("n_childkey"));
                        notificationIntent.putExtra("myId", receiverid);
                        notificationIntent.putExtra("hisId", id);

                        String oraStab = remoteMessage.getData().get("n_oraStab");

                            int timp = Integer.parseInt(Objects.requireNonNull(remoteMessage.getData().get("n_timp")));
                            Log.d(TAG,"new Comanda("+remoteMessage.getData().get("n_childkey")+","+id+","+timp+","+oraStab+");");
                            dbr.child(receiverid).child("comenzi").push().setValue(new Comanda(remoteMessage.getData().get("n_childkey"), id, timp, oraStab));

                            pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                                    0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    } else {
                        msg = "Clientul  a refuzat comanda.";
                        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
                        notificationIntent.putExtra("smsid", remoteMessage.getData().get("n_childkey"));
                        pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    }

                }


                notificationId = new Random().nextInt();


                createNotificationChannel();
                notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), "orders_channel")
                        .setContentTitle(id)
                        .setContentText(msg)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setContentIntent(pendingIntent)
                        .setSmallIcon(R.drawable.sms);
                notification = notificationBuilder.build();


                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    startForeground(notificationId, notification);

                } else {
                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                    notificationManager.notify(notificationId, notificationBuilder.build());

                }
                PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);

                PowerManager.WakeLock wl = null;
                if (pm != null) {
                    wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "com.adrian:MyLock");
                }
                if (wl != null) {
                    wl.acquire(10000);
                }
                PowerManager.WakeLock wl_cpu = null;
                if (pm != null) {
                    wl_cpu = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.adrian:MyCpuLock");
                }

                if (wl_cpu != null) {
                    wl_cpu.acquire(10000);
                }

            }
        }


    }

    int getMyPhoneNumberIncercari = 0;
    private String getMyPhoneNumber() {
        Log.e("AcceptatDebug","am apelat getMyPhone number : "+getMyPhoneNumberIncercari);
        //verific mai intai daca am luat deja numarul de telefon
        if (myPhoneNumber.length() < 5) {
            //incerc sa iau nr de telefon
            try {
                //daca reusesc pot merge mai departe

                myPhoneNumber= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                Log.e("AcceptatDebug","am gasit numarul: "+myPhoneNumber);
                getMyPhoneNumberIncercari = 0;
                return myPhoneNumber;

            } catch (Exception e) {
                Log.e("AcceptatDebug","Am gasit o eroare: "+e.getMessage());
                if (getMyPhoneNumberIncercari < 3) {
                    getMyPhoneNumberIncercari++;
                    return getMyPhoneNumber();
                } else {
                    getMyPhoneNumberIncercari = 0;
                    registerMe();
                    return "";

                }
            }
        } else {
            return myPhoneNumber;
        }
    }
    private void registerMe() {
        Intent register = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(register);
    }
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    private void sendRegistrationToServer(String token) {
        DatabaseReference dbref= FirebaseDatabase.getInstance().getReference("tokens");
        try{

            dbref.child(getMyPhoneNumber()).setValue(token);}
        catch (Exception err){
            Toast.makeText(this, err.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "orders_channel",
                    "Orders manager",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            Objects.requireNonNull(manager).createNotificationChannel(serviceChannel);
        }
    }
}
