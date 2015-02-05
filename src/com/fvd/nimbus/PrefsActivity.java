package com.fvd.nimbus;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fvd.utils.AsyncTaskCompleteListener;
import com.fvd.utils.appSettings;
import com.fvd.utils.prefsEditListener;
import com.fvd.utils.serverHelper;

import android.R.string;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
//import android.util.Log;
import android.widget.Toast;

public class PrefsActivity extends PreferenceActivity implements AsyncTaskCompleteListener<String, String>{
	private String remoteFolder="";
	private String mode="";
	private ArrayList<CharSequence> entries=new ArrayList<CharSequence>();
	private ArrayList<CharSequence> entryValues=new ArrayList<CharSequence>();
	String fid= "";
	String ftitle="";
	String format="";
	private SharedPreferences prefs;
	
	@SuppressWarnings("deprecation")
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
	    addPreferencesFromResource(R.xml.layout_prefs);
	    serverHelper.getInstance().setCallback(this,this);
	    
	    prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    if(serverHelper.getInstance().getSession().length()>0) {
	    	remoteFolder=prefs.getString("remFolder", "My Notes");
	    	sendRequest("notes:getFolders", "");
	    }
	    else {
	    	remoteFolder="My Notes";
	    	Editor e = prefs.edit();
	    	e.putString("remFolder", "My Notes");
    		e.putString("remFolderId", "default");
	    	e.commit();
	    	
	    }
	    
	    
	    
	    //if("default".equals(remoteFolder)) remoteFolder="My Notes";
	    mode=prefs.getString("clipStyle", "1");
	    findPreference("remoteFolder").setTitle(remoteFolder);
	    if (mode.equals("1")) {
	    	findPreference("clipStyle").setTitle(getString(R.string.save_all_styles));
	    	((ListPreference)findPreference("clipStyle")).setValueIndex(0);
	    }
	    else {
	    	findPreference("clipStyle").setTitle(getString(R.string.without_style_saving));
	    	((ListPreference)findPreference("clipStyle")).setValueIndex(1);
	    }
	    
	    if ("1".equals(prefs.getString("userAgent", "1"))) {
	    	findPreference("userAgent").setTitle(getString(R.string.mobile));
	    	((ListPreference)findPreference("userAgent")).setValueIndex(0);
	    }
	    else {
	    	findPreference("userAgent").setTitle(getString(R.string.desktop));
	    	((ListPreference)findPreference("userAgent")).setValueIndex(1);
	    }
	    
	    format=prefs.getString("saveFormat","1");
	    if ("0".equals(format)) {
	    	findPreference("saveFormat").setTitle("PNG");
	    	((ListPreference)findPreference("saveFormat")).setValueIndex(1);
	    }
	    else {
	    	findPreference("saveFormat").setTitle("JPEG");
	    	((ListPreference)findPreference("saveFormat")).setValueIndex(0);
	    }
	    
	    
	    ((ListPreference)findPreference("remoteFolder")).setValueIndex(0);
	    findPreference("remoteFolder").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				fid= arg1.toString();
				for(int i=0; i<entryValues.size(); i++){
					if(entryValues.get(i).equals(fid)){
						ftitle=entries.get(i).toString();
						arg0.setTitle(ftitle);
						((ListPreference)findPreference("remoteFolder")).setValueIndex(i);
						break;
					}
				}
				if(ftitle.length()>0){
					Editor e = prefs.edit();
					e.putString("remFolder", ftitle);
    	    		e.putString("remFolderId", fid);
					e.commit();
				}
				return false;
			}
	    });
	    
	    findPreference("clipStyle").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				if(arg1.toString().equals("1")) arg0.setTitle(getString(R.string.save_all_styles));
				else arg0.setTitle(getString(R.string.without_style_saving));
				return true;
			}
	    });
	    
	    findPreference("userAgent").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				if(arg1.toString().equals("0")) arg0.setTitle(getString(R.string.desktop));
				else arg0.setTitle(getString(R.string.mobile));
				return true;
			}
	    });
	    
	    findPreference("saveFormat").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				if(arg1.toString().equals("0")) arg0.setTitle("PNG");
				else arg0.setTitle("JPEG");
				return true;
			}
	    });
	}
	
	String fromUTF(String is) throws UnsupportedEncodingException
	{
		return URLDecoder.decode(is,"UTF-16");
	}
	
	@Override
    public void onTaskComplete(String result, String action)
    {
			int c=0;
			if(result.length()==0) return;
    		try{
    			result = URLDecoder.decode(result,"UTF-16"); 
            	JSONObject root = new JSONObject(result);
            	int error = root.getInt("errorCode");
            	if (error == 0){
            		String id="";
            		String title="";
            		String cid=prefs.getString("remFolderId", "default");
            		
            		entries.clear();
            		entryValues.clear();
            		JSONArray arr =  root.getJSONObject("body").getJSONArray("notes");
            		for(int i=0; i<arr.length();i++){
            			JSONObject obj = new JSONObject(arr.getString(i));
            			title= obj.getString("title");
            			//title = fromUTF(title);
            			entries.add(title);
            			id=obj.getString("global_id");
            			entryValues.add(id);
            			//Log.i("nimbus", String.format("%s , %s", ftitle,fid));
            			if(cid.equals(id)) c=i;
            			ListPreference lp = (ListPreference)findPreference("remoteFolder");
                		lp.setEntries(entries.toArray(new CharSequence[entries.size()]));
                		lp.setEntryValues(entryValues.toArray(new CharSequence[entryValues.size()]));
                		lp.setValueIndex(c);
            		}
            		
            	} else Toast.makeText(getApplicationContext(), String.format("Error: %s",serverHelper.errorMsg(error)), Toast.LENGTH_LONG).show();
            }
            catch (Exception Ex){
            	appSettings.appendLog("prefs:onTaskComplete "+Ex.getMessage());
            }
    }
	
	private void sendRequest(String action, String data){
		serverHelper.getInstance().sendRequest(action, data,"");
	}
}
