package com.fvd.nimbus;

import org.json.JSONObject;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;
import com.fvd.utils.AsyncTaskCompleteListener;
import com.fvd.utils.appSettings;
import com.fvd.utils.serverHelper;

public class RestoreActivity extends Activity implements OnClickListener,AsyncTaskCompleteListener<String, String>{
	private TextView tv=null;
	private String userMail = "";

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
        setContentView(R.layout.screen_restore);
        serverHelper.getInstance().setCallback(this,this);
        if (!appSettings.getInstance().getIsTablet())
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        tv=(TextView)findViewById(R.id.etRestoreMail);
        tv.setFocusableInTouchMode(true);
        tv.setFocusable(true);
        tv.requestFocus();
        
        findViewById(R.id.bRestore).setOnClickListener(this);
     }
	

	public void onClick(View v)
	{
		switch (v.getId()) 
		{
		    case R.id.bRestore:
		    	userMail = tv.getText().toString();
				if(userMail.indexOf("@")==-1 || userMail.indexOf(".")==-1){
		    		Toast.makeText(getApplicationContext(), getString(R.string.invalid_email), Toast.LENGTH_LONG).show();
    			}
    			else 
    				serverHelper.getInstance().sendOldRequest("remind_password", String.format("{\"action\": \"remind_password\",\"email\":\"%s\"}",userMail),"");
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
            		Toast.makeText(getApplicationContext(),"Please check email", Toast.LENGTH_LONG).show();
            		finish();
            	} else Toast.makeText(getApplicationContext(), String.format("Error: %s",serverHelper.errorMsg(error)), Toast.LENGTH_LONG).show();
            }
            catch (Exception Ex){
            }
    }

}
