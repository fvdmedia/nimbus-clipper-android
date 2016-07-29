package com.fvd.nimbus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ArticleSuccess extends Activity implements OnClickListener{
	private String id="";
	//private final String mask="The %s was successfully saved to your Nimbus account";
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
        setContentView(R.layout.layout_succes);
        String w= getIntent().getStringExtra("mode").toString();
        ((TextView)findViewById(R.id.tvText)).setText(String.format(getString(R.string.save_article_mask), w));
        ((Button)findViewById(R.id.bGetLink)).setOnClickListener(this);
        ((Button)findViewById(R.id.bFragmentSaveOk)).setOnClickListener(this);
     }
	
	public void onClick(View v)
	{
		Intent intent = new Intent();
		switch (v.getId()) 
		{
		    case R.id.bGetLink:
		    	setResult(RESULT_OK, intent);
		        break;
	    }
		//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
		overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
		finish();
	}
}
