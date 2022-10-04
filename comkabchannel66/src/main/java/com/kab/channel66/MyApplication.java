package com.kab.channel66;

import android.app.Application;
import android.content.Context;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;

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

	@Override
    public void onCreate() {
        super.onCreate();
      //  Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        myapp = this;
        //String token = FirebaseInstanceId.getInstance().getToken();
        FirebaseMessaging.getInstance().subscribeToTopic("news");


        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);


        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);
        //Facebook
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


    static public Application getMyApp()
    {
        return myapp;
    }
}
