package com.kab.channel66;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.analytics.tracking.android.EasyTracker;
import com.kab.channel66.utils.CommonUtils;
import com.kab.channel66.utils.SvivaTovaLoginApiHelper;
import com.kab.channel66.utils.SvivaTovaLoginHelper;
//import com.kab.channel66.utils.status;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;


public class SvivaTovaLogin extends BaseActivity implements LanguageSeletedListener {

	EditText mUser;
	EditText mPass;
	Button mSubmit;
	SvivaTovaLoginApiHelper mHelper;
	private TextView mError;
	LoginButton loginButton;
	CallbackManager callbackManager;
	ProgressDialog progressDialog;

	
	public SvivaTovaLogin() {
		// TODO Auto-generated constructor stub
	}
	
	public void onCreate(Bundle icicle) {
	    super.onCreate(icicle);
		EasyTracker.getInstance().setContext(this);


	    setContentView(R.layout.login);
	    mUser = (EditText)findViewById(R.id.et_un);
	    mPass = (EditText)findViewById(R.id.et_pw);
	    mSubmit = (Button)findViewById(R.id.btn_login);
	    mError = (TextView)findViewById(R.id.tv_error);

		FacebookSdk.sdkInitialize(this.getApplicationContext());

	    mSubmit.setOnClickListener(new OnClickListener() {
			
		
			public void onClick(View v) {
				// TODO Auto-generated method stub
				logionWithEmail();
			}
		});

		loginButton = (LoginButton) findViewById(R.id.login_button);
		loginButton.setReadPermissions("email");

		callbackManager =  CallbackManager.Factory.create();
		if(isLoggedIn())
		{
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					progressDialog = new ProgressDialog(SvivaTovaLogin.this);
					progressDialog.setMessage("Processing data...");
					progressDialog.show();
				}
			});
			loginwithFBToken();
		}
		// Callback registration
		loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {


				System.out.println("onSuccess");
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						progressDialog = new ProgressDialog(SvivaTovaLogin.this);
						progressDialog.setMessage("Processing data...");
						progressDialog.show();
					}
				});

				loginwithFBResult(loginResult);

			}

			@Override
			public void onCancel() {
				// App code
				Log.d("login","cancel");
			}

			@Override
			public void onError(FacebookException exception) {
				// App code
				Log.d("login","error");
			}
		});



	    
	}


	private void logionWithEmail()
	{
		ArrayList<String> details = new ArrayList<String>();
		details.add(mUser.getText().toString());
		details.add(mPass.getText().toString());
		SvivaTovaLoginApiHelper.status st;
		mHelper = (SvivaTovaLoginApiHelper) new SvivaTovaLoginApiHelper().execute(details);
		try {
			if(( st = (SvivaTovaLoginApiHelper.status)mHelper.get())!= SvivaTovaLoginApiHelper.status.sucess)
				if(st== SvivaTovaLoginApiHelper.status.not_allowed)
					mError.setText("Login failed (code 2)");
				else
					mError.setText("Login failed (code 1)");
			else
			{
				CommonUtils.setActivated(true, SvivaTovaLogin.this);
				//run language selector
				//with the language load the streams relevant like in weblogin and pass to stream list
				CommonUtils.ShowLanguageSelection(SvivaTovaLogin.this,SvivaTovaLogin.this);


			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void loginwithFBResult(LoginResult loginResult)
	{
		final String accessToken = loginResult.getAccessToken().getToken();
		Log.i("accessToken", accessToken);

		GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

			@Override
			public void onCompleted(JSONObject object, GraphResponse response) {
				Log.i("LoginActivity", response.toString());

				// Get facebook data from login
				Bundle bFacebookData = getFacebookData(object);
				String email = null;
				try {
					email = (String) object.getString("email");
				}
				catch (Exception e)
				{

				}

				ArrayList<String> details = new ArrayList<String>();
				details.add(accessToken);
				SvivaTovaLoginApiHelper.status st;


				mHelper = (SvivaTovaLoginApiHelper) new SvivaTovaLoginApiHelper().execute(details);
				try {
					if ((st = (SvivaTovaLoginApiHelper.status) mHelper.get()) != SvivaTovaLoginApiHelper.status.sucess)
						if (st == SvivaTovaLoginApiHelper.status.not_allowed) {
							mError.setText("Login failed (code 2)");
							EasyTracker.getTracker().trackEvent("SvivaTovaLogin", "failed", "EMAIL", 2L);
						}
						else {
							mError.setText("Login failed (code 1)");
							EasyTracker.getTracker().trackEvent("SvivaTovaLogin", "failed", "EMAIL", 1L);
						}
					else {
						CommonUtils.setActivated(true, SvivaTovaLogin.this);
						//run language selector
						//with the language load the streams relevant like in weblogin and pass to stream list
						CommonUtils.ShowLanguageSelection(SvivaTovaLogin.this, SvivaTovaLogin.this);
						EasyTracker.getTracker().trackEvent("SvivaTovaLogin","success","EMAIL",0L);

					}
					progressDialog.dismiss();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});


		Bundle parameters = new Bundle();
		parameters.putString("fields", "id, first_name, last_name, email,gender, birthday, location"); // Parámetros que pedimos a facebook
		request.setParameters(parameters);
		request.executeAsync();

	}

	private void loginwithFBToken()
	{
		AccessToken accessToken = AccessToken.getCurrentAccessToken();
		final String accessTokenstr = accessToken.getToken();
		Log.i("accessToken", accessTokenstr);

		GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {

			@Override
			public void onCompleted(JSONObject object, GraphResponse response) {
				Log.i("LoginActivity", response.toString());
				progressDialog.hide();
				// Get facebook data from login
				Bundle bFacebookData = getFacebookData(object);
				String email = null;
				try {
					email = (String) object.getString("email");
				}
				catch (Exception e)
				{

				}

				ArrayList<String> details = new ArrayList<String>();
				details.add(accessTokenstr);
				SvivaTovaLoginApiHelper.status st;
				mHelper = (SvivaTovaLoginApiHelper) new SvivaTovaLoginApiHelper().execute(details);
				try {
					if ((st = (SvivaTovaLoginApiHelper.status) mHelper.get()) != SvivaTovaLoginApiHelper.status.sucess)
						if (st == SvivaTovaLoginApiHelper.status.not_allowed) {
							mError.setText("Login failed (code 2)");
							EasyTracker.getTracker().trackEvent("SvivaTovaLogin", "failed", "FB", 2L);
						}
						else {
							mError.setText("Login failed (code 1)");
							EasyTracker.getTracker().trackEvent("SvivaTovaLogin","failed","FB",1L);
						}
					else {
						CommonUtils.setActivated(true, SvivaTovaLogin.this);
						//run language selector
						//with the language load the streams relevant like in weblogin and pass to stream list
						CommonUtils.ShowLanguageSelection(SvivaTovaLogin.this, SvivaTovaLogin.this);

						EasyTracker.getTracker().trackEvent("SvivaTovaLogin","success","FB",0L);

					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});


		Bundle parameters = new Bundle();
		parameters.putString("fields", "id, first_name, last_name, email,gender, birthday, location"); // Parámetros que pedimos a facebook
		request.setParameters(parameters);
		request.executeAsync();

	}



	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		callbackManager.onActivityResult(requestCode, resultCode, data);

	}

	@Override
	public void onSelectLanguage(String lang) {
		// TODO Auto-generated method stub
		int index = CommonUtils.languages.indexOf(lang);
		String langcode = CommonUtils.langs.get(index);
		CommonUtils.setLastKnownLang(langcode, this);
		
		if(CommonUtils.getGroup(SvivaTovaLogin.this).length()==0)
		{
			CommonUtils.inputGroupName(SvivaTovaLogin.this);
		}
		//Start streamlist activity

		
	}

	private Bundle getFacebookData(JSONObject object) {

		try {
			Bundle bundle = new Bundle();
			String id = object.getString("id");

			try {
				URL profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?width=200&height=150");
				Log.i("profile_pic", profile_pic + "");
				bundle.putString("profile_pic", profile_pic.toString());

			} catch (MalformedURLException e) {
				e.printStackTrace();
				return null;
			}

			bundle.putString("idFacebook", id);
			if (object.has("first_name"))
				bundle.putString("first_name", object.getString("first_name"));
			if (object.has("last_name"))
				bundle.putString("last_name", object.getString("last_name"));
			if (object.has("email"))
				bundle.putString("email", object.getString("email"));
			if (object.has("gender"))
				bundle.putString("gender", object.getString("gender"));
			if (object.has("birthday"))
				bundle.putString("birthday", object.getString("birthday"));
			if (object.has("location"))
				bundle.putString("location", object.getJSONObject("location").getString("name"));

			return bundle;

		}catch (Exception e)
		{

		}
		return null;
		}

	public boolean isLoggedIn() {
		AccessToken accessToken = AccessToken.getCurrentAccessToken();
		return accessToken != null;
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance().activityStop(this); // Add this method.


	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
	}


}
