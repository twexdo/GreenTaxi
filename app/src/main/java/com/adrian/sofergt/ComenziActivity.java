package com.adrian.sofergt;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.adrian.sofergt.communication.Acceptat;
import com.adrian.sofergt.ui.RegisterActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ComenziActivity extends AppCompatActivity {
    private static final String CHANNEL_ID ="ComenziActivity:channel" ;
    ComenziAdapter adapter;
    ArrayList<Comanda> lista;
    String myPhoneNumber="";
    ListView comenziView;
    DatabaseReference dbref;
    NotificationCompat.Builder mBuilder;
    NotificationManager mNotificationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        CounterClass.Add();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comenzi);
        comenziView=findViewById(R.id.comenzi_layout);

        lista = new ArrayList<>();
        adapter = new ComenziAdapter(getApplicationContext(), lista);
        comenziView.setAdapter(adapter);


        final Handler someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setTitle("Ora actuala: "+new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(new Date()));
                someHandler.postDelayed(this, 10000);
            }
        }, 10);
        createNotificationChannel();
        mBuilder= new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launch)
                .setContentTitle("Comenzi")
                .setContentText("Click pentru a intra in istoric")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mNotificationManager = (NotificationManager)
                getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        comenziView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String telefon = adapter.getItem(i).getNr_Tel_Client();
                String title=adapter.getItem(i).getTitlu();
                String smsID=adapter.getItem(i).getSmsID();
                Intent intent = new Intent(getApplicationContext(), ComenziActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
               mBuilder.setContentIntent(pendingIntent);
                mNotificationManager.notify(900, mBuilder.build());
                Log.e("LASTDEB",smsID);
                Intent gotoAcceptat=new Intent(getApplicationContext(), Acceptat.class);
                gotoAcceptat.putExtra("title",title);
                gotoAcceptat.putExtra("hisId",telefon);
                gotoAcceptat.putExtra("smsid",smsID);
                gotoAcceptat.putExtra("comandaId",adapter.getItem(i).getComandaId_inFDB());
                startActivity(gotoAcceptat);
                finish();
            }
        });



        dbref= FirebaseDatabase.getInstance().getReference("soferi/"+getMyPhoneNumber()+"/comenzi");
        Toast.makeText(this, "Path is:"+"soferi/"+getMyPhoneNumber()+"/comenzi", Toast.LENGTH_LONG).show();
        dbref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                      callAdapter(dataSnapshot);
                Toast.makeText(getApplicationContext(), "onChildAdded", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                adapter.clear();
                Toast.makeText(getApplicationContext(), "onChildChanged", Toast.LENGTH_LONG).show();
                adapter.notifyDataSetChanged();
                callAdapter(dataSnapshot);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                adapter.clear();
                Toast.makeText(getApplicationContext(), "onChildRemoved", Toast.LENGTH_LONG).show();
                adapter.notifyDataSetChanged();
                callAdapter(dataSnapshot);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                adapter.clear();
                Toast.makeText(getApplicationContext(), "onChildMoved", Toast.LENGTH_LONG).show();
                adapter.notifyDataSetChanged();
                callAdapter(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ComenziActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNotificationChannel(){
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "TestChannel";
            String description ="channelDescription";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void callAdapter(DataSnapshot snap) {
       try {
           Log.e("ComandaDebug", "called");
           String tel = snap.child("nr_Tel_Client").getValue(String.class);
           int timp=0;
           try {
                timp = snap.child("timp_Stabilit").getValue(Integer.class);
           }catch (Exception e){

           }
           String orastabilita = snap.child("ora_la_care_a_fost_stabilita_comanda").getValue(String.class);
           String smsID=snap.child("smsID").getValue(String.class);
            String comandaID=snap.getKey();
            Log.e("comandaId",comandaID);
           Comanda newComanda = new Comanda(tel, timp, orastabilita, lista.size(), comandaID,smsID);
           try {
               FirebaseDatabase.getInstance().getReference("mesaj").child(smsID).setValue(null);
           }catch (Exception e){

           }
           adapter.add(newComanda);
       }catch (Exception e){
           Log.e("ComandaDebug", e.toString());
       }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CounterClass.Remove();
    }

    int getMyPhoneNumberIncercari = 0;
    private String getMyPhoneNumber() {
        Log.e("AcceptatDebug","am apelat getMyPhone number : "+getMyPhoneNumberIncercari);
        //verific mai intai daca am luat deja numarul de telefon
        if (myPhoneNumber.length() < 5) {
            //incerc sa iau nr de telefon
            try {
                //daca reusesc pot merge mai departe

                myPhoneNumber= FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
                Log.e("AcceptatDebug","am gasit numarul: "+myPhoneNumber);
                getMyPhoneNumberIncercari = 0;
                return myPhoneNumber;

            } catch (Exception e) {
                Log.e("AcceptatDebug","Am gasit o eroare: "+e.getMessage());
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
