package com.adrian.sofergt;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static Context context;
    public static final String SHARE_PREFS = "sharedPrefs";
    public static final String TEXT = "text";
    static int STATUS = 0;
    static String TELEFON;
    static TextView textvvv;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference myRef;
    String textData;
    BroadcastReceiver broadcastReceiver;
    boolean inregistrat = false;
    Button liber, ocupat, setari, locatie, dezactivare, force;
    TextView status;
    String[] dates;
    boolean started = false;

    public static String getnrtel() {
        try {
            if (TELEFON.length() < 5) {
                FileHelper h = new FileHelper();
                String text = h.readFromFile(context);
                String[] separable = h.readFromFile(context).split("\n");

                String thisNrTel = separable[2].replaceAll("\\s+", "");
                return thisNrTel;

            } else
                return TELEFON;
        } catch (Exception e) {
            return e.toString();
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        liber = findViewById(R.id.b_liber);
        ocupat = findViewById(R.id.b_ocupat);
        setari = findViewById(R.id.b_setari);
        status = findViewById(R.id.t_status);
        locatie = findViewById(R.id.b_locatie);
        dezactivare = findViewById(R.id.b_dezactivare);
        force = findViewById(R.id.b_force);


        status.setBackgroundColor(Color.GRAY);
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {


                requestPermissions(new String[]{
                        Manifest.permission.FOREGROUND_SERVICE,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                }, 101);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{
                        Manifest.permission.INTERNET,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WAKE_LOCK,
                        Manifest.permission.ACCESS_NETWORK_STATE

                }, 10);


                return;
            } else {
                configureButton();

            }
        }
        force.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService();
                startService();
                StringBuilder sb = new StringBuilder();
                sb.append("\n\n De obicei se transmite in background, dar daca nu iti merge , tre sa ti ecranu aprins mereu!");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        sb.append("\n\n Locatia era dezactivata!");
                        requestPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,

                        }, 10);
                    }
                    if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        sb.append("\n\n Locatia nu era la acuratete maxima!");
                        requestPermissions(new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,

                        }, 10);
                    }
                }
                final ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                final NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (wifi.isConnected()) {
                    sb.append("\n\n Dezactiveaza wifi,s-ar putea sa incerce sa transmita prin wifi chiar daca nu esti conectat!");

                }
                if (!mobile.isConnected()) {
                    sb.append("\n\n Activeaza datele mobile, si verifica daca ai semnal bun!");
                }

                if (!isMyServiceRunning(SendLocation.class)) {

                    sb.append("\n\n Seteazate pe dezactivat apoi pe liber sau ocupat!");
                }


                myRef = db.getReference("soferi");

                myRef.child(dates[2]).child("x").setValue(getGPS()[0]);
                myRef.child(dates[2]).child("y").setValue(getGPS()[1]);

                sb.append("\n\n Apasa ORIGINAL MAP pentru a vedea locatia actuala(pct albastru) si locatia trimisa de aplicate(Marker rosu).\n\n In caz ca e distanta mare intre cele doua fa screenshot si trimite-mi!");

                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("Sfaturi:");
                alertDialog.setMessage(sb.toString());
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "ORIGINAL MAP",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Create a Uri from an intent string. Use the result to create an Intent.
                                Uri gmmIntentUri = Uri.parse("https://maps.google.com/?q=<" + getGPS()[0] + ">,<" + getGPS()[1] + ">");


                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);


                                startActivity(mapIntent);

                                dialog.dismiss();
                            }
                        });

                alertDialog.show();
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GREEN);
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN);


            }
        });


        textvvv = findViewById(R.id.lastSend);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String s1 = intent.getStringExtra("DATAPASSED");
                textvvv.setText(s1);
            }
        };


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
        if (!inregistrat) {
            Intent myIntent = new Intent(MainActivity.this, RegisterActivity.class);
            //myIntent.putExtra("key", value); //Optional parameters
            MainActivity.this.startActivityForResult(myIntent, 420);

        }


        textData = "00";
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

        dezactivare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status.setBackgroundColor(Color.GRAY);
                status.setText("DEZACTIVAT");

                myRef = db.getReference("soferi");
                myRef.child(dates[2]).child("status").setValue(0);
                stopService();
                STATUS = 0;
                saveData();

            }
        });

        liber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(MainActivity.this, "LIBER", Toast.LENGTH_SHORT).show();
                try {
                    status.setBackgroundColor(Color.GREEN);
                    status.setText("LIBER");
                    myRef = db.getReference("soferi");
                    myRef.child(dates[2]).child("status").setValue(1);
                    stopService();
                    startService();
                    STATUS = 1;
                    saveData();
                } catch (Exception e) {
                    Log.e("liber.", e.toString());
                }
            }
        });

        ocupat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status.setBackgroundColor(Color.RED);
                status.setText("OCUPAT");

                myRef = db.getReference("soferi");
                myRef.child(dates[2]).child("status").setValue(2);
                stopService();
                startService();
                STATUS = 2;
                saveData();

            }
        });

        setari.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.andy.myapplication");
        registerReceiver(broadcastReceiver, intentFilter);
    }


    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(broadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    private void configureButton() {

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

    public void startService() {
        saveData();
        if (!started) {

            started = true;
            Intent serviceIntent = new Intent(getApplicationContext(), SendLocation.class);
            serviceIntent.putExtra("inputExtra", "Locatia se transmite in background.");
            serviceIntent.putExtra("nrtel", dates[2]);
            SendLocation.myPhoneNumber = dates[2];
            //startService(serviceIntent);
            ContextCompat.startForegroundService(getApplicationContext(), serviceIntent);

        }
    }

    public void stopService() {
        if (started) {
            Intent serviceIntent = new Intent(getApplicationContext(), SendLocation.class);
            stopService(serviceIntent);
            started = false;
        }
    }

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
                return true;
            }
        }
        return false;
    }
}

