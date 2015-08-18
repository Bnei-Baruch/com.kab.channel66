package com.kab.channel66;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import com.parse.signpost.http.HttpResponse;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.CursorJoiner.Result;
import android.os.AsyncTask;
import android.widget.Toast;
import android.content.Context;

public class QuestionPoster extends AsyncTask< ArrayList<NameValuePair>, Void, Result> {

	@Override
	protected Result doInBackground( ArrayList<NameValuePair>... params ) {
		// TODO Auto-generated method stub
		 CustomHttpClient httpclient = new CustomHttpClient();
		    HttpPost httppost = new HttpPost("http://www.kab.tv/ask.php?lang=English");

		 try {
			httppost.setEntity(new UrlEncodedFormEntity(params[0]));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	        // Execute HTTP Post Request
	      
	        try {
				String response =  (String)httpclient.executeHttpPost("http://www.kab.tv/ask.php?lang=English", params[0]);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
		return null;
	}
	


	

}
