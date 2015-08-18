package com.kab.channel66;

import java.util.ArrayList;
import java.util.TreeSet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ToggleButton;

class CustomAdapter extends BaseAdapter {

	private static final int TYPE_ITEM = 0;
	private static final int TYPE_SEPARATOR = 1;
	private static final int TYPE_ITEM_TOGGLE = 2;
	
	
	private ArrayList<String> mData = new ArrayList<String>();
	private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();
	private TreeSet<Integer> toggle = new TreeSet<Integer>();

	private LayoutInflater mInflater;

	public CustomAdapter(Context context) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void addItem(final String item, boolean btoggle) {
		mData.add(item);
		if(btoggle)
			toggle.add(mData.size() - 1);
		notifyDataSetChanged();
	}

	public void addSectionHeaderItem(final String item) {
		mData.add(item);
		sectionHeader.add(mData.size() - 1);
		notifyDataSetChanged();
	}

	@Override
	public int getItemViewType(int position) {
		if( sectionHeader.contains(position) )
				return TYPE_SEPARATOR;
		else return (toggle.contains(position)?TYPE_ITEM_TOGGLE:TYPE_ITEM);
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public String getItem(int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		int rowType = getItemViewType(position);

		if (convertView == null) {
			holder = new ViewHolder();
			switch (rowType) {
			case TYPE_ITEM_TOGGLE:
				convertView = mInflater.inflate(R.layout.list_item, null);
				holder.textView = (TextView) convertView.findViewById(R.id.text);
				holder.toggleButton = (ToggleButton)convertView.findViewById(R.id.buffering);
				holder.toggleButton.setVisibility(View.VISIBLE);
				break;
			case TYPE_ITEM:
				convertView = mInflater.inflate(R.layout.list_item, null);
				holder.textView = (TextView) convertView.findViewById(R.id.text);
				
				break;
			case TYPE_SEPARATOR:
				convertView = mInflater.inflate(R.layout.list_item_seperator, null);
				holder.textView = (TextView) convertView.findViewById(R.id.textSeparator);
				break;
			}
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.textView.setText(mData.get(position));

		return convertView;
	}

	public static class ViewHolder {
		public TextView textView;
		public ToggleButton toggleButton;
	}
	
	
}






//package com.javatechig;
//
//import android.app.ListActivity;
//import android.os.Bundle;
//
//public class SectionListView extends ListActivity {
//
//	private CustomAdapter mAdapter;
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		mAdapter = new CustomAdapter(this);
//		for (int i = 1; i < 30; i++) {
//			mAdapter.addItem("Row Item #" + i);
//			if (i % 4 == 0) {
//				mAdapter.addSectionHeaderItem("Section #" + i);
//			}
//		}
//		setListAdapter(mAdapter);
//	}
//
//} http://javatechig.com/android/listview-with-section-header-in-android