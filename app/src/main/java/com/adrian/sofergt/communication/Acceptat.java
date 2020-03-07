package com.adrian.sofergt.communication;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.adrian.sofergt.R;
import com.adrian.sofergt.objects.sms;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Acceptat extends AppCompatActivity {
    public static String myid;
    double x, y;
    int timp;
    String smsid, from;
    Button arrived;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceptat);

        Bundle extras = getIntent().getExtras();

        arrived = findViewById(R.id.arrived);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        if (extras != null) {
            from = extras.getString("hisId");
            myid = extras.getString("myId");
            smsid = extras.getString("smsid");
            setTitle("Comanda Acceptata");

        }
        arrived.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                databaseReference.child("mesaj").child(smsid).setValue(null);

                databaseReference.child("mesaj").push().setValue(new sms(myid, from, "Am ajuns!"));
            }
        });


    }


}
