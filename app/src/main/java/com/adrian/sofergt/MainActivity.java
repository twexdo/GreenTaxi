package com.adrian.sofergt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.adrian.sofergt.services.MainService;
import com.adrian.sofergt.ui.RegisterActivity;
import com.adrian.sofergt.ui.SettingsActivity;
import com.adrian.sofergt.ui.View_MapActivity;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
/*
 * Todo: adauga verificarea cu FirebaseAuth in loc de verificarea cu fisier
 *
 *
 *
 * */

public class MainActivity extends AppCompatActivity {
    private final String SHARE_PREFS = "sharedPrefs";
    private final String TAG = "MainActivityDebug";
    private final String statusText_String = "text";
    MainService mainService = null;
    TextView textV_ultimaDataCandAmTrimisDate, status;
    int state, toastCounter = 0, threadCounter = 0, color;
    Button liber, ocupat, setari, locatie, dezactivare, comenzi;

    static int STATUS = 0;
    int getMyPhoneNumberIncercari = 0;
    int getMyNameIncercari = 0;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    BroadcastReceiver broadcastReceiver;
    DatabaseReference myRef;
    boolean butonAPASAT = false;
    DatabaseReference getnameref;
    private Context context;
    private FirebaseAuth mAuth;
    private boolean mBound = false;
    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            MainService.LocalBinder binder = (MainService.LocalBinder) iBinder;
            mainService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mainService = null;
            mBound = false;

        }
    };


    @Override
    protected void onStop() {
        super.onStop();

        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String tkn = "", myPhoneNumber = "", myName = "", string_ultimaDataCandAmTrimisDate = "", state_String = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CounterClass.Add();
        Log.e(TAG, "ActivityCreated");
        verificaPermisiunile();//Dupa verificare se intra in PermissionsListenerClass


    }

    public void verificaPermisiunile() {
        Log.e(TAG, "Verifica permisiunile");
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
        Log.e(TAG, "OnStart");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.adrian.sofergt");
        registerReceiver(broadcastReceiver, intentFilter);
        initVariables();
        initorgetStatus();
        initToken();

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CounterClass.Remove();
        if (mBound) {
            unbindService(mServiceConnection);
            mBound = false;
        }

    }

    public void saveData() {
        try {
            Log.e(TAG, "saveData");
            final ConnectivityManager connMgr = (ConnectivityManager) getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            final android.net.NetworkInfo mobile = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            final android.net.NetworkInfo wifi = connMgr
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mobile.isConnected() || wifi.isConnected()) {
                SharedPreferences sharedPreferences = getSharedPreferences(SHARE_PREFS, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(statusText_String, STATUS);
                editor.putString("lastC", textV_ultimaDataCandAmTrimisDate.getText().toString());
                editor.apply();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Nu am putut salva statusul actual...", Toast.LENGTH_SHORT).show();
        }

    }

    private void sendRegistrationToServer(String token) {
        Log.e(TAG, "send Regtoken to the server");
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("tokens");
        try {
            dbref.child(getMyPhoneNumber()).setValue(token);
        } catch (Exception err) {
            Toast.makeText(this, err.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void loadData() {

        SharedPreferences sharedPreferences = getSharedPreferences(SHARE_PREFS, MODE_PRIVATE);
        STATUS = sharedPreferences.getInt(statusText_String, 0);
        string_ultimaDataCandAmTrimisDate = sharedPreferences.getString("lastC", "__:__:__");
        Log.e(TAG, "loaded from shared pref: " + string_ultimaDataCandAmTrimisDate);
    }

    public void schimbaStatusul(int _state) {
        Log.e(TAG, "Schimbarestatus");
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
                    myRef.child(getMyPhoneNumber()).child("status").setValue(MainActivity.this.state).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {


                            status.setBackgroundColor(color);
                            status.setText(state_String);

                            if (MainActivity.this.state > 0)
                                mainService.requestLocationUpdates();
                            else mainService.removeLocationUpdates();

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
                    scrieEroarePeEcran(e);
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

    public void scrieEroarePeEcran(Exception e) {

        status.setBackgroundColor(Color.GRAY);
        status.setText("ERROR");
        STATUS = MainActivity.this.state;
    }

    public void initVariables() {
        Log.e(TAG, "initializare variabile");
        context = getApplicationContext();
        liber = findViewById(R.id.b_liber);
        ocupat = findViewById(R.id.b_ocupat);
        setari = findViewById(R.id.b_setari);
        status = findViewById(R.id.t_status);
        status.setBackgroundColor(Color.GRAY);
        locatie = findViewById(R.id.b_locatie);
        dezactivare = findViewById(R.id.b_dezactivare);
        comenzi = findViewById(R.id.b_comenzi);
        textV_ultimaDataCandAmTrimisDate = findViewById(R.id.lastSend);

    }

    public void initLocationBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s1 = intent.getStringExtra("lastTimeLocationHasBeenSent");
                textV_ultimaDataCandAmTrimisDate.setText(s1);
            }
        };
    }

    public void initorgetStatus() {
        Log.e(TAG, "Initializare status");
        loadData();
        Log.e(TAG, "data loaded");
        textV_ultimaDataCandAmTrimisDate.setText(string_ultimaDataCandAmTrimisDate);
        Log.e(TAG, "textView Changed");
        try {
            switch (STATUS) {
                case 0:
                    status.setBackgroundColor(Color.GRAY);
                    status.setText("DEZACTIVAT");
                    Log.e(TAG, "statusText Changed");
                    myRef = db.getReference("soferi");
                    myRef.child(getMyPhoneNumber()).child("status").setValue(0);
                    Log.e(TAG, "status 0");
                    break;
                case 1:
                    status.setBackgroundColor(Color.GREEN);
                    status.setText("LIBER");

                    myRef = db.getReference("soferi");
                    myRef.child(getMyPhoneNumber()).child("status").setValue(1);
                    Log.e(TAG, "status 1");
                    break;
                case 2:
                    status.setBackgroundColor(Color.RED);
                    status.setText("OCUPAT");

                    myRef = db.getReference("soferi");
                    myRef.child(getMyPhoneNumber()).child("status").setValue(2);
                    Log.e(TAG, "status 2");
                    break;
                case 3:
                    status.setBackgroundColor(Color.parseColor("#4A148C"));
                    status.setText("DUBLU OCUPAT");

                    myRef = db.getReference("soferi");
                    myRef.child(getMyPhoneNumber()).child("status").setValue(3);
                    break;
                default:
                    Log.e(TAG, "break");
                    break;

            }
        } catch (Exception e) {
            Log.e(TAG, "eroare initorgetStatus" + e.getMessage());
        }
    }

    public void initButtons() {
        Log.e(TAG, "initializare butoane");
        comenzi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, ComenziActivity.class);
                startActivity(i);
            }
        });

        dezactivare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                schimbaStatusul(0);
            }
        });

        liber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                schimbaStatusul(1);
            }
        });

        ocupat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                schimbaStatusul(2);
            }

        });

        setari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                i.putExtra("myPhoneNumber", getMyPhoneNumber());
                startActivity(i);
            }
        });
        locatie.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(MainActivity.this, View_MapActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });

    }

    private boolean userIsLogged() {
        Log.e(TAG, "verificare daca user e logat");
        try {
            FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
            Log.e(TAG, "user logat");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "user null");
            return false;
        }
    }

    public void initToken() {
        try {
            Log.e(TAG, "initializare token");
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(MainActivity.this, new OnSuccessListener<InstanceIdResult>() {
                @Override
                public void onSuccess(InstanceIdResult instanceIdResult) {
                    tkn = instanceIdResult.getToken();
                    sendRegistrationToServer(tkn);
                }
            });

        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void permisiuniGarantate() {
        Log.e(TAG, "am iesit din verificarea permisiunilor");
        if (userIsLogged()) {
            initVariables();
            initLocationBroadcastReceiver();
            initorgetStatus();
            initButtons();
            bindService(new Intent(MainActivity.this,
                            MainService.class),
                    mServiceConnection,
                    Context.BIND_AUTO_CREATE);
            initToken();
            getMyName();

        } else {
            Log.e(TAG, "merg sa ma inregistrez");
            registerMe("permisiuniGarantate");
        }
    }

    private void registerMe(String whoCalledMe) {
        Log.e(TAG, "registerMe() :" + whoCalledMe);
        Intent register = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(register);
        finish();
    }

    private String getMyPhoneNumber() {
        Log.e(TAG, "am apelat getMyPhone number : " + getMyNameIncercari);
        //verific mai intai daca am luat deja numarul de telefon
        if (myPhoneNumber.length() < 5) {
            //incerc sa iau nr de telefon
            try {
                //daca reusesc pot merge mai departe

                myPhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                Log.e(TAG, "am gasit numarul: " + myPhoneNumber);
                getMyPhoneNumberIncercari = 0;
                return myPhoneNumber;

            } catch (Exception e) {
                Log.e(TAG, "Am gasit o eroare: " + e.getMessage());
                if (getMyPhoneNumberIncercari < 3) {
                    getMyPhoneNumberIncercari++;
                    return getMyPhoneNumber();
                } else {
                    getMyPhoneNumberIncercari = 0;
                    registerMe("getMyPhone");
                    return "";

                }
            }
        } else {
            return myPhoneNumber;
        }
    }

    private String getMyName() {

        if (myName.length() < 3) {
            Log.e(TAG, "incerc sa iau nume:" + getMyNameIncercari);

            getnameref = FirebaseDatabase.getInstance().getReference();
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    myName = dataSnapshot.child("soferi").child(getMyPhoneNumber()).child("nume").getValue(String.class);
                    getMyNameIncercari = 0;
                    getnameref.removeEventListener(this);
                    Log.e(TAG, "data change " + myName);
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e(TAG, "database eror" + databaseError.getMessage());
                }
            };

            getnameref.addValueEventListener(valueEventListener);
            getnameref.child("triggerValue").setValue(String.valueOf(Math.random()));


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (myName != null) {
                        if (myName.length() < 3) {
                            getMyName();
                        } else {
                            setTitle("GreenPel Taxi Driver: " + myName);
                        }
                    } else {
                        registerMe("getMyName");
                    }

                }
            }, 1000);
        }
        return myName;
    }

}



