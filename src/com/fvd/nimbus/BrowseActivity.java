package com.fvd.nimbus;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
//import android.util.Log;
//import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JsResult;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import com.fvd.utils.FolderItem;
import com.fvd.utils.HistoryItem;
import com.fvd.utils.WebHistoryKeeper;
import com.fvd.utils.AsyncTaskCompleteListener;
import com.fvd.utils.appSettings;
import com.fvd.utils.prefsEditListener;
import com.fvd.utils.serverHelper;
import com.fvd.browser.fvdBrowerEventsListener;
import com.fvd.browser.fvdWebView;
//import android.content.DialogInterface.OnClickListener;

public class BrowseActivity extends Activity implements AsyncTaskCompleteListener<String, String>, fvdBrowerEventsListener,TextWatcher{

	private final String deskAgent="Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.17 Safari/537.36";
	
	private static final int IDM_OPEN = 101; 
	private static final int IDM_SAVE = 102; 
	
	
	final int DIALOG_CAPTURE = 1;
	final int DIALOG_CLIP = 2;
	String captureItems[] = { "view", "full" };
	String clipItems[] = { "one", "two", "three", "four" };
	
	private fvdWebView wv;
	private String lastUrl = "";
	Menu myMenu = null;
	/*static final String SaveDir= "Pictures";
	private String SavingPath;*/
	
	private AutoCompleteTextView urlField;
	private ImageButton     navButton;
	private ProgressBar     progressBar;
	//private SharedPreferences prefs;

	private String scripts="";
	private String selHtml="";
	private String selBuff="";
	private String selText="";
	Context ctx;
	
	private String userMail = "";
	private String userPass ="";
	private String sessionId = "";
	private Handler handler;
	private boolean saveCSS = false;
	private int clipMode=0;
	private ProgressDialog progressDialog;
	private SharedPreferences prefs;
	//Animation animationFadeIn;
	//Animation animationFadeOut;
	//List<HistoryItem>  m_arrHistoryItems=new ArrayList<HistoryItem>();
	List<String>  arrHistoryItems=new ArrayList<String>();
	//ArrayAdapter<HistoryItem> adapter;
	//ArrayAdapter<String> adapter;
	boolean isInitNow = true;
	private enum NavButtonState
    {
        NBS_GO,
        NBS_REFRESH,
        NBS_STOP
    }

	
    private  NavButtonState     navButtonState = NavButtonState.NBS_GO;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try{
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        catch (Exception e){
        	e.printStackTrace();
        }
        isInitNow = true;
        setContentView(R.layout.screen_browser);
        serverHelper.getInstance().setCallback(this,this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        lastUrl = prefs.getString("LAST_URL","");
        saveCSS = prefs.getString("clipStyle", "1").equals("1");
        ctx = this;
        //adapter = new TextAdapter(this);		
        
        /*Uri data = getIntent().getData();
        if(data!=null){
        	lastUrl=data.toString();
        }*/
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_VIEW.equals(action) /*&& type != null*/) {
        	Uri data = intent.getData();
            if(data!=null){
            	lastUrl=data.toString();
            	appSettings.appendLog("browse:onCreate  "+lastUrl);
            }
        }
        else if(Intent.ACTION_SEND.equals(action) /*&& type != null*/){
        	if ("text/plain".equals(type)) {
        		lastUrl=intent.getStringExtra(Intent.EXTRA_TEXT);
        		int i = lastUrl.indexOf("http");
        		if(i>0){
        			int c = lastUrl.indexOf(" ", i);
        			if(c>-1 && c>i) lastUrl=lastUrl.substring(i,c);
        		}
        		i=lastUrl.indexOf("\n");
        		if(i!=-1) 
        			lastUrl=lastUrl.substring(0,i);
        		appSettings.appendLog("browse:onCreate  "+lastUrl);
            } 
        }
        
       
        
        //lastUrl="http://en.wikipedia.org/wiki/Main_Page";
        
        wv = (fvdWebView) findViewById(R.id.wv);
        wv.setEventsHandler(this);
        registerForContextMenu(wv); 
        urlField = ( AutoCompleteTextView )findViewById(R.id.etAddess);
        urlField.setSelectAllOnFocus(true);
        urlField.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            	if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    /*InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);*/
                    onNavButtonClicked();
                    return true;
                }
            	else if (event !=null && event.getKeyCode()==KeyEvent.KEYCODE_ENTER){
            		onNavButtonClicked();
            		return true;
            	}
                return false;
            }
        });
        
          
        
        /*urlField.setOnTouchListener( new EditText.OnTouchListener(){

			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (arg1.getAction() == MotionEvent.ACTION_UP){
					EditText e = (EditText)arg0;
					if(e.getSelectionEnd()<=e.getSelectionStart())
					e.selectAll();
				}
				return false;
			}
        
        });*/
        
        handler = new Handler() {
    		@Override
    		public void handleMessage(Message msg) {
    			switch (msg.what) {
				case 0:
					//Log.e("fvdWebView",String.format("tsjiSelectionChanged hb: %d",msg.arg1));
					ImageButton ib =  (ImageButton)findViewById(R.id.bSaveFullPage);
					if(msg.arg1==1){
						if(!ib.isSelected()) ib.setSelected(true);
					}
					else {
						if(ib.isSelected()) ib.setSelected(false);
					}
					//ib.setSelected(msg.arg1==1);
					findViewById(R.id.bZoomStack).setVisibility(View.VISIBLE);
					findViewById(R.id.bDone).setVisibility(View.VISIBLE);
					findViewById(R.id.bSaveFullPage).setVisibility(View.GONE);
					findViewById(R.id.bTakeScreenshot).setVisibility(View.GONE);
					findViewById(R.id.bSavePageFragment).setVisibility(View.GONE);
					findViewById(R.id.bToggleMenu).setVisibility(View.GONE);
					
					
					//((ImageButton)findViewById(R.id.bZoomOut)).setVisibility(View.VISIBLE);
					break;
				/*case 1:
					if(selHtml.length()>0){
	            		if (sessionId.length() == 0) showSettings();
	            		else {
	            			sendNote(wv.getTitle(), selHtml);
	            			wv.endSelectionMode();
	            		}
	            	}
					break;*/
				default:
					break;
				}
    		}
    	};
        
        
        
        navButton = (ImageButton)findViewById(R.id.ibReloadWebPage);
        navButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            	//Toast.makeText(getApplicationContext(), "You made a mess", Toast.LENGTH_LONG).show();
            	onNavButtonClicked();
            }
        });
        
        ((ImageButton)findViewById(R.id.bSavePageFragment)).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            	toggleTools();
            	if(!wv.getInjected()) Toast.makeText(ctx, getString(R.string.wait_load), Toast.LENGTH_LONG).show();
            	clipMode =2;		
            	if(wv.getInjected()/* && !v.isSelected()*/){
            		wv.setCanClip(true);
            		v.setSelected(true);
            		Toast.makeText(ctx, ctx.getString(R.string.use_longtap), Toast.LENGTH_LONG).show();
            	}
            	/*else{
            		if(selHtml.length()>0){
            			wv.setCanClip(false);
            			wv.endSelectionMode();
            			//v.setSelected(false);
            			//appSettings.appendLog("<html><head><title>Untitled Document</title><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"></head><body>" + selHtml + "</body></html>");
            			Intent i = new Intent(getApplicationContext(), previewActivity.class);
            			clipMode =2;
            	    	i.putExtra("content", selHtml);
            	    	startActivityForResult(i,5);
            	    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
            			wv.endSelectionMode();
            			selHtml = "";
            		}
            	}*/
            }
        });
        
        ((ImageButton)findViewById(R.id.bSaveFullPage)).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            	/*if(wv.getInjected() && !v.isSelected()){
            		wv.saveArticle();
            		clipMode =1;
            		progressDialog = ProgressDialog.show(v.getContext(), "FVD Nimbus", getString(R.string.please_wait), true, false);
            	}
            	else
            	if(selHtml.length()>0){
            			Intent i = new Intent(getApplicationContext(), previewActivity.class);
            			clipMode =2;
            	    	i.putExtra("content", selHtml);
            	    	startActivityForResult(i,5);
            			wv.endSelectionMode();
            	}*/
            	toggleTools();
            	if(wv.getInjected()){
            		wv.setCanClip(false);
            		wv.saveArticle();
            		clipMode =1;
            		progressDialog = ProgressDialog.show(v.getContext(), "Nimbus Clipper", getString(R.string.please_wait), true, false);
            	}
            	else {
            		Toast.makeText(ctx, getString(R.string.wait_load), Toast.LENGTH_LONG).show();
            	}
            }
        });
        
        ((ImageButton)findViewById(R.id.bTakeScreenshot)).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            	//toggleTools();
    			findViewById(R.id.bSaveFullPage).setVisibility(View.GONE);
    			findViewById(R.id.bSavePageFragment).setVisibility(View.GONE);
    			findViewById(R.id.bTakeScreenshot).setVisibility(View.GONE);
            	if(wv.getInjected()){
            		wv.setCanClip(false);
            	}
            	findViewById(R.id.bToggleMenu).setVisibility(View.GONE);
            	/*screenCapture();
            	findViewById(R.id.bToggleMenu).setVisibility(View.VISIBLE);*/
            	
            	findViewById(R.id.bTakeScreenshot).postDelayed(new Runnable() {
		            @Override
		            public void run() {
		                // TODO Auto-generated method stub
		            	screenCapture();
		            	findViewById(R.id.bToggleMenu).setVisibility(View.VISIBLE);
		            	finish();
		            }
		        },10);
            	
            	
            	
            	//showDialog(DIALOG_CAPTURE);
            }
        });
        
        (findViewById(R.id.bDone)).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            	{
        			wv.setCanClip(false);
        			wv.endSelectionMode();
        			//findViewById(R.id.bSavePageFragment).setSelected(false);
        			clipMode =2;
           			wv.endSelectionMode();
        			if(selHtml.length()>0){
	        			Intent i = new Intent(getApplicationContext(), previewActivity.class);
	        	    	i.putExtra("content", selHtml);
	        	    	startActivityForResult(i,5);
	        	    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        			}
        			selHtml = "";
        		}
            	//showDialog(DIALOG_CAPTURE);
            }
        });
        
        ((ImageButton)findViewById(R.id.bZoomIn)).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            	wv.ZoomInSelection();
            }
        });
        
        ((ImageButton)findViewById(R.id.bZoomOut)).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            	wv.ZoomOutSelection();
            }
        });
        
        ((ImageButton)findViewById(R.id.bToggleMenu)).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            	toggleTools();
            }
        });
        
        setNavButtonState(NavButtonState.NBS_GO);
        
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        
        //CookieSyncManager.createInstance(this);
        
        //webSettings.setLoadsImagesAutomatically(imgOn);
        userMail = prefs.getString("userMail", "");
        userPass = prefs.getString("userPass", "");
        sessionId = prefs.getString("sessionId", "");
        
        appSettings.sessionId=sessionId;
 	    appSettings.userMail=userMail;
 	    appSettings.userPass=userPass;
        
        if("1".equals(prefs.getString("userAgent", "1"))){
        	wv.setUserAgent(null);
        }
        else wv.setUserAgent(deskAgent);
        
        final Activity activity = this;
        //lastUrl="file:///android_asset/android.html";
        if (lastUrl.length()>0){
        	//wv.navigate(lastUrl);
        	
        	//if(!urlField.getText().toString().equals(wv.getUrl()))
        	urlField.setText(lastUrl);
        	openURL();
        }
        isInitNow = false;
        //urlField.setAdapter(adapter);
        //adapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,arrHistoryItems);
        //adapter=new TextAdapter(this);
        //urlField.setAdapter(adapter);
        urlField.setOnItemClickListener(new OnItemClickListener() {
		    @Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				/*String item = (String)parent.getItemAtPosition(position);
				
				Toast.makeText(
						getApplicationContext(),
						"Вы выбрали "
								+ item,
						Toast.LENGTH_SHORT).show();*/
				openURL();
				
			}
        });
          
        urlField.addTextChangedListener(this);
        parent = prefs.getString("remFolderId", "default");
    }
    
    
    
    private void showSettings()
    {
    	Intent i = new Intent(getApplicationContext(), LoginDlg.class);
    	i.putExtra("userMail", userMail==null?"":userMail);
    	startActivityForResult(i, 3);
    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	//startActivity(i);
    }
    
    private void showArticleSuccess()
    {
    	String extra="";
    	switch (clipMode) {
		case 0: extra = getString(R.string.full_page);
			break;
		case 1: extra = getString(R.string.article);
			break;
		case 2: extra = getString(R.string.fragment);
			break;	

		default:
			break;
		}
    	Intent i = new Intent(getApplicationContext(), ArticleSuccess.class);
    	i.putExtra("mode", extra);
    	startActivityForResult(i, 4);
    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	//startActivity(i);
    }
    
    private void showArticleDlg(String url)
    {
    	Intent i = new Intent(getApplicationContext(), ArticleDlg.class);
    	i.putExtra("url", url);
    	startActivity(i);
    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	//startActivity(i);
    }
    
    void messageBox(String msg){
    	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(msg);
        dialog.setPositiveButton("OK", null); 
        dialog.setCancelable(true);
        dialog.create().show();
    	}
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
    		ContextMenuInfo menuInfo) 
    {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	//if (wv.getInjected()) return;
    	menu.add(Menu.NONE, IDM_OPEN, Menu.NONE, "Save Page");
    	menu.add(Menu.NONE, IDM_SAVE, Menu.NONE, "Save Image");
    	menu.add(Menu.NONE, IDM_SAVE, Menu.NONE, "Take Screenshot");
    }
    
    String getDate(){
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	Date date = new Date();
    	return (dateFormat.format(date));
    }
    
    String getDomainName(String href){
    	//return getDate();
    	try{
    		URL url = new URL(href);
    		return url.getHost();
    	}
    	catch (Exception e){
    		return getDate();//"about:blank";
    	}
    }
    
    public void screenCapture(/*boolean full*/)
    {
    	View v = super.findViewById(R.id.wvWrapper);  
    	/*if(full)  v = super.findViewById(R.id.wv);
    	else v = super.findViewById(R.id.wvWrapper);*/
    	 if( v != null) 
    	 { 
    		 Bitmap bm = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.RGB_565);
    		 v.draw(new Canvas(bm));
    	     

    		 if(bm!=null){
    			 try
    			 {
    				 /*String filename=String.valueOf(System.currentTimeMillis())+"-tmp.png";
    				 File file = new File(appSettings.getInstance(null).getSavingPath(),filename);              
    				 file.createNewFile();
    				 FileOutputStream ostream = new FileOutputStream(file);
    				 bm.compress(CompressFormat.PNG, 100, ostream);
    				 ostream.flush();
    				 ostream.close();*/
    				 String filename=appSettings.saveTempBitmap(bm);
    				 if(filename!=null && filename.length()>0){
    					 Intent iPaint = new Intent();
    					 /*ByteArrayOutputStream stream = new ByteArrayOutputStream();
    	    	     		bm.compress(Bitmap.CompressFormat.PNG, 100, stream);
    	    	     		byte[] byteArray = stream.toByteArray();*/
    					 //iPaint.putExtra("shot", byteArray);
    					 Toast.makeText(getApplicationContext(), "Saved as "+filename, Toast.LENGTH_LONG).show();
    					 iPaint.putExtra("temp", true);
    					 iPaint.putExtra("path", filename);
    					 iPaint.putExtra("domain", getDomainName(lastUrl));
    					 iPaint.setClassName("com.fvd.nimbus","com.fvd.nimbus.Paint");
    					 startActivity(iPaint);
    					 overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    				 }
    				 
    			}
    			 catch (Exception e) {
    				 e.printStackTrace();
    				 Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    			 }
    		 }
    		 else Toast.makeText(getApplicationContext(), "failed", Toast.LENGTH_LONG).show();
    		 
    	     	
    	 }
    	 else Toast.makeText(getApplicationContext(), "not found", Toast.LENGTH_LONG).show();
    }
    
    public boolean hasInternetConnection() {
	    ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
	    if (cm == null) {
	    	return false;
	    }
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    if (netInfo == null) {
	    	return false;
	    }	    
	    for (NetworkInfo ni : netInfo)
	    {
	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
	            if (ni.isConnected()) {
	                //Log.d(this.toString(), "test: wifi conncetion found");
					return true;
	            }
	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
	            if (ni.isConnected()) {
	                //Log.d(this.toString(), "test: mobile connection found");
					return true;
	            }
	    }
		return false;
	}
    
    protected void onNavButtonClicked()
    {
    	((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        if (this.navButtonState == NavButtonState.NBS_GO || this.navButtonState == NavButtonState.NBS_REFRESH)
        {
            //setNavButtonState(NavButtonState.NBS_STOP);
            openURL();
        }
        else if (this.navButtonState == NavButtonState.NBS_STOP)
        {
            setNavButtonState(NavButtonState.NBS_REFRESH);
            wv.stopLoading();

        }
    }
    
    private void setNavButtonState(NavButtonState nbs)
    {
        this.navButtonState = nbs;

        int res = R.drawable.button_reload;
        if (nbs == NavButtonState.NBS_REFRESH)
        {
            res = R.drawable.button_reload;
        }
        else if (nbs == NavButtonState.NBS_STOP)
        {
            res = R.drawable.button_stop;
        }

        Drawable icon = getResources().getDrawable(res);

        navButton.setImageDrawable(icon);
    }
    
    private void openURL()
    {
        String strUrl = urlField.getText().toString();

        if (strUrl != null && strUrl.length() > 0)
        {
            if (strUrl.toLowerCase().startsWith("http") == false && strUrl.length() != 0)
            {
                /*if (strUrl.startsWith("www.") == false)
                {
                    strUrl = "www." + strUrl;
                }*/

                strUrl = "http://" + strUrl;

                urlField.setText(strUrl);
            }
            wv.navigate(strUrl);
            //FVDOptions.setStringOption(FVDOptions.LAST_VISITED_URL, strUrl);
            setNavButtonState(NavButtonState.NBS_STOP);
        }

        wv.requestFocus();
    }
    
    /*
     protected void onStart();
     
     protected void onRestart();

     protected void onResume();

     protected void onPause();

     protected void onStop();

     protected void onDestroy();
     */
    
    @Override
    protected void onResume(){
    	super.onResume();
    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	if (prefs==null) prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	serverHelper.getInstance().setCallback(this,this);
    	if (sessionId.length()==0) sessionId = prefs.getString("sessionId", "");
    	serverHelper.getInstance().setSessionId(sessionId);
    }
    
    @Override
    protected void onPause(){
    	Editor e=prefs.edit();
    	e.putString("LAST_URL",lastUrl);
    	e.commit();
    	super.onPause();
    }
    
    boolean isInSelectionMode()
    {
    	return (wv.isInSelectionMode() || findViewById(R.id.bSaveFullPage).isSelected());
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_BACK) {
    	  if(findViewById(R.id.bSaveFullPage).getVisibility()!=View.GONE){
    		  toggleTools();
    		  return true;
    	  }
    	  if(isInSelectionMode()){
    		  wv.endSelectionMode();
    		  //wv.loadUrl("javascript:android.clearSelection();");
    		  return true;
    	  }
    	  if (wv.canGoBack()){
    		  wv.goBack();
    		  return true;
    	  }
    	  overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
      }
      else if (keyCode == KeyEvent.KEYCODE_HOME) {
    	  overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	  finish();
      }
      return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
    	super.onCreateOptionsMenu(menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item)  {
      switch (item.getItemId())
      {
      case R.id.om_settings:
    	  	Intent i = new Intent(getApplicationContext(), PrefsActivity.class);
      		startActivityForResult(i,6);
      		overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	  break;
      case R.id.om_home:
    	  overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	  finish();
    	  break;
      /*case R.id.om_login:
    	  selBuff="";
    	  showSettings();
    	  break;  */
      }
   
      return true;
    }
    
    /*private void saveSettings(Boolean val)
    {
      SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
      SharedPreferences.Editor editor = settings.edit();
      editor.putBoolean("IMGMODE", val);
      editor.commit();
    }*/
    
    private String getScriptContent(String sfile){
		String result="";
		InputStream is;
        try {
            is = this.getAssets().open(sfile);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            result = new String(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
		return result;
	}
	
	private String getScripts(){
		if (scripts.length()==0) scripts = getScriptContent("jq.js")/*+getScriptContent("rangy-core.js") */+getScriptContent("Article.js") +getScriptContent("android.selection.js")/*+getScriptContent("jq.js")*/;
		//String result=/*getScriptContent("jquery.js")+getScriptContent("randy-core.js")+getScriptContent("randy-serializer.js")+*/getScriptContent("android.selection.js");
		//return result;
		return scripts;
	}
	
	String tag="androidclipper";
	String parent = "default";
	@Override
    public void onTaskComplete(String result, String action)
    {
        /*if (progressDialog != null)
            progressDialog.dismiss();*/
        	try{
            	JSONObject root = new JSONObject(result);
            	int error = root.getInt("errorCode");
            	if (error == 0){
            		if (action.equalsIgnoreCase("user:auth")){
            			sessionId = root.getJSONObject("body").getString("sessionid");
            			serverHelper.getInstance().setSessionId(sessionId);
            					Editor e=prefs.edit();
            					e.putString("userMail", userMail);
                	    		e.putString("userPass", userPass);
                	    		e.putString("sessionId", sessionId);
                	    		e.commit();
                	    		
                	   appSettings.sessionId=sessionId;
                	   appSettings.userMail=userMail;
                	   appSettings.userPass=userPass;
            			
            			if(selHtml.length()>0){
                    	    sendNote(wv.getTitle(), selHtml,parent,tag);
                    	}
            			else if(selBuff.length()>0){
                    	    sendNote(wv.getTitle(), selBuff,parent,tag);
                    	    selBuff="";
                    	}
            			
            		}
            		else if(action.equalsIgnoreCase("notes:getfolders")){
            			
            			//ArrayList<FolderItem>items=new ArrayList<FolderItem>();
            			ArrayList<String>items=new ArrayList<String>();
            			try{
            				result = URLDecoder.decode(result,"UTF-16"); 
           	        	        String id="";
            	        		String title="";
            	        		JSONArray arr =  root.getJSONObject("body").getJSONArray("notes");
            	        		for(int i=0; i<arr.length();i++){
            	        			JSONObject obj = new JSONObject(arr.getString(i));
            	        			title= obj.getString("title");
            	        			id=obj.getString("global_id");
            	        			//title = fromUTF(title);
            	        			//items.add(new FolderItem(title,id));
            	        			items.add(title+"::"+id);
            	        		}
            	        		Intent intent = new Intent(getApplicationContext(), tagsActivity.class);
            	        		intent.putExtra("items", items);
            	        		intent.putExtra("current", prefs.getString("remFolderId", "default"));
            	    	    	startActivityForResult(intent,7);
            	    	    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
            	        		
            	        }
            	        catch (Exception Ex){
            	        	appSettings.appendLog("prefs:onTaskComplete "+Ex.getMessage());
            	        }
            		}
            		else if(action.equalsIgnoreCase("user:authstate")){
            			boolean auth = root.getJSONObject("body").getBoolean("authorized");
            			Toast.makeText(getApplicationContext(), "User " + (auth?"authorized":"not authorized"), Toast.LENGTH_LONG).show();
            		}
            		else if ("notes:update".equalsIgnoreCase(action)){
            			showArticleSuccess();
            		}
            		else if("notes:share".equalsIgnoreCase(action)){
            			String url=root.getString("url");
            			showArticleDlg(url);
            			//Toast.makeText(getApplicationContext(), "The fragment was successfully saved to your Nimbus account", Toast.LENGTH_LONG).show();
            		}
            		else if("user_register".equals(action)){
            			sendRequest("user:auth", String.format("\"email\":\"%s\",\"password\":\"%s\"",userMail,userPass));
            		}
            	} else Toast.makeText(getApplicationContext(), String.format("Error: %s",serverHelper.errorMsg(error)), Toast.LENGTH_LONG).show();
            }
            catch (Exception Ex){
            }
    }

	@Override
	public void onProgressChanged(int progress) {
		progressBar.setProgress(progress);
        if (progress == 100)
        {
            progressBar.setVisibility(View.INVISIBLE);
        }
        else
        {
            progressBar.setVisibility(View.VISIBLE);
        }
	}

	@Override
	public void onConsoleMessage(String message, int lineNumber, String sourceID) {
		//Log.v("ChromeClient", "invoked: onConsoleMessage() - " + sourceID + ":"  + lineNumber + " - " + message);
	}

	@Override
	public void onConsoleMessage(ConsoleMessage cm) {
		//Log.v("ChromeClient", cm.message() + " -- From line "  + cm.lineNumber() + " of " + cm.sourceId() );
	}

	@Override
	public void onJsAlert(String url, String message, final JsResult result) {
		if(this!=null)try{
		new AlertDialog.Builder(this)  
        .setTitle(wv.getTitle())  
        .setMessage(message)  
        .setPositiveButton(android.R.string.ok,  
                new AlertDialog.OnClickListener()   
                {  
                    public void onClick(DialogInterface dialog, int which)   
                    {  
                        result.confirm();  
                    }  
                })  
        .setCancelable(false)  
        .create()  
        .show();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void onReceivedError(int errorCode, String description,	String failingUrl) 
	{
		Toast.makeText(getApplicationContext(), "Error: " + description+ " " + failingUrl, Toast.LENGTH_LONG).show();
	}

	@Override
	public void onPageStarted(String url, Bitmap favicon) {
		setNavButtonState(NavButtonState.NBS_STOP);
		lastUrl = url;
    	urlField.setText(url);
    	saveToHistory(url);
	}

	@Override
	public void onPageFinished(String url) {
		String s=String.format("var fvdViewHeight=%d;\r\nvar fvdSaveCss=%s;\r\n",wv.getHeight(),saveCSS==true?"true":"false");
		wv.eval("javascript:" +String.format("var fvdViewHeight=%d;\r\nvar fvdSaveCss=%s;\r\n",wv.getHeight(),saveCSS==true?"true":"false") +getScripts()); 
  	  	setNavButtonState(NavButtonState.NBS_REFRESH);
  	  	lastUrl = url;
  	  	Editor e=prefs.edit();
  	  	e.putString("LAST_URL",lastUrl);
  	  	e.commit();
  	  	try{
  	  		WebHistoryKeeper.getInstance().UpdateItemDescription(url, wv.getTitle());
  	  	}
  	  catch (Exception ce) {
			//Log.e("fvdWebView","onSelectionChanged: "+e.getMessage());	
		} 
	}
	
	private String m_strLastUrl = "";
    protected void saveToHistory(String url)
    {
        String host = WebHistoryKeeper.getInstance().getHost(url);
        if (!host.equalsIgnoreCase(m_strLastUrl))
        {
            m_strLastUrl = host;
            WebHistoryKeeper.getInstance().AddHistoryItem(url, null, HistoryItem.ETipType.TIP_HOST);
        }
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// if statement prevents force close error when picture isn't selected
		if (progressDialog != null){
            progressDialog.dismiss();
			}
		if (requestCode==6){
			serverHelper.getInstance().setCallback(this,this);
			saveCSS = prefs.getString("clipStyle", "1").equals("1");
			//Log.i("nimbus",saveCSS==true?"true":"false");
			wv.eval(String.format("javascript:android.fvdSaveCss(%s)",saveCSS==true?"true":"false"));
			if("1".equals(prefs.getString("userAgent", "1"))){
	        	wv.setUserAgent(null);
	        }
	        else wv.setUserAgent(deskAgent);
		}
		else
    	if (requestCode==3){
    		if (resultCode==RESULT_OK|| resultCode==RESULT_FIRST_USER){
    			userMail=data.getStringExtra("userMail");
    			userPass=data.getStringExtra("userPass");
    				//sendRequest("user:auth", String.format("\"email\":\"%s\",\"password\":\"%s\"",userMail,userPass));
    			if(resultCode==RESULT_OK)	
    				sendRequest("user:auth", String.format("\"email\":\"%s\",\"password\":\"%s\"",userMail,userPass));
    			else serverHelper.getInstance().sendOldRequest("user_register", String.format("{\"action\": \"user_register\",\"email\":\"%s\",\"password\":\"%s\",\"_client_software\": \"ff_addon\"}",userMail,userPass), "");
    		}
    	}
    	else if (requestCode==4){
    		if (resultCode==RESULT_OK){
    			if (serverHelper.getInstance().canShare()){
    				serverHelper.getInstance().shareNote();
    			}
    		}
    	}
    	else if (requestCode==5){
    		if (resultCode==RESULT_OK){
    			selBuff=data.getStringExtra("content");
    			if(sessionId.length() == 0 || userPass.length()==0) showSettings();
    			else {
    				
    				if(prefs.getBoolean("check_fast", false)){
    					sendNote(wv.getTitle(), selBuff, parent, tag);
        				selBuff="";
    				}
    				else {
    				serverHelper.getInstance().setCallback(this,this);
    				if(serverHelper.getInstance().getSession().length()>0) {
    					serverHelper.getInstance().sendRequest("notes:getFolders", "","");
    					}
    				}
    			}
    			/*selBuff=data.getStringExtra("content");
    			parent = data.getStringExtra("parent");
    			tag = data.getStringExtra("tag");
    			if(sessionId.length() == 0 || userPass.length()==0) showSettings();
    			else {
    				sendNote(wv.getTitle(), selBuff, parent, tag);
    				selBuff="";
    			}*/
    		} 
    		wv.endSelectionMode();
    	}
    	else if(requestCode==7){
    		if (resultCode==RESULT_OK && data!=null){
    		
			parent = data.getStringExtra("id");
			tag = data.getStringExtra("tag");
			if(sessionId.length() == 0 || userPass.length()==0) showSettings();
			else {
				sendNote(wv.getTitle(), selBuff, parent, tag);
				selBuff="";
			}
    		}
    	}
    }
	
	@Override
	public void onSelectionChanged(final String plainText, final String html, Rect menuRect) {
		try{
			//Log.i("nimbus", plainText);
			if (progressDialog != null){
                progressDialog.dismiss();
   			}
			/*appSettings.appendLog(html);	
			Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();
			return;*/
			/*selHtml="";
			selBuff="";*/
			//Log.e("fvdWebView","SelectionChanged hb:");
			if(plainText.equals("action:savePage")){
				if(html.length()>0){
					
            		if (sessionId.length() == 0) {
            			selBuff=html;
            			showSettings();
            		}
            		else {
            			sendNote(wv.getTitle(), html,parent,tag);
            			wv.endSelectionMode();
            		}
            	}
			}
			else 
				if(plainText.equals("action:saveArticle")){
					if(html.length()>0){
						Intent i = new Intent(getApplicationContext(), previewActivity.class);
            	    	i.putExtra("url", wv.getUrl());
            	    	i.putExtra("title", wv.getTitle());
            	    	i.putExtra("content", html);
            	    	startActivityForResult(i,5);
            	    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
	            	}
				}
			else{
				if(plainText.equals("action:selectionChanged")){
					selBuff="";
					selText=plainText==null?"":plainText;
					selHtml = html==null?"":html;
					Message msg = new Message();
					msg.arg1=selHtml.length()>0?1:0;
					msg.what = 0;
					
					handler.sendMessage(msg);
				}
			}
		}
		catch (Exception e) {
			//Log.e("fvdWebView","onSelectionChanged: "+e.getMessage());	
		} 
		/*if(this.contextMenuVisible){
			return;
		}
		if(menuRect.right <= menuRect.left){
			return;
		}
		mContextMenu  = new QuickAction(this);
		mContextMenu.setOnDismissListener(this);
		mContextMenu.addActionItem(buildActionItem("Send to Nimbus",1,R.drawable.menu_search));
		mContextMenu.addActionItem(buildActionItem("Button 2",2,R.drawable.menu_info));
		mContextMenu.addActionItem(buildActionItem("Button 3",3,R.drawable.menu_eraser));
		mContextMenu.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			@Override
			public void onItemClick(QuickAction source, int pos, int actionId) {
				// TODO Auto-generated method stub
				switch (actionId) {
				case 1:
					//serverHelper.getInstance().uploadNote("test", "<html><body>" + plainText + "</body></html>");
					sendNote(wv.getTitle(), html);
					mContextMenu.dismiss();
					wv.endSelectionMode();					
					break;

				default:
					break;
				}
				contextMenuVisible = false;
			}
		});
		this.contextMenuVisible = true;
		mContextMenu.show(wv, menuRect);*/
		
	}

	private void showContextMenu(Rect displayRect){
		// Don't show this twice
		/*if(this.contextMenuVisible){
			return;
		}
		if(displayRect.right <= displayRect.left){
			return;
		}
		mContextMenu  = new QuickAction(this);
		mContextMenu.setOnDismissListener(this);
		mContextMenu.addActionItem(buildActionItem("Button 1",1,R.drawable.menu_search));
		mContextMenu.addActionItem(buildActionItem("Button 2",2,R.drawable.menu_info));
		mContextMenu.addActionItem(buildActionItem("Button 3",3,R.drawable.menu_eraser));
		mContextMenu.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
			@Override
			public void onItemClick(QuickAction source, int pos,
				int actionId) {
				// TODO Auto-generated method stub
				if (actionId == 1) { 
					// Do Button 1 stuff
		        } 
				else if (actionId == 2) { 
		        } 
		        else if (actionId == 3) { 
		        }
				contextMenuVisible = false;
			}
		});
		this.contextMenuVisible = true;
		mContextMenu.show(wv, displayRect);*/
	}

	@Override
	public void onEndSelection() {
		ImageButton ib =  (ImageButton)findViewById(R.id.bSaveFullPage);
		ib.setSelected(false);
		findViewById(R.id.bZoomStack).setVisibility(View.GONE);
		findViewById(R.id.bDone).setVisibility(View.GONE);
		findViewById(R.id.bToggleMenu).setVisibility(View.VISIBLE);
		

	}
	
	void sendNote(String title, String note, String parent, String tag){
		serverHelper.getInstance().uploadNote(title, /*"<html><body>" + */note /*+ "</body></html>"*/,wv.getUrl(),/*prefs.getString("remFolderId", "default")*/parent, tag);
	}
	
	private void sendRequest(String action, String data){
		serverHelper.getInstance().sendRequest(action, data,"");
	}



	@Override
	public boolean getCanBrowse() {
		// TODO Auto-generated method stub
		return !(findViewById(R.id.bSaveFullPage).isSelected());
	}


	public ArrayList<HistoryItem> getHistoryItems()
    {
        String pattern = urlField.getText().toString().toLowerCase();
        
        ArrayList<HistoryItem> hiArray = WebHistoryKeeper.getInstance().getItemsFor(pattern);

        if (pattern.trim().length() > 0 && pattern.indexOf("://")==-1 && hiArray.size() < 2)
        {
            // Add Google search
            //String gq = MiscTools.getGoogleSearchRequest(pattern);
           //hiArray.add(new HistoryItem(gq, m_Activity.getString(R.string.SEARCH_WEB_SITE), pattern, HistoryItem.ETipType.TIP_SEARCH));

            // Add google suggestions
            /*int nSuggestions = WebHistoryKeeper.FIRST_ITEMS_COUNT_TO_GET - hiArray.size();
            AddGoogleSuggestions(new SuggestionTaskParam(pattern, nSuggestions));*/
        }

        return hiArray;
    }

	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}


	
	
	/*private class TextAdapter extends ArrayAdapter<HistoryItem> {

		public TextAdapter(Context context) {
			super(context, R.layout.url_hint_layout, m_arrHistoryItems);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			HistoryItem cat = getItem(position);

			if (convertView == null) {
				convertView = LayoutInflater.from(getContext())
						.inflate(R.layout.url_hint_layout, null);
			}
			((TextView) convertView.findViewById(R.id.turl))
					.setText(cat.visibleUrl);
			((TextView) convertView.findViewById(R.id.tdesc))
			.setText(cat.desc);
			
			return convertView;
		}
	}*/
	
	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		if(!isInitNow){
			ArrayList<HistoryItem> history = getHistoryItems();
			/*m_arrHistoryItems.clear();
			m_arrHistoryItems.addAll(history);*/
			arrHistoryItems.clear();
			for (HistoryItem historyItem : history) {
				arrHistoryItems.add(historyItem.url);
			}
			//adapter.notifyDataSetChanged();
			//urlField.setAdapter(new TextAdapter(ctx));
			urlField.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,arrHistoryItems));
		}
	}
	
	/*protected Dialog onCreateDialog(int id) {
	    AlertDialog.Builder adb = new AlertDialog.Builder(this);
	    switch (id) {
	    // массив
	    case DIALOG_CAPTURE:
	      adb.setTitle("CAPTURE");
	      adb.setItems(captureItems, myClickListener);
	      break;
	    // адаптер
	    case DIALOG_CLIP:
	    	adb.setTitle("CLIP");
		    adb.setItems(clipItems, myClickListener);
	      break;
	    }
	    return adb.create();
	  }

	  protected void onPrepareDialog(int id, Dialog dialog) {
	    AlertDialog aDialog = (AlertDialog) dialog;
	    ListAdapter lAdapter = aDialog.getListView().getAdapter();
	 
	    switch (id) {
	    case DIALOG_CAPTURE:
	    case DIALOG_CLIP:
	      if (lAdapter instanceof BaseAdapter) {
	        BaseAdapter bAdapter = (BaseAdapter) lAdapter;
	        bAdapter.notifyDataSetChanged();
	      }
	      break;
	    default:
	      break;
	    }
	  };

	  OnClickListener myClickListener = new OnClickListener() {
	    public void onClick(DialogInterface dialog, int which) {
	    	screenCapture(which == 1);
	    }
	  };*/
	void toggleTools(){
		if(findViewById(R.id.bSaveFullPage).getVisibility()!=View.GONE){
			findViewById(R.id.vShadow).setVisibility(View.GONE);
			//findViewById(R.id.bToggleMenu).startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_ccw));
			findViewById(R.id.bSaveFullPage).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fadeout));
			findViewById(R.id.bSaveFullPage).setVisibility(View.GONE);
			findViewById(R.id.lSaveFullPage).setVisibility(View.GONE);
			
			findViewById(R.id.bSavePageFragment).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fadeout1));
			findViewById(R.id.bSavePageFragment).setVisibility(View.GONE);
			findViewById(R.id.lSavePageFragment).setVisibility(View.GONE);
			
			findViewById(R.id.bTakeScreenshot).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fadeout2));
			findViewById(R.id.bTakeScreenshot).setVisibility(View.GONE);
			findViewById(R.id.lTakeScreenshot).setVisibility(View.GONE);
			findViewById(R.id.lBrowser).setEnabled(true);
			findViewById(R.id.etAddess).setEnabled(true);
			findViewById(R.id.ibReloadWebPage).setEnabled(true);
			findViewById(R.id.wv).setClickable(true);
			findViewById(R.id.wv).setEnabled(true);
		}
		else {
			findViewById(R.id.lBrowser).setEnabled(false);
			findViewById(R.id.etAddess).setEnabled(false);
			findViewById(R.id.ibReloadWebPage).setEnabled(false);
			findViewById(R.id.wv).setEnabled(false);
			findViewById(R.id.wv).setClickable(false);
			
			//findViewById(R.id.bToggleMenu).startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_cw));
			findViewById(R.id.bTakeScreenshot).setVisibility(View.VISIBLE);
			findViewById(R.id.bTakeScreenshot).startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
			
			
			findViewById(R.id.bSavePageFragment).setVisibility(View.VISIBLE);
			findViewById(R.id.bSavePageFragment).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fadein));
			
			/*findViewById(R.id.bSavePageFragment).postDelayed(new Runnable() {
	            @Override
	            public void run() {
	                // TODO Auto-generated method stub
	            	findViewById(R.id.bSavePageFragment).setVisibility(View.VISIBLE);
	    			findViewById(R.id.bSavePageFragment).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fadein));
	            }
	        },50);*/
			
			findViewById(R.id.bSaveFullPage).setVisibility(View.VISIBLE);
			findViewById(R.id.bSaveFullPage).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fadein1));
			
			
			
			
			
			findViewById(R.id.bSaveFullPage).postDelayed(new Runnable() {
	            @Override
	            public void run() {
	            	findViewById(R.id.vShadow).setVisibility(View.VISIBLE);
	            	findViewById(R.id.lTakeScreenshot).setVisibility(View.VISIBLE);
	    			findViewById(R.id.lSavePageFragment).setVisibility(View.VISIBLE);
	    			findViewById(R.id.lSaveFullPage).setVisibility(View.VISIBLE);
	            }
	        },150);
		}
	}
}
