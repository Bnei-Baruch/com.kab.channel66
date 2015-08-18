package com.kab.channel66;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpConnection;
import org.apache.http.HttpConnectionMetrics;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.myjson.Gson;
import com.google.myjson.GsonBuilder;
import com.google.myjson.JsonArray;
import com.google.myjson.JsonObject;
 

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.os.Bundle;
//import android.util.Log;
import com.apphance.android.Log;


//{
//    "locale" : [{
//        "he" : {
//            "pages" : [{
//                "regular" : {
//                    "state" : "active",
//                    "description" : "Sviva Tova broadcast",
//                    "title" : "Sviva Tova",
//                    "urls" : [{
//                        "url_quality" : "high",
//                        "url_value" : "mms://wms.il.kab..."
//                    },
//                    {
//                        "url quality" : "medium",
//                        "url_value" : "kab heb"
//                    }]
//                }
//            },
//            {
//                "kenes" : {
//                    "state" : "active",
//                    "description" : "Sviva Tova broadcast",
//                    "title" : "Sviva Tova",
//                    "urls" : [{
//                        "url_quality" : "high",
//                        "url_value" : "mms://wms.il.kab..."
//                    },
//                    {
//                        "url quality" : "medium",
//                        "url_value" : "kab heb"
//                    }]
//                }
//            }]
//        }
//    },
//    {
//        "en" : {
//            "pages" : {
//                "regular" : {
//                    "state" : "active",
//                    "description" : "Sviva Tova broadcast",
//                    "title" : "Sviva Tova",
//                    "urls" : [{
//                        "url_quality" : "high",
//                        "url_value" : "mms://wms.il.kab..."
//                    },
//                    {
//                        "url quality" : "medium",
//                        "url_value" : "kab en"
//                    }]
//                }
//            }
//        }
//    }]
//}




public class Events {
	public class Url
	{
		String url_quality;
		String url_value;
		public Url()
		{
			
		}
		public Url(String val, String quality)
		{
			url_quality = quality;
			url_value = val;
				
		}
	}
	
	public class Urls{
		ArrayList <Url> urlslist;
		public Urls()
		{
			urlslist = new ArrayList<Events.Url>();
			
		}
	}
	public class Page{
		String state;
		String description;
		String title;
		Urls urls;
		public Page()
		{
			urls = new Urls();
		}
	}
	public class Pages{
		ArrayList<Page> pages;
		public Pages()
		{
			pages = new ArrayList<Events.Page>();
		}
	}
	//JSON Node names
	private static final String LOCALE = "locale";
	private static final String PAGES = "pages";
	private static final String STATE = "state";
	private static final String DESCRIPTION = "description";
	private static final String TITLE = "title";
	private static final String URLS = "urls";
	private static final String URL_QUALITY = "url_quality";
	private static final String URL_VALUE = "url_value";
	
	HashMap<String, Pages> locale;
	
	private JSONArray mLocale = null;
	HashMap<String, String> mEventsData;
	JSONObject mData;
	Context mContext;
	public Events(JSONObject data, Context context)
	{
		mData =  data;
		mContext = context;
	}
	
	public void parse() 
	{
		try {
			locale = new HashMap<String, Events.Pages>();
			String text = null;
			//only for testing purposes
			AssetManager assetManager = mContext.getAssets();
			InputStream input;
			try {
				input = assetManager.open("jsonresponseexample.json");
		          int size = input.available();
		          byte[] buffer = new byte[size];
		          input.read(buffer);
		          input.close();
		           text = new String(buffer);
			  } catch (IOException e) {
				   e.printStackTrace();
			  }
		//parse
			JSONObject json = mData;// new JSONObject(text);//mData;//
			JSONArray mLocale =  json.getJSONArray(LOCALE);
			String key_lang = null;
			String page_name = null;
			String state = null;
			String desc = null;
			String title = null;
			String val = null;
			String quality = null;
			for (int i=0;i<mLocale.length();i++)
			{
				Pages pages_c = new Pages();
				JSONObject lang =  mLocale.getJSONObject(i);
				if(lang.keys().hasNext())
					key_lang = (String)lang.keys().next();
				JSONObject lang_val = lang.getJSONObject(key_lang);
				JSONArray pages = lang_val.getJSONArray(PAGES);
				
				
				for(int j=0;j<pages.length();j++)
				{
					Page page_c = new Page();
					JSONObject page = pages.getJSONObject(j);
					if(page.keys().hasNext())
						page_name = (String)page.keys().next();
					JSONObject page_val = page.getJSONObject(page_name);
					state = page_val.getString(STATE);
					desc = page_val.getString(DESCRIPTION);
					title = page_val.getString(TITLE);
					JSONArray urls = page_val.getJSONArray(URLS);
					for(int k=0;k<urls.length();k++)
					{
						JSONObject url = urls.getJSONObject(k);
						val = url.getString(URL_VALUE);
						quality = url.getString(URL_QUALITY);
						
						page_c.urls.urlslist.add(new Url(val,quality));
					}
					page_c.description = desc;
					page_c.state = state;
					page_c.title = title;
					pages_c.pages.add(page_c);
					
				}
				Log.e("paser", lang.toString());
				locale.put(key_lang, pages_c);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			locale = null;
		}
		
		finally
		{
			if(locale !=null){
				//succesfull parsing of json from sever
				//save the json into shared preferences
				SharedPreferences pref = mContext.getSharedPreferences("events", 0);
				Editor edtitor = pref.edit();
				edtitor.putString("raw_data", mData.toString());
				edtitor.commit();
				
			}
			
		}
			/////
			
	}	
}
			
			
//			HashMap<String,Object> result =
//			        new ObjectMapper().readValue(text, HashMap.class);
//		    Log.e("parser",  result.toString());
			// Getting Array of Contacts
//		    mLocale = mData.getJSONArray(LOCALE);
//		 
//		    // looping through All Locales
//		    for(int i = 0; i < mLocale.length(); i++){
//		        JSONObject c = mLocale.getJSONObject(i);
//		        
//		        // Storing each json item in variable
//		        String language = c.keys().next().toString();
//		        JSONObject  pages = c.getJSONObject(language);
//		        JSONObject p = pages.
//		        for(int j = 0; j < mLocale.length(); j++){
//		        pages.getJSONArray(i);
//		        String name = c.getString(TAG_NAME);
//		        String email = c.getString(TAG_EMAIL);
//		        String address = c.getString(TAG_ADDRESS);
//		        String gender = c.getString(TAG_GENDER);
//		 
//		        // Phone number is agin JSON Object
//		        JSONObject phone = c.getJSONObject(TAG_PHONE);
//		        String mobile = phone.getString(TAG_PHONE_MOBILE);
//		        String home = phone.getString(TAG_PHONE_HOME);
//		        String office = phone.getString(TAG_PHONE_OFFICE);
//		 
//		    }
		
		