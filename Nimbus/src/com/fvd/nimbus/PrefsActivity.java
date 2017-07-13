package com.fvd.nimbus;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fvd.utils.AsyncTaskCompleteListener;
import com.fvd.utils.appSettings;
import com.fvd.utils.prefsEditListener;
import com.fvd.utils.serverHelper;

import android.R.string;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
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
	int quality=90;
	String[] qual;
	String[] startArr;
	private SharedPreferences prefs;
	
	@SuppressWarnings("deprecation")
	@Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    //overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
	    overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
	    addPreferencesFromResource(R.xml.layout_prefs);
	    getListView().setBackgroundColor(Color.TRANSPARENT);

	    getListView().setCacheColorHint(Color.TRANSPARENT);

	    getListView().setBackgroundColor(Color.rgb(255, 255, 255));
	    serverHelper.getInstance().setCallback(this,this);
	    qual=getResources().getStringArray(R.array.save_quality);
	    startArr=getResources().getStringArray(R.array.on_startup_titles);
	    prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    if(appSettings.sessionId.length()>0) {
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
	    
	    listDirs();
	    String sn=prefs.getString("startNew", "1");
	    findPreference("startNew").setTitle("1".equals(sn)?startArr[1]:startArr[0]);
	    ((ListPreference)findPreference("startNew")).setValueIndex("1".equals(sn)?1:0);
	    
	    //if("default".equals(remoteFolder)) remoteFolder="My Notes";
	    mode=prefs.getString("clipStyle", "1");
	    findPreference("remoteFolder").setTitle(remoteFolder);
	    findPreference("imageFolder").setTitle(prefs.getString("imgFolder", appSettings.SaveDir));
	    
	    
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
	    
	    try{
	    	quality=Integer.parseInt(prefs.getString("photoQuality", "90"));
	    } catch (Exception e){
	    	
	    }
	    int iq=3;
	    switch (quality){
		    case 40:
		    	iq=0;
		    	break;
		    case 60:
		    	iq=1;
		    	break;
		    case 90:
		    	iq=2;
		    	break;
		    case 95:
		    	iq=3;
		    	break;
		    case 100:
		    	iq=4;
		    	break;
		    	default: iq=3;
	    }
	    findPreference("photoQuality").setTitle(qual[iq]);
	    
	    try{
	    	((ListPreference)findPreference("photoQuality")).setValueIndex(iq);
	    }
		catch (Exception e){
			Log.e("ss", e.getMessage());
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
	    
	    //((ListPreference)findPreference("imageFolder")).setValueIndex(0);
	    findPreference("imageFolder").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				String path= arg1.toString();
				String xn="";
				for(int i=0; i<Values.size(); i++){
					if(Values.get(i).equals(path)){
						xn=Names.get(i).toString();
						arg0.setTitle(xn);
						((ListPreference)findPreference("imageFolder")).setValueIndex(i);
						break;
					}
				}
				if(xn.length()>0){
					Editor e = prefs.edit();
					e.putString("imgFolder", xn);
    	    		e.putString("imgFolderPath", path);
					e.commit();
					appSettings.img_path=path;
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
	    
	    findPreference("startNew").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				arg0.setTitle("1".equals(arg1.toString())?startArr[1]:startArr[0]);
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
	    
	    findPreference("photoQuality").setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				/*if(arg1.toString().equals("0")) arg0.setTitle("PNG");
				else arg0.setTitle("JPEG");*/
				quality=Integer.parseInt(arg1.toString());
				appSettings.photo_quality=quality;
				int iq=3;
				switch (quality){
			    case 40:
			    	iq=0;
			    	break;
			    case 60:
			    	iq=1;
			    	break;
			    case 90:
			    	iq=2;
			    	break;
			    case 95:
			    	iq=3;
			    	break;
			    case 100:
			    	iq=4;
			    	break;
			    	default: iq=3;
		    }
		    arg0.setTitle(qual[iq]);
				return true;
			}
	    });
	}
	List<File> m_entries = new ArrayList< File >();
    File m_currentDir=null;
    List<CharSequence> Names=new ArrayList<CharSequence>();
    List<CharSequence> Values=new ArrayList<CharSequence>();
	private void listDirs()
    {
		if(m_currentDir==null) m_currentDir = Environment.getExternalStorageDirectory();
        m_entries.clear();
 
        // Get files
        File[] files = m_currentDir.listFiles();
 
        // Add the ".." entry
        /*if ( m_currentDir.getParent() != null )
            m_entries.add( new File("..") );*/
 
        if ( files != null )
        {
            for ( File file : files )
            {
                if ( !file.isDirectory() )
                    continue;
                if(!file.getName().startsWith(".") && !file.getName().startsWith("_"))
                m_entries.add( file );
            }
        }
 
        Collections.sort( m_entries, new Comparator<File>() { 
                public int compare(File f1, File f2)
                {
                    return f1.getName().toLowerCase().compareTo( f2.getName().toLowerCase() );
                }
        } );
        
        Names.clear();
        Values.clear();
        for (File f : m_entries) {
			Names.add(f.getName());
			Values.add(f.getPath());
		}
        
        ListPreference lp = (ListPreference)findPreference("imageFolder");
		lp.setEntries(Names.toArray(new CharSequence[Names.size()]));
		lp.setEntryValues(Values.toArray(new CharSequence[Values.size()]));
		//lp.setValueIndex(c);
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
