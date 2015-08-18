package com.kab.channel66;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.GpsStatus.Listener;
import android.os.AsyncTask;

//import android.util.Log;
import com.apphance.android.Log;
public class ContentParser  extends AsyncTask <String, Void, String>{
	static InputStream is = null;
	static JSONObject jObj = null;
	static String content = "";
	
	// constructor
	public ContentParser() {

	}

	

	@Override
	protected String doInBackground(String... url) {
		// TODO Auto-generated method stub
		try {
			//defaultHttpClient
			URL myURL = new URL(url[0]);

			/* Open a connection to that URL. */
			URLConnection ucon = myURL.openConnection();
			ucon.setUseCaches(false);
			ucon.setRequestProperty("Cache-Control", "no-cache");
			ucon.connect();
			

			is= ucon.getInputStream();
			

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
			content = sb.toString();
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}

		

		return content;
	}
	

}
