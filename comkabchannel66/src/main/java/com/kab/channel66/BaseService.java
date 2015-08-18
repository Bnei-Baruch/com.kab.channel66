package com.kab.channel66;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.widget.Toast;

public class BaseService extends Service {

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		if(!checkConnectivity())
		{
			return 0;
		}
		return startId;
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public boolean checkConnectivity()
	{
		Dialog blockApp;
		boolean state;
		if(!(state = isOnline(BaseService.this)))
		{
//			new AlertDialog.Builder(this)
//		    .setTitle("Data not available")
//		    .setMessage("Appliaction needs data connection")
//		    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//		        public void onClick(DialogInterface dialog, int which) { 
//		            // continue with delete
//		        	return;
//		        	
//		        }
//		     })
//		    
//		    
//		     .show();
			
			Toast.makeText(getApplicationContext(), "No data connection", Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}
	 public boolean isOnline(Context context) { 
		    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);    
		    NetworkInfo netInfo = cm.getActiveNetworkInfo();    
		    return netInfo != null && netInfo.isConnected();
		}



}
