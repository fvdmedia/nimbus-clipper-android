package com.fvd.nimbus;

import java.net.URLDecoder;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fvd.classes.BugReporter;
import com.fvd.classes.DataExchange;
import com.fvd.utils.AsyncTaskCompleteListener;
import com.fvd.utils.FolderItem;
import com.fvd.utils.appSettings;
import com.fvd.utils.serverHelper;

import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.ZoomDensity;
import android.widget.Toast;

public class previewActivity extends Activity implements OnClickListener, AsyncTaskCompleteListener<String, String>{
	
	//private String content ="";
	private String title ="";
	private String url ="";
	private DataExchange cl;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try{
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        catch (Exception e){
        	e.printStackTrace();
        }
        //BugReporter.Send("preview.onCreate", "p1");
        setContentView(R.layout.screen_preview);
        findViewById(R.id.bSave2Nimbus).setOnClickListener(this);
        findViewById(R.id.bCancel).setOnClickListener(this);
        WebSettings settings = ((WebView )findViewById(R.id.wvPreview)).getSettings();
        settings.setDefaultZoom(ZoomDensity.FAR);
        settings.setDefaultTextEncodingName("utf-8");
        try{
        	//BugReporter.Send("preview.onCreate", "p2");
	        cl = (DataExchange) getIntent().getExtras().getSerializable("content");
	        //String content = cl.getContent();//getIntent().getStringExtra("content").toString();
	        title = "";
	        url = "";
	        
	        //BugReporter.Send("preview.onCreate", "p3");
	        if(cl!=null && cl.getContent().length()>0){
	        	WebView wv=( WebView )findViewById(R.id.wvPreview);
	        	//BugReporter.Send("preview.onCreate", "p4");
	        	wv.loadDataWithBaseURL("about:blank", toUtf8(cl.getContent()), "text/html", "", null);
	        	wv.getSettings().setBuiltInZoomControls(true);
	        	wv.getSettings().setDisplayZoomControls(false);
	        	wv.setWebViewClient(new WebViewClient() {
	                @Override
	                public boolean shouldOverrideUrlLoading(WebView view, String url)
	                {
	                	return true;
	                }
	            });
	        }
        }
        catch (Exception e){
        	BugReporter.Send("preview.onCreate", e.getMessage());
        }
        if (cl==null) cl=new DataExchange();
        
     }
	
	String toUtf8(String text){
		String mask="<html><body>%s</body></html>";
		return String.format(mask, text);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (requestCode==1){
    		if (resultCode==RESULT_OK){
    			Intent intent = new Intent();
    			intent.putExtra("content", cl);
    			intent.putExtra("parent", data.getStringExtra("id"));
    			intent.putExtra("tag", data.getStringExtra("tag"));
		    	setResult(RESULT_OK, intent);
		    	finish();
    		} 
    		
    	}
    }
	
	public void onClick(View v)
	{
		Intent intent = new Intent();
		switch (v.getId()) 
		{
		    case R.id.bSave2Nimbus:
		    	intent.putExtra("content", cl);
		    	setResult(RESULT_OK, intent);
		    	finish();
		        break;
		        default:
		        		intent.putExtra("content", cl);
		    			setResult(RESULT_CANCELED, intent);
		    			finish();
		        	break;
	    }
		
	}

	@Override
	public void onTaskComplete(String result, String adv) {
		// TODO Auto-generated method stub
		if(result.length()==0) return;
		ArrayList<String>items=new ArrayList<String>();
		try{
			result = URLDecoder.decode(result,"UTF-16"); 
        	JSONObject root = new JSONObject(result);
        	int error = root.getInt("errorCode");
        	if (error == 0){
        		String id="";
        		String title="";
        		JSONArray arr =  root.getJSONObject("body").getJSONArray("notes");
        		for(int i=0; i<arr.length();i++){
        			JSONObject obj = new JSONObject(arr.getString(i));
        			title= obj.getString("title");
        			id=obj.getString("global_id");
        			items.add(title+"::"+id);
        		}
        		Intent intent = new Intent(getApplicationContext(), tagsActivity.class);
        		intent.putExtra("items", items);
    	    	startActivityForResult(intent,1);
        		
        	} else Toast.makeText(getApplicationContext(), String.format("Error: %s",serverHelper.errorMsg(error)), Toast.LENGTH_LONG).show();
        }
        catch (Exception Ex){
        	appSettings.appendLog("prefs:onTaskComplete "+Ex.getMessage());
        }
		
	}

}
