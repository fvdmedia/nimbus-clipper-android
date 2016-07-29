package com.fvd.nimbus;

//import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.List;

import com.fvd.nimbus.R.string;
import com.fvd.utils.appSettings;
import com.fvd.utils.helper;
import com.fvd.utils.serverHelper;


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
//import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class loginWithActivity extends Activity{
	private SharedPreferences prefs;
	WebView wv;
	String service="";
	boolean doLogout=false;
	boolean logout=false;
	boolean doResult=false;
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
        if (!appSettings.getInstance().getIsTablet())
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        wv.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) 
            {
            	
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
            	return false;
            }
            
            
            public void onPageStarted (WebView view, String url, Bitmap favicon) {
            	
            }
          
            public void onPageFinished (WebView view, String url) {
              if(url.startsWith("https://everhelper.me/auth/openidconnect.php")){
            	  String title=view.getTitle();
            	  if(title.contains("###EVERFAUTH") && title.contains("\"errorCode\":0")){
            		  /*CookieManager manager = new CookieManager();
            		  CookieStore cookieJar = manager.getCookieStore();
                      List<HttpCookie> cookies = cookieJar.getCookies();
                      
                      String cx="";
                      for (HttpCookie cookie: cookies) {
                          if(cookie.getName().contains("eversessionid")){
                        	  cx=cookie.getValue();
                        	  break;
                          }
                      }*/
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
            	  
            	  /*String cx=CookieManager.getInstance().getCookie(url);
            	  if(cx!=null&&cx!=""){
            		  String[] acx=cx.split(";");
            		  for (String s : acx) {
						if(s.contains("NID=")){
							isOut = false;
	                          break;
						}
            		  }
            	  }
                  
                  if (isOut)
                  {
                      logout = false;
                      appSettings.SignOut(loginWithActivity.this);
                      finish();
                  }*/
              }
          }
        });
        
        service =getIntent().getStringExtra("service").toString();
        logout=getIntent().hasExtra("logout");
        doResult = getIntent().hasExtra("result");
        if(logout){
        	if("google".equals(service)) wv.loadUrl("https://accounts.google.com/Logout2?");
        	else wv.loadUrl("https://facebook.com");
        }else{
        	wv.loadUrl(String.format("https://everhelper.me/auth/openidconnect.php?env=app&provider=%s&fpp=1", service));
        }
	}
	
	
	
	@Override
	public void onBackPressed() {
		if(!logout){
			Intent  i = new Intent(getApplicationContext(), loginActivity.class);
			i.putExtra("userMail", appSettings.userMail);
	    	startActivity(i);
		}
    	finish();
	}
}
