package com.kab.channel66;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

import com.kab.channel66.utils.CommonUtils;
import com.kab.channel66.utils.SvivaTovaLoginApiHelper;
import com.kab.channel66.utils.SvivaTovaLoginHelper;
//import com.kab.channel66.utils.status;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class SvivaTovaLogin extends BaseActivity implements LanguageSeletedListener {

	EditText mUser;
	EditText mPass;
	Button mSubmit;
	SvivaTovaLoginApiHelper mHelper;
	private TextView mError;
	
	public SvivaTovaLogin() {
		// TODO Auto-generated constructor stub
	}
	
	public void onCreate(Bundle icicle) {
	    super.onCreate(icicle);
	    
	    setContentView(R.layout.login);
	    mUser = (EditText)findViewById(R.id.et_un);
	    mPass = (EditText)findViewById(R.id.et_pw);
	    mSubmit = (Button)findViewById(R.id.btn_login);
	    mError = (TextView)findViewById(R.id.tv_error); 
	    
	    

	    mSubmit.setOnClickListener(new OnClickListener() {
			
		
			public void onClick(View v) {
				// TODO Auto-generated method stub
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
						
						
						
						
//						CommonUtils.ShowLanguageSelection(SvivaTovaLogin.this,new LanguageSeletedListener() {
//							
//							@Override
//							public void onSelectLanguage(String lang) {
//								// TODO Auto-generated method stub
//								
//							}
//						});
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

}
