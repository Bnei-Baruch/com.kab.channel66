package com.kab.channel66;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.kab.channel66.utils.CallStateInterface;
import com.kab.channel66.utils.CallStateListener;

import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

public class PlayerService extends Service implements CallStateInterface{

	private VLCMediaPlayer mAudioplay;
	private CallStateListener calllistener;
	private String mUrl;
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
			// Return this instance of LocalService so clients can call public methods
			return PlayerService.this;
		}
	}




	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		mAudioplay =  VLCMediaPlayer.get();
		calllistener = new CallStateListener(PlayerService.this);
		mAudioplay.setCalllistener(calllistener);
		if(!checkConnectivity())
		{
			return 0;
		}
		//return startId;

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
