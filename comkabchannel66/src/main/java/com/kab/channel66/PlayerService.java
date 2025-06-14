package com.kab.channel66;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.kab.channel66.utils.CallStateInterface;
import com.kab.channel66.utils.CallStateListener;
import com.kab.channel66.utils.CallStateListenerSType;
import com.kab.channel66.utils.Constants;

import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;


public class PlayerService extends Service implements CallStateInterface,TomahawkMediaPlayerCallback{

	private VLCMediaPlayer mAudioplay;
	private CallStateListener calllistener;
	@RequiresApi(api = Build.VERSION_CODES.S)
	private CallStateListenerSType calllistenerTypeS;
	private String mUrl;
	private Notification notification;
	TomahawkMediaPlayerCallback mTomhawkCallback;
	// Binder given to clients
	private final IBinder mBinder = new LocalBinder();

	private BroadcastReceiver data_stat = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

				if (intent.getAction().contentEquals("network_status")) {

					onCompletion(mUrl);
				}
			}

	};
	private Timer mTimer;

	@Override
	public void PausePlay() {
		mAudioplay.pause();
	}


	@Override
	public void ResumePlay() {
		if(!mAudioplay.isPlaying(mUrl))
			mAudioplay.start();
	}

	@Override
	public Boolean isPaused() {
		return mAudioplay.isPrepared(mUrl);
	}


	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		PlayerService getService() {
			mAudioplay =  VLCMediaPlayer.get();
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
				calllistener = new CallStateListener(PlayerService.this);
				mAudioplay.setCalllistener(calllistener);
			}
			else
			{
				calllistenerTypeS = new CallStateListenerSType(PlayerService.this);
				mAudioplay.setCalllistenerTypeS(calllistenerTypeS);
			}

			Log.i("svc", "Received Start Foreground Intent ");

			createAndSetNotification("Audio playing", "Click to Access App");

			return PlayerService.this;
		}
	}




	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Log.i("svc", "Received Start Foreground Intent ");


		if (intent != null) {
			if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {

//			Log.i("svc", "Received Start Foreground Intent ");
//should not get here because we are using binder
			} else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
				Log.i("svc", "Clicked Play");
				playAudio(mUrl);
				createAndSetNotification("Audio playing", "Click to Access App");
				startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
						notification);
			} else if (intent.getAction().equals(Constants.ACTION.PAUSE_ACTION)) {
				Log.i("svc", "Clicked Pause");

				stopAudio();
				createAndSetNotification("Audio paused", "Click to Access App");
				startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
						notification);
			} else if (intent.getAction().equals(
					Constants.ACTION.STOPFOREGROUND_ACTION)) {
				Log.i("svc", "Received Stop Foreground Intent");
				stopForeground(false);
				//stopSelf();


			}
		}
		return START_STICKY;
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	private void createAndSetNotification(String title, String subtext)
	{


		String channelId;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			 channelId = createNotificationChannel("my_not_service", "audio");
		} else {
			// If earlier version channel ID is not used
			// https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
			channelId = "";
		}
		Log.i("svc", "Received Start Foreground Intent ");
		Intent notificationIntent = new Intent(PlayerService.this, StreamListActivity.class);
		notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
		notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
				);
		PendingIntent pendingIntent = PendingIntent.getActivity(PlayerService.this, 0,
				notificationIntent, PendingIntent.FLAG_IMMUTABLE);

		Intent pauseIntent = new Intent(PlayerService.this, PlayerService.class);
		pauseIntent.setAction(Constants.ACTION.PAUSE_ACTION);
		PendingIntent ppauseIntent = PendingIntent.getService(PlayerService.this, 0,
				pauseIntent, PendingIntent.FLAG_IMMUTABLE);

		Intent playIntent = new Intent(PlayerService.this, PlayerService.class);
		playIntent.setAction(Constants.ACTION.PLAY_ACTION);
		PendingIntent pplayIntent = PendingIntent.getService(PlayerService.this, 0,
				playIntent, PendingIntent.FLAG_IMMUTABLE);
		Bitmap icon = BitmapFactory.decodeResource(getResources(),
				R.drawable.icon);

		notification = new NotificationCompat.Builder(PlayerService.this,channelId)
				.setSmallIcon(R.drawable.icon)
				.setContentTitle(title)
				.setContentText(subtext)
				.setContentIntent(pendingIntent)
				.setOngoing(true)
				.addAction(android.R.drawable.ic_media_pause,
						"Pause", ppauseIntent)
				.addAction(android.R.drawable.ic_media_play, "Play",
						pplayIntent)
				.build();

	}

	@RequiresApi(Build.VERSION_CODES.O)
	private String createNotificationChannel(String channelId , String channelName){
		NotificationChannel chan = new NotificationChannel(channelId,
				channelName, NotificationManager.IMPORTANCE_NONE);
		chan.setLightColor(Color.BLUE);
		chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
		chan.setShowBadge(true);

		NotificationManager service = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		service.createNotificationChannel(chan);
		return channelId;
	}


	private boolean checkConnectivity()
	{
		Dialog blockApp;
		boolean state;
		if(!(state = isOnline(PlayerService.this)))
		{
//			new AlertDialog.Builder(this)
//		    .setTitle("Data not available")
//		    .setMessage("Appliaction needs data connection")
//		    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//		        public void onClick(DialogInterface dialog, int which) { 
//		            // continue with delete
//		        	return;
//		        	
//		        }
//		     })
//		    
//		    
//		     .show();
			
			Toast.makeText(getApplicationContext(), "No data connection", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}
	 public boolean isOnline(Context context) { 
		    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);    
		    NetworkInfo netInfo = cm.getActiveNetworkInfo();    
		    return netInfo != null && netInfo.isConnected();
		}


	public void setBackground()
	{
		stopForeground(true);
	}
	public void setForeground()
	{

//		Intent intent = new Intent(PlayerService.this,PlayerService.class);
//		intent.setAction(Constants.ACTION.STARTFOREGROUND_ACTION);
//
//
//		startService(intent);

		startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
				notification);

	}
	public int playAudio(String url)
	{

		registerReceiver(data_stat,new IntentFilter("network_status"),Context.RECEIVER_EXPORTED);
		mAudioplay.prepare(MyApplication.getMyApp(), url, this);



		return 0;
	}

	public int stopAudio()
	{
		if(isPlaying()) {
			mAudioplay.stop();
			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

			shared.edit().putBoolean("play", false).commit();
			try {
				unregisterReceiver(data_stat);
			}
			catch(IllegalArgumentException e) {

				e.printStackTrace();
			}
		}
		return 0;
	}
	public boolean isPlaying()
	{

		return mAudioplay!=null && mAudioplay.isPlaying(mUrl);
	}

	@Override
	public void onPrepared(String query) {
		if (mAudioplay.isPrepared(query)) {
			mAudioplay.start();
			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			shared.edit().putString("audiourl", query).commit();
			shared.edit().putBoolean("play",true).commit();
			mUrl = query;

			//setForeground();








		}
	}

	@Override
	public void onCompletion(String query) {

		//check if user asked or it was disconnected
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		if(shared.getBoolean("play",false)) {

			ConnectivityManager cm =
					(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			boolean isConnected = activeNetwork != null &&
					activeNetwork.isConnectedOrConnecting();

			if(isConnected)
				playAudio(mUrl);
			else {
				final long period = 1000;
				mTimer =new Timer();
				mTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

						if(shared.getBoolean("play",false)) {
							ConnectivityManager cm =
									(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

							NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
							boolean isConnected = activeNetwork != null &&
									activeNetwork.isConnectedOrConnecting();

							if (isConnected) {
								playAudio(mUrl);
								mTimer.cancel();

							}
						}
						// do your task here
					}
				}, 0, period);
			}
		}

		//if user asked then conitnue

		//if diconnected
		//check network status
		//if available play again
		//if not available wait 2 sec and try again
		//show diaglog to user that it reconnects

	}

	@Override
	public void onError(String message) {

	}

	@Override
	public void onDestroy()
	{
		stopAudio();
	}
}
