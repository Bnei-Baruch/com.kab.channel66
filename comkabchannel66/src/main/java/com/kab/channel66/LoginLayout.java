package com.kab.channel66;

import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

public class LoginLayout extends Activity {
    EditText un,pw;
	TextView error;
    Button ok;
    LoginButton loginButton;
    CallbackManager callbackManager;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        un=(EditText)findViewById(R.id.et_un);
        pw=(EditText)findViewById(R.id.et_pw);
        ok=(Button)findViewById(R.id.btn_login);
        error=(TextView)findViewById(R.id.tv_error);

        ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

            	ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();
            	postParameters.add(new BasicNameValuePair("username", un.getText().toString()));
            	postParameters.add(new BasicNameValuePair("password", pw.getText().toString()));

            	String response = null;
            	try {
            	    response = CustomHttpClient.executeHttpPost("<target page url>", postParameters);
            	    String res=response.toString();
            	    res= res.replaceAll("\\s+","");
            	    if(res.equals("1"))
            	    	error.setText("Correct Username or Password");
            	    else
            	    	error.setText("Sorry!! Incorrect Username or Password");
            	} catch (Exception e) {
            		un.setText(e.toString());
            	}

            }
        });

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.setLoginBehavior(LoginBehavior.DEVICE_AUTH);
        callbackManager =  CallbackManager.Factory.create();

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                Log.d("login","success");
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
}

