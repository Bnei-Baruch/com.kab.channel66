package com.kab.channel66;

import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.vov.vitamio.utils.Log;
import android.content.Context;
import android.content.Intent;
import android.sax.StartElementListener;
import android.text.format.DateFormat;

import com.kab.channel66.utils.CommonUtils;
import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePushBroadcastReceiver;

public  class ParsePushHandling extends ParsePushBroadcastReceiver {

	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		if(!CommonUtils.getActivated(context))
			return;
		
		
		// TODO Auto-generated method stub
		
		if(intent.getAction().equalsIgnoreCase("com.parse.push.intent.RECEIVE"))
		{
			super.onReceive(context, intent);
			Log.d("got a push", null);
		ParseObject message = new ParseObject("messages");
		String text = (String) intent.getExtras().get(KEY_PUSH_DATA);
		try {
			//JSONArray array = new JSONArray(text);
		
			JSONObject pushjson = new JSONObject(text);
			
			String val = (String) pushjson.get("alert");
			
			message.put("text", val);
			
			
			message.put("date",System.currentTimeMillis());
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		message.pinInBackground();
		context.sendBroadcast(new Intent("newMessage"));
		}
		
		if(intent.getAction().equalsIgnoreCase("com.parse.push.intent.OPEN"))
		{
			Intent intent1 = new Intent(context, PushMessagesActivity.class);
			intent1.putExtras(intent.getExtras());
			intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent1);
		}
		
		
		
		
	
	}
	
	
	
}
