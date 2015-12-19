package com.kab.channel66;

import com.audionowdigital.android.openplayer.Player;
import com.audionowdigital.android.openplayer.PlayerEvents;
import com.kab.channel66.utils.CallStateInterface;
import com.kab.channel66.utils.CallStateListener;


import android.app.Dialog;

import android.app.NotificationManager;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import android.widget.Toast;

import java.io.IOException;

public class NativeBackgroundPlayer extends BaseBackgroundPlayer implements OnPreparedListener,OnBufferingUpdateListener,CallStateInterface{


	private static final int NOTIFICATION_ID = 0;
	private CallStateListener calllistener;
	private  MediaPlayer mediaPlayer;
	ProgressDialog dialog;
	Dialog playDialog;
	public static final String STATUS = "Status";
	public static final String NOTIFICATION = "com.kab.channel66.service.receiver";

	Thread thread;
	StreamProxy sp; //adding streamproxy to solve audio failure of some devices before ics - http://stackoverflow.com/questions/9840523/mediaplayer-streams-mp3-in-emulator-but-not-on-device
	// Playback handler for callbacks
	private Handler playbackHandler;
	private Player.DecoderType type = Player.DecoderType.MX;

	private Player player;

	private BroadcastReceiver mConnReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
			String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
			boolean isFailover = intent.getBooleanExtra(ConnectivityManager.EXTRA_IS_FAILOVER, false);

			NetworkInfo currentNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			NetworkInfo otherNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_OTHER_NETWORK_INFO);

			if(currentNetworkInfo.isConnected()){
				//Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(getApplicationContext(), "Lost data connection", Toast.LENGTH_LONG).show();
				if( NativeBackgroundPlayer.this.mediaPlayer!=null)
					NativeBackgroundPlayer.this.mediaPlayer.stop();
				NativeBackgroundPlayer.this.stopSelf();
			}
		}
	};


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{



		if(mediaPlayer!=null && mediaPlayer.isPlaying())
			return START_NOT_STICKY;


		return super.onStartCommand(intent, flags, startId);
		
	}
	

	public void onPrepared(MediaPlayer player) {
        // We now have buffered enough to be able to play
		if(dialog!=null && dialog.isShowing())
			dialog.hide();
		else
			publishStatus(status.play.ordinal());
		
		
		mediaPlayer.start();
		telephony.listen(calllistener, PhoneStateListener.LISTEN_CALL_STATE); //Register our listener with TelephonyManager
    }
	/*
	private void addNotification()
	{
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
		        .setSmallIcon(R.drawable.icon)
		        .setContentTitle("My notification")
		        .setContentText("Hello World!");
		// Creates an explicit intent for an Activity in your app
		//Intent resultIntent = new Intent(this, ResultActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		//stackBuilder.addParentStack(ResultActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		//stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		int mId = 1;
		// mId allows you to update the notification later on.
		mNotificationManager.notify(mId , mBuilder.build());
	}
	*/
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate() {

	    super.onCreate();
//	    Intent intent = new Intent(this,InitActivity.class);
//	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//	    startActivity(intent);


	    registerReceiver(mConnReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)) ;  
	}
	

	@Override
	public void onStart(Intent intent, int startId) {

	    super.onStart(intent, startId);
	    try {
			
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    
	}
	
	
	@Override
	public void onDestroy() {

	    super.onDestroy();
	    //stopService();
	    
	    

	}
	
	void stopService()
	{
		unregisterReceiver(mConnReceiver);
	    if (mediaPlayer != null)
	    {
	    	mediaPlayer.stop();
	    	mediaPlayer.reset();
	    	mediaPlayer.release();
	    	mediaPlayer = null;
	    	if(sp!=null)
	    		sp.stop();
	    	mNM.cancel(NOTIFICATION_ID);
	    }
		if(player!=null)
		{
			player.stop();
			player.stopAudioTrack();
			player = null;

		}
	    
	    stopForeground(true);
	}

	@Override
	public void onBufferingUpdate(MediaPlayer mp, int percent) {
		// TODO Auto-generated method stub
		
		Log.d("audio", "test " + percent);
	}

	@Override
	public void Pause() {
		// TODO Auto-generated method stub
		 Log.i("BackgroundAudio", "Pausing");
		if(mediaPlayer!=null && mediaPlayer.isPlaying())
			mediaPlayer.pause();



	}

	@Override
	public void Start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Resume() {
		// TODO Auto-generated method stub
		 Log.i("BackgroundAudio", "resuming");
		if(mediaPlayer!=null && !mediaPlayer.isPlaying())
			mediaPlayer.start();

//		if(!player.isPlaying())
//			player.play();
	}

	@Override
	public void Stop() {
		// TODO Auto-generated method stub
		if(!player.isStopped())
			player.stop();
		
	}
	
	void SetAudioUrl(String audioUrl, Context context)
	  {
		
		  SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		  Editor ed = SP.edit();
		  ed.putString("audiourl", audioUrl);
		  ed.commit();
	  }


	protected void prepareAsyncPlayer() {
		mediaPlayer.prepareAsync();
	}

	protected void preparePlayer() {

		try {
			mediaPlayer.prepare();
			mediaPlayer.start();
		}
		catch (IOException e)
		{

		}
	}

	protected void setupPlayer(String url) {

		try {


		mediaPlayer = new MediaPlayer();
		mediaPlayer.setOnPreparedListener(this);
		mediaPlayer.setOnBufferingUpdateListener(this);
		mediaPlayer.setDataSource(url);
	}
	catch(IllegalArgumentException e)
	{
	}
	catch(SecurityException e)
	{}
		catch(IllegalStateException e)
		{

		}
		catch(IOException e)
		{

		}
	}
	protected void handleLostDataConnection() {
		if( super.mediaPlayer!=null)
			super.mediaPlayer.stop();
		super.stopSelf();
	}
}
