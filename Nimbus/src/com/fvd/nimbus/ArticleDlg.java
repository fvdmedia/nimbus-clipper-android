package com.fvd.nimbus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.text.ClipboardManager;

public class ArticleDlg extends Activity implements OnClickListener{
	private TextView tv=null;
	private String url="";
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try{
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        catch (Exception e){
        	e.printStackTrace();
        }
        setContentView(R.layout.layout_article_link);
        url = getIntent().getStringExtra("url").toString();
        tv=(TextView)findViewById(R.id.etArticleLink);
        tv.setFocusableInTouchMode(true);
        tv.setFocusable(true);
        tv.requestFocus();
        tv.setText(url);
        ((Button)findViewById(R.id.bCopyClipboard)).setOnClickListener(this);
        ((Button)findViewById(R.id.bShare)).setOnClickListener(this);
       
     }
	
	public void onClick(View v)
	{
		switch (v.getId()) 
		{
		    case R.id.bCopyClipboard:
		    	
		    	try{
		    	    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		    	    clipboard.setText(url);
		    	}
		    	catch (Exception e) {
					// TODO: handle exception
				}
		    	//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
		    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
		    	finish();
		        break;
		    case R.id.bShare:
		    	Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, url + "\r\n\r\nVia Nimbus Clipper for Android");
                intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Shared Note from Nimbus");
                startActivity(Intent.createChooser(intent, "Share"));
                //overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
                overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
		    	finish();
		        break;    
	    }
		finish();
	}
        
    }
