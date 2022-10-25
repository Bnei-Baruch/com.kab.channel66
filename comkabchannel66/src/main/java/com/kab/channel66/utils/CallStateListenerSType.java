package com.kab.channel66.utils;

//import io.vov.vitamio.utils.Log;
import android.os.Build;
import android.telephony.TelephonyCallback;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

/**
 * Listener to detect incoming calls. 
 */
@RequiresApi(api = Build.VERSION_CODES.S)
public class CallStateListenerSType extends TelephonyCallback implements TelephonyCallback.CallStateListener{

	CallStateInterface m_player;


	public CallStateListenerSType(CallStateInterface player)
	{
		m_player = player;
	}

    @Override
    public void onCallStateChanged(int state) {

     switch (state) {
         case TelephonyManager.CALL_STATE_OFFHOOK:
         // called when someone is ringing to this phone
        	 Log.i("Telephone state", "ringing");
        	 m_player.PausePlay();


         break;
         case TelephonyManager.CALL_STATE_IDLE:
        	 Log.i("Telephone state", "idle");
             if(m_player.isPaused())
        	 m_player.ResumePlay();

         break;
     }
 }
}
