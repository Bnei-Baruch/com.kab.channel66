package com.kab.channel66;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
//
//import com.apphance.android.Log;

import android.os.AsyncTask;

public class ASXExtractor extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... arg0) {
		InputStream is = null;
		try {
			URL serverAddress = new URL(arg0[0]);
			HttpURLConnection connection = (HttpURLConnection)serverAddress.openConnection();
            connection.setRequestMethod("GET");
            
            connection.setReadTimeout(2000);
            connection.setChunkedStreamingMode(0);
            
            connection.connect();

            InputStreamReader stream  = new InputStreamReader(connection.getInputStream());
		
			BufferedReader rd  = new BufferedReader(stream);

			String line = null;
			while ((line = rd.readLine()) != null) {
				if((line.indexOf("mms"))>-1)
				{
				String mms_url = (line.substring(line.indexOf("mms"),line.indexOf(" /")-1));
				if(mms_url.length()>0 && mms_url.contains("nl"))
				{
					//mms_url = mms_url.replace("mms", "http");
					return mms_url;
				}
				}
			}
		
	}
	 catch (Exception e) {
//		Log.e("Buffer Error", "Error converting result " + e.toString());
	}
		return "";
	}

}
