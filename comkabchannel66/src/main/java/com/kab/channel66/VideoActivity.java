
package com.kab.channel66;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteButton;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.kab.channel66.utils.CallStateInterface;
import com.kab.channel66.utils.CallStateListener;
import com.kab.channel66.utils.Constants;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.AndroidUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class VideoActivity extends AppCompatActivity implements IVLCVout.Callback, LibVLC.OnNativeCrashListener,CallStateInterface {
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
    private SessionManagerListener<CastSession> mSessionManagerListener;
    private CastSession mCastSession;
    private MediaRouteButton mMediaRouteButton;
    private CastContext mCastContext;
    private MediaMetadata movieMetadata;

    /*************
     * Activity
     *************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videoview);

        // Receive path to play from intent
        Intent intent = getIntent();
        mFilePath = intent.getExtras().getString(LOCATION);


        movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);

        movieMetadata.putString(MediaMetadata.KEY_TITLE, "Sviva Tova");
        movieMetadata.putString(MediaMetadata.KEY_SUBTITLE, "Kabbalah");
       
        //Log.d(TAG, "Playing back " + mFilePath);

        mSurface = (SurfaceView) findViewById(R.id.surface_view);
        holder = mSurface.getHolder();

        calllistener = new CallStateListener(this);
        telephony = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE); //TelephonyManager object
        telephony.listen(calllistener, PhoneStateListener.LISTEN_CALL_STATE); //Register our listener with TelephonyManager

        //holder.addCallback(this);

        mMediaRouteButton = (MediaRouteButton) findViewById(R.id.media_route_button);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mMediaRouteButton);

        mCastContext = CastContext.getSharedInstance(this);

        setupCastListener();

        setupActionBar();


    }

    @Override
    public void onStart() {
        CastContext.getSharedInstance(getApplicationContext()).getSessionManager()
                .addSessionManagerListener(mSessionManagerListener, CastSession.class);
        super.onStart();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.player_menu, menu);
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu,
                R.id.media_route_menu_item);
        return true;
    }



    private void setupCastListener() {
        mSessionManagerListener = new SessionManagerListener<CastSession>() {

            @Override
            public void onSessionEnded(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionResumed(CastSession session, boolean wasSuspended) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionResumeFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarted(CastSession session, String sessionId) {
                onApplicationConnected(session);
            }

            @Override
            public void onSessionStartFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarting(CastSession session) {
            }

            @Override
            public void onSessionEnding(CastSession session) {
            }

            @Override
            public void onSessionResuming(CastSession session, String sessionId) {
            }

            @Override
            public void onSessionSuspended(CastSession session, int reason) {
            }

            private void onApplicationConnected(CastSession castSession) {
                mCastSession = castSession;

                mMediaPlayer.pause();
                loadRemoteMedia(0, true);



//                updatePlayButton(mPlaybackState);
                invalidateOptionsMenu();
                return;
            }

            private void onApplicationDisconnected() {
//                updatePlaybackLocation(PlaybackLocation.LOCAL);
//                mPlaybackState = PlaybackState.IDLE;
//                mLocation = PlaybackLocation.LOCAL;
//                updatePlayButton(mPlaybackState);
                invalidateOptionsMenu();
            }
        };
    }


        private void loadRemoteMedia(int position, boolean autoPlay) {
            if (mCastSession == null) {
                return;
            }
            final RemoteMediaClient remoteMediaClient = mCastSession.getRemoteMediaClient();
            if (remoteMediaClient == null) {
                return;
            }
            remoteMediaClient.registerCallback(new RemoteMediaClient.Callback() {
                @Override
                public void onStatusUpdated() {
                    Intent intent = new Intent(VideoActivity.this, ExpandedControlsActivity.class);
                    startActivity(intent);
                    remoteMediaClient.unregisterCallback(this);
                }
            });
//            remoteMediaClient.load(MediaInfo.CREATOR.newArray(1)[0],
//                    new MediaLoadOptions.Builder()
//                            .setAutoplay(autoPlay)
//                            .setPlayPosition(position).build());

            MediaInfo mediaInfo = new MediaInfo.Builder(mFilePath)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType("videos/mp4")
                    .setMetadata(movieMetadata)
                    .build();

            remoteMediaClient.load(mediaInfo,
                    new MediaLoadOptions.Builder()
                            .setAutoplay(autoPlay)
                            .setPlayPosition(position).build());
        }

//        private void updatePlayButton(PlaybackState state) {
//            Log.d(TAG, "Controls: PlayBackState: " + state);
//            boolean isConnected = (mCastSession != null)
//                    && (mCastSession.isConnected() || mCastSession.isConnecting());
//            mControllers.setVisibility(isConnected ? View.GONE : View.VISIBLE);
//            mPlayCircle.setVisibility(isConnected ? View.GONE : View.VISIBLE);
//            switch (state) {
//                case PLAYING:
//                    mLoading.setVisibility(View.INVISIBLE);
//                    mPlayPause.setVisibility(View.VISIBLE);
//                    mPlayPause.setImageDrawable(
//                            getResources().getDrawable(R.drawable.ic_av_pause_dark));
//                    mPlayCircle.setVisibility(isConnected ? View.VISIBLE : View.GONE);
//                    break;
//                case IDLE:
//                    mPlayCircle.setVisibility(View.VISIBLE);
//                    mControllers.setVisibility(View.GONE);
//                    mCoverArt.setVisibility(View.VISIBLE);
//                    mVideoView.setVisibility(View.INVISIBLE);
//                    break;
//                case PAUSED:
//                    mLoading.setVisibility(View.INVISIBLE);
//                    mPlayPause.setVisibility(View.VISIBLE);
//                    mPlayPause.setImageDrawable(
//                            getResources().getDrawable(R.drawable.ic_av_play_dark));
//                    mPlayCircle.setVisibility(isConnected ? View.VISIBLE : View.GONE);
//                    break;
//                case BUFFERING:
//                    mPlayPause.setVisibility(View.INVISIBLE);
//                    mLoading.setVisibility(View.VISIBLE);
//                    break;
//                default:
//                    break;
//            }
//        }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Video");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    protected void onResume() {
        super.onResume();
        createPlayer(mFilePath);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);

//        mCastContext.addCastStateListener(mCastStateListener);
        mCastContext.getSessionManager().addSessionManagerListener(
                mSessionManagerListener, CastSession.class);
        if (mCastSession == null) {
            mCastSession = CastContext.getSharedInstance(this).getSessionManager()
                    .getCurrentCastSession();
        }
    }





    @Override
    protected void onPause() {
        super.onPause();

//        mCastContext.removeCastStateListener(mCastStateListener);
        mCastContext.getSessionManager().removeSessionManagerListener(
                mSessionManagerListener, CastSession.class);

        //releasePlayer();
    }


    @Override
    protected void onStop() {
        super.onStop();
       // releasePlayer();
        final IVLCVout vout = mMediaPlayer.getVLCVout();
        vout.removeCallback(this);
        vout.detachViews();

        Intent notificationIntent = new Intent(VideoActivity.this, VideoActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(VideoActivity.this, 0,
                notificationIntent, 0);


        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.icon);

        notification = new NotificationCompat.Builder(VideoActivity.this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Video playing in background")
                .setContentText("Click to Access App")
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, notification);

        CastContext.getSharedInstance(getApplicationContext()).getSessionManager()
                .removeSessionManagerListener(mSessionManagerListener, CastSession.class);
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

            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            options.add("--http-reconnect");
            options.add("--network-caching=2000");
            libvlc = new LibVLC(this,options);
            libvlc.setOnNativeCrashListener(this);

            holder.setKeepScreenOn(true);

            // Create media player
            mMediaPlayer = new MediaPlayer(libvlc);

            mMediaPlayer.setEventListener(mPlayerListener);

            // Set up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(mSurface);
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(this);
            vout.attachViews();

            Media m = new Media(libvlc,AndroidUtil.LocationToUri(media));

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
    public void onNewLayout(IVLCVout vout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {
        if (width * height == 0)
            return;

        // store video size
        mVideoWidth = width;
        mVideoHeight = height;
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    public void onSurfacesCreated(IVLCVout vout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vout) {

    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {

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
    public void onNativeCrash() {

    }


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
