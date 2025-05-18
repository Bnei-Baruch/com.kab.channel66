package com.kab.channel66;

//import io.vov.vitamio.VitamioInstaller.VitamioNotCompatibleException;
//import io.vov.vitamio.VitamioInstaller.VitamioNotFoundException;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.graphics.ColorMatrix;
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
import android.text.TextUtils;
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
import com.kab.channel66.auth.AuthStateManager;
import com.kab.channel66.auth.Configuration;
import com.kab.channel66.auth.LoginActivity;
import com.kab.channel66.utils.CallStateListener;
import com.kab.channel66.utils.CommonUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.browser.customtabs.CustomTabsIntent;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationException;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationResponse;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.AuthorizationServiceDiscovery;
import net.openid.appauth.ClientAuthentication;
import net.openid.appauth.ClientSecretBasic;
import net.openid.appauth.EndSessionRequest;
import net.openid.appauth.RegistrationRequest;
import net.openid.appauth.RegistrationResponse;
import net.openid.appauth.ResponseTypeValues;
import net.openid.appauth.TokenRequest;
import net.openid.appauth.TokenResponse;

import okio.Okio;

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

	private static final String EXTRA_FAILED = "failed";

	private RecreateAuthRequestTask mTask;
	static String TAG = StreamListActivity.class.getSimpleName();
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
	private ExecutorService mExecutor;
	private AuthStateManager mAuthStateManager;
	private Configuration mConfiguration;

	private AuthorizationService mAuthService;

	private final AtomicReference<String> mClientId = new AtomicReference<>();
	private final AtomicReference<AuthorizationRequest> mAuthRequest = new AtomicReference<>();
	private final AtomicReference<CustomTabsIntent> mAuthIntent = new AtomicReference<>();
	private CountDownLatch mAuthIntentLatch = new CountDownLatch(1);

	private static final int RC_AUTH = 100;

	private static final int END_SESSION_REQUEST_CODE = 911;
	private final AtomicReference<JSONObject> mUserInfoJson = new AtomicReference<>();


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


		//first run of keycloack , logout everything
		if(CommonUtils.getActivated(this) && !CommonUtils.isKeycloakFirstRun(this))
		{
			CommonUtils.setActivated(false,this);

		}

		CommonUtils.setKeycloakFirstRun(this);
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

		mExecutor = Executors.newSingleThreadExecutor();
		mAuthStateManager = AuthStateManager.getInstance(this);
		mConfiguration = Configuration.getInstance(this);
		mExecutor.submit(this::initializeAppAuth);



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

		if(activated && mAuthStateManager.getCurrent().hasClientSecretExpired())
		{
			startKeyKloackLogin();

		}
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

//			Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
//			startActivity(intent);

	//		FlutterFragment login = Flutter.createFragment("login");


			startKeyKloackLogin();



			return true;
		}
			case R.id.Logout:
			{

				endSession();

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

	private void startKeyKloackLogin()  {


		if (mAuthStateManager.getCurrent().isAuthorized()
				&& !mConfiguration.hasConfigurationChanged()) {
			Log.i(TAG, "User is already authenticated, proceeding to token activity");

			CommonUtils.setActivated(true, this);

			invalidateMenu();
			prepareStreamData();
		}
		else {


			//check if configuration is valid

			if(!mConfiguration.isValid())
			{
				Log.e(TAG,"configuration error, can not login");
				return;
			}

			if (mConfiguration.hasConfigurationChanged()) {
				// discard any existing authorization state due to the change of configuration
				Log.i(TAG, "Configuration change detected, discarding old state");
				mAuthStateManager.replace(new AuthState());
				mConfiguration.acceptConfiguration();
			}

			if (getIntent().getBooleanExtra(EXTRA_FAILED, false)) {
//            displayAuthCancelled();
			}





				startAuth();


		}


	}

	@MainThread
	void startAuth() {
		//displayLoading("Making authorization request");

		// WrongThread inference is incorrect for lambdas
		// noinspection WrongThread
		mExecutor.submit(this::doAuth);
	}


	private void recreateAuthorizationService() {
		if (mAuthService != null) {
			Log.i(TAG, "Discarding existing AuthService instance");
			mAuthService.dispose();
		}
		mAuthService = createAuthorizationService();
		mAuthRequest.set(null);
		mAuthIntent.set(null);
	}


	private AuthorizationService createAuthorizationService() {
		Log.i(TAG, "Creating authorization service");

		return new AuthorizationService(this);
	}


	@WorkerThread
	private void initializeAppAuth() {
		Log.i(TAG, "Initializing AppAuth");
		recreateAuthorizationService();

		if (mAuthStateManager.getCurrent().getAuthorizationServiceConfiguration() != null) {
			// configuration is already created, skip to client initialization
			Log.i(TAG, "auth config already established");
			initializeClient();
			return;
		}

		// if we are not using discovery, build the authorization service configuration directly
		// from the static configuration values.
		if (mConfiguration.getDiscoveryUri() == null) {
			Log.i(TAG, "Creating auth config from res/raw/auth_config.json");
			AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(
					mConfiguration.getAuthEndpointUri(),
					mConfiguration.getTokenEndpointUri(),
					mConfiguration.getRegistrationEndpointUri(),
					mConfiguration.getEndSessionEndpoint());

			mAuthStateManager.replace(new AuthState(config));
			initializeClient();
			return;
		}

		// WrongThread inference is incorrect for lambdas
		// noinspection WrongThread
		//runOnUiThread(() -> displayLoading("Retrieving discovery document"));
		Log.i(TAG, "Retrieving OpenID discovery doc");
		AuthorizationServiceConfiguration.fetchFromUrl(
				mConfiguration.getDiscoveryUri(),
				this::handleConfigurationRetrievalResult,
				mConfiguration.getConnectionBuilder());
	}

	@MainThread
	private void handleConfigurationRetrievalResult(
			AuthorizationServiceConfiguration config,
			AuthorizationException ex) {
		if (config == null) {
			Log.i(TAG, "Failed to retrieve discovery document", ex);
		//	displayError("Failed to retrieve discovery document: " + ex.getMessage(), true);
			return;
		}

		Log.i(TAG, "Discovery document retrieved");
		mAuthStateManager.replace(new AuthState(config));
		mExecutor.submit(this::initializeClient);
	}


	@WorkerThread
	private void doAuth() {
		try {
			mAuthIntentLatch.await();
		} catch (InterruptedException ex) {
			Log.w(TAG, "Interrupted while waiting for auth intent");
		}

		Intent intent = mAuthService.getAuthorizationRequestIntent(
				mAuthRequest.get(),
				mAuthIntent.get());
		startActivityForResult(intent, RC_AUTH);
	}

	@MainThread
	private void initializeAuthRequest() {
		createAuthRequest(null);
		warmUpBrowser();
		//  displayAuthOptions();
	}

	private void warmUpBrowser() {
		mAuthIntentLatch = new CountDownLatch(1);
		mExecutor.execute(() -> {
			Log.i(TAG, "Warming up browser instance for auth request");
			CustomTabsIntent.Builder intentBuilder =
					mAuthService.createCustomTabsIntentBuilder(mAuthRequest.get().toUri());
//            intentBuilder.setToolbarColor(getColorCompat(R.color.colorPrimary));
			mAuthIntent.set(intentBuilder.build());
			mAuthIntentLatch.countDown();
		});
	}

	private void createAuthRequest(@Nullable String loginHint) {
		Log.i(TAG, "Creating auth request for login hint: " + loginHint);
		AuthorizationRequest.Builder authRequestBuilder = new AuthorizationRequest.Builder(
				mAuthStateManager.getCurrent().getAuthorizationServiceConfiguration(),
				mClientId.get(),
				ResponseTypeValues.CODE,
				mConfiguration.getRedirectUri())
				.setScope(mConfiguration.getScope());

		if (!TextUtils.isEmpty(loginHint)) {
			authRequestBuilder.setLoginHint(loginHint);
		}

		mAuthRequest.set(authRequestBuilder.build());
	}


	@WorkerThread
	private void initializeClient() {
		if (mConfiguration.getClientId() != null) {
			Log.i(TAG, "Using static client ID: " + mConfiguration.getClientId());
			// use a statically configured client ID
			mClientId.set(mConfiguration.getClientId());
			runOnUiThread(this::initializeAuthRequest);
			return;
		}

		RegistrationResponse lastResponse =
				mAuthStateManager.getCurrent().getLastRegistrationResponse();
		if (lastResponse != null) {
			Log.i(TAG, "Using dynamic client ID: " + lastResponse.clientId);
			// already dynamically registered a client ID
			mClientId.set(lastResponse.clientId);
			runOnUiThread(this::initializeAuthRequest);
			return;
		}

		// WrongThread inference is incorrect for lambdas
		// noinspection WrongThread
		//runOnUiThread(() -> displayLoading("Dynamically registering client"));
		Log.i(TAG, "Dynamically registering client");

		RegistrationRequest registrationRequest = new RegistrationRequest.Builder(
				mAuthStateManager.getCurrent().getAuthorizationServiceConfiguration(),
				Collections.singletonList(mConfiguration.getRedirectUri()))
				.setTokenEndpointAuthenticationMethod(ClientSecretBasic.NAME)
				.build();

		mAuthService.performRegistrationRequest(
				registrationRequest,
				this::handleRegistrationResponse);
	}

	@MainThread
	private void handleRegistrationResponse(
			RegistrationResponse response,
			AuthorizationException ex) {
		mAuthStateManager.updateAfterRegistration(response, ex);
		if (response == null) {
			Log.i(TAG, "Failed to dynamically register client", ex);
			//displayErrorLater("Failed to register client: " + ex.getMessage(), true);
			return;
		}

		Log.i(TAG, "Dynamically registered client: " + response.clientId);
		mClientId.set(response.clientId);
		initializeAuthRequest();
	}

	@MainThread
	private void endSession() {
		AuthState currentState = mAuthStateManager.getCurrent();
		if(!currentState.isAuthorized())
		{
			CommonUtils.setActivated(false,this);
			invalidateMenu();
			prepareStreamData();
			return;
		}
		AuthorizationServiceConfiguration config =
				currentState.getAuthorizationServiceConfiguration();
		if (config.endSessionEndpoint != null) {
			Intent endSessionIntent = mAuthService.getEndSessionRequestIntent(
					new EndSessionRequest.Builder(config)
							.setIdTokenHint(currentState.getIdToken())
							.setPostLogoutRedirectUri(mConfiguration.getEndSessionRedirectUri())
							.build());
			startActivityForResult(endSessionIntent, END_SESSION_REQUEST_CODE);
		} else {
			signOut();
		}
	}
	@MainThread
	private void signOut() {
		// discard the authorization and token state, but retain the configuration and
		// dynamic client registration (if applicable), to save from retrieving them again.
		AuthState currentState = mAuthStateManager.getCurrent();
		AuthState clearedState =
				new AuthState(currentState.getAuthorizationServiceConfiguration());
		if (currentState.getLastRegistrationResponse() != null) {
			clearedState.update(currentState.getLastRegistrationResponse());
		}
		mAuthStateManager.replace(clearedState);

		

	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//displayAuthOptions();
		if (requestCode == END_SESSION_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			CommonUtils.setActivated(false,this);
			invalidateMenu();
			prepareStreamData();
			signOut();
		}
		else
		if (resultCode == RESULT_CANCELED) {
			//displayAuthCancelled();
		} else { //RC_AUTH
//

			AuthorizationResponse response = AuthorizationResponse.fromIntent(data);
			AuthorizationException ex = AuthorizationException.fromIntent(data);

			//get all data from keykloack

			if (response != null || ex != null) {
				mAuthStateManager.updateAfterAuthorization(response, ex);
			}

			if (response != null && response.authorizationCode != null) {
				// authorization code exchange is required
				mAuthStateManager.updateAfterAuthorization(response, ex);
				exchangeAuthorizationCode(response);
			} else if (ex != null) {
				Log.e(TAG,"Authorization flow failed: " + ex.getMessage());
			} else {
				Log.e(TAG,"No authorization state retained - reauthorization required");
			}

			//Log.d("LoginResult", response.authorizationCode != null ? response.authorizationCode :"no token");

			CommonUtils.setActivated(true, this);

			invalidateMenu();
			prepareStreamData();
		}
	}

	@MainThread
	private void exchangeAuthorizationCode(AuthorizationResponse authorizationResponse) {
		Log.d(TAG,"Exchanging authorization code");
		performTokenRequest(
				authorizationResponse.createTokenExchangeRequest(),
				this::handleCodeExchangeResponse);
	}

	@WorkerThread
	private void handleCodeExchangeResponse(
			@Nullable TokenResponse tokenResponse,
			@Nullable AuthorizationException authException) {

		mAuthStateManager.updateAfterTokenResponse(tokenResponse, authException);
		if (!mAuthStateManager.getCurrent().isAuthorized()) {
			final String message = "Authorization Code exchange failed"
					+ ((authException != null) ? authException.error : "");

			// WrongThread inference is incorrect for lambdas
			//noinspection WrongThread
			runOnUiThread(() -> Log.e(TAG,message));
		} else {
			runOnUiThread(this::fetchUserInfo);
		}
	}


	@MainThread
	private void performTokenRequest(
			TokenRequest request,
			AuthorizationService.TokenResponseCallback callback) {
		ClientAuthentication clientAuthentication;
		try {
			clientAuthentication = mAuthStateManager.getCurrent().getClientAuthentication();
		} catch (ClientAuthentication.UnsupportedAuthenticationMethod ex) {
			Log.d(TAG, "Token request cannot be made, client authentication for the token "
					+ "endpoint could not be constructed (%s)", ex);
			Log.d(TAG,"Client authentication method is unsupported");
			return;
		}

		mAuthService.performTokenRequest(
				request,
				clientAuthentication,
				callback);
	}

	@MainThread
	private void fetchUserInfo() {

		mAuthStateManager.getCurrent().performActionWithFreshTokens(mAuthService, this::fetchUserInfo);
	}

	@MainThread
	private void fetchUserInfo(String accessToken, String idToken, AuthorizationException ex) {
		if (ex != null) {
			Log.e(TAG, "Token refresh failed when fetching user info");
			mUserInfoJson.set(null);
//			runOnUiThread(this::displayAuthorized);
			return;
		}

		AuthorizationServiceDiscovery discovery =
				mAuthStateManager.getCurrent()
						.getAuthorizationServiceConfiguration()
						.discoveryDoc;

		Uri userInfoEndpoint =
				mConfiguration.getUserInfoEndpointUri() != null
						? Uri.parse(mConfiguration.getUserInfoEndpointUri().toString())
						: Uri.parse(discovery.getUserinfoEndpoint().toString());

		mExecutor.submit(() -> {
			try {
				HttpURLConnection conn = mConfiguration.getConnectionBuilder().openConnection(
						userInfoEndpoint);
				conn.setRequestProperty("Authorization", "Bearer " + accessToken);
				conn.setInstanceFollowRedirects(false);
				String response = Okio.buffer(Okio.source(conn.getInputStream()))
						.readString(Charset.forName("UTF-8"));
				mUserInfoJson.set(new JSONObject(response));
			} catch (IOException ioEx) {
				Log.e(TAG, "Network error when querying userinfo endpoint", ioEx);
//				showSnackbar("Fetching user info failed");
			} catch (JSONException jsonEx) {
				Log.e(TAG, "Failed to parse userinfo response");
//				showSnackbar("Failed to parse user info");
			}

//			runOnUiThread(this::displayAuthorized);
		});
	}




	private final class RecreateAuthRequestTask implements Runnable {

		private final AtomicBoolean mCanceled = new AtomicBoolean();

		@Override
		public void run() {
			if (mCanceled.get()) {
				return;
			}

			createAuthRequest(null);
			warmUpBrowser();
		}

		public void cancel() {
			mCanceled.set(true);
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
