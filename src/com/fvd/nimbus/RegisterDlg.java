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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class RegisterDlg extends Activity implements OnClickListener{
	private EditText tv=null;
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try{
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        catch (Exception e){
        	e.printStackTrace();
        }
        
        setContentView(R.layout.layout_register);
        String userMail = getIntent().getStringExtra("userMail").toString();
        tv=( EditText )findViewById(R.id.etrLogin);
        tv.setFocusableInTouchMode(true);
        tv.setFocusable(true);
        tv.requestFocus();
        tv.setText(userMail);
        tv.setOnKeyListener(new OnKeyListener() {           
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction()==KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                	findViewById(R.id.etrPassword).requestFocus();
                    return true;
                }
                return false;
            }
        });
        Button btnSave=(Button)findViewById(R.id.brRegister);
        btnSave.setOnClickListener(this);
        tv.postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                InputMethodManager keyboard = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(tv, 0);
            }
        },200);
     }
	
	public void onClick(View v)
	{
		
		switch (v.getId()) 
		{
		    case R.id.brRegister:
		    	((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		    	String mail  = tv.getText().toString();
		    	String pass = ((TextView)findViewById(R.id.etPassword)).getText().toString();
		    	if(mail.indexOf("@")==-1 || mail.indexOf(".")==-1){
		    		Toast.makeText(getApplicationContext(), getString(R.string.invalid_email), Toast.LENGTH_LONG).show();
    			}
    			else if(pass.length()<8){
    				Toast.makeText(getApplicationContext(), getString(R.string.short_password), Toast.LENGTH_LONG).show();
    			}
    			else{
    				Intent intent = new Intent();
    				intent.putExtra("userMail", tv.getText().toString());
    				intent.putExtra("userPass", ((TextView)findViewById(R.id.etPassword)).getText().toString());
    				setResult(RESULT_OK, intent);
    				finish();
    			}
		        break;
		  }
	}

}
