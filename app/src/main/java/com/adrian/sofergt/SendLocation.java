package com.adrian.sofergt;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SendLocation extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static LatLng LOCATION;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    LocationManager locationManager;
    LocationListener locationListener;
    String nrtel;
    Date currentTime;
    NotificationCompat.Builder b;
    Notification notification;
    BroadcastReceiver mReceiver;
    SimpleDateFormat sdf;

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onCreate() {
        super.onCreate();
        mReceiver = new InternetReciver(getApplicationContext());
        try {
            registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        } catch (Exception e) {
            Toast.makeText(this, "Dezactiveaza si reactiveaza statusul ", Toast.LENGTH_LONG).show();
        }

        sdf = new SimpleDateFormat("HH:mm:ss");
    }


    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        nrtel = intent.getStringExtra("nrtel");
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GreenTaxi - LOCATION")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent);
        notification = b.build();

        startForeground(1, notification);


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                if (location.getLatitude() > 48 || location.getLatitude() < 43 ||
                        location.getLongitude() > 26 || location.getLongitude() < 22) {
                    myRef = db.getReference("erorii");
                    myRef.child(nrtel).child("x").setValue(location.getLatitude());
                    myRef.child(nrtel).child("y").setValue(location.getLongitude());
                }
                myRef = db.getReference("soferi");

                myRef.child(nrtel).child("x").setValue(location.getLatitude());
                myRef.child(nrtel).child("y").setValue(location.getLongitude());
                LOCATION = new LatLng(location.getLatitude(), location.getLongitude());
                currentTime = Calendar.getInstance().getTime();

                myRef.child(nrtel).child("lastSignal").setValue(sdf.format(currentTime));

                MainActivity.change("Ultima locatie s-a trimisa : (" + sdf.format(currentTime) + ")");

                final ConnectivityManager connMgr = (ConnectivityManager) getApplicationContext()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                final NetworkInfo mobile = connMgr
                        .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

                final NetworkInfo wifi = connMgr
                        .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (mobile.isConnected() || wifi.isConnected()) {
                    SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("lastC", "Ultima locatie s-a trimisa : (" + sdf.format(currentTime) + ")");
                    editor.apply();
                }
            }


            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                try {
                    Toast.makeText(SendLocation.this, "ACTIVEAZA LOCATIA!!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (Exception e) {

                    Toast.makeText(SendLocation.this, "Activati locatia!!!!!!!!!!!", Toast.LENGTH_SHORT).show();
                }
            }
        };


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        //stopSelf();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

}
