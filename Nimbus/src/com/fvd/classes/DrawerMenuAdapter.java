package com.fvd.classes;

import com.fvd.nimbus.R;

import android.R.raw;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;



public class DrawerMenuAdapter extends ArrayAdapter<String> {
	Context ctx;
	boolean large;
	public DrawerMenuAdapter(Context context, String[] list) {
		super(context, R.layout.menu_list_layout, list);
		ctx=context;
		large=list.length==4;
	}
	
	int getImage(int pos){
		switch (pos) {
		case 0:
			return R.drawable.camera_menu;
		case 1:
			return R.drawable.icon_gallery;
		case 2:
			if(large) return R.drawable.clipper;
			else return R.drawable.icon_pdf;
		case 3:
			return R.drawable.icon_pdf;
		default:
			return R.drawable.icon_pdf;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String cat = getItem(position);

		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.menu_list_layout, null);
		}
		
		((TextView) convertView.findViewById(R.id.ttext)).setText(cat);
		((ImageView)convertView.findViewById(R.id.itemIcon)).setImageResource(getImage(position));
		
		/*if(folder_id.equals(cat.id)){
			((TextView) convertView.findViewById(R.id.ttext)).setTextSize(20);// Typeface(null, Typeface.BOLD);
			((TextView) convertView.findViewById(R.id.ttext)).setTypeface(null, Typeface.BOLD);
			((TextView) convertView.findViewById(R.id.ttext)).setTextColor(getResources().getColor(R.color.text_normal));
			((ImageView)convertView.findViewById(R.id.itemIcon)).setImageResource(R.drawable.icon_folder);
		}
		else {
			((TextView) convertView.findViewById(R.id.ttext)).setTextSize(15);//.setTypeface(null, Typeface.NORMAL);
			((TextView) convertView.findViewById(R.id.ttext)).setTypeface(null, Typeface.NORMAL);
			((TextView) convertView.findViewById(R.id.ttext)).setTextColor(getResources().getColor(R.color.text_normal));
			((ImageView)convertView.findViewById(R.id.itemIcon)).setImageResource(R.drawable.icon_folder);
		}*/
	   return convertView;
	}
}


