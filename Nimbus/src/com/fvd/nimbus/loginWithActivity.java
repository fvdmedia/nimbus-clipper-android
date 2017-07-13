package com.fvd.nimbus;

//import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.List;

import org.json.JSONObject;

import com.fvd.nimbus.R.string;
import com.fvd.utils.AsyncTaskCompleteListener;
import com.fvd.utils.appSettings;
import com.fvd.utils.helper;
import com.fvd.utils.serverHelper;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
//import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class loginWithActivity extends Activity{
	private SharedPreferences prefs;
	WebView wv;
	String service="";
	String redirect="";
	String code="";
	boolean doLogout=false;
	boolean logout=false;
	boolean doResult=false;
	@SuppressLint("NewApi") 
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try{
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        catch (Exception e){
        	e.printStackTrace();
        }
        
        setContentView(R.layout.layout_login_with);
        //overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
        wv=(WebView)findViewById(R.id.wvLogin);
        wv.getSettings().setJavaScriptEnabled(true);
		wv.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
    	wv.getSettings().setSaveFormData(true);
    	wv.getSettings().setLoadWithOverviewMode(true);
    	wv.getSettings().setUseWideViewPort(true);
        if (!appSettings.getIsTablet())
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        CookieManager cm = CookieManager.getInstance();
        if(android.os.Build.VERSION.SDK_INT>21){
        	cm.removeAllCookies(mCookieDeleted);
        } else {
        	cm.removeAllCookie();
        }
        wv.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) 
            {
            	if(failingUrl.equals(redirect)){
            		view.loadDataWithBaseURL("about:blank", "redirecting...", "text/html", "", null);
            	}
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
            	return false;
            }
            
            
            public void onPageStarted (WebView view, String url, Bitmap favicon) {
            	if(code!="" && url.equals(redirect)){
            		serverHelper.getInstance().postPocket("https://getpocket.com/v3/oauth/authorize", serverHelper.buildPocketQuery(String.format("\"code\":\"%s\"",code)),true, new AsyncTaskCompleteListener<String, String>() {
    					
    					@Override
    					public void onTaskComplete(String result, String adv) {
    						// TODO Auto-generated method stub
    						serverHelper.getInstance().completed();
    						String token="";
    						try{						
    							JSONObject root = new JSONObject(result);
    							if(root.has("access_token")){
    								token=root.getString("access_token");
    								appSettings.storePocket(loginWithActivity.this, token);
    								
    								
    							}
    						
    						} catch (Exception e){}
    						Intent i=new Intent();
							i.putExtra("code", code);
							//setResult(RESULT_OK, i);
							setResult(token!=""?RESULT_OK:RESULT_CANCELED, i);
							finish();
    					}
    				});
            	}
            }
          
            public void onPageFinished (WebView view, String url) {
            	/*if(code!="" && url.equals(redirect)){
            		serverHelper.getInstance().postPocket("https://getpocket.com/v3/oauth/authorize", serverHelper.buildPocketQuery(String.format("\"code\":\"%s\"",code)),true, new AsyncTaskCompleteListener<String, String>() {
    					
    					@Override
    					public void onTaskComplete(String result, String adv) {
    						// TODO Auto-generated method stub
    						serverHelper.getInstance().completed();
    						String token="";
    						try{						
    							JSONObject root = new JSONObject(result);
    							if(root.has("access_token")){
    								token=root.getString("access_token");
    								appSettings.storePocket(loginWithActivity.this, token);
    							}
    							
    						} catch (Exception e){}
    						
    					}
    				});
            	} else */
            	if(url.startsWith("https://everhelper.me/auth/openidconnect.php")){
            	  String title=view.getTitle();
            	  if(title.contains("###EVERFAUTH") && title.contains("\"errorCode\":0")){
            		  String email=helper.ExtractBtw(title, "\"email\":\"", "\"");
                      String token=helper.ExtractBtw(title, "\"token\":\"", "\"");
            		  String cx=CookieManager.getInstance().getCookie(url);
            		  if(cx!=null && cx!=""){
            			  if(cx.contains("eversessionid")){
            				  cx=helper.ExtractBtw(cx, "eversessionid=", ";");
            			  }
            			  else cx="";
            		  } 
                      if(email!=""&&token!=""&&cx!="") {
                    	  appSettings.storeUserData(loginWithActivity.this, email, token, cx, service);
                    	  //setResult(RESULT_OK, new Intent());
                    	  finish();
                    	  Toast.makeText(getApplicationContext(), "User authorized", Toast.LENGTH_LONG).show();
                      } else {
                    	  	Intent  i = new Intent(getApplicationContext(), loginActivity.class);
	                  		i.putExtra("userMail", "");
	                      	startActivity(i);
	                      	finish();
	                      	Toast.makeText(getApplicationContext(), "User not authorized", Toast.LENGTH_LONG).show();
                      }
                      
            	  }
              } else if(logout){
            	  if(!doLogout){
            		  doLogout=url.toLowerCase().contains("logout");
            	  } else {
            		  String cx=CookieManager.getInstance().getCookie(url);
                	  if(cx!=null&&cx!=""){
                		  String[] acx=cx.split(";");
                		  if(acx.length<2){
                			  logout = false;
                              appSettings.SignOut(loginWithActivity.this);
                              finish();
                		  }
                	  }
            	  }
              }
          }
        });
        
        Intent i=getIntent();
        service =i.getStringExtra("service").toString();
        logout=i.hasExtra("logout");
        doResult = i.hasExtra("result");
        if("pocket".equals(service)){
        	code=i.getStringExtra("code");
        	redirect=i.getStringExtra("redirect");
        	wv.loadUrl(String.format("https://getpocket.com/auth/authorize?mobile=1&request_token=%s&redirect_uri=%s", code,redirect));
        } else {
	        if(logout){
	        	if("google".equals(service)) wv.loadUrl("https://accounts.google.com/Logout2?");
	        	else wv.loadUrl("https://facebook.com");
	        }else{
	        	wv.loadUrl(String.format("https://everhelper.me/auth/openidconnect.php?env=app&provider=%s&fpp=1", service));
	        }
        }
	}
	
	private ValueCallback<Boolean> mCookieDeleted = new ValueCallback<Boolean>() {
        @Override
        public void onReceiveValue(Boolean value) {

      }
	};
	
	@Override
	public void onBackPressed() {
		if(!logout&&!"pocket".equals(service)){
			Intent  i = new Intent(getApplicationContext(), loginActivity.class);
			i.putExtra("userMail", appSettings.userMail);
	    	startActivity(i);
		}
    	finish();
	}
}
