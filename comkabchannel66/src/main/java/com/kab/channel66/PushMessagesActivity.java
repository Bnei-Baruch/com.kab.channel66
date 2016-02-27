package com.kab.channel66;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.kab.channel66.utils.CommonUtils;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

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


public class PushMessagesActivity extends BaseListActivity {

	private MessageAdapter mAdapter;
	ArrayList<String> pushMessages;
	BroadcastReceiver myReciever;
	private Handler handler;
	
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		
		
		
		mAdapter = new MessageAdapter(PushMessagesActivity.this, 0);

        PushMessagesActivity.this.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ParseObject obj = mAdapter.getItem(position);
                String text = obj.getString("text");
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
		 IntentFilter filter = new IntentFilter();
		    filter.addAction("newMessage");
		registerReceiver(myReciever,filter);
		
		setListAdapter(mAdapter);
		refreshMessages();
		
		 handler = new Handler() {
            public void handleMessage(Message msg) {
                 if(msg.arg1 == 1){
                	 
                	runOnUiThread(new Runnable() {
                	        @Override
                	        public void run() {
                	         mAdapter.notifyDataSetChanged();
                	         PushMessagesActivity.this.getListView().invalidateViews();
                	        }
                	});
                	 
                 }
             }
		 };
		 
		 
		 
		}
	
//	@Override
//	protected void onListItemClick(ListView l, View v, int position, long id) {
//		String item = (String) getListAdapter().getItem(position);
//		
//		
//	}
	
	private void refreshMessages()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery("messages");
		//query.whereEqualTo("playerName", "Joe Bob");
		query.fromLocalDatastore();
		query.findInBackground(new FindCallback<ParseObject>() {
		    public void done(List<ParseObject> messages,
		                     ParseException e) {
		        if (e == null) {
//		            Log.d("score", "Retrieved " + messages.size());
//		            messages.get
//		            pushMessages.addAll(messages.toArray());

		            mAdapter.addArray(messages);
//		          //FOR TESTING PURPOSES
//		   		 ParseObject p1 = new ParseObject("messages");
//		   		 p1.put("text", "ein od melvado ein od melvado ein od melvado ein od melvado ein od melvado ein od melvado");
//		   		 p1.put("date",Calendar.getInstance().getTime().toString());
//		   		 mAdapter.add(p1);
		            Message msg = handler.obtainMessage();
                    msg.arg1 = 1;
                    handler.sendMessage(msg);

		        } else {
//		            Log.d("score", "Error: " + e.getMessage());
		        }
		    }
		});
	}
	
	
	private void clearMessages()
	{
		ParseQuery<ParseObject> query = ParseQuery.getQuery("messages");
		//query.whereEqualTo("playerName", "Joe Bob");
		query.fromLocalDatastore();
		query.findInBackground(new FindCallback<ParseObject>() {
		    public void done(List<ParseObject> messages,
		                     ParseException e) {
		        if (e == null) {
//		            Log.d("score", "Retrieved " + messages.size());
		            Iterator<ParseObject> it = messages.iterator();
		            while(it.hasNext())
						try {
							it.next().unpin();
							
						} catch (ParseException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
		        } else {
//		            Log.d("score", "Error: " + e.getMessage());
		        }
		        mAdapter.clear();
				mAdapter.notifyDataSetChanged();
				 Message msg = handler.obtainMessage();
                    msg.arg1 = 1;
                    handler.sendMessage(msg);
		    }
		});
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
	
}

