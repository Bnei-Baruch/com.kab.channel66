package com.kab.channel66;

//import io.vov.vitamio.VitamioInstaller.VitamioNotCompatibleException;
//import io.vov.vitamio.VitamioInstaller.VitamioNotFoundException;


import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.view.WindowManager;
import android.widget.Toast;

import com.audionowdigital.android.openplayer.Player;
import com.audionowdigital.android.openplayer.PlayerEvents;
import com.kab.channel66.utils.CallStateInterface;
import com.kab.channel66.utils.CallStateListener;
import com.kab.channel66.utils.CommonUtils;

abstract public class BaseBackgroundPlayer extends BaseService implements OnPreparedListener,OnBufferingUpdateListener,CallStateInterface{

	TelephonyManager telephony;
	private static final int NOTIFICATION_ID = 0;
	private CallStateListener calllistener;
	protected  MediaPlayer mediaPlayer;
	ProgressDialog dialog;
	Dialog playDialog;
	Intent myIntent;
	public static final String STATUS = "Status";
	public static final String NOTIFICATION = "com.kab.channel66.service.receiver";
	NotificationManager mNM;
	Thread thread;
	StreamProxy sp; //adding streamproxy to solve audio failure of some devices before ics - http://stackoverflow.com/questions/9840523/mediaplayer-streams-mp3-in-emulator-but-not-on-device
	// Playback handler for callbacks
	//private Handler playbackHandler;
	//private Player.DecoderType type = Player.DecoderType.MX;

	//private Player player;

	public enum status
	{
		stop,
		play,
		buffer
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		//if(flags==CommonUtils.FROM_WIDGET)
//			if(!LibsChecker.checkVitamioLibs(this))
//			{
//				return START_NOT_STICKY;
//			}
		myIntent = intent;



		if(super.onStartCommand(intent, flags, startId)==0)
				return START_NOT_STICKY;
		
		telephony = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE); //TelephonyManager object  
		calllistener = new CallStateListener(this); 
		TelephonyManager Tel; 
		Tel = ( TelephonyManager )getSystemService(Context.TELEPHONY_SERVICE);
		 Tel.listen(calllistener ,PhoneStateListener.LISTEN_CALL_STATE);
		 
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

       if(intent==null)
       {
    	   //no intent recevied defaulting to channel 66
    	   
    	   mNM.cancel(NOTIFICATION_ID);
    	   return START_NOT_STICKY;
       }
        String url = intent.getStringExtra("audiourl");
		if(url==null)
		{
			mNM.cancel(NOTIFICATION_ID);
			return START_NOT_STICKY;
		}
		


		String songName;
		Class <?> cls;
		if(intent.getBooleanExtra("sviva", false))
		{
			songName = "Sviva Tova";
			cls = WebLogin.class;
			
		}
		else
		{
			songName = "Channel 66";
			cls = StreamListActivity.class;
		}
		// assign the song name to songName
		Intent in = new Intent(getApplicationContext(), cls);
		in.setAction(Intent.ACTION_MAIN);
		in.addCategory(Intent.CATEGORY_LAUNCHER);
		in.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		
		PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,in
		                ,
		                PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new Notification();
		notification.tickerText = "Playing audio";
		notification.icon = R.drawable.icon;
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		notification.setLatestEventInfo(getApplicationContext(), "Channel 66",
		                "Playing: " + songName, pi);
		
		startForeground(NOTIFICATION_ID, notification);
		
		 SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(this);
		 boolean isBuffering = shared.getBoolean("buffering", false);
		
		try {
			setupPlayer(url);
			//mediaPlayer = new MediaPlayer();
			//mediaPlayer.setOnPreparedListener(this);
			//mediaPlayer.setOnBufferingUpdateListener(this);
		//	mediaPlayer.setDataSource(url);
			SetAudioUrl(url, getApplicationContext());
//			new Thread(new Runnable() {
//				@Override
//				public void run() {
//					SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//					if(!player.isReadingHeader())
//					//player.setDataSource(SP.getString("audiourl",null),-1);
//						player.setDataSource("http://icecast.kab.tv/live1-heb-574bcfd5.mp3",-1);
//
//				}
//			}).start();



			if(isBuffering)
			{
				prepareAsyncPlayer();

				
			if(intent.getFlags() == CommonUtils.FROM_WIDGET)
			{
			dialog = new ProgressDialog(getApplicationContext());
			dialog.setMessage("Buffering...");
			
			
//			dialog.setCancelable(true);
//			dialog.setCanceledOnTouchOutside(true);
//			
			
//			dialog.setOnCancelListener(new OnCancelListener() {
//				
//				@Override
//				public void onCancel(DialogInterface dialog) {
//					// TODO Auto-generated method stub
//					stopSelf();
//				}
//			});
			
			dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			dialog.show();
			}
			else
				publishStatus(status.buffer.ordinal());
			
			}
			else
			{
				preparePlayer();
				telephony.listen(calllistener, PhoneStateListener.LISTEN_CALL_STATE); //Register our listener with TelephonyManager
			}
			//mediaPlayer.prepare();
			//mediaPlayer.start();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		
		
		
		
		
		mNM.notify(NOTIFICATION_ID, notification);

		return START_NOT_STICKY;
		
		
		
		
	}

	protected void prepareAsyncPlayer() {
	}


	protected void preparePlayer() {
	}

	abstract protected void setupPlayer(String url);

	protected void publishStatus( int status) {
		if(status == BaseBackgroundPlayer.status.play.ordinal() && myIntent.getFlags()  == CommonUtils.FROM_WIDGET && dialog!=null )
		{
			dialog.hide();
		}
	    Intent intent = new Intent(NOTIFICATION);
	    intent.putExtra(STATUS, status);

	    sendBroadcast(intent);
	  }

	public void onPrepared(MediaPlayer player) {
        // We now have buffered enough to be able to play
		if(dialog!=null && dialog.isShowing())
			dialog.hide();
		else
			publishStatus(status.play.ordinal());
		
		
		player.start();
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
					handleLostDataConnection();
//	                if( BaseBackgroundPlayer.this.mediaPlayer!=null)
//	                	BaseBackgroundPlayer.this.mediaPlayer.stop();
//	                BaseBackgroundPlayer.this.stopSelf();
	            }
	        }
	    };

	protected void handleLostDataConnection() {
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
	    stopService();
	    
	    

	}
	
	void stopService()
	{
		unregisterReceiver(mConnReceiver);
		stopPlayer();
//	    if (mediaPlayer != null)
//	    {
//	    	mediaPlayer.stop();
//	    	mediaPlayer.reset();
//	    	mediaPlayer.release();
//	    	mediaPlayer = null;
//	    	if(sp!=null)
//	    		sp.stop();
//	    	mNM.cancel(NOTIFICATION_ID);
//	    }
//		if(player!=null)
//		{
//			player.stop();
//			player.stopAudioTrack();
//			player = null;
//
//		}
	    
	    stopForeground(true);
	}

	private void stopPlayer() {
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
		pausePlayer();
		
//		if(mediaPlayer!=null && mediaPlayer.isPlaying())
//			mediaPlayer.pause();
//
//		if(player.isPlaying()) {
//			player.pause();
//		}
	}

	private void pausePlayer() {

	}

	@Override
	public void Start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void Resume() {
		// TODO Auto-generated method stub
		 Log.i("BackgroundAudio", "resuming");
		resumePlayer();

//		if(mediaPlayer!=null && !mediaPlayer.isPlaying())
//			mediaPlayer.start();
//
//		if(!player.isPlaying())
//			player.play();
	}

	private void resumePlayer() {
	}

	@Override
	public void Stop() {
		// TODO Auto-generated method stub
//		if(!player.isStopped())
//			player.stop();
		
	}
	
	void SetAudioUrl(String audioUrl, Context context)
	  {
		
		  SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
		  Editor ed = SP.edit();
		  ed.putString("audiourl", audioUrl);
		  ed.commit();
	  }


//	void initNewPlayer()
//	{
//		playbackHandler = new Handler() {
//			@Override
//			public void handleMessage(Message msg) {
//				switch (msg.what) {
//					case PlayerEvents.PLAYING_FAILED:
//						//logArea.setText("The decoder failed to playback the file, check logs for more details");
//						Log.d("Audio", "handleMessage ");
//						break;
//					case PlayerEvents.PLAYING_FINISHED:
//						//logArea.setText("The decoder finished successfully");
//						break;
//					case PlayerEvents.READING_HEADER:
//						//logArea.setText("Starting to read header");
//						Log.d("Audio", "handleMessage ");
//						break;
//					case PlayerEvents.READY_TO_PLAY:
//						//logArea.setText("READY to play - press play :)");
//						if(player.isReadyToPlay())
//							player.play();
//						break;
//					case PlayerEvents.PLAY_UPDATE:
//						//logArea.setText("Playing:" + (msg.arg1 / 60) + ":" + (msg.arg1 % 60) + " (" + (msg.arg1) + "s)");
//						//seekBar.setProgress((int) (msg.arg1 * 100 / player.getDuration()));
//						break;
//					case PlayerEvents.TRACK_INFO:
//						Bundle data = msg.getData();
//					//	logArea.setText("title:" + data.getString("title") + " artist:" + data.getString("artist") + " album:" + data.getString("album") +
//					//			" date:" + data.getString("date") + " track:" + data.getString("track"));
//						break;
//				}
//			}
//		};
//
//		// quick test for a quick player
//		player = new Player(playbackHandler, Player.DecoderType.MX);
//	}


}
