/* == This file is part of Tomahawk Player - <http://tomahawk-player.org> ===
 *
 *   Copyright 2014, Enno Gottschalk <mrmaffen@googlemail.com>
 *
 *   Tomahawk is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   Tomahawk is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with Tomahawk. If not, see <http://www.gnu.org/licenses/>.
 */
package com.kab.channel66;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.kab.channel66.utils.CallStateListener;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.AndroidUtil;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

import static com.kab.channel66.MyApplication.getMyApp;

/**
 * This class wraps a libvlc mediaplayer instance.
 */
public class VLCMediaPlayer implements TomahawkMediaPlayer {

    private static final String TAG = VLCMediaPlayer.class.getSimpleName();
    private static final String EQUALIZER_ENABLED_PREFERENCE_KEY = "EQUALIZER_ENABLED_PREFERENCE_KEY";
    private static final String EQUALIZER_VALUES_PREFERENCE_KEY = "EQUALIZER_VALUES_PREFERENCE_KEY";

    private CallStateListener calllistener;
    TelephonyManager telephony;

    private static class Holder {

        private static final VLCMediaPlayer instance = new VLCMediaPlayer();

    }

    private LibVLC mLibVLC;

    private MediaPlayer mMediaPlayer;

    private TomahawkMediaPlayerCallback mMediaPlayerCallback;

    private String mPreparedQuery;

    private String mPreparingQuery;

    private boolean supportSeek = false;

//    private final ConcurrentHashMap<Result, String> mTranslatedUrls
//            = new ConcurrentHashMap<>();

    private final MediaPlayer.EventListener mMediaPlayerListener = new MediaPlayer.EventListener() {
        @Override
        public void onEvent(MediaPlayer.Event event) {

            Log.d("vlc event",event.toString()+" type: "+event.type);
                switch (event.type) {
                    case MediaPlayer.Event.EncounteredError:
                        Log.d(TAG, "onError()");
                        mPreparedQuery = null;
                        mPreparingQuery = null;
                        mMediaPlayerCallback.onError("MediaPlayerEncounteredError");
                        break;
                    case MediaPlayer.Event.EndReached:
                        Log.d(TAG, "onCompletion()");
                        mMediaPlayerCallback.onCompletion(mPreparedQuery);
                        break;

                }
        }
    };


    Handler handler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message msg)
        {

            Bundle bundle = msg.getData();

            // Do something with message contents
        }
    };



    private VLCMediaPlayer() {
        ArrayList<String> options = new ArrayList<>();
        options.add("--http-reconnect");
        options.add("--network-caching=4000");
        mLibVLC = new LibVLC(getMyApp().getApplicationContext(),options);
        mMediaPlayer = new MediaPlayer(mLibVLC);
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(getMyApp());
        if (pref.getBoolean(EQUALIZER_ENABLED_PREFERENCE_KEY, false)) {
            MediaPlayer.Equalizer equalizer = MediaPlayer.Equalizer.create();
           float preAmp =  pref.getFloat(EQUALIZER_VALUES_PREFERENCE_KEY, 0f);
            equalizer.setPreAmp(preAmp);


            mMediaPlayer.setEqualizer(equalizer);

        }
        mMediaPlayer.setEventListener(mMediaPlayerListener);
        EventBus.getDefault().register(this);


        telephony = (TelephonyManager) getMyApp().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE); //TelephonyManager object
    }

    public LibVLC getLibVlcInstance() {
        return mLibVLC;
    }

    public MediaPlayer getMediaPlayerInstance() {
        return mMediaPlayer;
    }

    public static VLCMediaPlayer get() {
        return Holder.instance;
    }

    public void setCalllistener (CallStateListener listener){calllistener = listener;}

    @SuppressWarnings("unused")
    public void onEventAsync(String event) {

        if (mPreparingQuery != null
                && event == mPreparingQuery) {
            prepare(mPreparingQuery);
        }
    }

    /**
     * Start playing the previously prepared {@link org.tomahawk.libtomahawk.collection.Track}
     */
    @Override
    public void start() throws IllegalStateException {
        Log.d(TAG, "start()");
        if (!getMediaPlayerInstance().isPlaying()) {
            getMediaPlayerInstance().play();
            telephony.listen(calllistener, PhoneStateListener.LISTEN_CALL_STATE); //Register our listener with TelephonyManager

        }
    }

    /**
     * Pause playing the current {@link org.tomahawk.libtomahawk.collection.Track}
     */
    @Override
    public void pause() throws IllegalStateException {
        Log.d(TAG, "pause()");
        if (getMediaPlayerInstance().isPlaying()) {
            getMediaPlayerInstance().stop();

        }
    }

    @Override
    public void stop() throws IllegalStateException {
        Log.d(TAG, "pause()");
        if (getMediaPlayerInstance().isPlaying()) {
            getMediaPlayerInstance().pause();
            getMediaPlayerInstance().release();

        }
        telephony.listen(calllistener, PhoneStateListener.LISTEN_NONE); //Register our listener with TelephonyManager
    }

    /**
     * Seek to the given playback position (in ms)
     */
    @Override
    public void seekTo(int msec) throws IllegalStateException {
        Log.d(TAG, "seekTo()");

      if(supportSeek) {
            getMediaPlayerInstance().setTime(msec);
        }
    }

    /**
     * Prepare the given url
     */
    private TomahawkMediaPlayer prepare(String query) {
        release();
        mPreparedQuery = null;
        mPreparingQuery = query;

        Media media = new Media(mLibVLC, AndroidUtil.LocationToUri(query));
        media.setEventListener(new Media.EventListener() {
            @Override
            public void onEvent(Media.Event event) {
                Log.d("media vlc event",event.toString()+" type: "+event.type);
            }
        });
        getMediaPlayerInstance().setMedia(media);
        Log.d(TAG, "onPrepared()");
        mPreparedQuery = mPreparingQuery;
        mPreparingQuery = null;
        mMediaPlayerCallback.onPrepared(mPreparedQuery);
        return this;
    }

    /**
     * Prepare the given url
     */
    @Override
    public TomahawkMediaPlayer prepare(Application application, String query,
            TomahawkMediaPlayerCallback callback) {
        Log.d(TAG, "prepare()");
        mMediaPlayerCallback = callback;
        return prepare(query);
    }

    @Override
    public void release() {
        Log.d(TAG, "release()");
        mPreparedQuery = null;
        mPreparingQuery = null;
        try {
            getMediaPlayerInstance().stop();
        }
        catch (IllegalStateException e)
        {
            Log.d("VLCMediaPlayer",e.getStackTrace().toString());
        }
        telephony.listen(calllistener, PhoneStateListener.LISTEN_NONE); //Register our listener with TelephonyManager
    }

    /**
     * @return the current track position
     */
    @Override
    public int getPosition() {
        if (mPreparedQuery != null) {
            return (int) getMediaPlayerInstance().getTime();
        } else {
            return 0;
        }
    }

    @Override
    public boolean isPlaying(String query) {
        return isPrepared(query) && getMediaPlayerInstance().isPlaying();
    }

    @Override
    public boolean isPreparing(String query) {
        return mPreparingQuery != null && mPreparingQuery == query;
    }

    @Override
    public boolean isPrepared(String query) {
        return mPreparedQuery != null && mPreparedQuery == query;
    }
}
