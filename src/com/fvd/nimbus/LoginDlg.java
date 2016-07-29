package com.fvd.nimbus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginDlg extends Activity implements OnClickListener{
	private TextView tv=null;
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
        setContentView(R.layout.layout_login);
        String userMail = getIntent().getStringExtra("userMail").toString();
        tv=(TextView)findViewById(R.id.etlogin);
        /*tv.setFocusableInTouchMode(true);
        tv.setFocusable(true);
        tv.requestFocus();*/
        tv.setText(userMail);
        /*tv.setOnKeyListener(new OnKeyListener() {           
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                	findViewById(R.id.etPassword).requestFocus();
                    return true;
                }
                return false;
            }
        });*/
        findViewById(R.id.bCreateAccount).setOnClickListener(this);
        findViewById(R.id.bRegister).setOnClickListener(this);
        Button btnSave=(Button)findViewById(R.id.blogin);
        btnSave.setOnClickListener(this);
        /*tv.postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                InputMethodManager keyboard = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(tv, 0);
            }
        },200);*/
     }
	
	public void onClick(View v)
	{
		String mail="";
		String pass="";
		switch (v.getId()) 
		{
		    case R.id.blogin:
		    	//((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		    	try{
		    		hideKeyboard();
		    	}
		    	catch (Exception e) {
					// TODO: handle exception
				}
		    	mail  = tv.getText().toString();
		    	pass = ((TextView)findViewById(R.id.etPassword)).getText().toString();
		    	if(mail.indexOf("@")==-1 || mail.indexOf(".")==-1){
		    		Toast.makeText(getApplicationContext(), getString(R.string.invalid_email), Toast.LENGTH_LONG).show();
    			}
    			else if(pass.length()==0){
    				Toast.makeText(getApplicationContext(), getString(R.string.incorrect_password), Toast.LENGTH_LONG).show();
    			}
    			else{
    				Intent intent = new Intent();
    				intent.putExtra("userMail", mail);
    				intent.putExtra("userPass", pass);
    				setResult(RESULT_OK, intent);
    				finish();
    			}
		        break;
		    case R.id.bCreateAccount:
		    	findViewById(R.id.tvLogin).setVisibility(View.GONE);
		    	findViewById(R.id.tvOr).setVisibility(View.GONE);
		    	findViewById(R.id.blogin).setVisibility(View.GONE);
		    	findViewById(R.id.bCreateAccount).setVisibility(View.GONE);
		    	findViewById(R.id.tvRegister).setVisibility(View.VISIBLE);
		    	findViewById(R.id.bRegister).setVisibility(View.VISIBLE);
		        break;   
		    case R.id.bRegister:
		    	mail  = tv.getText().toString();
		    	pass = ((TextView)findViewById(R.id.etPassword)).getText().toString();
		    	if(mail.indexOf("@")==-1 || mail.indexOf(".")==-1){
		    		Toast.makeText(getApplicationContext(), getString(R.string.invalid_email), Toast.LENGTH_LONG).show();
    			}
    			else if(pass.length()<8){
    				Toast.makeText(getApplicationContext(), getString(R.string.incorrect_password), Toast.LENGTH_LONG).show();
    			}
    			else{
    				Intent intent = new Intent();
    				intent.putExtra("userMail", mail);
    				intent.putExtra("userPass", pass);
    				setResult(RESULT_FIRST_USER, intent);
    				//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    				overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    				finish();
    			}
		    	break;
	    }
		
	}
	void hideKeyboard(){
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm!=null){
			if(!imm.hideSoftInputFromWindow(findViewById(R.id.etlogin).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS))
				if(!imm.hideSoftInputFromWindow(findViewById(R.id.etPassword).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS));
		}
	}
        
    }
