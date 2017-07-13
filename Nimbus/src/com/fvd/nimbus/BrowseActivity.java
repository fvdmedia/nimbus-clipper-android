package com.fvd.nimbus;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.acl.LastOwnerException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.json.JSONArray;


import android.R.string;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
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
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
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
import android.net.IpPrefix;
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
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;


import com.fvd.utils.FolderItem;
import com.fvd.utils.HistoryItem;
import com.fvd.utils.WebHistoryKeeper;
import com.fvd.utils.AsyncTaskCompleteListener;
import com.fvd.utils.appSettings;
import com.fvd.utils.prefsEditListener;
import com.fvd.utils.serverHelper;
import com.fvd.browser.QuickReturnViewType;
import com.fvd.browser.QuickReturnWebViewOnScrollChangedListener;
import com.fvd.browser.fvdBrowerEventsListener;
import com.fvd.browser.fvdWebView;
import com.fvd.classes.BugReporter;
import com.fvd.classes.DataExchange;
import com.fvd.classes.DrawerMenuAdapter;
import com.fvd.classes.FolderListItem;

//import android.content.DialogInterface.OnClickListener;
import com.getbase.floatingactionbutton.FloatingActionsMenu;



public class BrowseActivity extends Activity implements AsyncTaskCompleteListener<String, String>, fvdBrowerEventsListener,TextWatcher, OnItemClickListener{

	private final String deskAgent="Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/30.0.1599.17 Safari/537.36";
	
	private static final int IDM_OPEN = 101; 
	private static final int IDM_SAVE = 102; 
	
	final int SHOW_SETTINGS=11;
	final int DIALOG_CAPTURE = 1;
	final int DIALOG_CLIP = 2;
	final int DIALOG_FOLDERS = 7;
	
	final int TAKE_PHOTO=210;
	final int TAKE_PICTURE=211;
	String captureItems[] = { "view", "full" };
	String clipItems[] = { "one", "two", "three", "four" };
	
	private fvdWebView wv;
	private String lastUrl = "";
	Menu myMenu = null;

	DrawerLayout drawer;
	DataExchange clipData;
	private AutoCompleteTextView urlField;
	private ImageButton     navButton;
	private ProgressBar     progressBar;
	//private SharedPreferences prefs;

	private String scripts="";
	//private String selHtml="";
	//private String selBuff="";
	//private String selText="";
	Context ctx;
	
	//private String userMail = "";
	private String userPass ="";
	//private String session_Id = "";
	private Handler handler;
	private boolean saveCSS = false;
	private int clipMode=0;
	private ProgressDialog progressDialog;
	private SharedPreferences prefs;
	String lastAction="";
	String surl="";
	List<String>  arrHistoryItems=new ArrayList<String>();
	boolean isInitNow = true;
	
	FloatingActionsMenu floatMenu;
	private enum NavButtonState
    {
        NBS_GO,
        NBS_REFRESH,
        NBS_STOP
    }
	
	View mQuickReturnHeaderTextView;
    View mQuickReturnFooterTextView;

	
    private  NavButtonState     navButtonState = NavButtonState.NBS_GO;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try{
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        catch (Exception e){
        	e.printStackTrace();
        }
        clipData = new DataExchange();
        isInitNow = true;
        setContentView(R.layout.screen_browser);
        serverHelper.getInstance().setCallback(this,this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        lastUrl = "0".equals(prefs.getString("startNew", "1"))?"":prefs.getString("LAST_URL","");
        saveCSS = prefs.getString("clipStyle", "1").equals("1");
        ctx = this;
        appSettings.init(ctx);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_VIEW.equals(action) /*&& type != null*/) {
        	Uri data = intent.getData();
            if(data!=null){
            	surl=data.toString();
            }
        }
        else if(Intent.ACTION_SEND.equals(action) /*&& type != null*/){
        	if ("text/plain".equals(type)) {
        		surl=intent.getStringExtra(Intent.EXTRA_TEXT);
            } 
        }
        //surl="Kick NOW! Будущее ближе с Kickstarter / Geektimes https://m.geektimes.ru/company/pochtoy/blog/280580//";
        if(surl.contains(" ")){
			String[] arr=surl.replace("\t", " ").replace("\r", " ").replace("\n", " ").split(" ");
			for (String s : arr) {
				if(s.contains("://")){
					lastUrl=s.trim();
					break;
				}
			}
		} else if(surl.contains("://")) lastUrl=surl.trim();
        
        drawer=(DrawerLayout)findViewById(R.id.root);

        wv = (fvdWebView) findViewById(R.id.wv);
        wv.setEventsHandler(this);
        urlField = ( AutoCompleteTextView )findViewById(R.id.etAddess);
        urlField.setSelectAllOnFocus(true);
        urlField.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            	if (actionId == EditorInfo.IME_ACTION_DONE)
                {
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
        onViewCreated();  
        
        
        
        handler = new Handler() {
    		@Override
    		public void handleMessage(Message msg) {
    			switch (msg.what) {
				case 0:
					findViewById(R.id.bZoomStack).setVisibility(View.VISIBLE);
					findViewById(R.id.bToggleMenu).setVisibility(View.GONE);
					break;
				
				default:
					break;
				}
    		}
    	};
        
        
        
        navButton = (ImageButton)findViewById(R.id.ibReloadWebPage);
        navButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            	
            	onNavButtonClicked();
            }
        });
        
        findViewById(R.id.bSavePageFragment).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            	//toggleTools();
            	floatMenu.collapse();
            	if(!wv.getInjected()) Toast.makeText(ctx, getString(R.string.wait_load), Toast.LENGTH_LONG).show();
            	clipMode =2;		
            	if(wv.getInjected()/* && !v.isSelected()*/){
            		wv.setCanClip(true);
            		v.setSelected(true);
            		Toast.makeText(ctx, ctx.getString(R.string.use_longtap), Toast.LENGTH_LONG).show();
            	}
            	
            }
        });
        
        (findViewById(R.id.bSaveFullPage)).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            	
            	floatMenu.collapse();
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
        
        
        (findViewById(R.id.bgetPcket)).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            	clipMode =1;
            	floatMenu.collapse();
            	progressDialog = ProgressDialog.show(v.getContext(), "Nimbus Clipper", getString(R.string.please_wait), true, false);
            	serverHelper.getInstance().getPocketRequest(wv.getUrl(), new AsyncTaskCompleteListener<String, String>() {
					
					@Override
					public void onTaskComplete(String result, String adv) {
						// TODO Auto-generated method stub
						try{
			            	progressDialog.dismiss();
							serverHelper.getInstance().completed();
							JSONObject root = new JSONObject(result);
			            	if(root.has("responseCode")&&root.getInt("responseCode")==200){
			            		
			            		String title=root.getString("title");
			            		String html=root.getString("article");
			            		html="<style> img {border:0; font-size:100%; margin:0 0 18px 0; vertical-align:baseline; display:inline-block; max-width:100%; height:auto;} a {color:#19a1b9; text-decoration:none;} p {margin:0 0 1.5em 0; font-size:1em;} ul {margin:0 0 1.5em 1.5em;} ol {margin:0 0 1.5em 1.5em;} li {padding:0 0 0 20px; position:relative;} table{border-collapse:collapse; border-spacing:0; margin:0 0 1.5em 0;} td {border:1px solid #555; padding:10px; text-align:left; vertical-align:top;} th {border:1px solid #555; padding:10px; text-align:left; vertical-align:top;}  h1 {font-size:1.5em; margin:1.7em 0 0.7em 0;} h2 {font-size:1.4em; margin:1.7em 0 0.7em 0;} h3 {font-size:1.3em; margin:1.7em 0 0.5em 0;} h4 {font-size:1.2em; margin:1.7em 0 0.5em 0;} h5 {font-size:1.2em; margin:1.7em 0 0.3em 0;} h6 {font-size:1.1em; margin:1.7em 0 0.3em 0;} </style><div style=\"font: 18px Georgia,Arial,Helvetica,sans-serif; line-height:1.7;\">"+html+"</div>";
			            		clipData.setContent(html);
								clipData.setTitle(title);
								
								Intent i = new Intent(getApplicationContext(), previewActivity.class);
		            	    	i.putExtra("url", wv.getUrl());
		            	    	i.putExtra("title", wv.getTitle());
		            	    	i.putExtra("content", clipData);
		            	    	i.putExtra("pocket", true);
		            	    	startActivityForResult(i,5);
		            	    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
		            	    	
			            	}
						}
						catch(Exception e){}
					}
				});
            }
        });
        
        findViewById(R.id.bTakeScreenshot).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            	//toggleTools();
            	floatMenu.collapse();
    			findViewById(R.id.bSaveFullPage).setVisibility(View.GONE);
    			findViewById(R.id.bSavePageFragment).setVisibility(View.GONE);
    			findViewById(R.id.bTakeScreenshot).setVisibility(View.GONE);
            	if(wv.getInjected()){
            		wv.setCanClip(false);
            	}
            	findViewById(R.id.bToggleMenu).setVisibility(View.GONE);
            	            	
            	findViewById(R.id.bTakeScreenshot).postDelayed(new Runnable() {
		            @Override
		            public void run() {
		                // TODO Auto-generated method stub
		            	screenCapture();
		            	findViewById(R.id.bToggleMenu).setVisibility(View.VISIBLE);
		            	finish();
		            }
		        },10);
            	
            }
        });
        
        (findViewById(R.id.bDone)).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            	{
            		try{
            		
	            		wv.setCanClip(false);
	        			wv.endSelectionMode();
	        			//findViewById(R.id.bSavePageFragment).setSelected(false);
	        			clipMode =2;
	           			wv.endSelectionMode();
	           			String selHtml=clipData.getContent();
	        			if(selHtml.length()>0){
	        				String ss=selHtml.substring(0,selHtml.indexOf(">")+1).toLowerCase();
	        				int j=ss.indexOf("<div");
	        				if(j==0){
	        					j=ss.indexOf("style");
	        					if(j>0){
	        						int k=ss.indexOf("\"",j+11);
	        						if(k>0) selHtml = selHtml.replace(selHtml.substring(j,k+1),"");
	        					}
	        				}
	        				clipData.setContent(selHtml);
	        				clipData.setTitle(wv.getTitle());
	        				
        				
		        			Intent i = new Intent(getApplicationContext(), previewActivity.class);
		        	    	i.putExtra("content", clipData);
		        	    	startActivityForResult(i,5);
		        	    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
	        			}
            		}
            		catch (Exception e){
            			BugReporter.Send("onEndSelection", e.getMessage());
            		}
        		}
            	//showDialog(DIALOG_CAPTURE);
            }
        });
        
        findViewById(R.id.bZoomIn).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            	wv.ZoomInSelection();
            }
        });
        
        findViewById(R.id.bZoomOut).setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
            	wv.ZoomOutSelection();
            }
        });
        
        
        
        setNavButtonState(NavButtonState.NBS_GO);
        
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        
        appSettings.userMail = prefs.getString("userMail", "");
        userPass = prefs.getString("userPass", "");
        appSettings.sessionId = prefs.getString("sessionId", "");
 	    appSettings.userPass=userPass;
        
        if("1".equals(prefs.getString("userAgent", "1"))){
        	wv.setUserAgent(null);
        }
        else wv.setUserAgent(deskAgent);
        
        //final Activity activity = this;
        
        if (lastUrl.length()>0){
        	lastUrl=lastUrl.replace("[", "").replace("]", "");
        	urlField.setText(lastUrl);
        	openURL();
        }
        isInitNow = false;
        
        urlField.setOnItemClickListener(new OnItemClickListener() {
		    @Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				// TODO Auto-generated method stub
				openURL();
				
			}
        });
          
        urlField.addTextChangedListener(this);
        parent = prefs.getString("remFolderId", "default");
    }
    
    /*private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //selectItem(position);
        }
    }*/
    
    
    
    private void showSettings()
    {
    	Intent i = new Intent(getApplicationContext(), loginActivity.class/* LoginDlg.class*/);
    	i.putExtra("userMail", appSettings.userMail==null?"":appSettings.userMail);
    	startActivityForResult(i, 22);
    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    }
    
    public void onButtonClick(View v){
    	drawer.closeDrawer(GravityCompat.START);
		Intent ip=new Intent();
		switch (v.getId()) {
		case R.id.lbTakePhoto:
			getPhoto();
			break;
		case R.id.lbFromGallery:
			getPicture();
			break;
		case R.id.lbPdfAnnotate:
    		ip.setClassName("com.fvd.nimbus","com.fvd.nimbus.ChoosePDFActivity");
    		startActivity(ip);
			break;
		case R.id.libSettings:
    		Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
        	startActivityForResult(intent,SHOW_SETTINGS);
        	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    		return;
		default:
			break;
		}
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
    	
    	Toast.makeText(this, String.format(getString(R.string.save_article_mask), extra), Toast.LENGTH_SHORT).show();
  	
    }
    
    private void showArticleDlg(String url)
    {
    	Intent i = new Intent(getApplicationContext(), ArticleDlg.class);
    	i.putExtra("url", url);
    	startActivity(i);
    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
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
    	 if( v != null) 
    	 { 
    		 Bitmap bm = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.RGB_565);
    		 v.draw(new Canvas(bm));
    	     

    		 if(bm!=null){
    			 try
    			 {
    				 String filename=appSettings.saveTempBitmap(bm,false);
    				 if(filename!=null && filename.length()>0){
    					 Intent iPaint = new Intent();
    					 iPaint.putExtra("temp", true);
    					 iPaint.putExtra("path", "file://"+filename);
    					 iPaint.putExtra("domain", getDomainName(lastUrl));
    					 iPaint.setClassName("com.fvd.nimbus","com.fvd.nimbus.PaintActivity");
    					 startActivity(iPaint);
    					 overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
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
    
    void hideKeyboard(){
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm!=null){
			imm.hideSoftInputFromWindow(findViewById(R.id.etAddess).getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
    
    protected void onNavButtonClicked()
    {
    	//((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    	try{
    		hideKeyboard();
    	}
    	catch (Exception e){
    		
    	}
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
        hideKeyboard();
    }
    
    private void openURL()
    {
        String strUrl = urlField.getText().toString();
        if (strUrl != null && strUrl.length() > 0)
        {
            if (strUrl.length() != 0 && strUrl.toLowerCase().startsWith("http") == false)
            {
                strUrl = "http://" + strUrl;
                urlField.setText(strUrl);
            }
            wv.navigate(strUrl);
            setNavButtonState(NavButtonState.NBS_STOP);
        }

        wv.requestFocus();
    }
    
   
    @Override
    protected void onResume(){
    	super.onResume();
    	if (prefs==null) prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	serverHelper.getInstance().setCallback(this,this);
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
    
    void updateBackButton(){
    	findViewById(R.id.ibBackPage).setVisibility(wv.canGoBack()?View.VISIBLE:View.GONE);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
      if (keyCode == KeyEvent.KEYCODE_BACK) {
    	  if(drawer.isDrawerOpen(GravityCompat.START)){
  			drawer.closeDrawer(GravityCompat.START);
  			return true;
  		  }
    	  if(floatMenu.isExpanded()){
    		  //toggleTools();
    		  floatMenu.collapse();
    		  return true;
    	  }
    	  if(isInSelectionMode()){
    		  wv.endSelectionMode();
    		  //wv.loadUrl("javascript:android.clearSelection();");
    		  return true;
    	  }
    	  overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
      }
      else if (keyCode == KeyEvent.KEYCODE_HOME) {
    	  overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
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
      		overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    	  break;
      case R.id.om_home:
    	  overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    	  finish();
    	  break;
      }
   
      return true;
    }
    
   
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
		if (scripts.length()==0) scripts = getScriptContent("jq.js")+getScriptContent("Article.js") +getScriptContent("android.selection.js");
		return scripts;
	}
	
	String tag="androidclipper";
	String parent = "default";
	@Override
    public void onTaskComplete(String result, String action)
    {
        	try{
            	JSONObject root = new JSONObject(result);
            	int error = root.getInt("errorCode");
            	if (error == 0){
            		if (action.equalsIgnoreCase("user:auth")){
            			String session_Id = root.getJSONObject("body").getString("sessionid");
            					Editor e=prefs.edit();
            					e.putString("userMail", user_mail);
                	    		e.putString("userPass", userPass);
                	    		e.putString("sessionId", session_Id);
                	    		e.commit();
                	    		
                	   appSettings.sessionId=session_Id;
                	   appSettings.userMail=user_mail;
                	   appSettings.userPass=userPass;
            			if(lastAction!="") {
            				serverHelper.getInstance().sendRequest(lastAction, "","");
            			} else
            			if(clipData!=null && clipData.getContent().length()>0){
            				if(prefs.getBoolean("check_fast", false)){
            					sendNote(clipData.getTitle(), clipData.getContent(), parent, tag);
            					clipData.setContent("");
            				}
            				else {
            				serverHelper.getInstance().setCallback(this,this);
            				if(appSettings.sessionId.length()>0) {
            					serverHelper.getInstance().sendRequest("notes:getFolders", "","");
            					}
            				}
                    	}
            		}
            		else if(action.equalsIgnoreCase("notes:getfolders")){
            			
            			try{
            					result = URLDecoder.decode(result,"UTF-16"); 
            	        		clipData.setData(result);
            	        		String cr=prefs.getString("remFolderId", "default");
            	        		if(cr==null || cr=="") cr="default";
            	        		clipData.setId(cr);
            	        		clipData.setTitle(wv.getTitle());
            	        		clipData.setTags("androidclipper");
            	        		Intent intent = new Intent(getApplicationContext(), tagsActivity.class);
            	        		intent.putExtra("xdata", clipData);
            	        		
            	    	    	startActivityForResult(intent,DIALOG_FOLDERS);
            	    	    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
            	    	    	clipData.setData("");
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
            			sendRequest("user:auth", String.format("\"email\":\"%s\",\"password\":\"%s\"",user_mail,userPass));
            		}
            	} else 
            		{	
            			if(action.equalsIgnoreCase("user:auth")){
            				lastAction="";
            				showSettings();
            			}
            			else if(error==-6){
                			if(action=="notes:getFolders") lastAction=action;
                			else lastAction="";
                			showSettings();
                		} 
            			else Toast.makeText(getApplicationContext(), String.format("Error: %s",serverHelper.errorMsg(error)), Toast.LENGTH_LONG).show();
            		}
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
    	View v=findViewById(R.id.ibBackPage);
    	
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
  	  updateBackButton();
  	  
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
	
    String user_mail;
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// if statement prevents force close error when picture isn't selected
		if (progressDialog != null){
            progressDialog.dismiss();
			}
		if (requestCode == TAKE_PHOTO) {
	    	  boolean isCropResult=false;
	    	if (resultCode == -1){
		    	try{
		    		if (data != null) {
		    			Uri resultUri = data.getData();
		    			
						if(resultUri!=null){
							String drawString = resultUri.getPath();
							if(drawString.startsWith("/storage"))  drawString ="file://"+drawString;
		    			 	else drawString = resultUri.toString();
							
							if (drawString.length() > 0 && drawString.indexOf("/exposed_content/")==-1)
		    			 	{
		    			 		try{
		    			 			Intent iPaint = new Intent();
		    			 			iPaint.putExtra("path", drawString);
		    			 			iPaint.setClassName("com.fvd.nimbus","com.fvd.nimbus.PaintActivity");
		    			 			startActivity(iPaint);
		    			 			overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
		    			 			finish();
		    			 		}
		    			 		catch (Exception e){}
		    			 	}
						}
		    		}
		    	}
		    	catch (Exception e) {
		    		appSettings.appendLog("main:onActivityResult: exception -  "+e.getMessage());
				}
	    	}
	      }
	      else if (requestCode == TAKE_PICTURE){
		    	  if(resultCode == -1 && data!=null){
		    		  boolean temp = false;
		    		  
		    		 try { 
		    			 	Uri resultUri = data.getData();
		    			 	String drawString = resultUri.getPath();
		    			 	if(drawString.startsWith("/storage"))  drawString ="file://"+drawString;
		    			 	else drawString = resultUri.toString();
	
		    			 	
		    			 	if (drawString.length() > 0 && drawString.indexOf("/exposed_content/")==-1)
		    			 	{
		    			 		try{
		    			 			Intent iPaint = new Intent();
		    			 			iPaint.putExtra("temp", temp);
		    			 			iPaint.putExtra("path", drawString);
		    			 			iPaint.setClassName("com.fvd.nimbus","com.fvd.nimbus.PaintActivity");
		    			 			startActivity(iPaint);
		    			 			overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
		    			 			finish();
		    			 		}
		    			 		catch (Exception e){}
		    			 	}
	
		    		 } 
		    		 catch(Exception e){
		    			 appSettings.appendLog("main:onActivityResult  "+e.getMessage()); 
		    	  }
		      } 
	      }
	      else
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
    			user_mail=data.getStringExtra("userMail");
    			userPass=data.getStringExtra("userPass");
    				//sendRequest("user:auth", String.format("\"email\":\"%s\",\"password\":\"%s\"",userMail,userPass));
    			if(resultCode==RESULT_OK)	
    				sendRequest("user:auth", String.format("\"email\":\"%s\",\"password\":\"%s\"",user_mail,userPass));
    			else serverHelper.getInstance().sendOldRequest("user_register", String.format("{\"action\": \"user_register\",\"email\":\"%s\",\"password\":\"%s\",\"_client_software\": \"ff_addon\"}",user_mail,userPass), "");
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
    			DataExchange cl = (DataExchange) data.getExtras().getSerializable("content");
    			if(appSettings.sessionId.length() == 0 || userPass.length()==0) showSettings();
    			else {
    				if(prefs.getBoolean("check_fast", false)){
    					sendNote(wv.getTitle(), cl.getContent(), parent, tag);
    					clipData.setContent("");
    				}
    				else {
    				serverHelper.getInstance().setCallback(this,this);
    				if(appSettings.sessionId.length()>0) {
    					serverHelper.getInstance().sendRequest("notes:getFolders", "","");
    					}
    				}
    			}
    		} 
    		wv.endSelectionMode();
    	}else if (requestCode==22){
    		if(appSettings.sessionId!=""){
    			//session_Id=appSettings.sessionId;
    			userPass=appSettings.userPass;
    			if(lastAction!="") {
    				serverHelper.getInstance().sendRequest(lastAction, "","");
    			} else
    			if(clipData!=null && clipData.getContent().length()>0){
    				if(prefs.getBoolean("check_fast", false)){
    					sendNote(clipData.getTitle(), clipData.getContent(), parent, tag);
    					clipData.setContent("");
    				}
    				else {
    				serverHelper.getInstance().setCallback(this,this);
    				if(appSettings.sessionId.length()>0) {
    					serverHelper.getInstance().sendRequest("notes:getFolders", "","");
    					}
    				}
            	}
    		}
    	}
    	else if(requestCode==DIALOG_FOLDERS){
    		
    		if (resultCode==RESULT_OK && data!=null){
    			try{ 
    				DataExchange xdata=(DataExchange)data.getExtras().getSerializable("xdata");
					parent = xdata.getId();
					tag = xdata.getTags();
					String xtitle = xdata.getTitle();
					clipData.setTitle(xtitle);
					//clipData.setTags(tag);
					if(appSettings.sessionId.length() == 0 || userPass.length()==0) showSettings();
					else {
						sendNote(xtitle, clipData.getContent(), parent, tag);
						clipData.setContent("");
					}
    			}
    			catch (Exception e){
    				BugReporter.Send("BrowseAct", e.getMessage());
    			}
    		}
    		
    	}else if(requestCode==SHOW_SETTINGS){
      	  switch (resultCode) {
      	  case RESULT_FIRST_USER+1:
        		Intent i = new Intent(getApplicationContext(), PrefsActivity.class);
            	startActivity(i);
            	
            	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
        		break;
        	case RESULT_FIRST_USER+2:
        		if(appSettings.sessionId.length() == 0) showLogin();
      		else {
      			appSettings.sessionId="";
      			//session_Id="";
      			//serverHelper.getInstance().setSessionId(appSettings.sessionId);
      			/*Editor e = prefs.edit();
      			e.putString("userMail", userMail);
          	    e.putString("userPass", "");
          	    e.putString("sessionId", appSettings.sessionId);
          	    e.commit();*/
          	    appSettings.storeUserData(this, appSettings.userMail, "", "", "");
      			showLogin();
      			}
        		break;	
        	case RESULT_FIRST_USER+3:
        		try{
                	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getApplicationInfo().packageName)));
                }
                catch(Exception e){
                }
        	case RESULT_FIRST_USER+4:
        		Uri uri = Uri.parse("http://help.everhelper.me/customer/portal/articles/1376820-nimbus-clipper-for-android---quick-guide");
        		Intent it = new Intent(Intent.ACTION_VIEW, uri);
        		startActivity(it);
        		//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        		overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
        		break;
        	case RESULT_FIRST_USER+5:
        		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
	            alertDialogBuilder.setMessage(getScriptContent("license.txt"))
	            .setCancelable(false)
	            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int id) {
	                    // no alert dialog shown
	                    //alertDialogShown = null;
	                    // canceled
	                    setResult(RESULT_CANCELED);
	                    // and finish
	                    //finish();
	                }
	            });
	            // create alert dialog
	            final AlertDialog alertDialog = alertDialogBuilder.create();
	            alertDialog.setTitle(getString(R.string.license_title));
	            
	            // and show
	            //alertDialogShown = alertDialog;
	            try {
	                alertDialog.show();
	            } catch (final java.lang.Exception e) {
	                // nothing to do
	            } catch (final java.lang.Error e) {
	                // nothing to do
	            }
        		break;
			default:
				break;
			}
        }
    }
	
	private void showLogin()
    {
    	Intent i = new Intent(getApplicationContext(), loginActivity.class);
    	i.putExtra("userMail",appSettings.userMail==null?"":appSettings.userMail);
    	startActivity(i);
    	//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
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
					
            		if (appSettings.sessionId.length() == 0) {
            			clipData.setContent(html);
            			clipData.setTitle(wv.getTitle());
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
						//appSettings.appendLog(html);
						clipData.setContent(html);
						clipData.setTitle(wv.getTitle());
						Intent i = new Intent(getApplicationContext(), previewActivity.class);
            	    	i.putExtra("url", wv.getUrl());
            	    	i.putExtra("title", wv.getTitle());
            	    	i.putExtra("content", clipData);
            	    	startActivityForResult(i,5);
            	    	//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
            	    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
	            	}
				}
			else{
				if(plainText.equals("action:selectionChanged")){
					
					String selHtml = html==null?"":html;
					Message msg = new Message();
					msg.arg1=selHtml.length()>0?1:0;
					msg.what = 0;
					clipData.setContent(html);
					clipData.setTitle(wv.getTitle());
					handler.sendMessage(msg);
				}
			}
		}
		catch (Exception e) {
			BugReporter.Send("onSelectionChanged", e.getMessage());
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
		try{
		ImageButton ib =  (ImageButton)findViewById(R.id.bSaveFullPage);
		ib.setSelected(false);
		findViewById(R.id.bZoomStack).setVisibility(View.GONE);
		findViewById(R.id.bToggleMenu).setVisibility(View.VISIBLE);
		}
		catch (Exception e){
			BugReporter.Send("onEndSelection", e.getMessage());
		}
		

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
		appSettings.appendLog(String.format("canBrowse: %s\r\n", findViewById(R.id.bSaveFullPage).isSelected()?"false":"true"));
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
	QuickReturnViewType mQuickReturnViewType=QuickReturnViewType.FOOTER;
	public void onViewCreated() {

        int headerHeight = getResources().getDimensionPixelSize(R.dimen.top_bar_height);
        int headerTranslation = -(headerHeight);

        int footerTranslation = headerHeight*3;//findViewById(R.id.bToggleMenu).getMeasuredHeight();

        mQuickReturnHeaderTextView=findViewById(R.id.lBrowser);
        mQuickReturnFooterTextView=findViewById(R.id.floatFrame);
        floatMenu=(FloatingActionsMenu) findViewById(R.id.bToggleMenu);
        QuickReturnWebViewOnScrollChangedListener scrollListener;

        switch (mQuickReturnViewType) {
            case HEADER:
                mQuickReturnHeaderTextView.setVisibility(View.VISIBLE);
                scrollListener = new QuickReturnWebViewOnScrollChangedListener.Builder(QuickReturnViewType.HEADER)
                        .header(mQuickReturnHeaderTextView)
                        .minHeaderTranslation(headerTranslation)
                        .build();
                wv.setOnScrollChangedListener(scrollListener);
                break;
            case FOOTER:
                mQuickReturnFooterTextView.setVisibility(View.VISIBLE);
                scrollListener = new QuickReturnWebViewOnScrollChangedListener.Builder(QuickReturnViewType.FOOTER)
                        .footer(mQuickReturnFooterTextView)
                        .minFooterTranslation(footerTranslation)
                        .build();
                wv.setOnScrollChangedListener(scrollListener);
                break;
            case BOTH:
                mQuickReturnHeaderTextView.setVisibility(View.VISIBLE);
                mQuickReturnFooterTextView.setVisibility(View.VISIBLE);
                scrollListener = new QuickReturnWebViewOnScrollChangedListener.Builder(QuickReturnViewType.BOTH)
                        .header(mQuickReturnHeaderTextView)
                        .minHeaderTranslation(headerTranslation)
                        .footer(mQuickReturnFooterTextView)
                        .minFooterTranslation(footerTranslation)
                        .build();
                wv.setOnScrollChangedListener(scrollListener);
                break;
        }

        wv.setOverScrollEnabled(false);
    }
	
	public void onGoBackClick (View v) {
		if (wv.canGoBack()){
  		  wv.goBack();
  	  	} else v.setVisibility(View.GONE);
	}



	@Override
	public void onTap() {
		// TODO Auto-generated method stub
		if(floatMenu.isExpanded()) floatMenu.collapse();
	}

	
	public void getPicture() {
    	try{
			Intent fileChooserIntent = new Intent();
			fileChooserIntent.addCategory(Intent.CATEGORY_OPENABLE);
			fileChooserIntent.setType("image/*");
			fileChooserIntent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(Intent.createChooser(fileChooserIntent, "Select Picture"), TAKE_PICTURE);
		}
		catch (Exception e) {
			appSettings.appendLog("main:onClick  "+e.getMessage());
		}
	}
	
	public void getPhoto(){
    	try{
    		Intent intent = new Intent(getApplicationContext(), com.fvd.cropper.ScannerActivity.class);
    		intent.putExtra("fname", appSettings.SavingPath+"temp.jpg"/*String.valueOf(System.currentTimeMillis())+"-tmp.jpg"*/);
    		//intent.putExtra("mode", prefs.getInt("scanMode", 1));
    		startActivityForResult(intent, TAKE_PHOTO);
    		overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    	}
    	catch (Exception e){
    		appSettings.appendLog("main:getPhoto  "+e.getMessage());
    	}
    	
    }
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		drawer.closeDrawer(GravityCompat.START);
		Intent ip=new Intent();
		switch (arg2) {
		case 0:
    		/*ip.putExtra("act", "photo");
    		ip.setClassName("com.fvd.nimbus","com.fvd.nimbus.PaintActivity");
    		startActivity(ip);*/
			getPhoto();
			break;
		case 1:
			/*ip.putExtra("act", "picture");
    		ip.setClassName("com.fvd.nimbus","com.fvd.nimbus.PaintActivity");
    		startActivity(ip);*/
			getPicture();
			break;
		case 2:
    		ip.setClassName("com.fvd.nimbus","com.fvd.nimbus.ChoosePDFActivity");
    		startActivity(ip);
    		overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    		finish();
			break;

		default:
			break;
		}
		
		
	}
	
	/*void toggleTools(){
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
	        },50);
			
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
	}*/
}
