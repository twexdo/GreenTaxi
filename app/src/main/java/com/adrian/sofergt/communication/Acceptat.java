package com.adrian.sofergt.communication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.adrian.sofergt.AnulareComanda;
import com.adrian.sofergt.ComenziActivity;
import com.adrian.sofergt.CounterClass;
import com.adrian.sofergt.R;
import com.adrian.sofergt.Variables;
import com.adrian.sofergt.objects.sms;
import com.adrian.sofergt.ui.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class Acceptat extends AppCompatActivity {
    double x, y;
    int timp;
    Button arrived, m1, m2, m3, cancel, finish, vezipeMapa;
    String smsid, from;
    String comandaId;
    DatabaseReference databaseReference;
    AlertDialog dialog;
    int getMyPhoneNumberIncercari = 0;
    private String m1_text = "", m2_text = "", m3_text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceptat);
        CounterClass.Add();
        Bundle extras = getIntent().getExtras();


        getMyPhoneNumber();


        databaseReference = FirebaseDatabase.getInstance().getReference();
        if (extras != null) {
            try {
                from = extras.getString("hisId");

            } catch (Exception e) {
                finish();
            }
            try {

                smsid = extras.getString("smsid");
                Log.e("LASTDEB", smsid);
            } catch (Exception e) {
                finish();
            }
            try {
                setTitle(extras.getString("title"));
            } catch (Exception e) {
                finish();
            }
            try {
                comandaId = (extras.getString("comandaId"));
            } catch (Exception e) {
                finish();
            }

        }
        init();
        getDatas();
    }

    public void sendMesage(String message) {
        databaseReference.child("mesaj").child(smsid).setValue(null);

        databaseReference.child("mesaj").push().setValue(new sms(getMyPhoneNumber(), from, message));
        Log.e("AcceptatDebug", "done");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CounterClass.Remove();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent ix = new Intent(getApplicationContext(), ComenziActivity.class);
        startActivity(ix);
        finish();
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

    private void registerMe() {
        Intent register = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(register);
    }

    public void saveDatas() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("m1", m1_text);
        editor.putString("m2", m2_text);
        editor.putString("m3", m3_text);
        editor.commit();
    }

    public void getDatas() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        m1_text = sharedPref.getString("m1", "Apasa lung pentru a edita");
        m1.setText(m1_text);
        m2_text = sharedPref.getString("m2", "Apasa lung pentru a edita");
        m2.setText(m2_text);
        m3_text = sharedPref.getString("m3", "Apasa lung pentru a edita");
        m3.setText(m3_text);
    }

    public void init() {
        initButtons();
        getDatas();

        setMotives(m1);
        setMotives(m2);
        setMotives(m3);

        arrived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(Acceptat.this, "Ai trimis mesaj clientului...", Toast.LENGTH_SHORT).show();
                sendMesage("Am ajuns!");
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Felicitari pentru aceasta cursa!", Toast.LENGTH_SHORT).show();
                databaseReference.child("mesaj").child(smsid).setValue(null);
                databaseReference.child("soferi").child(getMyPhoneNumber()).child("comenzi").child(comandaId).setValue(null);
                finish();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ac = new Intent(getApplicationContext(), AnulareComanda.class);
                ac.putExtra("clientPhone", from);
                ac.putExtra("comandaId", comandaId);

                startActivity(ac);
                finish();
            }
        });

        m1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Acceptat.this);
                builder.setTitle("Title");

                final EditText input = new EditText(Acceptat.this);
                input.setHint("Your Text Here...");
                input.setBackgroundColor(Color.YELLOW);

                builder.setView(input);


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m1_text = input.getText().toString();
                        m1.setText(m1_text);
                        saveDatas();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                dialog = builder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                    }
                });
                dialog.show();


                return true;
            }
        });

        m2.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Acceptat.this);
                builder.setTitle("Title");

                final EditText input = new EditText(Acceptat.this);
                input.setHint("Your Text Here...");
                input.setBackgroundColor(Color.YELLOW);

                builder.setView(input);


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m2_text = input.getText().toString();
                        m2.setText(m2_text);
                        saveDatas();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                dialog = builder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                    }
                });
                dialog.show();


                return true;
            }
        });

        m3.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Acceptat.this);
                builder.setTitle("Title");

                final EditText input = new EditText(Acceptat.this);
                input.setHint("Your Text Here...");
                input.setBackgroundColor(Color.YELLOW);

                builder.setView(input);


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m3_text = input.getText().toString();
                        m3.setText(m3_text);
                        saveDatas();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                dialog = builder.create();

                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                    }
                });
                dialog.show();


                return true;
            }
        });


    }

    public void setMotives(final Button b) {
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMesage(b.getText().toString());
            }
        });
        Toast.makeText(getApplicationContext(), "Ai trimis mesaj personalizat...", Toast.LENGTH_SHORT).show();
    }


    private void initButtons() {
        m1 = findViewById(R.id.acceptat_motiv1);
        m2 = findViewById(R.id.acceptat_motiv2);
        m3 = findViewById(R.id.acceptat_motiv3);

        arrived = findViewById(R.id.arrived);
        finish = findViewById(R.id.acceptat_comanda_incheiata);

        cancel = findViewById(R.id.acceptat_anuleaza);

        vezipeMapa = findViewById(R.id.acceptat_vezi_pe_mapa);
    }


}
