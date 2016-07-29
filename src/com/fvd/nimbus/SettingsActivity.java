package com.fvd.nimbus;

import com.fvd.utils.appSettings;
import com.fvd.utils.serverHelper;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SettingsActivity extends Activity{

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
        setContentView(R.layout.layout_settings);
        /*((Button)findViewById(R.id.bsSetting)).setOnClickListener(this);
        ((Button)findViewById(R.id.bsRegister)).setOnClickListener(this);
        ((Button)findViewById(R.id.bsRateUs)).setOnClickListener(this);
        ((Button)findViewById(R.id.bsLicenses)).setOnClickListener(this);
        ((Button)findViewById(R.id.bsHelp)).setOnClickListener(this);*/
        if(!appSettings.getInstance().getIsTablet())
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (appSettings.sessionId.length() == 0) ((Button)findViewById(R.id.bsRegister)).setText(getResources().getString(R.string.login));
    	else ((Button)findViewById(R.id.bsRegister)).setText(getResources().getString(R.string.logout));
     }
	
	public void onButtonClick(View v) {
		// TODO Auto-generated method stub
		Intent i=new Intent();
		switch (v.getId()) 
		{
		    case R.id.bsSetting:
		    	setResult(RESULT_FIRST_USER+1, i);
		    	break;
		    case R.id.bsRegister:
		    	setResult(RESULT_FIRST_USER+2, i);
		    	break;
		    case R.id.bsRateUs:
		    	setResult(RESULT_FIRST_USER+3, i);
		    	break;
		    case R.id.bsHelp:
		    	setResult(RESULT_FIRST_USER+4, i);
		    	break;
		    case R.id.bsLicenses:
		    	setResult(RESULT_FIRST_USER+5, i);
		    	break;
		}
		finish();
	}

}
