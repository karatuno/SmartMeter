package com.example.smartmete;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import android.arch.lifecycle.ProcessLifecycleOwner;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

public class HomeActivity extends AppCompatActivity {

    FusedLocationProviderClient mFusedLocationClient;
    LocationRequest mLocationRequest;
    LocationCallback mLocationCallback;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    Location Loc;
    Double MtrLat=0.0;
    Double MtrLon=0.0;

    public void getNotification(String Title, String Text) {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("DropNotify", "DropNotifications", NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(),"DropNotify")
                .setContentTitle(Title)
                .setContentText(Text)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_add_location_black_24dp);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(1,builder.build());

    }

    public class AppLifecycleListener implements LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        public void onMoveToForeground() {
            // app moved to foreground
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        public void onMoveToBackground() {

            startService(new Intent(getApplicationContext(),Myservice.class));

        }
    }

    private void startLocationUpdates() {

        Log.i("logcheck","startLocationupdates");

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},99);

            return;
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null );
    }

    public void getLastLocationFun() {

        Log.i("logcheck","getLastLocationFun");

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Not permitted", Toast.LENGTH_SHORT).show();
        }
        mFusedLocationClient.getLastLocation();
    }

    public void reqP(){

        Log.i("logcheck","reqP");
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
                99);

    }

    public boolean checkP(){
        Log.i("logcheck","checkP");
        return (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void stopLocationUpdates() {
        Log.i("logcheck","stopLocationUpdates");
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    protected void createLocationRequest() {
        Log.i("logcheck","createLocationRequest");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(2000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void OKBuilder()
    {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
                getLastLocationFun();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(HomeActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        createLocationRequest();

        final TextView Cno = findViewById(R.id.Cno);
        final TextView agency = findViewById(R.id.Agency);
        final TextView MeterNo = findViewById(R.id.MtrNumber);
        final TextView MeterType = findViewById(R.id.MtrType);
        final TextView MeterModel = findViewById(R.id.MtrModel);
        final Button setML = findViewById(R.id.setML);
        final ProgressBar unit_progressbar = findViewById(R.id.unit_progressbar);
        final TextView ti = findViewById(R.id.t1);
        final TextView tf = findViewById(R.id.t2);
        final TextView tm = findViewById(R.id.tm);

        ProcessLifecycleOwner.get().getLifecycle().addObserver(new AppLifecycleListener());

        getSupportActionBar().setTitle("Your details : ");

        final String id = getIntent().getStringExtra("id");

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String Name = String.valueOf(dataSnapshot.child(id).child("Name").getValue());
                String Mtrmodel = String.valueOf(dataSnapshot.child(id).child("Mtr Model").getValue());
                String Mtrtype = String.valueOf(dataSnapshot.child(id).child("Mtr Type").getValue());
                String Mtrnumber = String.valueOf(dataSnapshot.child(id).child("New Mtr No").getValue());
                String Agency = String.valueOf(dataSnapshot.child(id).child("Agency").getValue());
                String cno = String.valueOf(dataSnapshot.child(id).child("Consumer No").getValue());
                Integer Finalunits = Integer.parseInt(String.valueOf(dataSnapshot.child(id).child("Final Reading KWH").getValue()));
                Integer Initialunits = Integer.parseInt(String.valueOf(dataSnapshot.child(id).child("Initial Reading").getValue()));
                Integer units = Finalunits-Initialunits;
                if(units > 100) {
                    unit_progressbar.setMax(500);
                    unit_progressbar.setProgress(units);
                    ti.setText("0");
                    tf.setText("500");
                    tm.setText("");
                    if(units>400) {
                        tm.setText("Warning! you are about to cross 5Rs/Unit \n Next is 7.5Rs/Unit. Start Saving!");
                        getNotification("Smartmeter Warning!!"
                                ,"Units Consumed:"+units);
                    }
                }
                else {
                    unit_progressbar.setMax(100);
                    unit_progressbar.setProgress(units);
                    ti.setText("0");
                    tf.setText("100");
                    tm.setText("");
                    if(units>80) {
                        tm.setText("Warning! you are about to cross 2.5Rs/Unit \n Next is 5Rs/Unit. Start Saving!");
                        getNotification("Smartmeter Warning!!"
                                ,"Units Consumed:"+units);
                    }
                }

                if(dataSnapshot.child(id).hasChild("MtrLocation")) {
                    setML.setVisibility(View.GONE);
                MtrLat = Double.parseDouble(String.valueOf(dataSnapshot.child(id).child("MtrLocation").child("latitude").getValue()));
                MtrLon = Double.parseDouble(String.valueOf(dataSnapshot.child(id).child("MtrLocation").child("longitude").getValue()));

                }

                else {

                    setML.setVisibility(View.VISIBLE);
                }

                Cno.setText("Name: "+Name+"\nConsumer Number: "+cno );
                agency.setText("Agency: "+Agency);
                MeterModel.setText("Predictive next month unit consumption:");
                MeterType.setText("0000 units");
                MeterNo.setText("Can be reduced to XXXX Units");

                mLocationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        if (locationResult == null) {
                            Log.i("logcheck", "onLocationResult null");
                            return;
                        }
                        for (final Location location : locationResult.getLocations()) {
                            // Toast.makeText(HomeActivity.this, "Foreground "+location, Toast.LENGTH_SHORT).show();
                             Loc = location;
                             if(MtrLat != 0.0) {
                                 Double dist = distance(MtrLat, location.getLatitude(), MtrLon, location.getLongitude());
                                 if (dist > 20) {
                                     Toast.makeText(HomeActivity.this, "Foreground "+dist, Toast.LENGTH_SHORT).show();
                                     getNotification("SmartMeter Alert!!", "Going Somewhere? Switch off the appliances to save energy.");

                                 }
                             }
                            }
                    }

                };

                setML.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(HomeActivity.this, "Pressed", Toast.LENGTH_SHORT).show();
                        databaseReference.child(id).child("MtrLocation").setValue(Loc);
                    }
                });

                if (Build.VERSION.SDK_INT < 23)
                {
                    OKBuilder();
                }
                else {
                    if (!checkP()) {
                        reqP();
                    }

                    if (checkP()) {
                        OKBuilder();
                    }

                }
                startLocationUpdates();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
