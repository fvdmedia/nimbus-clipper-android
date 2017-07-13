package com.fvd.nimbus;

import java.net.URLDecoder;
import java.net.URLEncoder;
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
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
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
import android.webkit.WebSettings.PluginState;
import android.webkit.WebSettings.ZoomDensity;
import android.widget.Toast;

public class previewActivity extends Activity implements OnClickListener, AsyncTaskCompleteListener<String, String>{
	
	//private String content ="";
	/*private String title ="";*/
	private String url ="";
	private DataExchange cl;
	boolean doOverride=true;
	String code="";
	final String redirect="nimbusclipper:authorizationFinished";
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
        findViewById(R.id.bPocketConnect).setOnClickListener(this);
        WebSettings settings = ((WebView )findViewById(R.id.wvPreview)).getSettings();
        //settings.setDefaultZoom(ZoomDensity.FAR);
        if(isTablet()){
	        settings.setLoadWithOverviewMode(true);
	        settings.setUseWideViewPort(true);
        }
        settings.setDefaultTextEncodingName("utf-8");
        try{
        	//BugReporter.Send("preview.onCreate", "p2");
        	Intent i=getIntent();
            cl = (DataExchange) i.getExtras().getSerializable("content");
            
            if(i.hasExtra("url")){
            	url=i.getStringExtra("url");
            }
            if(i.hasExtra("pocket")){
            	if(appSettings.pocket_code==""){
            		//findViewById(R.id.bSave2Nimbus).setVisibility(View.GONE);
            	} else {
            		findViewById(R.id.bPocketConnect).setVisibility(View.GONE);
            		pocketSend();
            	}
            	
            } else {
            	findViewById(R.id.bPocketConnect).setVisibility(View.GONE);
            }
	        //String content = cl.getContent();//getIntent().getStringExtra("content").toString();
	        /*title = "";
	        url = "";*/
	        
	        //BugReporter.Send("preview.onCreate", "p3");
	        if(cl!=null && cl.getContent().length()>0){
	        	WebView wv=( WebView )findViewById(R.id.wvPreview);
	        	
	        	//wv.setInitialScale(0.5);
	        	//BugReporter.Send("preview.onCreate", "p4");
	        	wv.loadDataWithBaseURL("about:blank", toUtf8(cl.getContent()), "text/html", "", null);
	        	//wv.loadUrl("https://getpocket.com/auth/authorize?mobile=1&request_token=b48e02ab-88d3-38b7-29aa-e10643&redirect_uri=pocketapp1234:authorizationFinished");
	        	wv.getSettings().setBuiltInZoomControls(true);
	        	wv.getSettings().setDisplayZoomControls(false);
	        	
	        	wv.setWebViewClient(new WebViewClient() {
	                @Override
	                public boolean shouldOverrideUrlLoading(WebView view, String url)
	                {
	                	return true;
	                }
	                
	                /*@Override
	                public void onPageFinished (WebView view, String url) {

	                }*/

	            });
	        }
        }
        catch (Exception e){
        	BugReporter.Send("preview.onCreate", e.getMessage());
        }
        if (cl==null) cl=new DataExchange();
        
     }
	
	boolean isTablet(){
    	/*Display d = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    	Point size = new Point();
    	size.x = d.getWidth();
    	size.y = d.getHeight();*/
    	//boolean b = size.x>1280 || size.y>1280;//size.x>480 && size.y>800||size.x>800 && size.y>480;
    	boolean b=getResources().getInteger(R.integer.is_tablet)!=0;
    	//boolean b=(this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)>= Configuration.SCREENLAYOUT_SIZE_LARGE;
     	if (!b) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		return b;
    }
	
	/*@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        WebSettings settings = ((WebView )findViewById(R.id.wvPreview)).getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        //((WebView )findViewById(R.id.wvPreview)).loadDataWithBaseURL("about:blank", toUtf8(cl.getContent()), "text/html", "", null);
    }*/
	
	void pocketSend(){
		serverHelper.getInstance().postPocket("https://getpocket.com/v3/add", 
				serverHelper.buildPocketQuery(String.format("\"url\":\"%s\", \"title\":\"%s\", \"access_token\":\"%s\"",serverHelper.urlEncode(url), cl.getTitle(), appSettings.pocket_code)),true, new AsyncTaskCompleteListener<String, String>() {
			
			@Override
			public void onTaskComplete(String result, String adv) {
				// TODO Auto-generated method stub
				try{		
					serverHelper.getInstance().completed();
					code="";
					JSONObject root = new JSONObject(result);
					/*if(root.has("code")){
						code=root.getString("code");
						doOverride=false;
						WebView wv =( WebView )findViewById(R.id.wvPreview);
						wv.loadUrl(String.format("https://getpocket.com/auth/authorize?request_token=%s&redirect_uri=%s", code,redirect));
						//xwv.naviga;
					}*/
					Toast.makeText(previewActivity.this, getString(R.string.copy_saved), Toast.LENGTH_SHORT).show();
					
				} catch (Exception e){}
			}
		});
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
    	} else if(requestCode==2){
    		if (resultCode==RESULT_OK){
    			findViewById(R.id.bPocketConnect).setVisibility(View.GONE);
    			findViewById(R.id.bSave2Nimbus).setVisibility(View.VISIBLE);
    			pocketSend();
    			
    		}
    		/*findViewById(R.id.bPocketConnect).setVisibility(View.GONE);
			findViewById(R.id.bSave2Nimbus).setVisibility(View.VISIBLE);
			if(data.hasExtra("code")){
				String xcode=data.getStringExtra("code");
				serverHelper.getInstance().postPocket("https://getpocket.com/v3/oauth/authorize", serverHelper.buildPocketQuery(String.format("\"code\":\"%s\"",xcode)),true, new AsyncTaskCompleteListener<String, String>() {
					
					@Override
					public void onTaskComplete(String result, String adv) {
						// TODO Auto-generated method stub
						serverHelper.getInstance().completed();
						String token="";
						try{						
							JSONObject root = new JSONObject(result);
							if(root.has("access_token")){
								token=root.getString("access_token");
								appSettings.storePocket(previewActivity.this, token);
								pocketSend();
							}
						
						} catch (Exception e){}
					}
				});
			}*/
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
		    case R.id.bPocketConnect:
		    	final ProgressDialog progressDialog = ProgressDialog.show(v.getContext(), "Nimbus Clipper", getString(R.string.please_wait), true, false);
		    	progressDialog.show();
		    	//findViewById(R.id.llPreview).setVisibility(View.GONE);
		    	serverHelper.getInstance().postPocket("https://getpocket.com/v3/oauth/request", serverHelper.buildPocketQuery(String.format("\"redirect_uri\":\"%s\"",redirect)),true, new AsyncTaskCompleteListener<String, String>() {
					
					@Override
					public void onTaskComplete(String result, String adv) {
						// TODO Auto-generated method stub
						progressDialog.dismiss();
						try{			
							serverHelper.getInstance().completed();
							code="";
							JSONObject root = new JSONObject(result);
							if(root.has("code")){
								code=root.getString("code");
								doOverride=false;
								//WebView wv =( WebView )findViewById(R.id.wvPreview);
								//String xs=String.format("https://getpocket.com/auth/authorize?mobile=1&request_token=%s&redirect_uri=%s", code,redirect);
								Intent i = new Intent(getApplicationContext(), loginWithActivity.class);
						    	i.putExtra("service", "pocket");
						    	i.putExtra("code", code);
						    	i.putExtra("redirect", redirect);
						    	startActivityForResult(i, 2);
						    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
								
								/*wv.getSettings().setJavaScriptEnabled(true);
					        	wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
					        	wv.getSettings().setSaveFormData(true);
					        	wv.getSettings().setLoadWithOverviewMode(true);
					        	wv.getSettings().setUseWideViewPort(true);
								wv.loadUrl(String.format("https://getpocket.com/auth/authorize?mobile=1&request_token=%s&redirect_uri=%s", code,redirect));*/
								//xwv.naviga;
							}
							
						} catch (Exception e){}
					}
				});
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
