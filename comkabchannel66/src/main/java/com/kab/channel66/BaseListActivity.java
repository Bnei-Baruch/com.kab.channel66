package com.kab.channel66;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.FragmentActivity;

public class BaseListActivity extends FragmentActivity {
	AlertDialog dlg = null;
	@Override
	public void onResume()
	{
		super.onResume();
		
		if(!isOnline(this.getApplicationContext()))
		{
			if(dlg==null || (dlg!=null && !dlg.isShowing()))
			{
			dlg = new AlertDialog.Builder(this)
		    .setTitle("Data not available")
		    .setMessage("Appliaction needs data connection")
		    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // continue with delete
		        	//finish();
		        	return;
		        }
		     }).show();
			}
		}
	}
	 public boolean isOnline(Context context) { 
		    ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);    
		    NetworkInfo netInfo = cm.getActiveNetworkInfo();    
		    return netInfo != null && netInfo.isConnected();
		}

//	@Override
//	protected int getSupportLayoutResourceId() {
//		return R.id.list;
//	}
}
