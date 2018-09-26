package com.kab.channel66;

import android.database.CursorJoiner.Result;
import android.os.AsyncTask;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

//import com.parse.signpost.http.HttpResponse;

public class QuestionPoster extends AsyncTask< ArrayList<NameValuePair>, Void, Result> {


	@Override
	protected Result doInBackground( ArrayList<NameValuePair>... params ) {
		// TODO Auto-generated method stub
		 CustomHttpClient httpclient = new CustomHttpClient();
		    HttpPost httppost = new HttpPost("http://www.kab.tv/ask.php");

		 try {
			httppost.setEntity(new UrlEncodedFormEntity(params[0]));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	        // Execute HTTP Post Request
	      
	        try {
				String response =  (String)httpclient.executeHttpPost("http://www.kab.tv/ask.php", params[0]);
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
