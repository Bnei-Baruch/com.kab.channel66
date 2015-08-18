/*
 * Copyright (C) 2011 VOV IO (http://vov.io/)
 */

package com.kab.channel66;



import com.google.analytics.tracking.android.EasyTracker;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class VideoPlayerActivity extends Activity implements OnCompletionListener {

	private String mPath;
	private String mTitle;
	private com.kab.channel66.VideoView mVideoView;
	private View mVolumeBrightnessLayout;
	private ImageView mOperationBg;
	private ImageView mOperationPercent;
	private AudioManager mAudioManager;
	/** 最大声音 */
	private int mMaxVolume;
	/** 当前声音 */
	private int mVolume = -1;
	/** 当前亮度 */
	private float mBrightness = -1f;
	/** 当前缩放模式 */
	//private int mLayout = VideoView.VIDEO_LAYOUT_ZOOM;
	private int mLayout = VideoView.VIDEO_LAYOUT_ZOOM;
	private GestureDetector mGestureDetector;
	private MediaController mMediaController;
	private ProgressDialog progress;
	private WakeLock mWakeLock;
	private SurfaceHolder holder;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		 if (!LibsChecker.checkVitamioLibs(this))
				return;
		
			
		
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "My Tag");
		mWakeLock.acquire();
		
		Intent intent = getIntent();
		mPath = intent.getStringExtra("path");
		mTitle = intent.getStringExtra("title");
		if (TextUtils.isEmpty(mPath))
			mPath = Environment.getExternalStorageDirectory() + "/video/你太猖狂.flv";
		else if (intent.getData() != null)
			mPath = intent.getData().toString();

		setContentView(R.layout.videoview);
		mVideoView = (com.kab.channel66.VideoView) findViewById(R.id.surface_view);
		holder = mVideoView.getHolder();
		//holder.addCallback( this);
		holder.setFormat(PixelFormat.RGBA_8888);  
		mVolumeBrightnessLayout = findViewById(R.id.operation_volume_brightness);
		mOperationBg = (ImageView) findViewById(R.id.operation_bg);
		mOperationPercent = (ImageView) findViewById(R.id.operation_percent);

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		if (mPath.startsWith("http:"))
			mVideoView.setVideoURI(Uri.parse(mPath));
		else
			mVideoView.setVideoPath(mPath);
		//
		mVideoView.setOnCompletionListener(this);

		mMediaController = new MediaController(this);
		//设置显示名称
		mMediaController.setFileName(mTitle);
		mVideoView.setMediaController(mMediaController);
		
		progress = new ProgressDialog(VideoPlayerActivity.this);
		progress.setMax(100);
		
		mVideoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
		
		public void onBufferingUpdate(MediaPlayer arg0, int percent) {
			// TODO Auto-generated method stub
			int multiplier;
			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(VideoPlayerActivity.this);
        	if(shared.getBoolean("quality", false))
				multiplier= 1;
			else
				multiplier = 1;
			if(!progress.isShowing())
				progress.show();
			progress.setProgress(percent);
			progress.setMessage("Buffering "+Integer.toString(percent)+"%");
			if(percent*multiplier>=92){
				progress.hide();
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
				if (mLayout == VideoView.VIDEO_LAYOUT_ZOOM) //this is done due to a bug on some devices that the video won't start if layout is not zoom
					mLayout = VideoView.VIDEO_LAYOUT_SCALE;
				mVideoView.setVideoLayout(mLayout, 0);
				arg0.start();
				
			}
		}
	});
	
		mVideoView.requestFocus();

		mGestureDetector = new GestureDetector(this, new MyGestureListener());

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mVideoView != null)
			mVideoView.pause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mVideoView != null)
			mVideoView.resume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mVideoView != null)
			mVideoView.stopPlayback();
		if(mWakeLock!=null)
			mWakeLock.release();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event))
			return true;

		// 处理手势结束
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_UP:
			endGesture();
			break;
		}

		return super.onTouchEvent(event);
	}

	/** 手势结束 */
	private void endGesture() {
		mVolume = -1;
		mBrightness = -1f;

		// 隐藏
		mDismissHandler.removeMessages(0);
		mDismissHandler.sendEmptyMessageDelayed(0, 500);
	}

	private class MyGestureListener extends SimpleOnGestureListener {

		/** 双击 */
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			if (mLayout == VideoView.VIDEO_LAYOUT_ZOOM)
				mLayout = VideoView.VIDEO_LAYOUT_ORIGIN;
			else
				mLayout++;
			if (mVideoView != null)
				mVideoView.setVideoLayout(mLayout, 0);
			return true;
		}

		/** 滑动 */
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
			float mOldX = e1.getX(), mOldY = e1.getY();
			int y = (int) e2.getRawY();
			Display disp = getWindowManager().getDefaultDisplay();
			int windowWidth = disp.getWidth();
			int windowHeight = disp.getHeight();

			if (mOldX > windowWidth * 4.0 / 5)// 右边滑动
				onVolumeSlide((mOldY - y) / windowHeight);
			else if (mOldX < windowWidth / 5.0)// 左边滑动
				onBrightnessSlide((mOldY - y) / windowHeight);

			return super.onScroll(e1, e2, distanceX, distanceY);
		}
	}

	/** 定时隐藏 */
	private Handler mDismissHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mVolumeBrightnessLayout.setVisibility(View.GONE);
		}
	};

	/**
	 * 滑动改变声音大小
	 * 
	 * @param percent
	 */
	private void onVolumeSlide(float percent) {
		if (mVolume == -1) {
			mVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			if (mVolume < 0)
				mVolume = 0;

			// 显示
			mOperationBg.setImageResource(R.drawable.video_volumn_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}

		int index = (int) (percent * mMaxVolume) + mVolume;
		if (index > mMaxVolume)
			index = mMaxVolume;
		else if (index < 0)
			index = 0;

		// 变更声音
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);

		// 变更进度条
		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = findViewById(R.id.operation_full).getLayoutParams().width * index / mMaxVolume;
		mOperationPercent.setLayoutParams(lp);
	}

	/**
	 * 滑动改变亮度
	 * 
	 * @param percent
	 */
	private void onBrightnessSlide(float percent) {
		if (mBrightness < 0) {
			mBrightness = getWindow().getAttributes().screenBrightness;
			if (mBrightness <= 0.00f)
				mBrightness = 0.50f;
			if (mBrightness < 0.01f)
				mBrightness = 0.01f;

			// 显示
			mOperationBg.setImageResource(R.drawable.video_brightness_bg);
			mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
		}
		WindowManager.LayoutParams lpa = getWindow().getAttributes();
		lpa.screenBrightness = mBrightness + percent;
		if (lpa.screenBrightness > 1.0f)
			lpa.screenBrightness = 1.0f;
		else if (lpa.screenBrightness < 0.01f)
			lpa.screenBrightness = 0.01f;
		getWindow().setAttributes(lpa);

		ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
		lp.width = (int) (findViewById(R.id.operation_full).getLayoutParams().width * lpa.screenBrightness);
		mOperationPercent.setLayoutParams(lp);
	}

	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if (mVideoView != null)
			mVideoView.setVideoLayout(mLayout, 0);
		super.onConfigurationChanged(newConfig);
		
		int ot = getResources().getConfiguration().orientation;
		switch (ot)
		{
		case Configuration.ORIENTATION_LANDSCAPE:
			 EasyTracker.getTracker().trackEvent("Video player", "orientation", "LANDSCAPE",0L);
			  
			break;
		case Configuration.ORIENTATION_PORTRAIT:
			 EasyTracker.getTracker().trackEvent("Video player", "orientation", "PORTRAIT",0L);
			   
			break;
			default:
				break;
		}
	}

	@Override
	public void onCompletion(MediaPlayer player) {
		onBackPressed();
		//finish();
	}
	@Override
	public void onStart() {
	  super.onStart();
	   // The rest of your onStart() code.
	  EasyTracker.getInstance().setContext(this.getApplicationContext());
	  EasyTracker.getInstance().activityStart(this);
	 
	}


	@Override
	public void onStop() {
	  super.onStop();
	   // The rest of your onStop() code.
	  EasyTracker.getInstance().activityStop(this); // Add this method.
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.video_menu, menu);
	    return true;
		 
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.questions:
	        	Questions question = new Questions(VideoPlayerActivity.this);
	        	question.show();
	        	
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	  
	}

}
