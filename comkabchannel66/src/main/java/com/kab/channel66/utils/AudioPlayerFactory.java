package com.kab.channel66.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.kab.channel66.BaseBackgroundPlayer;
import com.kab.channel66.NativeBackgroundPlayer;
import com.kab.channel66.OpenPlayerBackgroundPlayer;

/**
 * Created by igal on 11/20/15.
 */
public class AudioPlayerFactory {
    Context mContext;

    enum PlayerType{
        Native,
        OpenPlayer
    }
    AudioPlayerFactory(Context context)
    {
      mContext = context;
    }

    static public BaseBackgroundPlayer GetAudioPlayer (Context context)
    {
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isNative = shared.getBoolean("isNative",true);

        if(isNative)
            return new NativeBackgroundPlayer();
        else
            return new OpenPlayerBackgroundPlayer();
    }
}
