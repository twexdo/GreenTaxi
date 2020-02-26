package com.adrian.sofergt;

import android.annotation.SuppressLint;
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
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SendLocation extends Service {
    public static final String CHANNEL_ID = "ForegroundServiceChannel";
    public static String myPhoneNumber;
    public static LatLng LOCATION;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    LocationManager locationManager;
    LocationListener locationListener;

    Date currentTime;
    NotificationCompat.Builder b;
    BroadcastReceiver mReceiver;
    SimpleDateFormat sdf;
    static int ms = 1000, metri = 5;
    DatabaseReference FirebaseRef;
    PowerManager powerManager;
    WakeLock wakeLock;


    @SuppressLint("SimpleDateFormat")
    @Override
    public void onCreate() {
        super.onCreate();

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.adrian.sofergt::SendLocationWakeLock");


        FirebaseRef = db.getReference();
        FirebaseRef.child("soferi").child(myPhoneNumber).child("settings").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                try {
                    ms = snapshot.child("ms").getValue(Integer.class);
                    metri = snapshot.child("metri").getValue(Integer.class);
                } catch (Exception e) {
                    try {
                        FirebaseRef.child("soferi").child(myPhoneNumber).child("settings").child("ms").setValue(0);
                        FirebaseRef.child("soferi").child(myPhoneNumber).child("settings").child("metri").setValue(5);
                    } catch (Exception ex) {
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });





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


        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        b = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("GreenTaxi - LOCATION")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent);

        startForeground(1, b.build());

        wakeLock.acquire();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {


            @Override
            public void onLocationChanged(Location location) {
                if (location.getLatitude() > 48 || location.getLatitude() < 43 ||
                        location.getLongitude() > 26 || location.getLongitude() < 22) {
                    myRef = db.getReference("erorii");
                    myRef.child(myPhoneNumber).child("x").setValue(location.getLatitude());
                    myRef.child(myPhoneNumber).child("y").setValue(location.getLongitude());
                }
                myRef = db.getReference("soferi");

                myRef.child(myPhoneNumber).child("x").setValue(location.getLatitude());
                myRef.child(myPhoneNumber).child("y").setValue(location.getLongitude());
                LOCATION = new LatLng(location.getLatitude(), location.getLongitude());
                currentTime = Calendar.getInstance().getTime();

                myRef.child(myPhoneNumber).child("lastSignal").setValue(sdf.format(currentTime));
                Intent intent1 = new Intent();
                intent1.setAction("com.example.andy.myapplication");
                intent1.putExtra("DATAPASSED", "Ultima locatie s-a trimisa : (" + sdf.format(currentTime) + ")");
                sendBroadcast(intent1);


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


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, ms, metri, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, ms, metri, locationListener);



        //stopSelf();
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        wakeLock.release();
        locationManager.removeUpdates(locationListener);
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
