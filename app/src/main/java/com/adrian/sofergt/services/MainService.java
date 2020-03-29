package com.adrian.sofergt.services;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.adrian.sofergt.MainActivity;
import com.adrian.sofergt.R;
import com.adrian.sofergt.ui.RegisterActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class MainService extends Service {
    private final String TAG="NEW_SERVICE";
    private final static String CHANNEL_ID = "location_channel";
    private static final String EXTRA_STARTED_FROM_NOTIFICATION = "com.adrian.sofergt.services"+".started_from_notification";
    private final IBinder myBinder = new LocalBinder();
    private static final long UPDATE_INTERFAL_IN_MIL=5000;
    private static final long FASTEST_UPDATE_INTERFAL_IN_MIL=UPDATE_INTERFAL_IN_MIL/2;
    private static final int NOTIF_ID=2020;
    private boolean mChangingConfiguration=false;
    private NotificationManager mNotificationManager;

    private LocationRequest locationRequest;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;
    private Handler mServiceHandler;
    private Location mLocation;

    DatabaseReference myref;

    PowerManager.WakeLock wakeLock;
    PowerManager powerManager;

    String myPhoneNumber="";
    public MainService(){

    }

    @Override
    public void onCreate() {


        getMyPhoneNumber();
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = Objects.requireNonNull(powerManager).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.adrian.sofergt::ServiceWakeLock");
        Log.d(TAG,"onCreate");

        myref= FirebaseDatabase.getInstance().getReference();
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);

        locationCallback=new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                onNewLocation(locationResult.getLastLocation());

            }
        };
        createLocationRequest();
        getLastLocation();

        HandlerThread handlerThread=new HandlerThread("LBS");
        handlerThread.start();
        mServiceHandler=new Handler(handlerThread.getLooper());
        mNotificationManager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
        Uri sound = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/raw/silent");
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            Log.d(TAG,"Build version is >= API 26");
            NotificationChannel mNotificationChannel=new NotificationChannel(CHANNEL_ID,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationChannel.setSound(sound, audioAttributes);
            mNotificationManager.createNotificationChannel(mNotificationChannel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
        try {
            wakeLock.acquire(60 * 60 * 1000L /*1 hour*/);
            Log.d(TAG,"WakeLock aquired");
        }catch (Exception e){
            Log.e(TAG,e.toString());
        }
        boolean startedFromNotification=intent.getBooleanExtra(EXTRA_STARTED_FROM_NOTIFICATION,false);
        if(startedFromNotification){
            Log.d(TAG,"Removed from noification");
            removeLocationUpdates();
           // stopSelf();

        }
        startForeground(NOTIF_ID,getNotification());
        return START_NOT_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mChangingConfiguration=true;

    }


    public void removeLocationUpdates() {
        try{

            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            CommonClass.setRequestLocationUpates(this,false);
            stopForeground(true);
            stopSelf();
        }catch (SecurityException secEx){
            CommonClass.setRequestLocationUpates(this,true);
            Log.e("SVC:LBS:RLU","Lost Location permissions could not update permissions!"+secEx);
        }
    }

    private void getLastLocation() {
        try{

            fusedLocationProviderClient.getLastLocation()
                    .addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if(task.isSuccessful() && task.getResult() !=null){
                                mLocation=task.getResult();
                            }else{
                                Log.e("SVC:LBS:GLL","Failed to get Location!");

                            }
                        }
                    });

        }catch (SecurityException secEx){
            Log.e("SVC:LBS:GLL","Lost location permissions:"+secEx);
        }
    }

    private void createLocationRequest() {
        locationRequest=new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERFAL_IN_MIL);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERFAL_IN_MIL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void onNewLocation(Location lastLocation) {
        Log.d(TAG,"New Location...");
        //trimite locatia

        //EventBus.getDefault().postSticky(new SendLocationToActivity(mLocation));
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String result = sdf.format(currentTime);
        myref.child("soferi").child(myPhoneNumber).child("lastSignal").setValue(result);

        myref.child("soferi").child(myPhoneNumber).child("x").setValue(lastLocation.getLatitude());
        myref.child("soferi").child(myPhoneNumber).child("y").setValue(lastLocation.getLongitude());

        //Log.e("NEWLOC",lastLocation.toString());

        if(serviceIsRunningInForeground(this))
            mNotificationManager.notify(NOTIF_ID,getNotification()) ;
    }

    private Notification getNotification() {
        Intent intent=new Intent(this, MainService.class);
        String text= CommonClass.getLocationText(mLocation);
        createNotificationChannel();
        intent.putExtra(EXTRA_STARTED_FROM_NOTIFICATION,true);
        PendingIntent servicePendingIntent= PendingIntent.getService(this,0,intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent activityPendingIntent=PendingIntent.getActivity(this,0,
                new Intent(this, MainActivity.class),0);
        Uri sound = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/raw/silent");
        NotificationCompat.Builder builder=new NotificationCompat.Builder(this)
                .setContentText(text)
                .setContentTitle(CommonClass.getLocationText(this))
                .setOngoing(true)
                 .setSound(sound)
                .setPriority(Notification.PRIORITY_HIGH)
                .setTicker(text)
                .setSmallIcon(R.drawable.ic_location_on_black_24dp)
                //.setWhen(System.currentTimeMillis())
                ;

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                builder.setChannelId(CHANNEL_ID);
        }
        Log.d(TAG,"returned builder.build()");
        return builder.build();


    }

    private boolean serviceIsRunningInForeground(MainService mainService) {
        ActivityManager manager=(ActivityManager)getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if (getClass().getName().equals(service.service.getClassName()))
                if(service.foreground)return true;
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"Ibinder on bind ");
        stopForeground(true);
        mChangingConfiguration=false;
        return  myBinder;

    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG,"Ibinder on rebind ");
        stopForeground(true);
        mChangingConfiguration=false;
        super.onRebind(intent);

    }


    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG,"Ibinder on UNbind ");
        if(!mChangingConfiguration && CommonClass.requestLocationUpates(this))
            startForeground(NOTIF_ID,getNotification());
        return true;
    }

    public void requestLocationUpdates() {
        Log.d(TAG,"Requested Updates ");
        CommonClass.setRequestLocationUpates(this,true);
        startService(new Intent(getApplicationContext(), MainService.class));

        try{

            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());


        }catch (SecurityException ex){
            Log.e("SVC:LBS:RLU","Lost location permission.Could not request it "+ex);
        }


    }

    public class LocalBinder extends Binder {
        public MainService getService() {
            return MainService.this;
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        try {
            wakeLock.release();
        }catch (Exception e){
            Log.e("Exception_e",e.toString());
        }
        mServiceHandler.removeCallbacks(null);
        super.onDestroy();
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
    int getMyPhoneNumberIncercari = 0;

    private String getMyPhoneNumber() {
        Log.e(TAG,"am apelat getMyPhone number : "+getMyPhoneNumberIncercari);
        //verific mai intai daca am luat deja numarul de telefon
        if (myPhoneNumber.length() < 5) {
            //incerc sa iau nr de telefon
            try {
                //daca reusesc pot merge mai departe

                myPhoneNumber=FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                Log.e(TAG,"am gasit numarul: "+myPhoneNumber);
                getMyPhoneNumberIncercari = 0;
                return myPhoneNumber;

            } catch (Exception e) {
                Log.e(TAG,"Am gasit o eroare: "+e.getMessage());
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


}