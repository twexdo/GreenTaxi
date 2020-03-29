package com.adrian.sofergt;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.adrian.sofergt.objects.sms;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class AnulareComanda extends AppCompatActivity {
    String clientPhone, comandaId;
    Button anuleaza, renunta, blocheaza;
    TextView nrtel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anulare_comanda);

        getBundle();//if data not exist activity will finish();
        initViews();

        nrtel.setText("Client Phone: " + clientPhone);
        initButtons();

    }

    private void initButtons() {

        anuleaza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    FirebaseDatabase.getInstance().getReference("soferi").child(Variables.myPhoneNumber).child("comenzi").child(comandaId).setValue(null);
                    Toast.makeText(getApplicationContext(), "Comanda anulata!", Toast.LENGTH_SHORT).show();
                    sendMesage("Soferul a anulat comanda");
                    finish();
                } catch (Exception e) { }
            }
        });
        renunta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        blocheaza.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    FirebaseDatabase.getInstance().getReference("useriBlocati").push().setValue(clientPhone);
                }catch (Exception e){}
            }
        });

    }
    public void sendMesage(String message){

        FirebaseDatabase.getInstance().getReference().child("mesaj").push().setValue(new sms(getMyPhoneNumber(), clientPhone, message));
        Log.e("AcceptatDebug","done");
    }
    private void initViews() {
        anuleaza = findViewById(R.id.anulare_b_anulare);
        renunta = findViewById(R.id.anulare_b_renunta);
        blocheaza = findViewById(R.id.anulare_b_blocheaza);
        nrtel = findViewById(R.id.anulare_nrtel);

    }

    public void getBundle() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            clientPhone = extras.getString("clientPhone", "");
            comandaId = extras.getString("comandaId", "");
        } else {
            finish();
        }
    }
    int getMyPhoneNumberIncercari = 0;

    private String getMyPhoneNumber() {
        Log.e("AcceptatDebug","am apelat getMyPhone number : "+getMyPhoneNumberIncercari);
        //verific mai intai daca am luat deja numarul de telefon
        if (Variables.myPhoneNumber.length() < 5) {
            //incerc sa iau nr de telefon
            try {
                //daca reusesc pot merge mai departe

                Variables.myPhoneNumber= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                Log.e("AcceptatDebug","am gasit numarul: "+Variables.myPhoneNumber);
                getMyPhoneNumberIncercari = 0;
                return Variables.myPhoneNumber;

            } catch (Exception e) {
                Log.e("AcceptatDebug","Am gasit o eroare: "+e.getMessage());
                if (getMyPhoneNumberIncercari < 3) {
                    getMyPhoneNumberIncercari++;
                    return getMyPhoneNumber();
                } else {
                    getMyPhoneNumberIncercari = 0;
                    return "";

                }
            }
        } else {
            return Variables.myPhoneNumber;
        }
    }
}
