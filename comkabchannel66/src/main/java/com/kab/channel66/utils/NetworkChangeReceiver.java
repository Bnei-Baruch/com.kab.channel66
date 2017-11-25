package com.kab.channel66.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import static android.provider.Settings.Global.WIFI_ON;

public class NetworkChangeReceiver extends BroadcastReceiver {

    public static String MOBILE_DATA_ON = "3G_ON";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi =
                connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile =
                connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);






        if (wifi.isConnected()) {
            context.sendBroadcast(new Intent("network_status").putExtra("status",WIFI_ON));
        }
            if (mobile!=null && mobile.isConnectedOrConnecting()) {
                context.sendBroadcast(new Intent("network_status").putExtra("status",MOBILE_DATA_ON));
            }

    }
}
