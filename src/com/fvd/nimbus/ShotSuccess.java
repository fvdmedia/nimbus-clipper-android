package com.fvd.nimbus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ShotSuccess extends Activity implements OnClickListener{
	private String id="";
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
        setContentView(R.layout.layout_shot_success);
        id = getIntent().getStringExtra("id").toString();
        
        ((Button)findViewById(R.id.bGetLink)).setOnClickListener(this);
        ((Button)findViewById(R.id.bFragmentSaveOk)).setOnClickListener(this);
        if(getIntent().getBooleanExtra("isPDF", false)){
        	findViewById(R.id.tvText).setVisibility(View.GONE);
        	findViewById(R.id.tvDoc).setVisibility(View.VISIBLE);
        }
     }
	
	public void onClick(View v)
	{
		Intent intent = new Intent();
		switch (v.getId()) 
		{
		    case R.id.bGetLink:
		    	intent.putExtra("id", id);
		    	setResult(RESULT_OK, intent);
		        break;
		    case R.id.bFragmentSaveOk:
		        break;    
	    }
		finish();
	}

}
