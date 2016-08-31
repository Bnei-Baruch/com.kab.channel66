package com.kab.channel66.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieStore;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

//import com.google.myjson.JsonObject;
//import com.parse.signpost.http.HttpResponse;

import android.app.Activity;
import android.database.CursorJoiner.Result;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.CookieManager;





public class SvivaTovaLoginApiHelper extends AsyncTask< ArrayList<String>, Void, SvivaTovaLoginApiHelper.status> {


	final public String  kSvivaTovaLoginURL ="http://kabbalahgroup.info/internet/api/v1/tokens.json";

	HttpClient mHttpclient;
	String mUser;
	String mPassword;
	String mToken;
	String mLocalization;
	status mSuccess = status.fail;


	static public enum status{
		sucess,
		fail,
		not_allowed
	}


	public HttpPost getHeaderpost(HttpPost post)
	{
		post.addHeader("Content-Type","application/json");
		return post;


	}


	public String postLoginDetails() throws Exception
	{
		mHttpclient = new DefaultHttpClient();
		 BufferedReader in = null;
		try{


		HttpPost request = new HttpPost(kSvivaTovaLoginURL);
		request = getHeaderpost(request);

		 JSONObject jsonObject = new JSONObject();
			if(mToken!=null)
				jsonObject.accumulate("fb_token", mToken);
			else
			{
				jsonObject.accumulate("email", mUser);
				jsonObject.accumulate("password", mPassword);
			}



         // 4. convert JSONObject to JSON to String
         String json = jsonObject.toString();

         // ** Alternative way to convert Person object to JSON string usin Jackson Lib
         // ObjectMapper mapper = new ObjectMapper();
         // json = mapper.writeValueAsString(person);

         // 5. set json to StringEntity
         StringEntity se = new StringEntity(json);

         // 6. set httpPost Entity
         request.setEntity(se);

//         // 7. Set some headers to inform server about the type of the content   
//         httpPost.setHeader("Accept", "application/json");
//         httpPost.setHeader("Content-type", "application/json");

		   // request.setHeader("Content-Length", Integer.toString(bodyToSend.toString().length()));
//		    mHttpclient.getConnectionManager().closeExpiredConnections();
		    org.apache.http.HttpResponse response =  (org.apache.http.HttpResponse) mHttpclient.execute(request);
		    in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();

            String result = sb.toString();
            return result;
		}
            catch(Exception e)
            {
            	 Log.e("log_tag", "Error in http connection "+e.toString());
            }
         finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


		return "";
	}

	@Override
	protected status doInBackground(ArrayList<String>... params) {
		// TODO Auto-generated method stub
		if(params[0].size()>1) {
			mUser = (String) params[0].get(0);
			mPassword = (String) params[0].get(1);
		}
		else
			mToken = (String) params[0].get(0);
		try {
			return process(postLoginDetails());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return status.fail;
	}
	 private status process(String result) {
         
		 
		 try {
			JSONObject res = new JSONObject(result);
			if(!res.has("error"))
			{
				
				boolean allowed = res.getBoolean("allow_archived_broadcasts");
				if(allowed)
		        	 mSuccess = status.sucess;
		         else
		        	 mSuccess = status.not_allowed;
				
				
			}
			 else
			{
				mSuccess = status.fail;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mSuccess;
		
		 
        	 
     }
	
}


