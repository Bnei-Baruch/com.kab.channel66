package com.kab.channel66;

import java.util.ArrayList;
import java.util.List;

import com.kab.channel66.CustomAdapter.ViewHolder;
import com.parse.ParseObject;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MessageAdapter extends ArrayAdapter<ParseObject> {

	Context context;
//	public MessageAdapter()
//	{
//		super();
//	}
//	public MessageAdapter(Context context, int resource) {
//		
//	mInflater = (LayoutInflater) context
//	.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		
//		// TODO Auto-generated constructor stub
//	}

//	public MessageAdapter(Context context, int list) {
//		super(context, list);
//		
//		mInflater = (LayoutInflater) context
//		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		// TODO Auto-generated constructor stub
//	}

	public MessageAdapter(Context context, int resource) {
		super(context, resource);
		this.context = context;
		// TODO Auto-generated constructor stub
	}

	private ArrayList<ParseObject> mData = new ArrayList<ParseObject>();
	
	private LayoutInflater mInflater;
//	public MessageAdapter() {
////		mInflater = (LayoutInflater) context
////				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//	}

	public void addItem(final ParseObject item) {
		mData.add(item);
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public ParseObject getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(mInflater==null)
		mInflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null) {
			holder = new ViewHolder();
			
				convertView = mInflater.inflate(R.layout.message_list_item, null);
				holder.textView = (TextView) convertView.findViewById(R.id.text);
				holder.dateView = (TextView) convertView.findViewById(R.id.date);
			
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.textView.setText(mData.get(position).getString("text"));
		holder.dateView.setText(DateFormat.format("yyyy-MM-dd hh:mm:ss",  mData.get(position).getLong("date")).toString() );
		
		

		return convertView;
	}

	public static class ViewHolder {
		public TextView textView;
		public TextView dateView;
	}

	public void addArray(List<ParseObject> messages) {
		// TODO Auto-generated method stub
		mData = new ArrayList<ParseObject>(messages) ;
	}
	public void add(ParseObject obj) {
		// TODO Auto-generated method stub
		mData.add(obj) ;
	}
	public void clear()
	{
		mData.clear();
	}

}
