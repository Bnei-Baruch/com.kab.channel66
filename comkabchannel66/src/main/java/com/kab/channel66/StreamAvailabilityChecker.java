package com.kab.channel66;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
//
//import com.apphance.android.Log;


import android.app.ProgressDialog;
import android.os.AsyncTask;

public class StreamAvailabilityChecker extends
		AsyncTask<String, Void, Boolean> {

	boolean auto = false;
	StreamListActivity activity = null;
	public StreamListActivity getActivity() {
		return activity;
	}
	public void setActivity(StreamListActivity activity) {
		this.activity = activity;
	}
	public boolean isAuto() {
		return auto;
	}
	public void setAuto(boolean auto) {
		this.auto = auto;
	}
	@Override
	protected Boolean doInBackground(String... params) {
		URLConnection cn = null;
		if(!auto)
		{	
		try {

			cn = new URL(params[0]).openConnection();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			cn.connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		InputStream stream = null;
		try {
			stream = cn.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (stream == null) {

		
		
		return false;
		}
		return true;
	}
	
	else
	{
		InputStream stream = null;
		while(stream==null)
		{
		try {

			cn = new URL(params[0]).openConnection();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			cn.connect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			stream = cn.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		 if (isCancelled()) 
			 return false;
		}
		activity.streamfound();
		return true;
	}
	}

	

}
