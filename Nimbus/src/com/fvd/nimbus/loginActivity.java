package com.fvd.nimbus;

import org.json.JSONObject;

import com.fvd.utils.AsyncTaskCompleteListener;
import com.fvd.utils.appSettings;
import com.fvd.utils.prefsEditListener;
import com.fvd.utils.serverHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
//import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class loginActivity extends Activity implements OnClickListener,AsyncTaskCompleteListener<String, String>{
	private TextView tv=null;
	private String userMail = "";
	private String userPass ="";
	private SharedPreferences prefs;
	boolean needResult=false;
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
        
        setContentView(R.layout.screen_login);
        
        overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
        serverHelper.getInstance().setCallback(this,this);
        if (!appSettings.getIsTablet())
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        userMail = getIntent().getStringExtra("userMail").toString();
        
        needResult=getIntent().hasExtra("needresult");

        tv=(TextView)findViewById(R.id.etUsername);
        tv.setText(userMail);
        tv.setOnKeyListener(new OnKeyListener() {           
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                	findViewById(R.id.etPassword).requestFocus();
                    return true;
                }
                return false;
            }
        });
        
        
        findViewById(R.id.bLogin).setOnClickListener(this);
        findViewById(R.id.bNewAccount).setOnClickListener(this);
        findViewById(R.id.bFLogin).setOnClickListener(this);
        findViewById(R.id.bGLogin).setOnClickListener(this);
     }
	

	 @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	      if (requestCode == 1) {
	    	if (resultCode == RESULT_OK){
				userMail=data.getStringExtra("userMail");
    			userPass=data.getStringExtra("userPass");
    			serverHelper.getInstance().sendOldRequest("user_register", String.format("{\"action\": \"user_register\",\"email\":%s,\"password\":%s,\"_client_software\": \"ff_addon\"}",JSONObject.quote(userMail),JSONObject.quote(userPass)), "");
	    	}
	      }
	 }

	public void onClick(View v)
	{
		//JSONObject j;
		Intent i;
		switch (v.getId()) 
		{
		    case R.id.bLogin:
		    	userMail = tv.getText().toString();
				userPass = ((TextView)findViewById(R.id.etPassword)).getText().toString();
				if(userMail.indexOf("@")==-1 || userMail.indexOf(".")==-1){
		    		Toast.makeText(getApplicationContext(), getString(R.string.invalid_email), Toast.LENGTH_LONG).show();
    			}
    			else if(userPass.length()==0){
    				Toast.makeText(getApplicationContext(), getString(R.string.incorrect_password), Toast.LENGTH_LONG).show();
    			}
    			else {
    				sendRequest("user:auth",String.format("\"email\":%s,\"password\":%s",JSONObject.quote(userMail),JSONObject.quote(userPass)));
    			}
    			break;
		    case R.id.bNewAccount:
		    	i = new Intent(getApplicationContext(), RegisterDlg.class);
		    	i.putExtra("userMail", userMail==null?"":userMail);
		    	startActivityForResult(i, 1);
		    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
		    	break;
		    case R.id.bFLogin:
		    	i = new Intent(getApplicationContext(), loginWithActivity.class);
		    	i.putExtra("service", "facebook");
		    	startActivity(i);
		    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
		    	finish();
		    	break;
		    case R.id.bGLogin:
		    	i = new Intent(getApplicationContext(), loginWithActivity.class);
		    	i.putExtra("service", "google");
		    	startActivity(i);
		    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
		    	finish();
		    	break;
	
	    }
		
	}
	
	private void sendRequest(String action, String data){
		serverHelper.getInstance().sendRequest(action, data,"");
	}
	
	@Override
    public void onTaskComplete(String result, String action)
    {
        	try{
            	JSONObject root = new JSONObject(result);
            	int error = root.getInt("errorCode");
            	if (error == 0){
            		if (action.equalsIgnoreCase("user:auth")){
            			final String sessionId = root.getJSONObject("body").getString("sessionid");
            			Editor editor=prefs.edit();
        	    		editor.putString("userMail", userMail);
        	    		editor.putString("userPass", userPass);
        	    		editor.putString("sessionId", sessionId);
        	    		appSettings.sessionId=sessionId;
        	    		appSettings.userMail = userMail;
        	    		appSettings.userPass = userPass;
        	    		editor.commit();
        	    		Toast.makeText(getApplicationContext(), "user authorized", Toast.LENGTH_LONG).show();

        	    		overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
        	    		if(needResult){
        	    			Intent i=new Intent();
        	    			setResult(RESULT_OK, i);
        	    		}
        	    		finish();
        	    		
            		}
            		else if(action.equalsIgnoreCase("user:authstate")){
            			boolean auth = root.getJSONObject("body").getBoolean("authorized");
            			Toast.makeText(getApplicationContext(), "User " + (auth?"authorized":"not authorized"), Toast.LENGTH_LONG).show();
            		}
            		else if("user_register".equals(action)){
            			sendRequest("user:auth", String.format("\"email\":\"%s\",\"password\":\"%s\"",userMail,userPass));
            		}
            	} else Toast.makeText(getApplicationContext(), String.format("Error: %s",serverHelper.errorMsg(error)), Toast.LENGTH_LONG).show();
            }
            catch (Exception Ex){
            }
    }

}
