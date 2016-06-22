package com.kab.channel66;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

//import com.parse.signpost.http.HttpResponse;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.ExifInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Questions extends Dialog {
	private EditText question;
	private EditText from;
	private EditText name;
	private Button ask;
	public Context mContext; 
	
	public Questions(Context context) {
		super(context);
		setContentView(R.layout.questions);
		question = (EditText) findViewById(R.id.et_question);
		from = (EditText) findViewById(R.id.et_from);
		name = (EditText) findViewById(R.id.et_name);
		ask = (Button) findViewById(R.id.btn_ask);
		mContext = context;
		
		ask.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//preform question, format the question
				postData();
				dismiss();
				
				
			}
		});
		// TODO Auto-generated constructor stub
	}

	@SuppressWarnings("unchecked")
	public void postData() {
	    // Create a new HttpClient and Post Header
	   
	   
	        // Add your data
	        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("QName", name.getText().toString()));
	        nameValuePairs.add(new BasicNameValuePair("QFrom", from.getText().toString()));
	        nameValuePairs.add(new BasicNameValuePair("QQuestion", question.getText().toString()));
	        nameValuePairs.add(new BasicNameValuePair("ask", "1"));
	        nameValuePairs.add(new BasicNameValuePair("is_hidden", "0"));
	        nameValuePairs.add(new BasicNameValuePair("isquestion", "1"));
	        
	        
	       
	        new QuestionPoster().execute(nameValuePairs);
	        Toast.makeText(mContext, "Sent question successfully", 5);
	       
	        
	} 

}

//
//POST /ask.php?lang=Hebrew HTTP/1.1
//Host: www.kab.tv
//User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.8; rv:18.0) Gecko/20100101 Firefox/18.0
//Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
//Accept-Language: en-US,en;q=0.5
//Accept-Encoding: gzip, deflate
//Referer: http://www.kab.tv/ask.php?lang=Hebrew&is_hidden=0
//Cookie: __utma=209654408.1998300655.1353809115.1360805091.1361149226.62; __utmz=209654408.1359939901.54.4.utmcsr=google|utmccn=(organic)|utmcmd=organic|utmctr=(not%20provided); FLPlayerID=FL50925761; WRUID=377275497.2068752246; __atuvc=1%7C49; _session_id=1194c550dd544aca8e74cc2f944acab7; __utmb=209654408.2.9.1361149235824; __utmc=209654408
//Connection: keep-alive
//Content-Type: application/x-www-form-urlencoded
//Content-Length: 360
//
//QName=%D7%97%D7%91%D7%A8%D7%99%D7%9D&QFrom=%D7%A7%D7%91%D7%95%D7%A6%D7%AA+%D7%97%D7%95%D7%9C%D7%95%D7%9F+%2B+%D7%A8%D7%90%D7%A9%D7%95%D7%9F&QQuestion=%D7%90%D7%99%D7%9A+%D7%A0%D7%96%D7%94%D7%A8%D7%99%D7%9D+%D7%9E%D7%94%D7%92%D7%90%D7%95%D7%95%D7%94%3F+%D7%90%D7%99%D7%9A+%D7%9E%D7%96%D7%94%D7%99%D7%9D+%D7%90%D7%95%D7%AA%D7%94%3F&ask=1&isquestion=1&is_hidden=0

