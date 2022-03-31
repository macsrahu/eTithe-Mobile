package com.prgs.etithe.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prgs.etithe.R;
import com.prgs.etithe.utilities.FirebaseTables;
import com.prgs.etithe.utilities.Global;


public class TrackingService extends Service {

    private static final String TAG = TrackingService.class.getSimpleName();
    String SALES_PERSON_KEY = "";

    @Override
    public IBinder onBind(Intent intent) {
        //SALES_PERSON_KEY = intent.getStringExtra("key");
        //Log.d("KEY",SALES_PERSON_KEY);
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildNotification();
        requestLocationUpdates();
    }

    //Create the persistent notification//


    private void buildNotification() {
        String stop = "stop";
        registerReceiver(stopReceiver, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(
                this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);
        String channelId = "channel-01";
        String channelName = getString(R.string.app_name);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelName, importance);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(mChannel);

        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_logo)
                .setContentTitle(getString(R.string.app_name))
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setContentText(getString(R.string.tracking_enabled_notif));

       /* Notification.Builder builder = null;
        builder = new Notification.Builder(this)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.tracking_enabled_notif))
                .setOngoing(true)
                .setContentIntent(broadcastIntent)
                .setSmallIcon(R.drawable.ic_launcher);*/

        //startForeground(1, mBuilder.build());


    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            unregisterReceiver(stopReceiver);
            stopSelf();
            Toast.makeText(getApplicationContext(), "Tracking Service has been stopped", Toast.LENGTH_LONG).show();
        }
    };


//Initiate the request to track the device's location//

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();

        //Specify how often your app should request the device’s location//
        //request.setInterval(10000);

        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(90000);
        request.setFastestInterval(100000);

        //Log.d("TWO",SALES_PERSON_KEY);
        //Get the most accurate location data available//
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);

        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        //If the app currently has access to the location permission...//
//        if (permission == PackageManager.PERMISSION_GRANTED) {
//
////...then request location updates//

            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_LOCATION);
                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        //Toast.makeText(getApplicationContext(),"COOL",Toast.LENGTH_LONG).show();
                        SharedPreferences sharedPref = getSharedPreferences(Global._ETITHE_REMEMBER_ME, MODE_PRIVATE);
                        boolean saveLogin = sharedPref.getBoolean("saveLogin", false);
                        if (saveLogin) {
                            String USERKEY = sharedPref.getString("loginid", "");
                            if (USERKEY != null && !USERKEY.isEmpty()) {
                                ref.child(USERKEY).setValue(location);
                            }

                        }

                    }
                }
            }, null);
        }
}