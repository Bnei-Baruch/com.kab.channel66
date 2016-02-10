package com.kab.channel66;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.kab.channel66.utils.CommonUtils;
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
			//Log.d("got a push", null);
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
        else if(intent.getAction().equalsIgnoreCase("com.parse.push.intent.OPEN"))
        {
            String text = (String) intent.getExtras().get(KEY_PUSH_DATA);
            String val = "";
            try {
                JSONObject pushjson = new JSONObject(text);
                val = (String) pushjson.get("alert");
            }
            catch (Exception ex)
            {

            }
            Uri uri = CommonUtils.findURIInText(val);
            if(uri != null) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(uri);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
            else {

                Intent intent1 = new Intent(context, PushMessagesActivity.class);
                intent1.putExtras(intent.getExtras());
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent1);
            }
        }
		
		
		
		
	
	}
}
