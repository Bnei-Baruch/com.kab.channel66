
package com.kab.channel66;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.kab.channel66.utils.CallStateInterface;
import com.kab.channel66.utils.CallStateListener;
import com.kab.channel66.utils.Constants;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.interfaces.IVLCVout;
import org.videolan.libvlc.util.AndroidUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class VideoActivity extends Activity implements IVLCVout.Callback,IVLCVout.OnNewVideoLayoutListener,CallStateInterface {
    public final static String TAG = "LibVLCVideoActivity";

    public final static String LOCATION = "com.compdigitec.libvlcandroidsample.VideoActivity.location";

    private String mFilePath;

    // display surface
    private SurfaceView mSurface;
    private SurfaceHolder holder;
//    private CastStateListener mCastStateListener;

    // media player
    private LibVLC libvlc;
    private MediaPlayer mMediaPlayer = null;
    private int mVideoWidth;
    private int mVideoHeight;
    private final static int VideoSizeChanged = -1;

    private CallStateListener calllistener;
    private TelephonyManager telephony;
    private Notification notification;


    /*************
     * Activity
     *************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videoview);

        // Receive path to play from intent
        Intent intent = getIntent();
        if(intent!=null && intent.getExtras()!=null) {
            mFilePath = intent.getExtras().getString(LOCATION);
        }
        else if (mFilePath==null)
        {
            mFilePath = savedInstanceState.getString("url");
        }


       
        //Log.d(TAG, "Playing back " + mFilePath);

        mSurface = (SurfaceView) findViewById(R.id.surface_view);
        holder = mSurface.getHolder();

        calllistener = new CallStateListener(this);
        telephony = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE); //TelephonyManager object
        telephony.listen(calllistener, PhoneStateListener.LISTEN_CALL_STATE); //Register our listener with TelephonyManager

        //holder.addCallback(this);




    }



    @Override
    public void onStart() {

        super.onStart();
    }



    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        ViewGroup.LayoutParams videoParams = mSurface.getLayoutParams();
        videoParams.width = displayMetrics.widthPixels;
        videoParams.height = displayMetrics.heightPixels;
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.setWindowSize(videoParams.width,videoParams.height);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mFilePath!=null) {
            if(mMediaPlayer!=null) {
                final IVLCVout vout = mMediaPlayer.getVLCVout();
                if(!vout.areViewsAttached())
                {
                    vout.setVideoView(mSurface);
                    vout.attachViews(this);
                }

                vout.addCallback(this);

            }
            else
                createPlayer(mFilePath);
        }
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);


    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putString("url", mFilePath);

        // etc.
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.

        mFilePath = savedInstanceState.getString("url");
    }


    @Override
    protected void onPause() {
        super.onPause();

    }


    @Override
    protected void onStop() {
        super.onStop();
       // releasePlayer();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();

        String channelId;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = createNotificationChannel("service", "video");
        } else {
            // If earlier version channel ID is not used
            // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
            channelId = "";
        }
        Intent notificationIntent = new Intent(VideoActivity.this, VideoActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(VideoActivity.this, 0,
                notificationIntent, 0);


        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.icon);

        notification = new NotificationCompat.Builder(VideoActivity.this,channelId)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Video playing in background")
                .setContentText("Click to Access App")
                .setContentIntent(pendingIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(true)
                .build();
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, notification);


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel(String channelId , String channelName){
        NotificationChannel chan = new NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_DEFAULT);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        chan.setShowBadge(true);

        NotificationManager service = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return channelId;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
        holder = null;
        telephony.listen(calllistener, PhoneStateListener.LISTEN_NONE);
    }

    /*************
     * Surface
     *************/
    private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        if(holder == null || mSurface == null)
            return;

        // get screen size
        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        // force surface buffer size
        holder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        LayoutParams lp = mSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();
    }

    /*************
     * Player
     *************/

    private void createPlayer(String media) {
        releasePlayer();
        try {
            if (media.length() > 0) {
                Toast toast = Toast.makeText(this, media, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                        0);
                toast.show();
            }

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            ViewGroup.LayoutParams videoParams = mSurface.getLayoutParams();
            videoParams.width = displayMetrics.widthPixels;
            videoParams.height = displayMetrics.heightPixels;


            mFilePath = media;
            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            options.add("--http-reconnect");
            options.add("--network-caching=2000");
            libvlc = new LibVLC(getBaseContext());

            //libvlc.setOnNativeCrashListener(this);


            holder.setKeepScreenOn(true);

            // Create media player
            mMediaPlayer = new MediaPlayer(libvlc);

            mMediaPlayer.setEventListener(mPlayerListener);
            mMediaPlayer.setVideoScale(MediaPlayer.ScaleType.SURFACE_4_3);
            // Set up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setWindowSize(videoParams.width,videoParams.height);
            vout.setVideoView(mSurface);
            vout.attachViews(this);
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(this);



            Media m = new Media(libvlc,AndroidUtil.LocationToUri(media));
            m.addOption(":aout=opensles");
            m.addOption(":audio-time-stretch");
            m.addOption(":http-reconnect");
            m.addOption(":network-caching=2000");

            mMediaPlayer.setMedia(m);

            mMediaPlayer.play();
        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    // TODO: handle this cleaner
    private void releasePlayer() {
        if (libvlc == null)
            return;
        mFilePath = "";
        mMediaPlayer.stop();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();
        //holder = null;
        libvlc.release();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
        mMediaPlayer.release();

    }

    /*************
     * Events
     *************/

    private MediaPlayer.EventListener mPlayerListener = new MyPlayerListener(this);



    @Override
    public void onSurfacesCreated(IVLCVout vout) {
        Log.d(TAG,"onSurfacesCreated");
    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vout) {

    }



    @Override
    public void PausePlay() {

        try {
            if (mMediaPlayer != null && mMediaPlayer.isPlaying())
                mMediaPlayer.stop();
        }
        catch (IllegalStateException e)
        {//report crash to crashlytics on this crash
          //  Crashlytics.getInstance().answers.onException(new Crash.FatalException(e.getMessage()));
        }
    }



    @Override
    public void ResumePlay() {

        //int state = mMediaPlayer.getPlayerState();
        if(mMediaPlayer!=null && !mMediaPlayer.isPlaying() )//&& mMediaPlayer.getPlayerState()== MediaPlayer.Event.Paused)
           mMediaPlayer.play();
    }

    @Override
    public Boolean isPaused() {

        if(mMediaPlayer!=null &&! mMediaPlayer.isReleased() && !mMediaPlayer.isPlaying() )
            return true;
        else
            return false;
    }

    @Override
    public void onNewVideoLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
//        if (width * height == 0)
//            return;

        Display display = getWindowManager(). getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);


        // store video size
        if(display.getRotation() == Surface.ROTATION_0 ||display.getRotation() == Surface.ROTATION_180 ) {
            mVideoHeight = size.x;
            mVideoWidth = size.y;
        }
        else
        {
            mVideoWidth = size.x;
            mVideoHeight = size.y;


        }

        Log. e("Width", "" + width);
        Log. e("height", "" + height);
        vlcVout.setWindowSize(mVideoWidth,mVideoHeight);
        setSize(mVideoWidth, mVideoHeight);
    }

//    @Override
//    public void onNativeCrash() {
//
//    }


    private static class MyPlayerListener implements MediaPlayer.EventListener {
        private WeakReference<VideoActivity> mOwner;

        public MyPlayerListener(VideoActivity owner) {
            mOwner = new WeakReference<VideoActivity>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            VideoActivity player = mOwner.get();

            switch(event.type) {
                case MediaPlayer.Event.EndReached:
                  //  Log.d(TAG, "MediaPlayerEndReached");
                  //  player.releasePlayer();
                    break;
                case MediaPlayer.Event.Playing:
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }



    }

//    @Override
//    public void eventHardwareAccelerationError() {
//        // Handle errors with hardware acceleration
//        //Log.e(TAG, "Error with hardware acceleration");
//        this.releasePlayer();
//        Toast.makeText(this, "Error with hardware acceleration", Toast.LENGTH_LONG).show();
//    }


}
