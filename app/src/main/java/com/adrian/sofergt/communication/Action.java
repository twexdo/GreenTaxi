package com.adrian.sofergt.communication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.adrian.sofergt.CounterClass;
import com.adrian.sofergt.R;
import com.adrian.sofergt.Variables;
import com.adrian.sofergt.objects.sms;
import com.adrian.sofergt.ui.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
public class Action extends AppCompatActivity {
    private static final String TAG = "AcceptatDebug";
    static String myId;
    Button b1, b2, b3, bVezimapa, bAnuleaza, bpersonalizat;
    TextView contentView;
    EditText action_time;
    String id = "", smsid = "";
    double x, y;
    DatabaseReference databaseReference;
    int getMyPhoneNumberIncercari = 0;
    int getMyNameIncercari = 0;
    DatabaseReference getnameref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);

        CounterClass.Add();

        databaseReference = FirebaseDatabase.getInstance().getReference();
        contentView = findViewById(R.id.content);
        b1 = findViewById(R.id.b1);
        b2 = findViewById(R.id.b2);
        b3 = findViewById(R.id.b3);
        bVezimapa = findViewById(R.id.veziMapa);
        bAnuleaza = findViewById(R.id.b_anulare);
        action_time = findViewById(R.id.action_minute);
        bpersonalizat = findViewById(R.id.action_button);

        Bundle extras = getIntent().getExtras();


        if (extras != null) {
            Toast.makeText(this, "extras != null", Toast.LENGTH_SHORT).show();
            smsid = extras.getString("smsid");

            id = extras.getString("from");
            myId = extras.getString("myId");
            String content = extras.getString("content");

            setTitle(id);

            contentView.setText(content);


            x = extras.getDouble("x", 0.0);
            y = extras.getDouble("y", 0.0);

        }

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accept(x, y, 5);
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accept(x, y, 10);
            }
        });
        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accept(x, y, 15);
            }
        });
        bpersonalizat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int time = Integer.parseInt(action_time.getText().toString());
                    accept(x, y, time);
                } catch (Exception e) {
                    Toast.makeText(Action.this, "Ai introdus un numar gresit...", Toast.LENGTH_SHORT).show();
                }
            }
        });


        bVezimapa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + x + "," + y));
                startActivity(intent);
            }
        });
        bAnuleaza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                anuleaza();

            }
        });


    }

    public void anuleaza() {
        if (getMyName().length() > 3) {
            databaseReference.child("mesaj").child(smsid).setValue(null);
            databaseReference.child("mesaj").push().setValue(new sms(id, myId, 0, getMyName()));
        } else {
            Toast.makeText(this, "Please Wait...", Toast.LENGTH_SHORT).show();
            SystemClock.sleep(1000);
            anuleaza();
        }
        //refuza
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CounterClass.Remove();
    }

    public void accept(double x, double y, int time) {
        if (getMyName().length() >= 3) {

            databaseReference.child("mesaj").push().setValue(new sms(id, myId, time, getMyName()));
            databaseReference.child("mesaj").child(smsid).setValue(null);

            Toast.makeText(this, "Ai acceptat comanda!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Log.e(TAG, "please wait");
            Toast.makeText(this, "Te rog asteapta 2 secunde pentru a te sincroniza cu serverul ,apoi apasa din nou...", Toast.LENGTH_LONG).show();

        }
    }

    private String getMyPhoneNumber() {
        Log.e("AcceptatDebug", "am apelat getMyPhone number : " + getMyPhoneNumberIncercari);
        //verific mai intai daca am luat deja numarul de telefon
        if (Variables.myPhoneNumber.length() < 5) {
            //incerc sa iau nr de telefon
            try {
                //daca reusesc pot merge mai departe

                Variables.myPhoneNumber = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                Log.e("AcceptatDebug", "am gasit numarul: " + Variables.myPhoneNumber);
                getMyPhoneNumberIncercari = 0;
                return Variables.myPhoneNumber;

            } catch (Exception e) {
                Log.e("AcceptatDebug", "Am gasit o eroare: " + e.getMessage());
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
            return Variables.myPhoneNumber;
        }
    }

    private String getMyName() {

        if (Variables.myName.length() < 3) {
            Log.e("AcceptatDebug", "incerc sa iau nume:" + getMyNameIncercari);

            getnameref = FirebaseDatabase.getInstance().getReference();
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Variables.myName = dataSnapshot.child("soferi").child(getMyPhoneNumber()).child("nume").getValue(String.class);
                    getMyNameIncercari = 0;
                    getnameref.removeEventListener(this);
                    Log.e("AcceptatDebug", "data change " + Variables.myName);
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("AcceptatDebug", "database eror" + databaseError.getMessage());
                }
            };

            getnameref.addValueEventListener(valueEventListener);
            getnameref.child("triggerValue").setValue(String.valueOf(Math.random()));


            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Variables.myName.length() < 3) {
                        getMyName();
                    }

                }
            }, 1000);
        }
        return Variables.myName;
    }

    private void registerMe() {
        Intent register = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(register);
    }
}
