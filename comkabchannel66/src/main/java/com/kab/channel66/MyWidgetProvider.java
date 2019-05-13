package com.kab.channel66;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Random;

public class MyWidgetProvider extends AppWidgetProvider {

  private static final String ACTION_CLICK_PLAY = "ACTION_CLICK_PLAY";
  private static final String ACTION_CLICK_PAUSE = "ACTION_CLICK_PAUSE";
  
  private static final String ACTION_CLICK_TV = "ACTION_CLICK_TV";
  private static final String ACTION_CLICK_RADIO = "ACTION_CLICK_RADIO";
  private static final String ACTION_CLICK_LAST = "ACTION_CLICK_LAST";
  

  Intent svc;
    VLCMediaPlayer audioplayer;
    private Tracker mTracker;

    @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager,
      int[] appWidgetIds) {
	  //EasyTracker.getInstance().setContext(context.getApplicationContext());

      MyApplication application = (MyApplication) MyApplication.getMyApp();
      mTracker = application.getDefaultTracker();

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("installing widget")
                .setAction("onUpdate")
                .setValue(0)
                .build());
	 // EasyTracker.getTracker().trackEvent("Widget", "installing widget","onUpdate",0L);
	    audioplayer = VLCMediaPlayer.get();
    // Get all ids
    ComponentName thisWidget = new ComponentName(context,
        MyWidgetProvider.class);
    int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
    for (int widgetId : allWidgetIds) {
      // create some random data
      int number = (new Random().nextInt(100));

      RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
          R.layout.widget_layout);
      Log.w("WidgetExample", String.valueOf(number));
      // Set the text
      //remoteViews.setTextViewText(R.id.update, String.valueOf(number));

      // Register an onClickListener
      Intent intent = new Intent(context, MyWidgetProvider.class);

      intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
      intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

      PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
          0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      //remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.imageButton1, getPendingSelfIntent(context, ACTION_CLICK_PLAY));
      remoteViews.setOnClickPendingIntent(R.id.imageButton2, getPendingSelfIntent(context, ACTION_CLICK_PAUSE));
      
      remoteViews.setOnClickPendingIntent(R.id.tv, getPendingSelfIntent(context, ACTION_CLICK_TV));
      remoteViews.setOnClickPendingIntent(R.id.radio, getPendingSelfIntent(context, ACTION_CLICK_RADIO));
      remoteViews.setOnClickPendingIntent(R.id.last, getPendingSelfIntent(context, ACTION_CLICK_LAST));
      remoteViews.setTextColor(R.id.last, Color.BLUE);
      appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }
  }
  
  @Override
  public void onReceive(Context context, Intent intent) {
      // TODO Auto-generated method stub
      super.onReceive(context, intent);
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

      RemoteViews remoteViews;
      ComponentName watchWidget;

      remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
      watchWidget = new ComponentName(context, MyWidgetProvider.class);

     
      if (ACTION_CLICK_PLAY.equals(intent.getAction())) {

         
//    	  remoteViews.setTextViewText(R.id.Button3, "playing");
          // svc = new Intent(context.getApplicationContext(), AudioPlayerFactory.GetAudioPlayer(context.getApplicationContext()).getClass());
           SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
			 
			 final String audiourl = shared.getString("audiourl", "http://icecast.kab.tv/heb.mp3");
			 audiourl.replace("heb", shared.getString("lang", "heb"));
	    	// svc.putExtra("audiourl", audiourl);
	    	// svc.addFlags(CommonUtils.FROM_WIDGET);
	    	 //context.stopService(svc);
	    	// context.startService(svc);
          new Thread(new Runnable() {
              @Override
              public void run() {
                  audioplayer.prepare(MyApplication.getMyApp(), audiourl, new TomahawkMediaPlayerCallback() {
                      @Override
                      public void onPrepared(String query) {
                          if(query.equalsIgnoreCase(audiourl))
                              audioplayer.start();
                      }

                      @Override
                      public void onCompletion(String query) {

                      }

                      @Override
                      public void onError(String message) {

                      }
                  });

              }
          }).start();

      }
      else if (ACTION_CLICK_PAUSE.equals(intent.getAction())) 
      {
//    	  remoteViews.setTextViewText(R.id.Button3, "stopped");
//    	  svc = new Intent(context.getApplicationContext(),
//                  AudioPlayerFactory.GetAudioPlayer(context.getApplicationContext()).getClass());
//
//    	  context.stopService(svc);
          audioplayer.pause();
      }
      else if (ACTION_CLICK_TV.equals(intent.getAction())) 
      {
    	  String audiourl = "http://icecast.kab.tv/heb.mp3";
    	  SetAudioUrl(audiourl, context);
    	  remoteViews.setTextColor(R.id.tv, Color.BLUE);
    	  remoteViews.setTextColor(R.id.radio, Color.BLACK);
    	  remoteViews.setTextColor(R.id.last, Color.BLACK);
    	 
      }
      else if (ACTION_CLICK_RADIO.equals(intent.getAction())) 
      {
    	  String audiourl = "http://icecast.kab.tv/radiozohar2014.mp3";
    	  SetAudioUrl(audiourl, context);
    	  remoteViews.setTextColor(R.id.radio, Color.BLUE);
    	  remoteViews.setTextColor(R.id.last, Color.BLACK);
    	  remoteViews.setTextColor(R.id.tv, Color.BLACK);
     	 
      }
      else if (ACTION_CLICK_LAST.equals(intent.getAction())) 
      {
    	  String audiourl = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext()).getString("audiourl", "http://icecast.kab.tv/heb.mp3");
    	
    	  remoteViews.setTextColor(R.id.radio, Color.BLACK);
    	  remoteViews.setTextColor(R.id.last, Color.BLUE);
    	  remoteViews.setTextColor(R.id.tv, Color.BLACK);
      }
      
      appWidgetManager.updateAppWidget(watchWidget, remoteViews);

  }

  protected PendingIntent getPendingSelfIntent(Context context, String action) {
      Intent intent = new Intent(context, getClass());
      intent.setAction(action);
      return PendingIntent.getBroadcast(context, 0, intent, 0);
  }
  
 @SuppressLint("CommitPrefEdits")
void SetAudioUrl(String audioUrl, Context context)
  {
	
	  SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
	  Editor ed = SP.edit();
	  ed.putString("audiourl", audioUrl);
	  ed.commit();
  }
} 