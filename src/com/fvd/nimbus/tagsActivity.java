package com.fvd.nimbus;

import java.util.ArrayList;
import java.util.Arrays;

import com.fvd.utils.FolderItem;

import android.R.string;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Selection;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;



public class tagsActivity extends Activity implements OnClickListener{

	private ArrayAdapter<FolderItem> adapter;

	private String selectedItem;
	private final Context context = this;
	ArrayList<FolderItem> list = new ArrayList<FolderItem>();
	private class DoneOnEditorActionListener implements TextView.OnEditorActionListener
    {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
        {
            if (event!=null && event.getKeyCode()==KeyEvent.KEYCODE_ENTER && event.getAction()==KeyEvent.ACTION_DOWN)
            {
            	String tx=v.getText().toString();
            	if(tx.length()>0&&tx.lastIndexOf(",")<tx.length()-1) 
            		{	tx+=",";
            			v.setText(tx);
            			(( EditText )findViewById(R.id.eTag)).setSelection(tx.length());
            		}
                return true;
            }
            return false;
        }
    }
	
	private class TextAdapter extends ArrayAdapter<FolderItem> {

		public TextAdapter(Context context) {
			super(context, R.layout.folders_list_layout, list);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			FolderItem cat = getItem(position);

			if (convertView == null) {
				convertView = LayoutInflater.from(getContext())
						.inflate(R.layout.folders_list_layout, null);
			}
			((TextView) convertView.findViewById(R.id.ttext))
					.setText(cat.name);
			
			
			if(folder_id.equals(cat.id)){
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
			}
		   return convertView;
		}
	}
	public void hideSoftKeyboard() {
	    if(getCurrentFocus()!=null) {
	        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
	        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
	    }
	}
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try{
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        catch (Exception e){
        	e.printStackTrace();
        }
		setContentView(R.layout.layout_folders);
		((Button)findViewById(R.id.bSave)).setOnClickListener(this);
		((Button)findViewById(R.id.bCancel)).setOnClickListener(this);
		OnItemClickListener itemListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				FolderItem item = (FolderItem)parent.getItemAtPosition(position);
				folder_id= item.id;
				adapter.notifyDataSetChanged();
			}
		
		};
		Intent intent = getIntent();
		ArrayList<String> items=intent.getStringArrayListExtra("items");
		for(int i=0; i<items.size(); i++){
			list.add(new FolderItem(items.get(i)));
		}
		folder_id=intent.getStringExtra("current");
		if(intent.getBooleanExtra("hideTags", false)){
			findViewById(R.id.eTag).setVisibility(View.GONE);
		}
		adapter = new TextAdapter(this);
		((ListView)findViewById(R.id.ilist)).setAdapter(adapter);

		((ListView)findViewById(R.id.ilist)).setOnItemClickListener(itemListener);
		((EditText)findViewById(R.id.eTag)).setOnEditorActionListener(new DoneOnEditorActionListener());
	}
	
	String folder_id="default";

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch (v.getId()) 
		{
		    case R.id.bSave:
		    	Intent intent = new Intent();
		    	String tag = ((TextView)findViewById(R.id.eTag)).getText().toString();
				intent.putExtra("id", folder_id);
				intent.putExtra("tag", tag.length()>0?tag:"androidclipper");	
		    	setResult(RESULT_OK, intent);
		    	finish();
		    	break;
		    case R.id.bCancel:
		    	finish();
		    	break;
		}
	}
}
