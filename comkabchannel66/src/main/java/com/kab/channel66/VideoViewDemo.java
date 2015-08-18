/*
 * Copyright (C) 2011 VOV IO (http://vov.io/)
 */

package com.kab.channel66;

import com.kab.channel66.R;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
//import io.vov.vitamio.MediaPlayer.OnSubtitleUpdateListener;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Layout;
//import android.util.Log;
import com.apphance.android.Log;
import android.view.View;

public class VideoViewDemo extends Activity {

	private String path = "mms://wms1.il.kab.tv/heb";
	private com.kab.channel66.VideoView mVideoView;
	private ProgressDialog progress;
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.videoview);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		Intent input = getIntent();
		String pathinput = input.getStringExtra("path");
		path = pathinput;
		
		
		mVideoView = (com.kab.channel66.VideoView) findViewById(R.id.surface_view);
		mVideoView.setVideoPath(path);
//		mVideoView.setOnErrorListener(new OnErrorListener() {
//			
//			@Override
//			public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
//				// TODO Auto-generated method stub
//				AlertDialog ad = new AlertDialog.Builder(VideoViewDemo.this).create();
//				ad.setTitle("Stream probelm, please try again later");
//				ad.setButton("OK", new DialogInterface.OnClickListener() {
//        	    			   public void onClick(DialogInterface dialog, int which) {
//				finish();
//        	    			   }
//				});
//				ad.show();
//				return true;
//			}
//		});
		progress = new ProgressDialog(VideoViewDemo.this);
		progress.setMax(100);
		
//		mVideoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
					
//			public void onBufferingUpdate(MediaPlayer arg0, int percent) {
//				// TODO Auto-generated method stub
//				if(!progress.isShowing())
//					progress.show();
//				progress.setProgress(percent);
//				progress.setMessage("Buffering "+Integer.toString(percent)+"%");
//				if(percent>98){
//					progress.hide();
//					setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
//					
//				}
//			}
//		});
		
		
//		mVideoView.setOnPreparedListener(new OnPreparedListener() {
//			
//			public void onPrepared(MediaPlayer mp) {
//				//mVideoView.setSubPath("/sdcard/Video/238_mongoid.srt");
//				mVideoView.setSubShown(true);
//				
//			}
//		});
//		mVideoView.setOnSubtitleUpdateListener(new OnSubtitleUpdateListener() {
//			
//			public void onSubtitleUpdate(String arg0) {
//				Log.i("VitamioDemo", arg0);
//			}
//
//			
//			public void onSubtitleUpdate(byte[] arg0, int arg1, int arg2) {
//			}
//		});
		//mVideoView.setMediaController(new MediaController(this));
		mVideoView.requestFocus();
	}

	private int mLayout = VideoView.VIDEO_LAYOUT_ZOOM;
	private int mLayout2 = VideoView.VIDEO_LAYOUT_ORIGIN;

//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//		
//		
//		if (mVideoView != null)
//		{
//			int ot = getResources().getConfiguration().orientation;
//			switch (ot)
//			{
//			case Configuration.ORIENTATION_LANDSCAPE:
//				mVideoView.setVideoLayout(mLayout, 0);
//				break;
//			case Configuration.ORIENTATION_PORTRAIT:
//				if(!mVideoView.isPlaying())
//					mVideoView.setVideoLayout(mLayout, 0);
//				else
//					mVideoView.setVideoLayout(mLayout2, 0);
//				break;
//				default:
//					break;
//			}
//			//
//			
//		}
//		super.onConfigurationChanged(newConfig);
//	}
//	
	
	
}
