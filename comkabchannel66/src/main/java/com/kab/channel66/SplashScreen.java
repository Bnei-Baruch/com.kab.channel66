package com.kab.channel66;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

//import com.parse.Parse;
//import com.parse.ParseAnalytics;
//import com.parse.ParseInstallation;
//import com.parse.PushService;

//import com.bugsense.trace.BugSenseHandler;

public class SplashScreen extends Activity {
	
	protected int _splashTime = 2000;
	Intent i = null;
	Bundle extra;
	private Thread splashTread;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	   // BugSenseHandler.initAndStartSession(SplashScreen.this, "031c1eab");
	    setContentView(R.layout.splash);
		String title = null;
		String data = null;
		Intent intent = getIntent();

		extra = intent.getExtras();


	    final SplashScreen sPlashScreen = this;
//		if (!io.vov.vitamio.LibsChecker.checkVitamioLibs(this))
//			return;
	    // thread for displaying the SplashScreen
	    splashTread = new Thread() {
	        @Override
	        public void run() {
	            try {	            	
	            	synchronized(this){
	            		wait(_splashTime);
	            	}
	            	
	            } catch(InterruptedException e) {} 
	            finally {
	                finish();
	                SharedPreferences userInfoPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());	
	    			Boolean active = userInfoPreferences.getBoolean("activated", false);
//	                if(active)
//	                {
//	                Intent i = new Intent();
//	                i.setClass(sPlashScreen, SvivaTovaLogin.class);
//	        		startActivity(i);
//	                }
//	                else
//	        		{
	                	Intent intent = new Intent(sPlashScreen,StreamListActivity.class);
					if(extra!=null)
					intent.putExtras(extra);
	  					startActivity(intent);
					if(i!=null)
					{
						startActivity(i);
					}
//	        		}
	        		
	            }
	        }
	    };
	    
	    splashTread.start();
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    if (event.getAction() == MotionEvent.ACTION_DOWN) {
	    	synchronized(splashTread){
	    		splashTread.notifyAll();
	    	}
	    }
	    return true;
	}

@Override
public void onStart() {
  super.onStart();
   // The rest of your onStart() code.
  //EasyTracker.getInstance().setContext(this.getApplicationContext());
  //EasyTracker.getInstance().activityStart(this);
	MyApplication application = (MyApplication) getApplication();
	Tracker mTracker = application.getDefaultTracker();
	mTracker.enableAutoActivityTracking(true);
	mTracker.send(new HitBuilders.ScreenViewBuilder().build());
  
 
}


@Override
public void onStop() {
  super.onStop();
   // The rest of your onStop() code.

}
			
}
