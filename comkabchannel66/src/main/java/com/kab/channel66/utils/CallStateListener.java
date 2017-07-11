package com.kab.channel66.utils;

//import io.vov.vitamio.utils.Log;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Listener to detect incoming calls. 
 */
public class CallStateListener extends PhoneStateListener {
	
	CallStateInterface m_player;

	
	public CallStateListener(CallStateInterface player)
	{
		m_player = player;
	}
	
 @Override
 public void onCallStateChanged(int state, String incomingNumber) {
     switch (state) {
         case TelephonyManager.CALL_STATE_OFFHOOK:
         // called when someone is ringing to this phone
        	 Log.i("Telephone state", "ringing");
        	 m_player.PausePlay();

         
         break;
         case TelephonyManager.CALL_STATE_IDLE:
        	 Log.i("Telephone state", "idle");
        	 m_player.ResumePlay();

         break;
     }
 }
}
