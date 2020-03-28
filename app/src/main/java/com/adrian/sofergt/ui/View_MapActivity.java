package com.adrian.sofergt.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.adrian.sofergt.CounterClass;
import com.adrian.sofergt.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class View_MapActivity extends AppCompatActivity
        implements OnMapReadyCallback {
    GoogleMap googleMap2;
    HashMap<String, Marker> lista;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view__map);
        CounterClass.Add();
        lista = new HashMap<>();

        setTitle("Live Map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);
        myRef = db.getReference("soferi");

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    try {

                        String nrtel = String.valueOf(snap.child("nrtel").getValue(String.class));
                        String x = String.valueOf(snap.child("x").getValue(Double.class));
                        String y = String.valueOf(snap.child("y").getValue(Double.class));
                        String name = String.valueOf(snap.child("nume").getValue(String.class));

                        double a = Double.parseDouble(x);
                        double b = Double.parseDouble(y);

                        LatLng location2 = new LatLng(a, b);


                        float color;
                        double aux = Double.parseDouble(String.valueOf(snap.child("status").getValue(Double.class)));
                        int stat = (int) aux;
                        String statStr;
                        switch (stat) {
                            default:
                                color = BitmapDescriptorFactory.HUE_AZURE;
                                statStr = "ERROR";

                                break;
                            case 1:
                                color = BitmapDescriptorFactory.HUE_GREEN;
                                statStr = "LIBER";
                                break;
                            case 2:
                                color = BitmapDescriptorFactory.HUE_RED;
                                statStr = "OCUPAT";
                                break;
                            case 3:
                                color = BitmapDescriptorFactory.HUE_VIOLET;
                                statStr = "DUBLU OCUPAT";
                                break;
                        }

                        if (!lista.containsKey(nrtel)) {

                            Marker m = googleMap2.addMarker(new MarkerOptions().position(location2).snippet(nrtel)
                                    .icon(BitmapDescriptorFactory.defaultMarker(color))
                                    .title(name + " (" + statStr + ")"));
                            if (stat < 1) m.setVisible(false);
                            lista.put(nrtel, m);
                        } else {


                            Objects.requireNonNull(lista.get(nrtel)).setPosition(location2);
                            Objects.requireNonNull(lista.get(nrtel)).setTitle(name + " (" + statStr + ")");
                            Objects.requireNonNull(lista.get(nrtel)).setIcon(BitmapDescriptorFactory.defaultMarker(color));
                            if (stat < 1)
                                Objects.requireNonNull(lista.get(nrtel)).setVisible(false);
                            else Objects.requireNonNull(lista.get(nrtel)).setVisible(true);
                        }

                    } catch (NumberFormatException ignored) {
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        myRef.addValueEventListener(postListener);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap2 = googleMap;
        try {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(getGPS(), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(getGPS())      // Sets the center of the map to location user
                    .zoom(15)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } catch (Exception e) {
            Log.e("MAPERROR", e.toString());
            Toast.makeText(this, "MAPERROR" + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private LatLng getGPS() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = Objects.requireNonNull(lm).getProviders(true);

        Location l = null;

        for (int i = providers.size() - 1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) break;
        }

        return new LatLng(Objects.requireNonNull(l).getLatitude(), l.getLongitude());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CounterClass.Remove();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
