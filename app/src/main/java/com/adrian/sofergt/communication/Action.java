package com.adrian.sofergt.communication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.adrian.sofergt.FileHelper;
import com.adrian.sofergt.R;
import com.adrian.sofergt.objects.sms;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Action extends AppCompatActivity {
    static String myId;
    Button b1, b2, b3, b4, b5, bVezimapa, bAnuleaza;
    TextView contentView;
    String id, smsid, myName;
    double x, y;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        contentView = findViewById(R.id.content);
        b1 = findViewById(R.id.b1);
        b2 = findViewById(R.id.b2);
        b3 = findViewById(R.id.b3);
        b4 = findViewById(R.id.b4);
        b5 = findViewById(R.id.b5);
        bVezimapa = findViewById(R.id.veziMapa);
        bAnuleaza = findViewById(R.id.b_anulare);


        Bundle extras = getIntent().getExtras();


        FileHelper f = new FileHelper();

        myName = f.readFromFile(this);
        String[] strings = myName.split(" ");
        try {
            myName = strings[4];
        } catch (Exception e) {
            Toast.makeText(this, "Register first", Toast.LENGTH_SHORT).show();
        }
        if (extras != null) {
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
        b4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accept(x, y, 20);
            }
        });
        b5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                accept(x, y, 25);
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

                databaseReference.child("mesaj").child(smsid).setValue(null);
                databaseReference.child("mesaj").push().setValue(new sms(id, myId, 0, myName));
                //refuza
            }
        });


    }

    public void accept(double x, double y, int time) {


        databaseReference.child("mesaj").push().setValue(new sms(id, myId, time, myName));
        databaseReference.child("mesaj").child(smsid).setValue(null);
//        Intent i=new Intent(getApplicationContext(),Acceptat.class);
//        i.putExtra("x",x);
//        i.putExtra("y",y);
//        i.putExtra("time",time);
        //  startActivity(i);
        Toast.makeText(this, "Ai acceptat comanda!", Toast.LENGTH_SHORT).show();
        finish();
    }


}
