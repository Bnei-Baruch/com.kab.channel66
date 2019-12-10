package com.kab.channel66;

import android.app.Application;
import android.content.Context;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;
import io.fabric.sdk.android.Fabric;

//import io.vov.vitamio.LibsChecker;
//
//import org.acra.*;
//import org.acra.annotation.*;
//import com.firebase.client.Firebase;
//import com.parse.Parse;
//import com.parse.ParseInstallation;
//import com.parse.ParsePush;
//import com.parse.PushService;

//@ReportsCrashes(formKey = "", // will not be used
//mailTo = "igal.avraham@gmail.com",
//mode = ReportingInteractionMode.TOAST,
//resToastText = R.string.report)

//
//@ReportsCrashes(
//        formKey = "",
//        formUri = "https://channel66-acra.cloudant.com/acra-channel66/_design/acra-storage/_update/report",
//        reportType = org.acra.sender.HttpSender.Type.JSON,
//        httpMethod = org.acra.sender.HttpSender.Method.PUT,
//        formUriBasicAuthLogin="terlydrighterimparmenoth",
//        formUriBasicAuthPassword="tKMx7rB0TpFgmVUHtvuXRNPM",
//        // Your usual ACRA configuration
//        mode = ReportingInteractionMode.TOAST,
//        resToastText = R.string.report)
//
//

public class MyApplication extends Application {
    static Application myapp;
    private static GoogleAnalytics sAnalytics;
    private static Tracker sTracker;
	@Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        sAnalytics = GoogleAnalytics.getInstance(this);
        myapp = this;
        String token = FirebaseInstanceId.getInstance().getToken();
        FirebaseMessaging.getInstance().subscribeToTopic("news");


        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);


        //Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    synchronized public Tracker getDefaultTracker() {
        // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
        if (sTracker == null) {
            sTracker = sAnalytics.newTracker(R.xml.analytics);
        }

        return sTracker;
    }

    static public Application getMyApp()
    {
        return myapp;
    }
}
