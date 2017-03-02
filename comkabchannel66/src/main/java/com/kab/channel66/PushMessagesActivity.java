package com.kab.channel66;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.kab.channel66.db.MessagesDataSource;
import com.kab.channel66.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;

//import com.parse.FindCallback;
//import com.parse.ParseException;
//import com.parse.ParseObject;
//import com.parse.ParseQuery;


public class PushMessagesActivity extends BaseListActivity implements ListView.OnItemClickListener {

	private MessageAdapter mAdapter;
	ArrayList<String> pushMessages;
	BroadcastReceiver myReciever;
	ListView listview;
	private Handler handler;
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.listviewlayout);
		listview = (ListView)findViewById(R.id.listview);
		
		
		mAdapter = new MessageAdapter(PushMessagesActivity.this, 0);

		refreshMessages();
        PushMessagesActivity.this.listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                com.kab.channel66.db.Message obj = mAdapter.getItem(position);
               String text = obj.getComment();
                Uri uri = CommonUtils.findURIInText(text);
                if(uri != null)
                {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(uri);
                    startActivity(i);
                }
            }
        });

		pushMessages = new ArrayList<String>();


		myReciever= new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				if(intent.getAction().contentEquals("newMessage"))
					refreshMessages();

			}
		};

		
		listview.setAdapter(mAdapter);
		//refreshMessages();
		
		 handler = new Handler() {
            public void handleMessage(Message msg) {
                 if(msg.arg1 == 1){
                	 
                	runOnUiThread(new Runnable() {
                	        @Override
                	        public void run() {
                	         mAdapter.notifyDataSetChanged();
                	         PushMessagesActivity.this.listview.invalidateViews();
                	        }
                	});
                	 
                 }
             }
		 };
		 
		 
		 
		}

	@Override
	public void onStop()
	{
		super.onStop();
		unregisterReceiver(myReciever);
	}

	@Override
	public void onResume()
	{
		super.onResume();
		IntentFilter filter = new IntentFilter();
		filter.addAction("newMessage");
		registerReceiver(myReciever,filter);
	}

//	@Override
//	protected void onListItemClick(ListView l, View v, int position, long id) {
//		String item = (String) listview.getAdapter().getItem(position);
//
//
//	}
	
	private void refreshMessages()
	{
		MessagesDataSource datasource;

		datasource = new MessagesDataSource(this);
		datasource.open();

		List<com.kab.channel66.db.Message> values = datasource.getAllComments();

					mAdapter.clear();
		            mAdapter.addArray(values);
					mAdapter.notifyDataSetChanged();


		datasource.close();
	}




	private void clearMessages()
	{
		MessagesDataSource datasource;

		datasource = new MessagesDataSource(this);
		datasource.open();
		datasource.deleteAllMessages();
		datasource.close();
		mAdapter.clear();
		mAdapter.notifyDataSetChanged();

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.pushoptionsmenu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		
		switch (item.getItemId()) {
	case R.id.ClearMessages:
		clearMessages();
		return true;
	default:
		return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		String item = (String) listview.getAdapter().getItem(i);

	}
}

