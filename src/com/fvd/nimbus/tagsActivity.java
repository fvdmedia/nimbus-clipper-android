package com.fvd.nimbus;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fvd.classes.BugReporter;
import com.fvd.classes.DataExchange;
import com.fvd.classes.FolderListItem;
import com.fvd.classes.FolderListLoader;
import com.fvd.classes.FoldersListAdapter;
import com.fvd.classes.GroupItem;
import com.fvd.classes.ResultListener;
import com.fvd.classes.ShowHideOnScroll;
import com.fvd.nimbus.R.string;
import com.fvd.utils.AsyncTaskCompleteListener;
import com.fvd.utils.FolderItem;
import com.fvd.utils.helper;
import com.fvd.utils.serverHelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.ViewAnimator;


public class tagsActivity extends Activity implements OnClickListener{

	//private ArrayAdapter<FolderItem> adapter;

	private String selectedItem;
	private final Context context = this;
	ArrayList<FolderListItem> list = new ArrayList<FolderListItem>();
	ArrayList<tagItem> tags=new ArrayList<tagItem>();
	boolean[] mCheckedItems={};
	String[] checkCatsName={};
	String s_tags="";
	DataExchange data;
	
	/*private class TextAdapter extends ArrayAdapter<FolderItem> {

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
	*/
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
        overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
		setContentView(R.layout.layout_folders);
		((Button)findViewById(R.id.lfbSave)).setOnClickListener(this);
		((Button)findViewById(R.id.lfbCancel)).setOnClickListener(this);
		(findViewById(R.id.lfBtnAdd)).setOnClickListener(this);
		(findViewById(R.id.lfBtnAdd2)).setOnClickListener(this);
		OnItemClickListener itemListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				// TODO Auto-generated method stub
				FolderListItem item = (FolderListItem)parent.getItemAtPosition(position);
				folder_id= item.getGlobalId();
				//adapter.notifyDataSetChanged();
			}
		
		};
		
		findViewById(R.id.btnFolders).setSelected(true);
		Intent intent = getIntent();
		data =(DataExchange)intent.getExtras().getSerializable("xdata");
		folder_id=data.getId();// intent.getStringExtra("current");
		((EditText)findViewById(R.id.fetTitle)).setText(data.getTitle());
		if(folder_id==null || folder_id=="") folder_id="default";
		
		try{
			JSONObject root = new JSONObject(data.getData());
			String id="";
	        String parent_id="";
	   		String title="";
	  		JSONArray arr =  root.getJSONObject("body").getJSONArray("notes");
	  		for(int i=0; i<arr.length();i++){
			 			JSONObject obj = new JSONObject(arr.getString(i));
			   			title= obj.getString("title");
			   			
			            id=obj.getString("global_id");
			            parent_id=obj.getString("parent_id");
			            if(title!=null && parent_id!=null&& id!=null)
			            	list.add(new FolderListItem(id, parent_id, title, 0));
	  		}
	  		
	  		((ListView)findViewById(R.id.ilist)).setOnTouchListener(new ShowHideOnScroll(findViewById(R.id.lfBtnAdd)));
	  		((ListView)findViewById(R.id.itags)).setOnTouchListener(new ShowHideOnScroll(findViewById(R.id.lfBtnAdd2)));
	  		((ListView)findViewById(R.id.ilist)).setAdapter(new FoldersListAdapter(this, list, folder_id, new FoldersListAdapter.OnFolderListClickListener() {
				
				@Override
				public void onLongClick(FolderListItem groupItem) {
					// TODO Auto-generated method stub
					final String parent = groupItem.getGlobalId();
					final int level = groupItem.getLevel();
					final String nid=helper.getRandomString(12);
					InputBox(getString(R.string.mk_sfolder),getString(R.string.mkh_folder), new ResultListener<String>() {
						
						@Override
						public void onResult(final String result) {
							// TODO Auto-generated method stub
							//final String title=result;
							folder_id=nid;
							if(result!=null && result!=""){
								serverHelper.getInstance().MkFolder(result, nid, parent, new AsyncTaskCompleteListener<String, String>() {
									@Override
									public void onTaskComplete(String r, String adv) {
										// TODO Auto-generated method stub
										serverHelper.getInstance().completed();
										if(r.contains("\"errorCode\":0"))
											((FoldersListAdapter)((ListView)findViewById(R.id.ilist)).getAdapter()).add(new FolderListItem(nid, parent, result, level+1));
									}
								});
							}
						}
					});
					
				}
				
				@Override
				public void onClick(FolderListItem groupItem) {
					// TODO Auto-generated method stub
					folder_id= groupItem.getGlobalId();
					//adapter.notifyDataSetChanged();
				}
			}));
	  		
		}
		catch(Exception e){e.printStackTrace();}
		            	        		
		
		
		
		
		/*if(intent.getBooleanExtra("hideTags", false)){
			findViewById(R.id.eTag).setVisibility(View.GONE);
		}*/
		
		
		
		//adapter = new TextAdapter(this);
		//((ListView)findViewById(R.id.ilist)).setAdapter(adapter);

		//((ListView)findViewById(R.id.ilist)).setOnItemClickListener(itemListener);
		//((EditText)findViewById(R.id.eTag)).setOnEditorActionListener(new DoneOnEditorActionListener());
		
		serverHelper.getInstance().sendCallbackRequest("notes:getTags", "", new AsyncTaskCompleteListener<String, String>() {
			
			@Override
			public void onTaskComplete(String result, String adv) {
				// TODO Auto-generated method stub
				serverHelper.getInstance().completed();
				try{
	            	JSONObject root = new JSONObject(result);
	            	int error = root.getInt("errorCode");
	            	if (error == 0){
	            		JSONArray arr =  root.getJSONObject("body").getJSONArray("tags");
    	        		for(int i=0; i<arr.length();i++){
    	        			tags.add(new tagItem(arr.getString(i), false));
    	        		}
    	        		//if(tags.size()>0) findViewById(R.id.ibtTags).setVisibility(View.VISIBLE);
    	        		if(tags.size()==0) tags.add(new tagItem("androidclipper",false));
	            	}
	            	((ListView)findViewById(R.id.itags)).setAdapter(new CheckArrayAdapter(tagsActivity.this));
				}
	            catch (Exception e) {
						// TODO: handle exception
				}
			}
			
		});
		
	}
	String folder_id="default";

	private void InputBox(String title, String hint, final ResultListener<String> complete) {

		  LayoutInflater layoutInflater = LayoutInflater.from(this);
		  View promptView = layoutInflater.inflate(R.layout.addnewcategory, null);
		  final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		  alert.setTitle(title);
		  alert.setView(promptView);

		  final EditText input = (EditText) promptView
		    .findViewById(R.id.etCategory);

		  
		  input.setHint(hint);
		  input.setTextColor(Color.BLACK);
		  final InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			
		  alert.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int whichButton) {
				    String newCategoryName = input.getText().toString();
				    if(imm!=null){
						imm.hideSoftInputFromWindow(input.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					}
				    complete.onResult(newCategoryName);
				    }
				  });

		  alert.setNegativeButton(getString(R.string.cancel),
		    new DialogInterface.OnClickListener() {
		     public void onClick(DialogInterface dialog, int whichButton) {
		    	 if(imm!=null){
						imm.hideSoftInputFromWindow(input.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					}
		    	 
		      // Canceled.
		      /*Toast.makeText(getApplicationContext(),
		        "Ok Clicked", Toast.LENGTH_SHORT).show();*/
		     }
		    });

		  // create an alert dialog
		  AlertDialog alert1 = alert.create();

		  alert1.show();
		  input.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				input.requestFocus();
				if(imm!=null){
					imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
				}
			}
		},50);

		 }

	public void onTabClick(View v) {
		switch (v.getId()) 
		{
			case R.id.btnFolders:
				findViewById(R.id.folder_s).setVisibility(View.VISIBLE);
				findViewById(R.id.folder_ns).setVisibility(View.INVISIBLE);
				findViewById(R.id.tag_s).setVisibility(View.INVISIBLE);
				findViewById(R.id.tag_ns).setVisibility(View.VISIBLE);
				((ViewAnimator)findViewById(R.id.ftop_switcher)).setDisplayedChild(0);
				break;
			case R.id.btnTags:
				findViewById(R.id.folder_s).setVisibility(View.INVISIBLE);
				findViewById(R.id.folder_ns).setVisibility(View.VISIBLE);
				findViewById(R.id.tag_s).setVisibility(View.VISIBLE);
				findViewById(R.id.tag_ns).setVisibility(View.INVISIBLE);
				((ViewAnimator)findViewById(R.id.ftop_switcher)).setDisplayedChild(1);
				break;
		}
		
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch (v.getId()) 
		{
		    case R.id.lfbSave:
		    	try{
		    	boolean b=false;
		    	StringBuilder state = new StringBuilder();
				
				if(tags==null|| tags.size()==0){
					state.append("androidclipper");
				} else
				for (int i = 0; i < tags.size(); i++) {
					if(tags.get(i).checked){
						if(b) state.append(", " + tags.get(i).title);
						else {
							b=true;
							state.append(tags.get(i).title);
						}
					}
				}
					s_tags=state.toString();
					String ttl = ((EditText)findViewById(R.id.fetTitle)).getText().toString();
					if(folder_id==null || folder_id=="") folder_id="default";
			    	Intent intent = new Intent();
			    	if(data==null) data = new DataExchange();
			    	data.setTags(s_tags.length()>0?s_tags:"androidclipper");
			    	data.setTitle(ttl);
			    	data.setId(folder_id);
			    	//BugReporter.Send("tagsActivity", String.format("%s. %s. %s", data.getId(), data.getTags(),  data.getTitle()));
			    	intent.putExtra("xdata", data);
					/*intent.putExtra("id", folder_id);
					intent.putExtra("tag", s_tags.length()>0?s_tags:"androidclipper");
					intent.putExtra("title", ttl);*/
			    	setResult(RESULT_OK, intent);
			    	finish();
		    	}
		    	catch (Exception e) {
					// TODO: handle exception
		    		BugReporter.Send("tagsActivity", e.getMessage());
				}
		    	break;
		    case R.id.lfbCancel:
		    	finish();
		    	break;
		    case R.id.lfBtnAdd:
		    case R.id.lfBtnAdd2:
		    	final int m=((ViewAnimator)findViewById(R.id.ftop_switcher)).getDisplayedChild();
		    	InputBox(m==0?getString(R.string.mk_folder):getString(R.string.mk_tag),m==0?getString(R.string.mkh_folder):getString(R.string.mkh_tag), new ResultListener<String>() {
					
					@Override
					public void onResult(final String result) {
						// TODO Auto-generated method stub
						if(result!=null && result!="")
						switch (m) {
						case 0:
							
							final String nid=helper.getRandomString(12);
							//((FoldersListAdapter)((ListView)findViewById(R.id.ilist)).getAdapter()).add(new FolderListItem(helper.getRandomString(12), "root", result, 0));
							folder_id=nid;
							if(result!=null && result!=""){
								Toast.makeText(getApplicationContext(), getString(R.string.please_wait), Toast.LENGTH_SHORT).show();
								serverHelper.getInstance().MkFolder(result, nid, "root", new AsyncTaskCompleteListener<String, String>() {
									@Override
									public void onTaskComplete(String r, String adv) {
										// TODO Auto-generated method stub
										serverHelper.getInstance().completed();
										serverHelper.getInstance().hideProgress();
										if(r.contains("\"errorCode\":0")){
											((FoldersListAdapter)((ListView)findViewById(R.id.ilist)).getAdapter()).add(new FolderListItem(nid, "root", result, 0));
										}
										else if(r=="" || r==null) 
											Toast.makeText(getApplicationContext(), getString(R.string.chk_intenet), Toast.LENGTH_SHORT).show();
										else Toast.makeText(getApplicationContext(), getString(R.string.sync_err), Toast.LENGTH_SHORT).show();
									}
								});
							}
							break;
						case 1:
							tags.add(new tagItem(result, true));
							
							((CheckArrayAdapter)((ListView)findViewById(R.id.itags)).getAdapter()).notifyDataSetChanged();
							break;
						default:
							break;
						}
					}
				});
		    	break;
		}
	}
	
	public void onTagsClick(View v){
		showDialog(0);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		

		case 0:
			//ArrayList<boolean> mChecked=new ArrayList<boolean>();
			//checkCatsName= tags.toArray(String);
			checkCatsName = new String[tags.size()];
			checkCatsName = tags.toArray(checkCatsName);
			/*for (String string : tags) {
				mChecked.add(false);
			}*/
			mCheckedItems = new boolean[tags.size()];
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getResources().getString(R.string.select_tags))
					.setCancelable(false)

					.setMultiChoiceItems(checkCatsName, mCheckedItems,
							new DialogInterface.OnMultiChoiceClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which, boolean isChecked) {
									mCheckedItems[which] = isChecked;
								}
							})

					// Добавляем кнопки
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									StringBuilder state = new StringBuilder();
									boolean b=false;
									for (int i = 0; i < checkCatsName.length; i++) {
										if(mCheckedItems[i]){
											if(b) state.append(", " + checkCatsName[i]);
											else {
												b=true;
												state.append(checkCatsName[i]);
											}
										}
									}
									s_tags=state.toString();
									//(( TextView )findViewById(R.id.eTag)).setText(s_tags);
									
									
								}
							})

					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									dialog.cancel();
								}
							});
			return builder.create();

		default:
			return null;
		}
	}
	
	static class tagItem{
		public String title;
		public boolean checked;
		public tagItem(String t, boolean b){
			title = t;
			checked = b;
		}
	}
	
	static class ViewHolder {
		public CheckBox checkView;
		public TextView textView;
	}
	
	class CheckArrayAdapter extends ArrayAdapter<tagItem> {
		private final Activity context;
		//private final String[] names;

		public CheckArrayAdapter(Activity context) {
			super(context, R.layout.check_item, tags);
			this.context = context;
			//this.names = names;
		}

		// Класс для сохранения во внешний класс и для ограничения доступа
		// из потомков класса
		

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			

			ViewHolder holder;
			
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater inflater = context.getLayoutInflater();
				rowView = inflater.inflate(R.layout.check_item, null, true);
				holder = new ViewHolder();
				holder.textView = (TextView) rowView.findViewById(R.id.label);
				holder.checkView = (CheckBox) rowView.findViewById(R.id.check);
				holder.checkView.setOnCheckedChangeListener(myCheckChangList);
				holder.checkView.setTag(position);
				rowView.setTag(holder);
				rowView.setClickable(true);
				rowView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						ViewHolder h=(ViewHolder)arg0.getTag();
						h.checkView.setChecked(!h.checkView.isChecked());
						tags.get((Integer) h.checkView.getTag()).checked=h.checkView.isChecked();
					}
				});
			} else {
				holder = (ViewHolder) rowView.getTag();
			}

			holder.textView.setText(tags.get(position).title);
			holder.checkView.setChecked(tags.get(position).checked);
			// Изменение иконки для Windows и iPhone
			//String s = tags.get(position);
			

			return rowView;
		}
		
	}
	
	 OnCheckedChangeListener myCheckChangList = new OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView,
		        boolean isChecked) {
		      // меняем данные товара (в корзине или нет)
		      tags.get((Integer) buttonView.getTag()).checked = isChecked;
		    }

			
		  };
}
