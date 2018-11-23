package com.kab.channel66;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.onesignal.OneSignal;

import java.util.Date;

public class HanderActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedBundle)
    {
        super.onCreate(savedBundle);
        //set tag on user
        OneSignal.sendTag((new Date(System.currentTimeMillis())).toString(),"clicked");
        finish();
    }
}
