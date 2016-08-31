package com.kab.channel66;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.RemoteMessage;
import com.kab.channel66.utils.CallStateInterface;
import com.kab.channel66.utils.CallStateListener;
import com.kab.channel66.utils.Constants;

import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;



public class PlayerService extends Service implements CallStateInterface{

	private VLCMediaPlayer mAudioplay;
	private CallStateListener calllistener;
	private String mUrl;
	private Notification notification;
	// Binder given to clients
	private final IBinder mBinder = new LocalBinder();

	@Override
	public void PausePlay() {
		mAudioplay.pause();
	}


	@Override
	public void ResumePlay() {
		if(!mAudioplay.isPlaying(mUrl))
			mAudioplay.start();
	}


	/**
	 * Class used for the client Binder.  Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		PlayerService getService() {
			mAudioplay =  VLCMediaPlayer.get();
			calllistener = new CallStateListener(PlayerService.this);
			mAudioplay.setCalllistener(calllistener);


			Log.i("svc", "Received Start Foreground Intent ");
			Intent notificationIntent = new Intent(PlayerService.this, StreamListActivity.class);
			notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TASK);
			PendingIntent pendingIntent = PendingIntent.getActivity(PlayerService.this, 0,
					notificationIntent, 0);

			Intent pauseIntent = new Intent(PlayerService.this, PlayerService.class);
			pauseIntent.setAction(Constants.ACTION.PAUSE_ACTION);
			PendingIntent ppauseIntent = PendingIntent.getService(PlayerService.this, 0,
					pauseIntent, 0);

			Intent playIntent = new Intent(PlayerService.this, PlayerService.class);
			playIntent.setAction(Constants.ACTION.PLAY_ACTION);
			PendingIntent pplayIntent = PendingIntent.getService(PlayerService.this, 0,
					playIntent, 0);
			Bitmap icon = BitmapFactory.decodeResource(getResources(),
					R.drawable.icon);

			notification = new NotificationCompat.Builder(PlayerService.this)
					.setContentTitle("Channel 66")
					.setTicker("Channel 66 playing")
					.setSmallIcon(R.drawable.icon)
					.setContentIntent(pendingIntent)
					.setOngoing(true)
					.addAction(android.R.drawable.ic_media_pause,
							"Pause", ppauseIntent)
					.addAction(android.R.drawable.ic_media_play, "Play",
							pplayIntent).build();
			// Return this instance of LocalService so clients can call public methods
			return PlayerService.this;
		}
	}




	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		mAudioplay = VLCMediaPlayer.get();
//		calllistener = new CallStateListener(PlayerService.this);
//		mAudioplay.setCalllistener(calllistener);
//		if (!checkConnectivity()) {
//			return 0;
//		}

		Log.i("svc", "Received Start Foreground Intent ");




		//return startId;
		if (intent.getAction().equals(Constants.ACTION.STARTFOREGROUND_ACTION)) {

			Log.i("svc", "Received Start Foreground Intent ");
			Intent notificationIntent = new Intent(PlayerService.this, StreamListActivity.class);
			notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_CLEAR_TASK);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);

			Intent pauseIntent = new Intent(this, PlayerService.class);
			pauseIntent.setAction(Constants.ACTION.PAUSE_ACTION);
			PendingIntent ppauseIntent = PendingIntent.getService(this, 0,
					pauseIntent, 0);

			Intent playIntent = new Intent(this, PlayerService.class);
			playIntent.setAction(Constants.ACTION.PLAY_ACTION);
			PendingIntent pplayIntent = PendingIntent.getService(this, 0,
					playIntent, 0);
			Bitmap icon = BitmapFactory.decodeResource(getResources(),
					R.drawable.icon);

			notification = new NotificationCompat.Builder(this)
					.setContentTitle("Channel 66")
					.setContentText("Channel 66 playing")
					.setTicker("Channel 66 playing")
//					.setLargeIcon(
//							Bitmap.createScaledBitmap(icon, 128, 128, false))
					.setSmallIcon(R.drawable.icon)
					.setContentIntent(pendingIntent)
					.setOngoing(true)
					.addAction(android.R.drawable.ic_media_pause,
							"Pause", ppauseIntent)
					.addAction(android.R.drawable.ic_media_play, "Play",
							pplayIntent).build();

//			Log.i("svc", "Received Start Foreground Intent ");
//			Intent notificationIntent = new Intent(this, StreamListActivity.class);
//			notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
//			notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//					| Intent.FLAG_ACTIVITY_CLEAR_TASK);
//			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
//					notificationIntent, 0);
//
//			Intent previousIntent = new Intent(this, PlayerService.class);
//			previousIntent.setAction(Constants.ACTION.PREV_ACTION);
//			PendingIntent ppreviousIntent = PendingIntent.getService(this, 0,
//					previousIntent, 0);
//
//			Intent playIntent = new Intent(this, PlayerService.class);
//			playIntent.setAction(Constants.ACTION.PLAY_ACTION);
//			PendingIntent pplayIntent = PendingIntent.getService(this, 0,
//					playIntent, 0);
//
//			Intent nextIntent = new Intent(this, PlayerService.class);
//			nextIntent.setAction(Constants.ACTION.NEXT_ACTION);
//			PendingIntent pnextIntent = PendingIntent.getService(this, 0,
//					nextIntent, 0);
//
//			Bitmap icon = BitmapFactory.decodeResource(getResources(),
//					R.drawable.icon);
//
//			Notification notification = new NotificationCompat.Builder(this)
//					.setContentTitle("Channel 66")
//					.setTicker("Channel 66 playing")
//					.setContentText("streaming")
//					.setSmallIcon(R.drawable.icon)
//					.setLargeIcon(
//							Bitmap.createScaledBitmap(icon, 128, 128, false))
//					.setContentIntent(pendingIntent)
//					.setOngoing(true)
//					.addAction(android.R.drawable.ic_media_previous,
//							"Previous", ppreviousIntent)
//					.addAction(android.R.drawable.ic_media_play, "Play",
//							pplayIntent)
//					.addAction(android.R.drawable.ic_media_next, "Next",
//							pnextIntent).build();
			startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
					notification);
		}  else if (intent.getAction().equals(Constants.ACTION.PLAY_ACTION)) {
			Log.i("svc", "Clicked Play");
			playAudio(mUrl);
		} else if (intent.getAction().equals(Constants.ACTION.PAUSE_ACTION)) {
			Log.i("svc", "Clicked Pause");

			stopAudio();
		} else if (intent.getAction().equals(
				Constants.ACTION.STOPFOREGROUND_ACTION)) {
			Log.i("svc", "Received Stop Foreground Intent");
			stopForeground(false);
			//stopSelf();


		}
		return START_STICKY;
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
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
		mAudioplay.prepare(MyApplication.getMyApp(), url, new TomahawkMediaPlayerCallback() {
			@Override
			public void onPrepared(String query) {
				if (mAudioplay.isPrepared(query)) {
					mAudioplay.start();
					SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					shared.edit().putString("audiourl", query).commit();
					mUrl = query;

					//setForeground();








				}
			}

			@Override
			public void onCompletion(String query) {

			}

			@Override
			public void onError(String message) {

			}
		});
		return 0;
	}

	public int stopAudio()
	{
		mAudioplay.stop();
		return 0;
	}
	public boolean isPlaying()
	{

		return mAudioplay.isPlaying(mUrl);
	}


}
