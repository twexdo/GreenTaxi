package com.adrian.sofergt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.adrian.sofergt.services.CommonClass;
import com.adrian.sofergt.services.LocationBackgroundService;
import com.adrian.sofergt.ui.RegisterActivity;
import com.adrian.sofergt.ui.SettingsActivity;
import com.adrian.sofergt.ui.View_MapActivity;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    LocationBackgroundService locationBackgroundService = null;
    boolean mBound = false;


    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            LocationBackgroundService.LocalBinder binder = (LocationBackgroundService.LocalBinder) iBinder;
            locationBackgroundService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            locationBackgroundService = null;
            mBound = false;

        }
    };

    //Stringuri
    public static final String SHARE_PREFS = "sharedPrefs";
    //Inturi
    static int STATUS = 0;
    public static final String TEXT = "text";
    static String TELEFON;
    //TextView-uri
    static TextView textvvv;
    String[] dates;
    String textData, state_String;
    int state, toastCounter = 0, threadCounter = 0;
    int color;
    TextView status;
    //Firebase
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    //BroadcastReceivere
    BroadcastReceiver broadcastReceiver;
    DatabaseReference myRef;
    //Valori ooleene
    boolean inregistrat = false;
    boolean butonAPASAT = false;
    //Butoane
    Button liber, ocupat, setari, locatie, dezactivare;
    //Clase proprii
    FileHelper fileHelper;
    //Context
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Dexter.withActivity(MainActivity.this)
                    .withPermissions(Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.INTERNET,
                            Manifest.permission.WAKE_LOCK,
                            Manifest.permission.FOREGROUND_SERVICE,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    .withListener(new PermissionsListenerClass(MainActivity.this))
                    .check();
        } else {
            Dexter.withActivity(MainActivity.this)
                    .withPermissions(Manifest.permission.ACCESS_NETWORK_STATE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.INTERNET,
                            Manifest.permission.WAKE_LOCK)
                    .withListener(new PermissionsListenerClass(MainActivity.this))
                    .check();
        }



    }

    @Override
    protected void onStart() {
        super.onStart();
//        PreferenceManager.getDefaultSharedPreferences(this)
//                .registerOnSharedPreferenceChangeListener(this);
//        EventBus.getDefault().register(this);


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.adrian.sofergt");
        registerReceiver(broadcastReceiver, intentFilter);
    }


    @Override
    protected void onStop() {
        super.onStop();

//        PreferenceManager.getDefaultSharedPreferences(this)
//                .unregisterOnSharedPreferenceChangeListener(this);
//        EventBus.getDefault().unregister(this);

        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }
        // stopServiceFunction();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 10) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                configureButton();

            }
        } else if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(context, "Permission Garanted!", Toast.LENGTH_SHORT).show();

            }
        }

    }

    public void configureButton() {

        locatie.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, View_MapActivity.class);

                MainActivity.this.startActivity(myIntent);
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (420 == requestCode) {
            if (resultCode == 42) {

                FileHelper h = new FileHelper();

                String text = h.readFromFile(this);

                dates = text.split("\n");

                if (text.length() > 3) {
                    dates[1] = dates[1].replaceAll("\\s+", "");
                    dates[2] = dates[2].replaceAll("\\s+", "");
                    TELEFON = dates[2];
                    inregistrat = true;
                    setTitle(dates[1] + "\n" + dates[2]);
                    // Toast.makeText(this, "Bine ai venit " + dates[1] + "! ", Toast.LENGTH_SHORT).show();
                }
                loadData();
                switch (STATUS) {
                    case 0:
                        status.setBackgroundColor(Color.GRAY);
                        status.setText("DEZACTIVAT");

                        myRef = db.getReference("soferi");
                        myRef.child(dates[2]).child("status").setValue(0);
                        break;
                    case 1:
                        status.setBackgroundColor(Color.GREEN);
                        status.setText("LIBER");

                        myRef = db.getReference("soferi");
                        myRef.child(dates[2]).child("status").setValue(1);
                        break;
                    case 2:
                        status.setBackgroundColor(Color.RED);
                        status.setText("OCUPAT");

                        myRef = db.getReference("soferi");
                        myRef.child(dates[2]).child("status").setValue(2);
                        break;
                    case 3:
                        status.setBackgroundColor(Color.parseColor("#4A148C"));
                        status.setText("DUBLU OCUPAT");

                        myRef = db.getReference("soferi");
                        myRef.child(dates[2]).child("status").setValue(3);
                        break;
                    default:
                        break;

                }

            }
        }

    }

    public void saveData() {
        final ConnectivityManager connMgr = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        final android.net.NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (mobile.isConnected() || wifi.isConnected()) {
            SharedPreferences sharedPreferences = getSharedPreferences(SHARE_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(TEXT, STATUS);
            editor.putString("lastC", textvvv.getText().toString());
            editor.apply();
        }

    }

    public void loadData() {

        SharedPreferences sharedPreferences = getSharedPreferences(SHARE_PREFS, MODE_PRIVATE);
        STATUS = sharedPreferences.getInt(TEXT, 0);
        textData = sharedPreferences.getString("lastC", "0000");

    }
    //THESE METHODS ARE NO LONGER USED
//    public void startServiceFunction() {
//        if (!isMyServiceRunning(SendLocation.class)) {
//            try {
//                Intent serviceIntent = new Intent(this, SendLocation.class);
//                serviceIntent.putExtra("inputExtra", "Locatia se transmite in background.");
//                serviceIntent.putExtra("nrtel", dates[2]);
//                SendLocation.myPhoneNumber = dates[2];
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                    ContextCompat.startForegroundService(this, serviceIntent);
//                } else
//                    startService(serviceIntent);
//
//                //ContextCompat.startForegroundService(this, serviceIntent);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public void stopServiceFunction() {
//        if (isMyServiceRunning(SendLocation.class)) {
//
//            Intent serviceIntent = new Intent(this, SendLocation.class);
//            stopService(serviceIntent);
//
//        }
//    }

    @SuppressLint("MissingPermission")
    private double[] getGPS() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);

        /* Loop over the array backwards, and if you get an accurate location, then break                 out the loop*/
        Location l = null;

        for (int i = providers.size() - 1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }

        double[] gps = new double[2];
        if (l != null) {
            gps[0] = l.getLatitude();
            gps[1] = l.getLongitude();
        }
        return gps;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                //Toast.makeText(context, "SERVICE ON", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        //Toast.makeText(context, "SERVICE OFF", Toast.LENGTH_SHORT).show();
        return false;
    }

    public boolean isNetworkAvailable() {
        boolean swither = false;
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = null;
            if (connectivityManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    networkInfo = connectivityManager.getActiveNetworkInfo();

            }
            swither = networkInfo.isConnected();

        } catch (Exception e) {

        }
        return swither;
    }


    public void changeState(int _state) {
        if (!butonAPASAT) {
            butonAPASAT = true;
            if (isNetworkAvailable()) {
                MainActivity.this.state = _state;
                state_String = "DEZACTIVAT";
                switch (_state) {
                    case 0:
                        color = Color.GRAY;
                        state_String = "DEZACTIVAT";
                        break;
                    case 1:
                        color = Color.GREEN;
                        state_String = "LIBER";
                        break;
                    case 2:
                        color = Color.RED;
                        state_String = "OCUPAT";
                        break;
                }


                try {
                    myRef = db.getReference("soferi");
                    myRef.child(dates[2]).child("status").setValue(MainActivity.this.state).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {


                            status.setBackgroundColor(color);
                            status.setText(state_String);

                            if (MainActivity.this.state > 0)
                                locationBackgroundService.requestLocationUpdates();
                            else locationBackgroundService.removeLocationUpdates();

                            STATUS = MainActivity.this.state;
                            saveData();
                            butonAPASAT = false;
                            Toast.makeText(context, "Succes!", Toast.LENGTH_SHORT).show();

                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    butonAPASAT = false;
                                    Toast.makeText(getApplicationContext(), "Poor internet connection, please try again!", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnCanceledListener(new OnCanceledListener() {
                                @Override
                                public void onCanceled() {
                                    butonAPASAT = false;
                                    Toast.makeText(getApplicationContext(), "Poor internet connection, please try again!", Toast.LENGTH_SHORT).show();

                                }
                            });

                } catch (Exception e) {
                    butonAPASAT = false;
                    errorState(e);
                }
            } else {
                if (threadCounter < 1) {
                    Toast.makeText(getApplicationContext(), "Eroare ,incearca din nou in 2 secunde...", Toast.LENGTH_SHORT).show();

                    threadCounter++;
                    final Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                                toastCounter = 0;
                                threadCounter = 0;
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            butonAPASAT = false;
                        }
                    });
                    thread.start();
                }

            }
        } else {
            if (toastCounter < 1) {
                toastCounter++;
                Toast.makeText(getApplicationContext(), "Te rog asteapta...", Toast.LENGTH_SHORT).show();
            }
        }


    }

    public void errorState(Exception e) {

        status.setBackgroundColor(Color.GRAY);
        status.setText("ERROR");
        STATUS = MainActivity.this.state;
    }

    public void continueActivity() {

        context = this;

        liber = findViewById(R.id.b_liber);
        ocupat = findViewById(R.id.b_ocupat);
        setari = findViewById(R.id.b_setari);
        status = findViewById(R.id.t_status);
        locatie = findViewById(R.id.b_locatie);
        dezactivare = findViewById(R.id.b_dezactivare);

        textvvv = findViewById(R.id.lastSend);

        status.setBackgroundColor(Color.GRAY);

        //ecran mereu aprins
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);

        fileHelper = new FileHelper();

        configureButton();


        //acest receiver primeste semnal de la clasa SendLocation cu ultima data HH:MM:SS cand locaatia a fost updatata
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s1 = intent.getStringExtra("lastTimeLocationHasBeenSent");
                textvvv.setText(s1);
            }
        };

        String text = fileHelper.readFromFile(this);
        dates = text.split("\n");
        //verificare daca userul e logat (daca exista date in fisier )
        if (text.length() > 3) {
            dates[1] = dates[1].replaceAll("\\s+", "");
            dates[2] = dates[2].replaceAll("\\s+", "");
            TELEFON = dates[2];
            inregistrat = true;
            setTitle(dates[1] + "\n" + dates[2]);
            // Toast.makeText(this, "Bine ai venit " + dates[1] + "! ", Toast.LENGTH_SHORT).show();
        }
        if (!inregistrat) {
            Intent myIntent = new Intent(MainActivity.this, RegisterActivity.class);
            MainActivity.this.startActivityForResult(myIntent, 420);

        }
        /*******Incarcare date , si setare text in functie de acele date*******/
        loadData();
        textvvv.setText(textData);
        try {
            switch (STATUS) {
                case 0:
                    status.setBackgroundColor(Color.GRAY);
                    status.setText("DEZACTIVAT");

                    myRef = db.getReference("soferi");
                    myRef.child(dates[2]).child("status").setValue(0);
                    break;
                case 1:
                    status.setBackgroundColor(Color.GREEN);
                    status.setText("LIBER");

                    myRef = db.getReference("soferi");
                    myRef.child(dates[2]).child("status").setValue(1);
                    break;
                case 2:
                    status.setBackgroundColor(Color.RED);
                    status.setText("OCUPAT");

                    myRef = db.getReference("soferi");
                    myRef.child(dates[2]).child("status").setValue(2);
                    break;
                case 3:
                    status.setBackgroundColor(Color.parseColor("#4A148C"));
                    status.setText("DUBLU OCUPAT");

                    myRef = db.getReference("soferi");
                    myRef.child(dates[2]).child("status").setValue(3);
                    break;
                default:
                    break;

            }
        } catch (Exception ignored) {
        }
        /**********************************************************************/


        dezactivare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeState(0);
            }
        });

        liber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeState(1);
            }
        });

        ocupat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeState(2);
            }

        });

        setari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                i.putExtra("myPhoneNumber", dates[2]);
                startActivity(i);

            }
        });

        bindService(new Intent(MainActivity.this,
                        LocationBackgroundService.class),
                mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(CommonClass.KEY_REQUESTING_LOCATION_UPDATES)) {


        }
    }

}
