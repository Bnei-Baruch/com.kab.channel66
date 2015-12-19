package com.kab.channel66;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import io.vov.vitamio.LibsChecker;
import android.app.Application;

import org.acra.*;
import org.acra.annotation.*;

import com.kab.channel66.utils.CommonUtils;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.PushService;


//@ReportsCrashes(formKey = "", // will not be used
//mailTo = "igal.avraham@gmail.com",
//mode = ReportingInteractionMode.TOAST,
//resToastText = R.string.report)


@ReportsCrashes(
        formKey = "",
        formUri = "https://channel66-acra.cloudant.com/acra-channel66/_design/acra-storage/_update/report",
        reportType = org.acra.sender.HttpSender.Type.JSON,
        httpMethod = org.acra.sender.HttpSender.Method.PUT,
        formUriBasicAuthLogin="terlydrighterimparmenoth",
        formUriBasicAuthPassword="tKMx7rB0TpFgmVUHtvuXRNPM",
        // Your usual ACRA configuration
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.report)
        


public class MyApplication extends Application {

	@Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
//        if(CommonUtils.checkConnectivity(getApplicationContext()))
//        	ACRA.init(this);
        LibsChecker.checkVitamioLibs(this);
       
        Parse.enableLocalDatastore(getApplicationContext());

        
     // channel66 
        Parse.initialize(this, "dmSTSXcOcBxITZBioUAmC7HXps0OCUteMJEklSCD", "b0gN0SoJgOmQ51fkQoNb9B7bNEIF2agc9SYhFG7U");//real
		
		// test
        //channel Parse.initialize(this, "KZGRjYuBEwh6vubjJBRzscvVixyLC8fWg9YqAwVS", "H3JqHHIKrd8xN44weGfAsWmUeCJQdqh8bPR8H4M6");//test

		ParseInstallation.getCurrentInstallation().saveInBackground();

       
		Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
		
        // The following line triggers the initialization of ACRA
        
    }
}
