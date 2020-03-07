//package com.adrian.sofergt.services;
//
//import android.annotation.SuppressLint;
//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.PendingIntent;
//import android.app.Service;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.content.SharedPreferences;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.IBinder;
//import android.os.PowerManager;
//import android.os.PowerManager.WakeLock;
//import android.provider.Settings;
//import android.util.Log;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.app.NotificationCompat;
//import androidx.core.app.NotificationManagerCompat;
//
//import com.adrian.sofergt.InternetReciver;
//import com.adrian.sofergt.MainActivity;
//import com.adrian.sofergt.R;
//import com.adrian.sofergt.communication.Acceptat;
//import com.adrian.sofergt.communication.Action;
//import com.google.android.gms.maps.model.LatLng;
//import com.google.firebase.database.ChildEventListener;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//import java.text.SimpleDateFormat;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.Objects;
//import java.util.Random;
//
//public class SendLocation extends Service {
//    public static final String CHANNEL_ID = "GreenTaxiForegroundService";
//    public static String myPhoneNumber;
//    public static LatLng LOCATION;
//    DatabaseReference mdb;
//    FirebaseDatabase db = FirebaseDatabase.getInstance();
//    DatabaseReference myRef;
//    PendingIntent pendingIntent;
//    LocationManager locationManager;
//    LocationListener locationListener;
//    Notification notification;
//    int notificationId;
//
//
//    Date currentTime;
//    NotificationCompat.Builder b;
//    BroadcastReceiver mReceiver;
//    SimpleDateFormat sdf;
//   public  static Integer ms = 0, metri = 0;
//    DatabaseReference FirebaseRef;
//    PowerManager powerManager;
//    WakeLock wakeLock;
//
//    Context context;
//
//    @SuppressLint("SimpleDateFormat")
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        context = this;
//        b = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("GreenTaxi - LOCATION")
//                .setContentText("serviciu creat")
//                .setSmallIcon(R.drawable.ic_launcher_foreground);
//
//        createNotificationChannel();
//        ((SendLocation) context).startForeground(1, b.build());
//
//
//        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//        wakeLock = Objects.requireNonNull(powerManager).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.adrian.sofergt::SendLocationWakeLock");
//
//        try {
//        mReceiver = new InternetReciver(getApplicationContext());
//        try {
//            registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
//        } catch (Exception e) {
//            Toast.makeText(this, "Dezactiveaza si reactiveaza statusul ", Toast.LENGTH_LONG).show();
//        }
//
//        sdf = new SimpleDateFormat("HH:mm:ss");
//        } catch (Exception e) {
//            stopForeground(true);
//
//        }
//    }
//
//    String receiverid, id, msg;
//
//
//    @Override
//    public void onDestroy() {
//        wakeLock.release();
//        locationManager.removeUpdates(locationListener);
//        unregisterReceiver(mReceiver);
//        if (context.getClass().isInstance(SendLocation.class)) {
//            ((SendLocation) context).stopForeground(true);
//        }
//
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        Log.e("SEND_LOCATION", "I BIDER");
//        Log.e("SEND_LOCATION", "I BIDER");
//        Log.e("SEND_LOCATION", "I BIDER");
//        Log.e("SEND_LOCATION", "I BIDER");
//        Log.e("SEND_LOCATION", "I BIDER");
//        Log.e("SEND_LOCATION", "I BIDER");
//        return null;
//    }
//
//    private void createNotificationChannel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel serviceChannel = new NotificationChannel(
//                    CHANNEL_ID,
//                    "Foreground Service Channel",
//                    NotificationManager.IMPORTANCE_DEFAULT
//            );
//            NotificationManager manager = getSystemService(NotificationManager.class);
//            Objects.requireNonNull(manager).createNotificationChannel(serviceChannel);
//        }
//    }
//
//    double x, y;
//
//    @SuppressLint("MissingPermission")
//    @Override
//    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
//
//        String input = intent.getStringExtra("inputExtra");
//
//        String nrteeel = intent.getStringExtra("nrtel");
//
//        if (nrteeel.length() > 4) myPhoneNumber = nrteeel;
//        else if (myPhoneNumber.length() < 5) {
//            input = "EROARE RESETEAZA STATUSUL";
//            Toast.makeText(context, "EROARE EROARE EROARE", Toast.LENGTH_SHORT).show();
//        }
//
//        createNotificationChannel();
//
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//
//        PendingIntent pendingIntent = PendingIntent.getActivity(this,
//                0, notificationIntent, 0);
//
//        b = new NotificationCompat.Builder(this, CHANNEL_ID)
//                .setContentTitle("GreenTaxi - LOCATION")
//                .setContentText(input)
//                .setSmallIcon(R.drawable.ic_launcher_foreground)
//                .setContentIntent(pendingIntent);
//        startForeground(1, b.build());
//
//        try {
//            wakeLock.acquire(60 * 60 * 1000L /*1 hour*/);
//
//            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//
//            locationListener = new LocationListener() {
//
//
//                @Override
//                public void onLocationChanged(Location location) {
//                    if (location.getLatitude() > 49 || location.getLatitude() < 42 ||
//                            location.getLongitude() > 27 || location.getLongitude() < 21) {
//                        myRef = db.getReference("erorii");
//                        myRef.child(myPhoneNumber).child("x").setValue(location.getLatitude());
//                        myRef.child(myPhoneNumber).child("y").setValue(location.getLongitude());
//                    }
//                    myRef = db.getReference("soferi");
//
//                    myRef.child(myPhoneNumber).child("x").setValue(location.getLatitude());
//                    myRef.child(myPhoneNumber).child("y").setValue(location.getLongitude());
//                    LOCATION = new LatLng(location.getLatitude(), location.getLongitude());
//                    currentTime = Calendar.getInstance().getTime();
//
//                    myRef.child(myPhoneNumber).child("lastSignal").setValue(sdf.format(currentTime));
//                    Intent intent1 = new Intent();
//                    intent1.setAction("com.adrian.sofergt");
//                    intent1.putExtra("lastTimeLocationHasBeenSent", "Ultima locatie s-a trimisa : (" + sdf.format(currentTime) + ")");
//                    sendBroadcast(intent1);
//
//
//                    final ConnectivityManager connMgr = (ConnectivityManager) getApplicationContext()
//                            .getSystemService(Context.CONNECTIVITY_SERVICE);
//
//                    final NetworkInfo mobile = Objects.requireNonNull(connMgr)
//                            .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//
//                    final NetworkInfo wifi = connMgr
//                            .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//
//                    if (Objects.requireNonNull(mobile).isConnected() || Objects.requireNonNull(wifi).isConnected()) {
//                        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.putString("lastC", "Ultima locatie s-a trimisa : (" + sdf.format(currentTime) + ")");
//                        editor.apply();
//                    }
//                }
//
//
//                @Override
//                public void onStatusChanged(String s, int i, Bundle bundle) {
//
//                }
//
//                @Override
//                public void onProviderEnabled(String s) {
//
//                }
//
//                @Override
//                public void onProviderDisabled(String s) {
//                    try {
//                        Toast.makeText(SendLocation.this, "ACTIVEAZA LOCATIA!!", Toast.LENGTH_LONG).show();
//                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        startActivity(intent);
//                    } catch (Exception e) {
//
//                        Toast.makeText(SendLocation.this, "Activati locatia!!!!!!!!!!!", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            };
//
//
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, ms, metri, locationListener);
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, ms, metri, locationListener);
//            retrivemsg();
//
//
//
//
//
//
//
//        } catch (Exception e) {
//            Log.e("DEBUGFUCK", e.toString());
//            stopForeground(true);
//            stopSelf();
//
//        }
//        Toast.makeText(context, "DONE!", Toast.LENGTH_SHORT).show();
//        return START_STICKY;
//    }
//
//    public void retrivemsg() {
//        Toast.makeText(context, "RETRIVE MSG ONz", Toast.LENGTH_SHORT).show();
//        mdb = FirebaseDatabase.getInstance().getReference("mesaj");
//
//
//        mdb.addChildEventListener(new ChildEventListener() {
//
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
//                try {
//                    Toast.makeText(context, "onChildAdded", Toast.LENGTH_SHORT).show();
//                    receiverid = dataSnapshot.child("to").getValue(String.class);
//
//
//                    if (receiverid.equals(myPhoneNumber)) {
//                        Toast.makeText(SendLocation.this, "Mesaj pentru mine!", Toast.LENGTH_SHORT).show();
//                        id = dataSnapshot.child("from").getValue(String.class);
//                        int type = dataSnapshot.child("type").getValue(Integer.class);
//                        if (type == 0) {
//                            x = dataSnapshot.child("x").getValue(Double.class);
//                            y = dataSnapshot.child("y").getValue(Double.class);
//                            msg = dataSnapshot.child("content").getValue(String.class);
//
//
//                            Intent notificationIntent = new Intent(SendLocation.this, Action.class);
//
//                            notificationIntent.putExtra("from", id);
//                            notificationIntent.putExtra("myId", receiverid);
//
//                            notificationIntent.putExtra("content", msg);
//                            notificationIntent.putExtra("x", x);
//                            notificationIntent.putExtra("y", y);
//                            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
//
//                                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
//                            notificationIntent.putExtra("smsid", dataSnapshot.getKey());
//
//
//                            pendingIntent = PendingIntent.getActivity(SendLocation.this,
//                                    0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//                        } else if (type == 2) {
//                            if (dataSnapshot.child("clientConfirm").getValue(Boolean.class)) {
//                                msg = "Clientul  a acceptat comanda.";
//                                Intent notificationIntent = new Intent(SendLocation.this, Acceptat.class);
//                                notificationIntent.putExtra("smsid", dataSnapshot.getKey());
//                                notificationIntent.putExtra("myId", receiverid);
//                                notificationIntent.putExtra("hisId", id);
//                                pendingIntent = PendingIntent.getActivity(SendLocation.this,
//                                        0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//                            } else {
//                                msg = "Clientul  a refuzat comanda.";
//                                Intent notificationIntent = new Intent(SendLocation.this, MainActivity.class);
//                                notificationIntent.putExtra("smsid", dataSnapshot.getKey());
//                                pendingIntent = PendingIntent.getActivity(SendLocation.this,
//                                        0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
//
//                            }
//
//                        }
//
//
//                        notificationId = new Random().nextInt();
//
//
//                        createNotificationChannel();
//                        b = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
//                                .setContentTitle(id)
//                                .setContentText(msg)
//                                .setContentIntent(pendingIntent)
//                                .setSmallIcon(R.drawable.sms);
//                        notification = b.build();
//
//
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//
//
//                            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
//                            notificationManager.notify(notificationId, b.build());
//
//                        } else {
//                            startForeground(notificationId, notification);
//                        }
//                    }
//
//                } catch (Exception e) {
//                    Toast.makeText(SendLocation.this, "EROARE!!", Toast.LENGTH_SHORT).show();
//                    Log.e("onChildAdded", e.toString());
//                    // myNR = "0";
//                }
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//    }
//
//}
//THIS CLASS IS NO LONGER USED