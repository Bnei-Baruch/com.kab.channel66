package com.kab.channel66;

//import io.vov.vitamio.VitamioInstaller.VitamioNotCompatibleException;
//import io.vov.vitamio.VitamioInstaller.VitamioNotFoundException;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.kab.channel66.utils.CallStateListener;
import com.kab.channel66.utils.CommonUtils;
import com.suredigit.inappfeedback.FeedbackDialog;
import com.suredigit.inappfeedback.FeedbackSettings;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

//import com.apphance.android.Log;

public class StreamListActivity extends BaseListActivity implements GoogleApiClient.OnConnectionFailedListener  ,LanguageSeletedListener , ListView.OnItemClickListener {

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder iservice) {
			PlayerService.LocalBinder binder = (PlayerService.LocalBinder) iservice;
			mService = binder.getService();
			mService.setBackground();
			mBound = true;
		}
		@Override
		public void onServiceDisconnected(ComponentName className) {

			 mBound = false;
		}


	};

	PlayerService mService;
	boolean mBound = false;

	private CallStateListener calllistener;
	private HLSEvents events;
	private String TranslationInfoString;
	private int status = 0;
	private static ProgressDialog myProgressDialog = null;
	private StreamAvailabilityChecker myChecker = null;
	PowerManager.WakeLock wl = null;
	JSONObject serverJSON = null;
	String content = null;

	ArrayList<String> pushMessages;
	BroadcastReceiver myReciever;
	//final VLCMediaPlayer audioplay;
	Dialog playDialog;
	Intent svc;
	private ArrayList<com.kab.channel66.HLSEvents.Page> pages;
	private CustomAdapter mAdataper;
	private ListView listview;

	GoogleApiClient mGoogleApiClient;
	private FeedbackDialog feedBackDialog;


	public StreamListActivity() {


	}


	public void onToggleClicked(View view)
		{
			 // Is the toggle on?
		    boolean on = ((ToggleButton) view).isChecked();
		    SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(StreamListActivity.this);
			SharedPreferences.Editor edit = shared.edit();
		    if (on) {
		        // Enable buffering
		    	edit.putBoolean("buffering", true);
		    	
		    } else {
		        // Disable buffering
		    	edit.putBoolean("buffering", false);
		    	
		    }
		    edit.commit();
		}

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
//		PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//		String version = pInfo.versionName;
//		int verCode = pInfo.versionCode;
		//remove old plugin library
//		Backendless.initApp( this, "EAD5E9C4-0007-D572-FF17-08523CED4200", "B485FC16-0FC2-A9D2-FF3E-406DF81A7000", version );

		feedBackDialog = new FeedbackDialog(this,"AF-2EF522E45745-F5");
		FeedbackSettings settings = new FeedbackSettings();
				settings.setText("Would like to suggest a new feature or report a bug? We would love to hear from you");
		feedBackDialog.setSettings(settings);
		CommonUtils.RemoveOldPlugin(this);
		setContentView(R.layout.listviewlayout);

		 listview = (ListView) findViewById(R.id.listview);

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this, this)
				.addApi(AppInvite.API)
				.build();

//		Backendless.Messaging.registerDevice("727406170147", new AsyncCallback<Void>() {
//			@Override
//			public void handleResponse(Void response) {
//
//			}
//
//			@Override
//			public void handleFault(BackendlessFault fault) {
//
//			}
//		});


//		try {
//			//  test channel
//			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(StreamListActivity.this);
//			final String audiourl = shared.getString("audiourl", "http://icecast.kab.tv/heb.mp3");
//
//			if (mService.mAudioplay.isPlaying(audiourl)) {
//				//show dialog
//				playDialog = new Dialog(this);
//				playDialog.setTitle("Playing audio");
//				playDialog.setContentView(R.layout.mediacontroller);
//				final ImageButton ask = (ImageButton) playDialog.findViewById(R.id.mediacontroller_ask);
//				final ImageButton but = (ImageButton) playDialog.findViewById(R.id.mediacontroller_play_pause);
//				but.setImageResource(R.drawable.mediacontroller_pause01);
//				but.setOnClickListener(new OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						// TODO Auto-generated method stub
//						SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(StreamListActivity.this);
//						String location = shared.getString("audiourl", "http://icecast.kab.tv/heb.mp3");
//						if(audioplay.isPlaying(location))
//						{
//							but.setImageResource(R.drawable.mediacontroller_play01);
//							audioplay.pause();
//
//						}
//						else
//						{
//							but.setImageResource(R.drawable.mediacontroller_pause01);
////									svc=new Intent(StreamListActivity.this, AudioPlayerFactory.GetAudioPlayer(StreamListActivity.this).getClass());
//
//							audioplay.prepare(MyApplication.getMyApp(), audiourl, new TomahawkMediaPlayerCallback() {
//								@Override
//								public void onPrepared(String query) {
//									if (audioplay.isPrepared(query))
//										audioplay.start();
//
//								}
//
//								@Override
//								public void onCompletion(String query) {
//
//								}
//
//								@Override
//								public void onError(String message) {
//
//								}
//							});
//						}
//					}
//				});
//				ask.setImageResource(R.drawable.system_help);
//				ask.setOnClickListener(new OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						// TODO Auto-generated method stub
//						Questions question = new Questions(StreamListActivity.this);
//						question.show();
//					}
//				});
//
//				playDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
//				{
//					@Override
//					public
//					void onCancel(DialogInterface dialog)
//					{
//						dialogBackpressed();
//					}
//				});
//				playDialog.show();
//			}
//			else
//				audioplay.pause();
//
//
//		} catch (Throwable t) {
////			Log.e("Failure during static initialization", t);
//
//		}
//


		ArrayList<String> channels = new ArrayList<String>();
		channels = getIntent().getStringArrayListExtra("channel");
		ArrayList<String> description = new ArrayList<String>();

//		String notification_body = getIntent().getExtras().getString("body");
//		if(notification_body!=null)
//		{
//			Uri uri = CommonUtils.findURIInText(notification_body);
//			if(uri != null)
//			{
//				Intent i = new Intent(Intent.ACTION_VIEW);
//				i.setData(uri);
//				startActivity(i);
//			}
//		}

		if(getIntent()!=null)
			handleMessageClicked(getIntent());
	}
	
	@Override
	public void onNewIntent(Intent intent)
	{

		handleMessageClicked(intent);
	}

	private void handleMessageClicked(Intent intent)
	{
		Bundle extras = intent.getExtras();
		if(extras!=null) {
			String notification_body = extras.getString("body");
			if (notification_body != null) {
				Uri uri = CommonUtils.findURIInText(notification_body);
				if (uri != null) {
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(uri);
					startActivity(i);
				} else {
					Intent i = new Intent(this, PushMessagesActivity.class);

					startActivity(i);
				}
			}
		}
		else
		{
			String data = intent.getStringExtra("data");
			if(data!=null)
				Log.d("fcm",data.toString());
		}
	}
	private String ExtractMMSfromAsx(String url1) {
		// TODO Auto-generated method stub
		//Making HTTP request
		String ret = "";
		ASXExtractor asxextractor = new ASXExtractor();
		asxextractor.execute(url1);
		try {
			ret =  asxextractor.get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ret;
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View v, int position, long id)
//	@Override
//	public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l)
	{


//	@Override
//	protected void onListItemClick(ListView l, View v, int position, long id) {
		String item = (String) listview.getAdapter().getItem(position);
		Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
		// Intent player = new Intent(StreamListActivity.this, VideoViewDemo.class);
		//Intent player = new Intent(StreamListActivity.this, VideoPlayerActivity.class);
		Intent player = new Intent(StreamListActivity.this, VideoActivity.class);

		if(pages!=null)
		{
			for(int i=0;i<pages.get(0).urls.urlslist.size();i++)
				if(pages.get(0).urls.urlslist.get(i).url_quality_name.equalsIgnoreCase(item))
				{



					HLSEvents.Url chosenStream = pages.get(0).urls.urlslist.get(i);
					if(chosenStream.type_val.equals("Video"))
					{
						
						//stop audio if playing
						StopAudioIfNeeded();
						String url1 = chosenStream.url_value;
						//playvideo
						String mms_url = null;
						//replace key
						String key = PreferenceManager.getDefaultSharedPreferences(this).getString("key", null);



						if(url1.contains("asx")){
							if(key!=null)
							{
								int j = url1.indexOf("special-")+ "special-".length();
								String replace = url1.substring(j, j+8);
								url1 = url1.replace(replace, key);
							}
							mms_url = ExtractMMSfromAsx(url1.trim());
							player.putExtra("path", mms_url);
						}
						else
						{
							player.putExtra(VideoActivity.LOCATION, url1);
						}


						EasyTracker.getTracker().trackEvent("Stream list", "on item clicked",url1,0L);

						startActivity(player);

					}
					else //play audio
					{
						//svc=new Intent(this, NativeBackgroundPlayer.class);
						//svc=new Intent(this, AudioPlayerFactory.GetAudioPlayer(StreamListActivity.this).getClass());
						//svc.putExtra("audiourl", chosenStream.url_value);
						final String location = chosenStream.url_value;
						mService.playAudio(location);
						EasyTracker.getTracker().trackEvent("Stream list audio", "on item clicked",location,0L);
//						audioplay.prepare(MyApplication.getMyApp(), chosenStream.url_value, new TomahawkMediaPlayerCallback() {
//							@Override
//							public void onPrepared(String query) {
//								if (audioplay.isPrepared(query))
//									audioplay.start();
//
//							}
//
//							@Override
//							public void onCompletion(String query) {
//
//							}
//
//							@Override
//							public void onError(String message) {
//
//							}
//						});
						//startService(svc);
						playDialog = new Dialog(this);
						playDialog.setTitle("Playing audio");
						playDialog.setContentView(R.layout.mediacontroller);
						final ImageButton ask = (ImageButton) playDialog.findViewById(R.id.mediacontroller_ask);
						final ImageButton but = (ImageButton) playDialog.findViewById(R.id.mediacontroller_play_pause);
						but.setImageResource(R.drawable.mediacontroller_pause01);
						but.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								if(mService.isPlaying())
								{
									but.setImageResource(R.drawable.mediacontroller_play01);
									mService.stopAudio();




								}
								else
								{
									SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(StreamListActivity.this);
									shared.edit().putString("audiourl", location).commit();
									but.setImageResource(R.drawable.mediacontroller_pause01);
									mService.playAudio(location);
									but.setImageResource(R.drawable.mediacontroller_pause01);
//									svc=new Intent(StreamListActivity.this, AudioPlayerFactory.GetAudioPlayer(StreamListActivity.this).getClass());
//									SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(StreamListActivity.this);
//									String audiourl = shared.getString("audiourl", "http://icecast.kab.tv/heb.mp3");

//									startService(svc);
//									audioplay.prepare(MyApplication.getMyApp(), audiourl, new TomahawkMediaPlayerCallback() {
//										@Override
//										public void onPrepared(String query) {
//											if (audioplay.isPrepared(query))
//												audioplay.start();
//
//										}
//
//										@Override
//										public void onCompletion(String query) {
//
//										}
//
//										@Override
//										public void onError(String message) {
//
//										}
//									});
								}
							}
						});
						ask.setImageResource(R.drawable.system_help);
						ask.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								Questions question = new Questions(StreamListActivity.this);
								question.show();
							}
						});

						playDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
						{
							@Override
							public
							void onCancel(DialogInterface dialog)
							{
								dialogBackpressed();
							}
						});
						playDialog.show();      
					}
				}
		}
		if(item.equals("ערוץ 66 - וידאו"))
		{
			StopAudioIfNeeded();
			//"mms://wms1.il.kab.tv/heb"
			// String url = ExtractMMSfromAsx("http://streams.kab.tv/heb.asx");
			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(StreamListActivity.this);
			EasyTracker.getTracker().trackEvent("ערוץ 66 - וידאו", "on item clicked","http://edge1.il.kab.tv/rtplive/tv66-heb-mobile.stream/playlist.m3u8",0L);
			if(shared.getBoolean("quality", false))
			{
				//player.putExtra("path", ExtractMMSfromAsx("http://streams.kab.tv/heb.asx"));//"rtsp://wms1.il.kab.tv/heb");// ExtractMMSfromAsx("http://streams.kab.tv/heb.asx"));
				//player.putExtra("path", "http://edge1.il.kab.tv/rtplive/tv66-heb-mobile.stream/playlist.m3u8");//"rtsp://wms1.il.kab.tv/heb");// ExtractMMSfromAsx("http://streams.kab.tv/heb.asx"));
				player.putExtra(VideoActivity.LOCATION, "http://edge1.il.kab.tv/rtplive/tv66-heb-mobile.stream/playlist.m3u8");//"rtsp://wms1.il.kab.tv/heb");// ExtractMMSfromAsx("http://streams.kab.tv/heb.asx"));

				startActivity(player);
			}
			else
			{
				//player.putExtra("path", "http://edge1.il.kab.tv/rtplive/tv66-heb-low.stream/playlist.m3u8");//"rtsp://wms1.il.kab.tv/heb");// ExtractMMSfromAsx("http://streams.kab.tv/heb.asx"));
				player.putExtra(VideoActivity.LOCATION, "http://edge1.il.kab.tv/rtplive/tv66-heb-mobile.stream/playlist.m3u8");//"rtsp://wms1.il.kab.tv/heb");// ExtractMMSfromAsx("http://streams.kab.tv/heb.asx"));

				startActivity(player);
			}

		}
		else if(item.equals("ערוץ 66 - אודיו") || item.equals("רדיו ערוץ 66"))
		{





//			svc=new Intent(this, AudioPlayerFactory.GetAudioPlayer(StreamListActivity.this).getClass());
//
			final String url;
			if(item.equals("ערוץ 66 - אודיו"))
				url =  "http://icecast.kab.tv/heb.mp3";
			else
				url =  "http://icecast.kab.tv/radiozohar2014.mp3";
			mService.playAudio(url);
			EasyTracker.getTracker().trackEvent(item,"on item clicked", url,0L);
//			startService(svc);
//			audioplay.prepare(MyApplication.getMyApp(), url, new TomahawkMediaPlayerCallback() {
//				@Override
//				public void onPrepared(String query) {
//					if (audioplay.isPrepared(query))
//						audioplay.start();
//
//				}
//
//				@Override
//				public void onCompletion(String query) {
//
//				}
//
//				@Override
//				public void onError(String message) {
//
//				}
//			});
			playDialog = new Dialog(this);
			playDialog.setTitle("Playing audio");
			playDialog.setContentView(R.layout.mediacontroller);
			final ImageButton ask = (ImageButton) playDialog.findViewById(R.id.mediacontroller_ask);
			final ImageButton but = (ImageButton) playDialog.findViewById(R.id.mediacontroller_play_pause);
			but.setImageResource(R.drawable.mediacontroller_pause01);
			but.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(mService.isPlaying() )
					{
						but.setImageResource(R.drawable.mediacontroller_play01);
						//audioplay.pause();
						//svc= null;
						mService.stopAudio();
					}
					else {
						but.setImageResource(R.drawable.mediacontroller_pause01);

						SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(StreamListActivity.this);
						shared.edit().putString("audiourl", url).commit();
						but.setImageResource(R.drawable.mediacontroller_pause01);
						mService.playAudio(url);
						but.setImageResource(R.drawable.mediacontroller_pause01);
//						svc=new Intent(StreamListActivity.this, AudioPlayerFactory.GetAudioPlayer(StreamListActivity.this).getClass());
//						SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(StreamListActivity.this);
//						String audiourl = shared.getString("audiourl", "http://icecast.kab.tv/heb.mp3");
//						svc.putExtra("audiourl",audiourl);
//						startService(svc);
					}
//
//
//						//svc=new Intent(StreamListActivity.this, AudioPlayerFactory.GetAudioPlayer(StreamListActivity.this).getClass());
//						SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(StreamListActivity.this);
//						String audiourl = shared.getString("audiourl", "http://icecast.kab.tv/heb.mp3");
//						//svc.putExtra("audiourl",audiourl);
//						//startService(svc);
//
////						audioplay.prepare(MyApplication.getMyApp(), url, new TomahawkMediaPlayerCallback() {
////							@Override
////							public void onPrepared(String query) {
////								if (audioplay.isPrepared(query))
////									audioplay.start();
////
////							}
////
////							@Override
////							public void onCompletion(String query) {
////
////							}
////
////							@Override
////							public void onError(String message) {
////
////							}
////						});
//						mService.playAudio(url);
//					}
				}
			});
			ask.setImageResource(R.drawable.system_help);
			ask.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Questions question = new Questions(StreamListActivity.this);
					question.show();
				}
			});

			playDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				@Override
				public
				void onCancel(DialogInterface dialog)
				{
					dialogBackpressed();
				}
			});
			playDialog.show();      

			//            
			//            bindService(svc, connection, Context.BIND_AUTO_CREATE);
			//	    	Uri uri = Uri.parse("http://icecast.kab.tv/heb.mp3");
			//	    	Intent player1 = new Intent(Intent.ACTION_VIEW,uri);
			//	    	 player1.setDataAndType(uri, "audio/*");
			//			startActivity(player1);	  
			//http://stackoverflow.com/questions/14043618/background-music-in-my-app-doesnt-start

		}
		else if(item.equals("Канал 66 на Русском - Видео"))
		{
			StopAudioIfNeeded();
			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(StreamListActivity.this);

			if(shared.getBoolean("quality", false))
			{
				player.putExtra(VideoActivity.LOCATION,  "http://edge1.il.kab.tv/rtplive/tv66-rus-mobile.stream/playlist.m3u8");
				startActivity(player);
			}
			else
			{
				player.putExtra(VideoActivity.LOCATION, "http://edge1.il.kab.tv/rtplive/tv66-rus-low.stream/playlist.m3u8");
				startActivity(player);
			}
		}
		else if(item.equals("Канал 66 на Русском - Аудио"))
		{
			//	    	Uri uri = Uri.parse("http://icecast.kab.tv/rus.mp3");
			//	    	Intent player1 = new Intent(Intent.ACTION_VIEW,uri);
			//	    	 player1.setDataAndType(uri, "audio/*");
			//			startActivity(player1);	 
			//svc=new Intent(this, AudioPlayerFactory.GetAudioPlayer(StreamListActivity.this).getClass());
			//svc.putExtra("audiourl", "http://icecast.kab.tv/rus.mp3");
			//startService(svc);
			final String url = ("http://icecast.kab.tv/rus.mp3");
//			audioplay.prepare(MyApplication.getMyApp(), url, new TomahawkMediaPlayerCallback() {
//				@Override
//				public void onPrepared(String query) {
//					if (audioplay.isPrepared(query))
//						audioplay.start();
//
//				}
//
//				@Override
//				public void onCompletion(String query) {
//
//				}
//
//				@Override
//				public void onError(String message) {
//
//				}
//			});
			mService.playAudio(url);
			playDialog = new Dialog(this);
			playDialog.setTitle("Playing audio");
			playDialog.setContentView(R.layout.mediacontroller);
			final ImageButton but = (ImageButton) playDialog.findViewById(R.id.mediacontroller_play_pause);
			but.setImageResource(R.drawable.mediacontroller_pause01);
			but.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(mService.isPlaying())
					{
						but.setImageResource(R.drawable.mediacontroller_play01);
						//stopService(svc);
						//svc= null;
						//audioplay.pause();
						mService.stopAudio();
					}
					else
					{
						but.setImageResource(R.drawable.mediacontroller_pause01);

						SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(StreamListActivity.this);
						shared.edit().putString("audiourl", url).commit();
						but.setImageResource(R.drawable.mediacontroller_pause01);
						mService.playAudio(url);
						but.setImageResource(R.drawable.mediacontroller_pause01);
//						audioplay.prepare(MyApplication.getMyApp(), url, new TomahawkMediaPlayerCallback() {
//							@Override
//							public void onPrepared(String query) {
//								if (audioplay.isPrepared(query))
//									audioplay.start();
//
//							}
//
//							@Override
//							public void onCompletion(String query) {
//
//							}
//
//							@Override
//							public void onError(String message) {
//
//							}
//						});
					}
				}
			});
			playDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
			{
				@Override
				public
				void onCancel(DialogInterface dialog)
				{
					dialogBackpressed();
				}
			});
			playDialog.show();      

		}


	}
	
	
	
	private void StopAudioIfNeeded() {
		// TODO Auto-generated method stub
		//svc=new Intent(this, AudioPlayerFactory.GetAudioPlayer(StreamListActivity.this).getClass());
		//audioplay.pause();
		mService.stopAudio();
	    //stopService(svc);
	}


	@Override
	public void onPause() {


		super.onPause();

		feedBackDialog.dismiss();
		
		
	}
	@SuppressLint("NewApi")
	@Override
	public void onResume() {
		super.onResume();


		EasyTracker.getInstance().setContext(this);



		if(mBound)
		{
			mService.setBackground();
			if(playDialog!=null && playDialog.isShowing() && !mService.isPlaying())
				playDialog.dismiss();
		}

		ContentParser cparser = new ContentParser();
		JSONParser parser = new JSONParser();
		parser.myContext = this;

		if (Build.VERSION.SDK_INT >= 11)
		{
			cparser.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"http://kabbalahgroup.info/internet/events/render_event_response?locale=he&source=stream_container&type=update_presets&timestamp=2011-11-25+13:29:53+UTC&stream_preset_id=3&flash=true&wmv=true");
			parser.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"http://mobile.kbb1.com/kab_channel/sviva_tova/jsonresponseexample.json");

		}
		else
		{
			cparser.execute("http://kabbalahgroup.info/internet/events/render_event_response?locale=he&source=stream_container&type=update_presets&timestamp=2011-11-25+13:29:53+UTC&stream_preset_id=3&flash=true&wmv=true");
			parser.execute("http://mobile.kbb1.com/kab_channel/sviva_tova/jsonresponseexample.json");    
		}

		try {
			if(serverJSON==null)
			{
				serverJSON = parser.get();
				content = cparser.get();
			}
			
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (ExecutionException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}



		prepareStreamData();


	}

	public void prepareStreamData()
	{
		JSONObject returned_Val = serverJSON;
		String time_stamp = null;
		String isUpdate = null;
		String version = null;
		//test events
		try {
			if(returned_Val==null)
			{
				Toast.makeText(this, "Could not retrieve data from server",Toast.LENGTH_LONG);

			}
			else
			{
				time_stamp = returned_Val.getString("time_stamp");
				isUpdate = returned_Val.getString("updateandlock");
				version = returned_Val.getString("version");
				TranslationInfoString = returned_Val.getString("TranslationWIFISupport");

				if(isUpdate.equalsIgnoreCase("true"))
				{
					//String versionName = getResources().getString(R.string.version_name);
					String versionName = "100";
					try {
						versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
					} catch (NameNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(Float.parseFloat(version)>Float.parseFloat(versionName))
					{
						AlertDialog chooseToInstall = new AlertDialog.Builder(StreamListActivity.this).create();
						chooseToInstall.setTitle("New version available, do you want to update?");

						chooseToInstall.setButton("Ok", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								// here you can add functions
								Intent goToMarket = new Intent(Intent.ACTION_VIEW)
								.setData(Uri.parse("market://details?id=com.kab.channel66"));
								startActivity(goToMarket); 



							}
						});
						chooseToInstall.setButton2("Cancel", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								// here you can add functions
								finish();
							}
						});
						chooseToInstall.show();
					}

				}
			}

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		ArrayList<String> description = new ArrayList<String>();

		mAdataper = new CustomAdapter(this);


		if(CommonUtils.getActivated(this) && returned_Val!=null)
		{
			events = new HLSEvents(returned_Val, getApplicationContext());
			events.parse();

			String key = CommonUtils.getLastKnownLang(this);
			HLSEvents.Pages channels =(HLSEvents.Pages) events.locale.get(key);
			//		     pages = new ArrayList<Page>();
			pages = channels.getPages();

			// mAdataper.addSectionHeaderItem("Sviva Tova");
			for(int i = 0 ; i<pages.size();i++)
			{
				mAdataper.addSectionHeaderItem(pages.get(i).description);
				for(int j = 0;j<pages.get(i).urls.urlslist.size();j++)
				{

					if(pages.get(i).urls.urlslist.get(j).type_val.equalsIgnoreCase("audio"))
						mAdataper.addItem(pages.get(i).urls.urlslist.get(j).url_quality_name,true);
					else
						mAdataper.addItem(pages.get(i).urls.urlslist.get(j).url_quality_name,false);
					//mAdataper.addItem(pages.get(i).urls.urlslist.get(j).type_val+" "+pages.get(i).urls.urlslist.get(j).url_quality);
					
				}	
				//pages.add(new Gson().fromJson(channels.pages, Page.class));
				//description .add(channels.pages.get(i).urls.urlslist.get(i).);
			}

		} 	


		description.add("ערוץ 66 - וידאו");
		description.add("ערוץ 66 - אודיו");
		description.add("רדיו ערוץ 66");

		description.add("Канал 66 на Русском - Видео");
		description.add("Канал 66 на Русском - Аудио");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, description);

		mAdataper.addSectionHeaderItem("Channel 66");
		for(int i=0;i<description.size();i++)
			mAdataper.addItem(description.get(i),false);

		listview.setAdapter(mAdataper);
		listview.setOnItemClickListener(this);
		//listview.setChoiceMode(ListView.);
listview.setItemsCanFocus(true);


	}
	@Override
	public void onStart() {
		super.onStart();
		// The rest of your onStart() code.
		EasyTracker.getInstance().setContext(this.getApplicationContext());
		EasyTracker.getInstance().activityStart(this);
		// Bind to LocalService
		Intent intent = new Intent(this, PlayerService.class);
		bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

	}
		 @Override
		 public void onDestroy() {
		   super.onDestroy();
		    // The rest of your onStart() code.

		   
	
		 }


	@Override
	public void onStop() {
		super.onStop();
		// The rest of your onStop() code.
		if (mBound && !mService.isPlaying()) {
			unbindService(mConnection);
			mBound = false;
		}
		else if(mBound && mService.isPlaying())
		{
			mService.setForeground();
		}
		EasyTracker.getInstance().activityStop(this); // Add this method.


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SharedPreferences userInfoPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Boolean activated = userInfoPreferences.getBoolean("activated", false);
		Boolean isNative =  userInfoPreferences.getBoolean("isNative", true);
		if(!activated)
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.streamoptionmenu, menu);

		}
		else
		{
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.streamoptionmenu_activated, menu);

		}

//		MenuItem item = menu.findItem(R.id.playType);
//		item.setChecked(isNative);
		return true;
	}

	public void dialogBackpressed()
	{
		playDialog.hide();
		//audioplay.pause();
		mService.stopAudio();
	}
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();

//		if(svc!=null)
//			stopService(svc);

		mService.stopAudio();
	}
	@SuppressLint("ShowToast")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.login: {

			String token = FirebaseInstanceId.getInstance().getToken();

			// Log and toast
			String msg = getString(R.string.msg_token_fmt, token);
			Log.d("token", msg);
			Toast.makeText(StreamListActivity.this, msg, Toast.LENGTH_SHORT).show();


			Intent intent = new Intent(getApplicationContext(), SvivaTovaLogin.class);
			startActivity(intent);

//			AlertDialog.Builder alert = new AlertDialog.Builder(this);
//			alert.setTitle("Login");
//			alert.setMessage("Enter Pin :");
//
//			// Set an EditText view to get user input
//			final EditText input = new EditText(this);
//			alert.setView(input);
//
//			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int whichButton) {
//					String value = input.getText().toString();
//					EasyTracker.getTracker().trackEvent("Stream list", "pin code value",value,0L);
//
//					if(value.equals("arvut"))
//					{
//						Intent intent = new Intent(getApplicationContext(), SvivaTovaLogin.class);
//						startActivity(intent);
//					}
////					Log.d( "Login", "Pin Value : " + value);
//
//					return;
//				}
//			});
//
//			alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//
//				public void onClick(DialogInterface dialog, int which) {
//					// TODO Auto-generated method stub
//					return;
//				}
//			});
//			alert.show();


			return true;
		}
		case R.id.quality:
			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(StreamListActivity.this);
			SharedPreferences.Editor edit = shared.edit();
			if(shared.getBoolean("quality", false))
			{
				Toast.makeText(StreamListActivity.this, "Changed quality to medium", Toast.LENGTH_LONG).show();
				edit.putBoolean("quality", false);
				edit.commit();
			}
			else
			{
				Toast.makeText(StreamListActivity.this, "Changed quality to high", Toast.LENGTH_LONG).show();
				edit.putBoolean("quality", true);
				edit.commit();
			}

			return true;

		case R.id.Changelang:
			CommonUtils.ShowLanguageSelection(StreamListActivity.this, this);

			return true;
		case R.id.PushMessages:
			Intent intent = new Intent(StreamListActivity.this,PushMessagesActivity.class);
			startActivity(intent);
			return true;
			case R.id.Feedback:
				feedBackDialog.show();

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void autocheckdone()
	{
		if(myChecker!=null)
		{
			myChecker.cancel(true);
			myProgressDialog.hide();
			myChecker.setAuto(false);
			if(wl.isHeld())
				wl.release();
		}
	}

	public void streamfound() {
		// TODO Auto-generated method stub
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				//Your code to run in GUI thread here
				myProgressDialog.hide();

				Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
				r.play();

				Toast.makeText(StreamListActivity.this, "broadcast started", Toast.LENGTH_LONG).show();
				wl.release();
			}//public void run() {
		});

	}

	

	public boolean isOnline(Context context) { 
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);    
		NetworkInfo netInfo = cm.getActiveNetworkInfo();    
		return netInfo != null && netInfo.isConnected();
	}
	@Override
	public void onSelectLanguage(String lang) {
		// TODO Auto-generated method stub
		int index = CommonUtils.languages.indexOf(lang);
		String langcode = CommonUtils.langs.get(index);
		CommonUtils.setLastKnownLang(langcode, this);
		prepareStreamData();

	}


	@Override
	public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

	}





}
