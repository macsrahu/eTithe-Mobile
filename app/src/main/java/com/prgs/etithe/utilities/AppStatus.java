package com.prgs.etithe.utilities;

/**
 * Created by rahupathi on 9/10/2017.
 */

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


public class AppStatus {

    private static AppStatus instance = new AppStatus();
    static Context context;
    ConnectivityManager connectivityManager;
    NetworkInfo wifiInfo, mobileInfo;
    boolean connected = false;

    public static AppStatus getInstance(Context ctx) {
        context = ctx.getApplicationContext();
        return instance;
    }

    public boolean isOnline() {
        try {
            boolean haveConnectedWiFi = false;
            boolean haveConnectedMobile = false;

            connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
            for (NetworkInfo ni : networkInfo) {
                String _NETWORK_TYPE = "";
                if (InternalStorage.readObject(context, Global._URL_KEY) != null) {
                    _NETWORK_TYPE = InternalStorage.readObject(context, Global._URL_KEY).toString();
                }
                if (!_NETWORK_TYPE.isEmpty() && _NETWORK_TYPE.equalsIgnoreCase("WIFI")) {
                    if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
                        if (ni.isConnected())
                            haveConnectedWiFi = true;
                    }

                } else {
                    if (ni.getTypeName().equalsIgnoreCase("WIFI")) {
                        if (ni.isConnected())
                            haveConnectedWiFi = true;
                    }
                    if (ni.getTypeName().equalsIgnoreCase("MOBILE")) {
                        if (ni.isConnected())
                            haveConnectedMobile = true;
                    }
                }

            }
            connected = haveConnectedWiFi || haveConnectedMobile;
            return connected;


        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
            Log.v("connectivity", e.toString());
        }
        return connected;
    }
}