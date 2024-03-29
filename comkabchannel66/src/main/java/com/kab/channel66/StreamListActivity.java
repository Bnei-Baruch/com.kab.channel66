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
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.storage.FirebaseStorage;
import com.kab.channel66.utils.CallStateListener;
import com.kab.channel66.utils.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

//import com.apphance.android.Log;

public class StreamListActivity extends BaseListActivity implements GoogleApiClient.OnConnectionFailedListener , GoogleApiClient.ConnectionCallbacks,LanguageSeletedListener , ListView.OnItemClickListener {

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


	ArrayList<String> pushMessages;
	BroadcastReceiver myReciever;
	//final VLCMediaPlayer audioplay;
	Dialog playDialog;
	Intent svc;
	private ArrayList<com.kab.channel66.HLSEvents.Page> pages;
	private CustomAdapter mAdataper;
	private ListView listview;

	GoogleApiClient mGoogleApiClient;
	private FirebaseStorage storage;
	private AdView mAdView;
	private FirebaseAnalytics mFirebaseAnalytics;
	private AdRequest adRequest;

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
//		MobileAds.initialize(this, "ca-app-pub-5716767383344062~9051358001");
		MobileAds.initialize(this, "ca-app-pub-4525606414173317~9308887615");

		//MobileAds.openDebugMenu(this,"ca-app-pub-5716767383344062/6401822554");


//		List<String> testDeviceIds = Arrays.asList("D2BE1DC818CF2B7B8FED459FBCA250CD");
//		RequestConfiguration configuration =
//				new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
//		MobileAds.setRequestConfiguration(configuration);


		// Obtain the FirebaseAnalytics instance.
		mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

//

		Intent stickyService = new Intent(this, StickyService.class);
		startService(stickyService);

		MyApplication application = (MyApplication) MyApplication.getMyApp();

		CommonUtils.RemoveOldPlugin(this);
		setContentView(R.layout.listviewlayout);

		//mAdView = findViewById(R.id.adView);
		//mAdView = new AdView();

		LinearLayout adContainer = findViewById(R.id.main_layout);

		mAdView = new AdView(this);
		mAdView.setAdSize(AdSize.SMART_BANNER);

		String language = Locale.getDefault().getLanguage();

		if(language.contentEquals("ru"))
		{
			mAdView.setAdUnitId("ca-app-pub-4525606414173317/8397601947");
		}
		else if(language.contentEquals("iw"))
		{
			mAdView.setAdUnitId("ca-app-pub-4525606414173317/4583206361");
		}
		else if (language.contentEquals("es"))
		{
			mAdView.setAdUnitId("ca-app-pub-4525606414173317/4893471200");
		}else {
			mAdView.setAdUnitId("ca-app-pub-4525606414173317/6238396369");
		}


		//mAdView.setAdSize(AdSize.SMART_BANNER);
		//ca-app-pub-5716767383344062/6401822554
		//mAdView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");
		//ca-app-pub-3940256099942544/6300978111 - test

		adRequest = new AdRequest.Builder().build();
		Log.d("StreamListActivity", "IS TEST DEVICE: "+adRequest.isTestDevice(this));

		mAdView.loadAd(adRequest);


		adContainer.addView(mAdView,0);

		/*
		ca-app-pub-5716767383344062/5342277352 - ad unit english

		ca-app-pub-5716767383344062/6401822554 - hebrew
        ads:adUnitId="ca-app-pub-5716767383344062/6401822554">


		virtual home russian
		ca-app-pub-5716767383344062/8487315445
		 */

		listview = (ListView) findViewById(R.id.listview);

		mGoogleApiClient = new GoogleApiClient.Builder(this)
				.enableAutoManage(this, this)
				.addConnectionCallbacks(this)
				.addApi(AppInvite.API)
				.build();

		mGoogleApiClient.connect();

		boolean autoLaunchDeepLink = false;
		AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, this, autoLaunchDeepLink)
				.setResultCallback(
						new ResultCallback<AppInviteInvitationResult>() {
							@Override
							public void onResult(@NonNull AppInviteInvitationResult result) {
								if (result.getStatus().isSuccess()) {
									// Extract deep link from Intent
									Intent intent = result.getInvitationIntent();
									String deepLink = AppInviteReferral.getDeepLink(intent);

									if(deepLink.contentEquals("https://channel66.com/radio"))
									{
										openRadio();
									}
									// Handle the deep link. For example, open the linked
									// content, or apply promotional credit to the user's
									// account.

									// ...
								} else {
									Log.d("invite", "getInvitation: no deep link found.");
								}
							}
						});


		storage = FirebaseStorage.getInstance();


		if(getIntent()!=null)
			handleMessageClicked(getIntent());
	}
	
	@Override
	public void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		handleMessageClicked(intent);
	}

	private void openRadio()
	{
		 final String url =  "https://icecast.kab.tv/radiozohar2014.mp3";
		mService.playAudio(url);
		MyApplication application = (MyApplication) MyApplication.getMyApp();


		Bundle bundle = new Bundle();
		bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "start");
		bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "radio");
		bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "audio");
		mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle);
		//EasyTracker.getTracker().trackEvent("radio by link","on item clicked", url,0L);
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
		playDialog.setTitle("Playing audio -"+ "רדיו קבלה לעם");
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


	}

	private void handleMessageClicked(Intent intent)
	{
		Bundle extras = intent.getExtras();
		if(extras!=null) {
			String notification_body = extras.getString("data");
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
	{


		String item = (String) listview.getAdapter().getItem(position);
		if(BuildConfig.DEBUG)
			Toast.makeText(this, item + " selected", Toast.LENGTH_LONG).show();
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




						Bundle bundle = new Bundle();
						bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Video");
						bundle.putString(FirebaseAnalytics.Param.ITEM_NAME,chosenStream.url_quality_name);
						bundle.putString(FirebaseAnalytics.Param.ITEM_VARIANT,pages.get(0).description);
						bundle.putString(FirebaseAnalytics.Param.CONTENT, url1);
						mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle);

						startActivity(player);

					}
					else //play audio
					{
						//svc=new Intent(this, NativeBackgroundPlayer.class);
						//svc=new Intent(this, AudioPlayerFactory.GetAudioPlayer(StreamListActivity.this).getClass());
						//svc.putExtra("audiourl", chosenStream.url_value);
						final String location = chosenStream.url_value;
						mService.playAudio(location);

						Bundle bundle = new Bundle();
						bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Audio");
						bundle.putString(FirebaseAnalytics.Param.ITEM_NAME,chosenStream.url_quality_name);
						bundle.putString(FirebaseAnalytics.Param.ITEM_VARIANT,pages.get(0).description);
						bundle.putString(FirebaseAnalytics.Param.CONTENT, location);
						mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle);


						playDialog = new Dialog(this);
						playDialog.setTitle("Playing audio -"+chosenStream.url_quality_name);
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
//
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
		if(item.equals("ערוץ קבלה לעם - וידאו"))
		{
			StopAudioIfNeeded();
			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(StreamListActivity.this);
			//EasyTracker.getTracker().trackEvent("ערוץ קבלה לעם - וידאו", "on item clicked","http://edge1.il.kab.tv/rtplive/tv66-heb-mobile.stream/playlist.m3u8",0L);


			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Video");
			bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "ערוץ קבלה לעם - וידאו");
			bundle.putString(FirebaseAnalytics.Param.CONTENT, "https://edge3.uk.kab.tv/live/tv66-heb-medium/playlist.m3u8");
			mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle);




				player.putExtra(VideoActivity.LOCATION, "https://edge3.uk.kab.tv/live/tv66-heb-medium/playlist.m3u8");//"rtsp://wms1.il.kab.tv/heb");// ExtractMMSfromAsx("http://streams.kab.tv/heb.asx"));

				startActivity(player);


		}
		else if(item.equals("ערוץ קבלה לעם - אודיו") || item.equals("רדיו קבלה לעם"))// || item.equals("רדיו חיים חדשים"))
		{





//			svc=new Intent(this, AudioPlayerFactory.GetAudioPlayer(StreamListActivity.this).getClass());
//
			String info = "";
			String url = "";
			if(item.equals("ערוץ קבלה לעם - אודיו")) {
				url = "https://icecast.kab.tv/heb.mp3";
				info = "ערוץ קבלה לעם";
			}
			else if(item.equals("רדיו קבלה לעם")) {
				url = "https://icecast.kab.tv/radiozohar2014.mp3";
				info = "רדיו קבלה לעם";
			}
//			else {
//				url = "http://icecast.kab.tv/newlife";
//				info = "רדיו חיים חדשים";
//			}
			mService.playAudio(url);
			//EasyTracker.getTracker().trackEvent(item,"on item clicked", url,0L);


			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Audio");
			bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, info);
			bundle.putString(FirebaseAnalytics.Param.CONTENT, url);
			mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle);

			playDialog = new Dialog(this);
			playDialog.setTitle("Playing audio -"+info );
			playDialog.setContentView(R.layout.mediacontroller);
			final ImageButton ask = (ImageButton) playDialog.findViewById(R.id.mediacontroller_ask);
			final ImageButton but = (ImageButton) playDialog.findViewById(R.id.mediacontroller_play_pause);
			but.setImageResource(R.drawable.mediacontroller_pause01);
			final String finalUrl = url;
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
						shared.edit().putString("audiourl", finalUrl).commit();
						but.setImageResource(R.drawable.mediacontroller_pause01);
						mService.playAudio(finalUrl);
						but.setImageResource(R.drawable.mediacontroller_pause01);
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
		else if(item.equals("Каббала на Русском - Видео"))
		{
			StopAudioIfNeeded();
			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(StreamListActivity.this);

			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Video");
			bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Каббала на Русском - Видео");
			bundle.putString(FirebaseAnalytics.Param.CONTENT, "https://edge3.uk.kab.tv/live/tv66-rus-medium/playlist.m3u8");
			mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle);

				player.putExtra(VideoActivity.LOCATION,  "https://edge3.uk.kab.tv/live/tv66-rus-medium/playlist.m3u8");
				startActivity(player);

		}
		else if(item.equals("Каббала на Русском - Аудио"))
		{

			final String url = ("http://icecast.kab.tv/rus.mp3");
//

			Bundle bundle = new Bundle();
			bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "Audio");
			bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "Каббала на Русском - Аудио");
			bundle.putString(FirebaseAnalytics.Param.CONTENT, url);
			mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM, bundle);



			mService.playAudio(url);
			playDialog = new Dialog(this);
			playDialog.setTitle("Playing audio -"+"Каббала на Русском - Аудио");
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
//
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


		
	}
	@SuppressLint("NewApi")
	@Override
	public void onResume() {
		super.onResume();


//		EasyTracker.getInstance().setContext(this);



		if(mBound)
		{
			mService.setBackground();
			if(playDialog!=null && playDialog.isShowing() && !mService.isPlaying())
				playDialog.dismiss();
			if(playDialog!=null && !playDialog.isShowing() && mService.isPlaying())
				playDialog.show();
		}

		JSONParser parser = new JSONParser();
		parser.myContext = this;

		if (Build.VERSION.SDK_INT >= 11)
		{
			parser.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"https://mobile.kbb1.com/kab_channel/sviva_tova/jsonresponseexample.json");

		}
		else
		{
			parser.execute("https://mobile.kbb1.com/kab_channel/sviva_tova/jsonresponseexample.json");
		}
//


		try {
			if(serverJSON==null)
			{
				serverJSON = parser.get();
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


		description.add("ערוץ קבלה לעם - וידאו");
		description.add("ערוץ קבלה לעם - אודיו");
		description.add("רדיו קבלה לעם");
		//description.add("רדיו חיים חדשים");

		description.add("Каббала на Русском - Видео");
		description.add("Каббала на Русском - Аудио");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, description);

		mAdataper.addSectionHeaderItem("קבלה לעם");
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
		//EasyTracker.getInstance().setContext(this.getApplicationContext());
		//EasyTracker.getInstance().activityStart(this);
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
		//Tracker.getInstance().activityStop(this); // Add this method.


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

		if(mService!=null)
		mService.stopAudio();
	}
	@SuppressLint("ShowToast")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.login: {

			//String token = FirebaseInstanceId.getInstance().getToken();

			if(BuildConfig.DEBUG) {
				// Log and toast
			//String msg = getString(R.string.msg_token_fmt, token);
			//	Log.d("token", msg);
			//	Toast.makeText(StreamListActivity.this, msg, Toast.LENGTH_SHORT).show();
			}

			Intent intent = new Intent(getApplicationContext(), SvivaTovaLogin.class);
			startActivity(intent);

	//		FlutterFragment login = Flutter.createFragment("login");
			



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
//			case R.id.Feedback:
//				feedBackDialog.show();

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


	@Override
	public void onConnected(@Nullable Bundle bundle) {

	}

	@Override
	public void onConnectionSuspended(int i) {

	}
}
