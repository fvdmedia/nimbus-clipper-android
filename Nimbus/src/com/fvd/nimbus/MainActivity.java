package com.fvd.nimbus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.json.JSONObject;

import android.R.string;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
//import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;


import com.fvd.utils.AppRate;
import com.fvd.utils.AsyncTaskCompleteListener;
import com.fvd.utils.helper;
import com.fvd.utils.prefsEditListener;
import com.fvd.utils.appSettings;
import com.fvd.utils.serverHelper;

public class MainActivity extends Activity implements AsyncTaskCompleteListener<String, String>{
	private static final int TAKE_PHOTO = 1;
	private static final int TAKE_PICTURE = 2;
	private static final int SIGN_IN = 3;
	private static final int SHOW_SETTINGS=7;
	private String photoFileName = "";
	Uri outputFileUri=null;
	private String userMail = "";
	private String userPass ="";
	
	View settingsLayout;
	private SharedPreferences prefs;
	
	
	Cursor myCursor;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	try{
    		
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try{
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        catch (Exception e){
        	e.printStackTrace();
        }
        File ex=getExternalCacheDir();
        if (ex==null) ex=getCacheDir();
        if (ex==null) ex=Environment.getExternalStorageDirectory();
        if(ex!=null) appSettings.cacheDir=ex.getPath();
        
        setContentView(R.layout.screen_start);
        //overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
        overridePendingTransition(R.anim.activity_open_translate,R.anim.activity_close_translate);
        
        if (!isTaskRoot()) {
        
        	Intent intent = getIntent();
        	String action = intent.getAction();
        	if (action != null && action.equals(Intent.ACTION_MAIN)) {
            	finish();
            	return;
        	}
   		}	
        
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        appSettings.getInstance().setIsTablet(isTablet());
        serverHelper.getInstance().setCallback(this,this);
        /*findViewById(R.id.bTakePhoto).setOnClickListener(this);
        findViewById(R.id.bFromGallery).setOnClickListener(this);
        findViewById(R.id.bWebClipper).setOnClickListener(this);
        findViewById(R.id.bPdfAnnotate).setOnClickListener(this);
        findViewById(R.id.ibSettings).setOnClickListener(this);*/
        
        /*userMail = prefs.getString("userMail", "");
        userPass = prefs.getString("userPass", "");
        String sessionId = prefs.getString("sessionId", "");
        appSettings.sessionId=sessionId;
 	    appSettings.userMail=userMail;
 	    appSettings.userPass=userPass;*/
        appSettings.init(this);
        userMail = appSettings.userMail;
        userPass = appSettings.userPass;
 	    
 	    
        if(isFirst()){
        	createShortcut(this);
        	showHelp();
        }
        else
        	if(!prefs.getBoolean("offline", false)){
        		if ((userMail.length()== 0 || userPass.length() == 0)) showLogin();
        		else{
        			if (appSettings.sessionId.length() == 0 /*|| true*/){ 
        				//serverHelper.getInstance().setSessionId(sessionId);
        				serverHelper.getInstance().sendRequest("user:auth", String.format("\"email\":\"%s\",\"password\":\"%s\"",userMail,userPass),"");
        			}
        			else {
        				//serverHelper.getInstance().setSessionId(sessionId);
        				serverHelper.getInstance().sendQuietRequest("user:authstate", "","");
        			}
        			AppRate.start(this);
        		}
        	} else AppRate.start(this);

    	}
        catch (Exception e) {
        	appSettings.appendLog("main:onCreate  "+e.getMessage());
		}
        
    }
    
    boolean isFirst(){
		boolean res = false;
		long ms=prefs.getLong("ms", 0);
		if(ms==0){
			res = true;
			ms=System.currentTimeMillis();
			Editor e=prefs.edit();
			e.putLong("ms", ms);
			e.commit();
		}
		return res;
	}
    
    @Override
	  public void onResume() {
	    super.onResume();
	    //overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
	    overridePendingTransition(R.anim.activity_open_scale,R.anim.activity_close_translate);
	    if(prefs == null) prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    appSettings.getInstance().setIsTablet(isTablet());
        serverHelper.getInstance().setCallback(this,this);
        appSettings.sessionId=prefs.getString("sessionId", "");
	  }
    
   
    
    
    public static void createShortcut(Context context)
    {
    	
    	Intent shortcutIntent = new Intent(context, MainActivity.class);
        shortcutIntent.setAction(Intent.ACTION_MAIN);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Nimbus Clipper");
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,Intent.ShortcutIconResource.fromContext(context, R.drawable.app_icon));
        addIntent.putExtra("duplicate", false);
        addIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
        context.sendBroadcast(addIntent);
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        context.sendBroadcast(addIntent);
    }
    
    public static void deleteShortcut(Context context)
    {
        Intent shortcut = new Intent("com.android.launcher.action.UNINSTALL_SHORTCUT");
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, "Nimbus Clipper");
        ComponentName comp = new ComponentName("com.fvd.nimbus", "com.fvd.nimbus.MainActivity");
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent(Intent.ACTION_MAIN).setComponent(comp));
        context.sendBroadcast(shortcut);
    }
    
    boolean canRate(){
		long ms=prefs.getLong("ms", 0);
		return ((System.currentTimeMillis() - ms)/(24 * 60 * 60 * 1000))>4;
	}
    
    /*public void showSettingsPopup(View view) {

    	if (serverHelper.getInstance().getSession().length() == 0) ((Button)settingsLayout.findViewById(R.id.bssLogin)).setText(getResources().getString(R.string.login));
    	else ((Button)settingsLayout.findViewById(R.id.bssLogin)).setText(getResources().getString(R.string.logout));
    	if (canRate()) ((Button)settingsLayout.findViewById(R.id.bssRateUs)).setVisibility(View.VISIBLE);
    	else ((Button)settingsLayout.findViewById(R.id.bssRateUs)).setVisibility(View.GONE);
    }*/
    
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.screen_start);
    }
    
    boolean isTablet(){
    	/*Display d = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    	Point size = new Point();
    	size.x = d.getWidth();
    	size.y = d.getHeight();*/
    	//boolean b = size.x>1280 || size.y>1280;//size.x>480 && size.y>800||size.x>800 && size.y>480;
    	boolean b=getResources().getInteger(R.integer.is_tablet)!=0;
    	//boolean b=(this.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)>= Configuration.SCREENLAYOUT_SIZE_LARGE;
     	if (!b) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		return b;
    }
    
    public void onButtonClick(View v)
    {
    	switch(v.getId()){
    	case R.id.bTakePhoto:
    		showProgress(true);
    		getPhoto();
    		break;
    	case R.id.bFromGallery:
    		try{
    			showProgress(true);
    			Intent fileChooserIntent = new Intent();
    			fileChooserIntent.addCategory(Intent.CATEGORY_OPENABLE);
    			fileChooserIntent.setType("image/*");
    			fileChooserIntent.setAction(Intent.ACTION_GET_CONTENT);
    			startActivityForResult(Intent.createChooser(fileChooserIntent, "Select Picture"), TAKE_PICTURE);
    		}
    		catch (Exception e) {
				appSettings.appendLog("main:onClick  "+e.getMessage());
				showProgress(false);
			}
    		break;
    	case R.id.bWebClipper:
    		Intent iBrowse = new Intent();
    		iBrowse.setClassName("com.fvd.nimbus","com.fvd.nimbus.BrowseActivity");
    		startActivity(iBrowse);
    		//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    		overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    		break;
    	case R.id.bPdfAnnotate:
    		Intent ip = new Intent();
    		ip.setClassName("com.fvd.nimbus","com.fvd.nimbus.ChoosePDFActivity");
    		startActivity(ip);
    		//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    		overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    		break;
    	case R.id.ibSettings:
    		Intent inten = new Intent(getApplicationContext(), SettingsActivity.class);
        	startActivityForResult(inten,SHOW_SETTINGS);
        	//overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
        	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    		break;
    	/*case R.id.bssSettings:
    		Intent i = new Intent(getApplicationContext(), PrefsActivity.class);
        	startActivity(i);
        	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    		break;
    	case R.id.bssRegister:
    		Intent intent = new Intent(getApplicationContext(), RegisterDlg.class);
    		intent.putExtra("userMail", userMail==null?"":userMail);
	    	startActivityForResult(intent, 5);
	    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    		break;	
    	case R.id.bssHelp:
    		Uri uri = Uri.parse("http://help.everhelper.me/customer/portal/articles/1376820-nimbus-clipper-for-android---quick-guide");
    		Intent it = new Intent(Intent.ACTION_VIEW, uri);
    		startActivity(it);
    		overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    		break;
    	case R.id.bssLogin:
    		if(serverHelper.getInstance().getSession().length() == 0) showLogin();
    		else {
    			String sessionId="";
    			serverHelper.getInstance().setSessionId(sessionId);
    			appSettings.storeUserData(this, userMail, "", "");
    			Editor e = prefs.edit();
    			e.putString("userMail", userMail);
        	    e.putString("userPass", "");
        	    e.putString("sessionId", sessionId);
        	    e.commit();
    			showLogin();
    			}
    		break;
    	case R.id.bssRateUs:
    		try{
            	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getApplicationInfo().packageName)));
            }
            catch(Exception e){
            }
    		break;*/
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        return true;
    }
    
    public void getPhoto(){
    	try{
    		
    		/*photoFileName = String.valueOf(System.currentTimeMillis())+"-tmp.jpg";	
    		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    		File file = new File(appSettings.getInstance().SavingPath, photoFileName);
    		outputFileUri = Uri.fromFile(file);
    		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
    		startActivityForResult(intent, TAKE_PHOTO);
    		overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);*/
    		
    		photoFileName = "temp.jpg";//String.valueOf(System.currentTimeMillis())+"-tmp.jpg";	
    		Intent intent = new Intent(getApplicationContext(), com.fvd.cropper.ScannerActivity.class);
    		intent.putExtra("fname", appSettings.getInstance().SavingPath+"temp.jpg"/*String.valueOf(System.currentTimeMillis())+"-tmp.jpg"*/);
    		//intent.putExtra("mode", prefs.getInt("scanMode", 1));
    		startActivityForResult(intent, TAKE_PHOTO);
    		overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    	}
    	catch (Exception e){
    		appSettings.appendLog("main:getPhoto  "+e.getMessage());
    	}
    }
    
    void showProgress(boolean b){
    	findViewById(R.id.lWait).setVisibility(b?View.VISIBLE:View.INVISIBLE);
		findViewById(R.id.lMenu).setVisibility(b?View.INVISIBLE:View.VISIBLE);
		findViewById(R.id.llauncher).setVisibility(b?View.INVISIBLE:View.VISIBLE);
    }
    
    @SuppressLint("NewApi")
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    			 		}
    			 		catch (Exception e)
    			 		{
    			 			
    			 		}
    			 	}
				}
    		}
    		/*else {
    			if(outputFileUri!=null) photoFileName = outputFileUri.getPath();
    			else photoFileName = getImagePath();
    		}
    		
    		
    		if(appSettings.isFileExists(photoFileName)){
    			if(isCropResult){
    				Intent iPaint = new Intent();
    				iPaint.putExtra("temp", true);
    				iPaint.putExtra("path", "file://"+photoFileName);
    				//iPaint.putExtra("isDocument", true);
    				if(data.hasExtra("mode")) iPaint.putExtra("mode", data.getIntExtra("mode", 0));
    				iPaint.setClassName("com.fvd.nimbus","com.fvd.nimbus.PaintActivity");
    				startActivity(iPaint);
    			} else {
    				Intent intent = new Intent(getApplicationContext(),com.fvd.cropper.CropImageActivity.class);
            		intent.putExtra(com.fvd.cropper.CropImageActivity.IMAGE_PATH, photoFileName);
                    intent.putExtra(com.fvd.cropper.CropImageActivity.SCALE, true);

                    intent.putExtra(com.fvd.cropper.CropImageActivity.ASPECT_X, 0);
                    intent.putExtra(com.fvd.cropper.CropImageActivity.ASPECT_Y, 0);
            		startActivityForResult(intent, TAKE_PHOTO);
    			}
    			
    		}*/

    		showProgress(false);
    	}
    	catch (Exception e) {
    		appSettings.appendLog("main:onActivityResult: exception -  "+e.getMessage());
    		showProgress(false);
		}
    	}
    	else showProgress(false);
      }
      else if (requestCode == TAKE_PICTURE){
    	  if(resultCode == -1 && data!=null){
    		  boolean temp = false;
    		  
    		 try { 
    			 	Uri resultUri = data.getData();
    			 	String drawString = resultUri.getPath();
    			 	if(drawString.startsWith("/storage"))  drawString ="file://"+drawString;
    			 	else drawString = resultUri.toString();
    			 	/*String galleryString = !drawString.startsWith("/storage")?getGalleryPath(resultUri):resultUri.getPath();
    			 	if (galleryString != null && galleryString.length()>0)
    			 	{
    			 		drawString = galleryString;
    			 	}
    			 	else {
    			 		try{
    			 			InputStream input = getApplicationContext().getContentResolver().openInputStream(resultUri);
    			 			drawString = helper.saveTmp(input);
    			 			temp=true;
    			 			
    			 		}
    			 		catch (Exception e) {
    			 			drawString = "";
    			 			showProgress(false);
    			 		}
    			 	}*/
    			 	
    			 	if (drawString.length() > 0 && drawString.indexOf("/exposed_content/")==-1)
    			 	{
    			 		try{
    			 			Intent iPaint = new Intent();
    			 			iPaint.putExtra("temp", temp);
    			 			iPaint.putExtra("path", drawString);
    			 			iPaint.setClassName("com.fvd.nimbus","com.fvd.nimbus.PaintActivity");
    			 			startActivity(iPaint);
    			 		}
    			 		catch (Exception e)
    			 		{
    			 			
    			 		}
    			 	}
    			 	showProgress(false);

    		 } 
    		 catch(Exception e){
    			 appSettings.appendLog("main:onActivityResult  "+e.getMessage()); 
    			 showProgress(false);
    	  }
      } else showProgress(false);
      }
      else
      if (requestCode == 5) {
	    	if (resultCode == RESULT_OK){
				userMail=data.getStringExtra("userMail");
				userPass=data.getStringExtra("userPass");
				serverHelper.getInstance().sendOldRequest("user_register", String.format("{\"action\": \"user_register\",\"email\":\"%s\",\"password\":\"%s\",\"_client_software\": \"ff_addon\"}",userMail,userPass), "");
	    	}
	      }
      else
          if (requestCode == 6) {
    				showLogin();
    	      }
          else if(requestCode==SHOW_SETTINGS){
        	  switch (resultCode) {
        	  case RESULT_FIRST_USER+1:
          		Intent i = new Intent(getApplicationContext(), PrefsActivity.class);
              	startActivity(i);
              	//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
              	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
          		break;
          	case RESULT_FIRST_USER+2:
          		if(appSettings.sessionId.length() == 0) showLogin();
        		else {
        			if(true || appSettings.service==""){
	        			appSettings.sessionId="";
	        			Editor e = prefs.edit();
	        			e.putString("userMail", userMail);
	            	    e.putString("userPass", "");
	            	    e.putString("sessionId", appSettings.sessionId);
	            	    e.commit();
	        			showLogin();
        			} else {
	        				i = new Intent(getApplicationContext(), loginWithActivity.class);
	        		    	i.putExtra("logout", "true");
	        		    	i.putExtra("service", appSettings.service);
	        		    	startActivity(i);
	        		    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
        				}
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
    
    public String getGalleryPath(Uri uri) {
    	String picturePath=null;
    	String[] filePathColumn = { MediaStore.Images.Media.DATA };
    	try{
	    	Cursor cursor = getContentResolver().query(uri,filePathColumn, null, null,   null);
	    	cursor.moveToFirst();
	    	int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
	    	picturePath = cursor.getString(columnIndex);
	    	cursor.close();
    	} catch (Exception e){}
    	return picturePath;
    }
    
    String getImagePath(){
    
    	String[] projection = {
    			 MediaStore.Images.Thumbnails._ID,  // The columns we want
    			 MediaStore.Images.Thumbnails.IMAGE_ID,
    			 MediaStore.Images.Thumbnails.KIND,
    			 MediaStore.Images.Thumbnails.DATA};
    			 String selection = MediaStore.Images.Thumbnails.KIND + "="  + // Select only mini's
    			 MediaStore.Images.Thumbnails.MINI_KIND;
    			 
    			 String sort = MediaStore.Images.Thumbnails._ID + " DESC";
    			 
    			//At the moment, this is a bit of a hack, as I'm returning ALL images, and just taking the latest one. There is a better way to narrow this down I think with a WHERE clause which is currently the selection variable
    			Cursor myCursor = this.managedQuery(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, selection, null, sort);
    			 
    			long imageId = 0l;
    			long thumbnailImageId = 0l;
    			String thumbnailPath = "";
    			 
    			try{
    			 myCursor.moveToFirst();
    			imageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
    			thumbnailImageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID));
    			thumbnailPath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
    			}
    			finally{myCursor.close();}
    			 
    			 //Create new Cursor to obtain the file Path for the large image
    			 
    			 String[] largeFileProjection = {
    			 MediaStore.Images.ImageColumns._ID,
    			 MediaStore.Images.ImageColumns.DATA
    			 };
    			 
    			 String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
    			 myCursor = this.managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, largeFileProjection, null, null, largeFileSort);
    			 String largeImagePath = "";
    			 
    			try{
    			 myCursor.moveToFirst();
    			 
    			largeImagePath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
    			}
    			finally{myCursor.close();}
    			 // These are the two URI's you'll be interested in. They give you a handle to the actual images
    			 Uri uriLargeImage = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(imageId));
    			 Uri uriThumbnailImage = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, String.valueOf(thumbnailImageId));
    			 
    			 if (largeImagePath.length()>0)
    				 return largeImagePath;
    			 else if(uriLargeImage!=null) return uriLargeImage.getPath();
    			 else if(uriThumbnailImage!=null) return uriThumbnailImage.getPath();
    			 else return "";
    			 
    }
    
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (outputFileUri != null) {
            outState.putString("cameraImageUri", outputFileUri.getPath());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("cameraImageUri")) {
        	outputFileUri = Uri.parse(savedInstanceState.getString("cameraImageUri"));
        }
    }
    
    private void showLogin()
    {
    	Intent i = new Intent(getApplicationContext(), loginActivity.class);
    	i.putExtra("userMail", appSettings.userMail==null?"":appSettings.userMail);
    	startActivity(i);
    	//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    }
    
    private void showHelp()
    {
    	Intent i = new Intent(getApplicationContext(), helpActivity.class);
    	startActivity(i);
    	//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    }
    
    @Override
    public void onTaskComplete(String result, String action)
    {
        	if(result.length()==0) return;
    		try{
            	JSONObject root = new JSONObject(result);
            	int error = root.getInt("errorCode");
            	//error=-6;
            	
            	if (error == 0){
            		if (action.equalsIgnoreCase("user:auth")){
            			final String sessionId = root.getJSONObject("body").getString("sessionid");
            					Editor e = prefs.edit();
            					e.putString("userMail", userMail);
                	    		e.putString("userPass", userPass);
                	    		e.putString("sessionId", sessionId);
                	    		e.commit();
                	    		
                	    		appSettings.sessionId=sessionId;
                         	    appSettings.userMail=userMail;
                         	    appSettings.userPass=userPass;
            				
            			Toast.makeText(getApplicationContext(), "user authorized", Toast.LENGTH_LONG).show();
            		}
            		else if(action.equalsIgnoreCase("user:authstate")){
            			boolean auth = root.getJSONObject("body").getBoolean("authorized");
            			Toast.makeText(getApplicationContext(), "user " + (auth?"authorized":"not authorized"), Toast.LENGTH_LONG).show();
            			if(!auth){
            				appSettings.sessionId="";
                			Editor e = prefs.edit();
                			e.putString("userMail", userMail);
                    	    e.putString("userPass", "");
                    	    e.putString("sessionId", appSettings.sessionId);
                    	    e.commit();
                			showLogin();
            			}
            		}
            		else if("user_register".equals(action)){
            			sendRequest("user:auth", String.format("\"email\":\"%s\",\"password\":\"%s\"",userMail,userPass));
            		}
            	} else 
            		{
            			if(error==-6){
            				showLogin();
            				/*if(userMail.length()>0 && userPass.length()>0){
            					sendRequest("user:auth", String.format("\"email\":\"%s\",\"password\":\"%s\"",userMail,userPass));
            				} else showLogin();*/
            			} else 
            			Toast.makeText(getApplicationContext(), String.format("Error: %s",serverHelper.errorMsg(error)), Toast.LENGTH_LONG).show();
            		}
            }
            catch (Exception Ex){
            }
    }
	
	private void sendRequest(String action, String data){
		serverHelper.getInstance().sendRequest(action, data,"");
	}
}
