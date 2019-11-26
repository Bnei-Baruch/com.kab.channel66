package com.kab.channel66;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
//import android.util.Log;
//import com.apphance.android.Log;
public class JSONParser  extends AsyncTask <String, Void, JSONObject>{
	static InputStream is = null;
	static JSONObject jObj = null;
	static String json = "";
	  protected ProgressDialog progressDialog;
	  Context myContext;
	// constructor
	public JSONParser() {

	}

	@Override
	protected void onPreExecute(){ 
	   super.onPreExecute();
	   //progressDialog  = ProgressDialog.show(myContext, "Loading...",null, true, false);  
	}

	@Override
	protected JSONObject doInBackground(String... url) {
		// TODO Auto-generated method stub
		try {
			//defaultHttpClient
			URL myURL = new URL(url[0]);

			/* Open a connection to that URL. */
			URLConnection ucon = myURL.openConnection();
			ucon.setReadTimeout(10000);
			ucon.setConnectTimeout(10000);
			ucon.setUseCaches(false);
			ucon.setRequestProperty("Cache-Control", "no-cache");
			ucon.connect();
			

			is= ucon.getInputStream();
			

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			try {
				return new JSONObject("");
			} catch (JSONException ex) {
				ex.printStackTrace();
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			is.close();
			json = sb.toString();
		} catch (Exception e) {
//			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		try {
			jObj = new JSONObject(json);
		} catch (JSONException e) {
//			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}

		return jObj;
	}
	
	 protected void onPostExecute(Long result) {
//		 progressDialog.hide();
     }

}
