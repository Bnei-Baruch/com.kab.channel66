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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;
import org.videolan.libvlc.util.AndroidUtil;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import de.greenrobot.event.EventBus;

/**
 * This class wraps a libvlc mediaplayer instance.
 */
public class VLCMediaPlayer implements TomahawkMediaPlayer {

    private static final String TAG = VLCMediaPlayer.class.getSimpleName();
    private static final String EQUALIZER_ENABLED_PREFERENCE_KEY = "EQUALIZER_ENABLED_PREFERENCE_KEY";
    private static final String EQUALIZER_VALUES_PREFERENCE_KEY = "EQUALIZER_VALUES_PREFERENCE_KEY";

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

    private VLCMediaPlayer() {
        ArrayList<String> options = new ArrayList<>();
        options.add("--http-reconnect");
        options.add("--network-caching=2000");
        mLibVLC = new LibVLC(options);
        mMediaPlayer = new MediaPlayer(mLibVLC);
        SharedPreferences pref =
                PreferenceManager.getDefaultSharedPreferences(MyApplication.getMyApp());
        if (pref.getBoolean(EQUALIZER_ENABLED_PREFERENCE_KEY, false)) {
            MediaPlayer.Equalizer equalizer = MediaPlayer.Equalizer.create();
           float preAmp =  pref.getFloat(EQUALIZER_VALUES_PREFERENCE_KEY, 0f);
            equalizer.setPreAmp(preAmp);


            mMediaPlayer.setEqualizer(equalizer);
        }
        mMediaPlayer.setEventListener(mMediaPlayerListener);
        EventBus.getDefault().register(this);
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
        }
    }

    /**
     * Pause playing the current {@link org.tomahawk.libtomahawk.collection.Track}
     */
    @Override
    public void pause() throws IllegalStateException {
        Log.d(TAG, "pause()");
        if (getMediaPlayerInstance().isPlaying()) {
            getMediaPlayerInstance().pause();
        }
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
        getMediaPlayerInstance().stop();
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