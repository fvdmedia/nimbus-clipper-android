package com.fvd.nimbus;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.json.JSONObject;

import yuku.ambilwarna.AmbilWarnaDialog;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.IpPrefix;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.Media;
import android.provider.MediaStore.Images;
//import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.fvd.classes.BugReporter;
import com.fvd.classes.CircleButton;
import com.fvd.classes.DataExchange;
import com.fvd.classes.DrawerMenuAdapter;
import com.fvd.cropper.R.string;
import com.fvd.paint.DrawView;
import com.fvd.utils.AsyncTaskCompleteListener;
import com.fvd.utils.FileProviderCompat;
import com.fvd.utils.appSettings;
import com.fvd.utils.helper;
import com.fvd.utils.prefsEditListener;
import com.fvd.utils.serverHelper;
import com.fvd.utils.shapeSelectionListener;
import android.view.inputmethod.InputMethodManager;



public class PaintActivity extends Activity implements OnClickListener, AsyncTaskCompleteListener<String, String>,OnItemClickListener{
	private static final String TAG = "FingerPaint";
	private static final int TAKE_PHOTO = 1;
	private static final int TAKE_PICTURE = 2;
	private static final int GET_FOLDERS = 7;
	private static final int SIGN_IN = 8;
	final int SHOW_SETTINGS=11;
	protected static final String CONTENT_PHOTOS_URI_PREFIX = "content://com.google.android.apps.photos.contentprovider";
	final String pColor="pColor";
	DrawView drawView;
	private int dWidth=0; 
	private int fWidth =0;
	private int dColor =0;
	private String userMail = "";
	private String userPass ="";
	//private String sessionId = "";
	private SharedPreferences prefs;
	//private boolean saved = false;
	private int saveFormat=0;
 	private String domain="";
	private boolean exitOnComplete = false;
	private String storePath=""; 
	CircleButton paletteButton, paletteButton_land;
	OnSeekBarChangeListener fontSizeListener;
	OnSeekBarChangeListener lineWidthListener;
	DrawerLayout drawer;
	Context ctx;
	boolean canChange=true;
	//boolean isDocument=false;
	String preName="Picture";
	String defTag="androidclipper";
	int ccolor;
	String storeFileName=null;
	int[] buttons={R.id.bEditPage,R.id.bToolShape,R.id.bToolText, R.id.bToolColor,R.id.bToolCrop, R.id.bErase};
	
	//private String[] mPlanetTitles;
	
    /** Called when the activity is first created. */
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
        ctx = this;
        appSettings.init(ctx);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        dWidth= prefs.getInt("dWidth", 2); 
    	fWidth =prefs.getInt("fWidth", 1);
    	dColor =prefs.getInt(pColor, Color.RED);
    	saveFormat = Integer.parseInt(prefs.getString("saveFormat", "1"));
        serverHelper.getInstance().setCallback(this,this);
        serverHelper.getInstance().setMode(saveFormat);
        setContentView(R.layout.screen_edit);
        drawer=(DrawerLayout)findViewById(R.id.root);
    	findViewById(R.id.bDraw1).setOnClickListener(this);
    	findViewById(R.id.bDraw2).setOnClickListener(this);
    	findViewById(R.id.bDraw3).setOnClickListener(this);
    	findViewById(R.id.bDraw4).setOnClickListener(this);
    	findViewById(R.id.bDraw5).setOnClickListener(this);
    	findViewById(R.id.bDraw6).setOnClickListener(this);
    	findViewById(R.id.bDraw8).setOnClickListener(this);
    	findViewById(R.id.bColor1).setOnClickListener(this);
    	findViewById(R.id.bColor2).setOnClickListener(this);
    	findViewById(R.id.bColor3).setOnClickListener(this);
    	findViewById(R.id.bColor4).setOnClickListener(this);
    	findViewById(R.id.bColor5).setOnClickListener(this);
    	paletteButton=(CircleButton)findViewById(R.id.bToolColor);
    	paletteButton.setOnClickListener(this);
    	
    	paletteButton_land=(CircleButton)findViewById(R.id.bToolColor_land);
    	//paletteButton_land.setOnClickListener(this);
    	
    	((SeekBar)findViewById(R.id.seekBarLine)).setProgress(dWidth*10);
		((SeekBar)findViewById(R.id.seekBarType)).setProgress(fWidth*10);
		
		(( TextView ) findViewById(R.id.tvTextType)).setText(String.format("%d",40+fWidth*20));

		//setSelectedFoot(1);
		if(appSettings.cacheDir==""){
			File ex=getExternalCacheDir();
	        if (ex==null) ex=getCacheDir();
	        if (ex==null) ex=Environment.getExternalStorageDirectory();
	        if(ex!=null) appSettings.cacheDir=ex.getPath();
		}
    	
    	findViewById(R.id.bUndo).setOnClickListener(this);
    	findViewById(R.id.btnBack).setOnClickListener(this);
    	findViewById(R.id.bClearAll).setOnClickListener(this);
    	findViewById(R.id.bTurnLeft).setOnClickListener(this);
    	findViewById(R.id.bTurnRight).setOnClickListener(this);
    	findViewById(R.id.bDone).setOnClickListener(this);
    	findViewById(R.id.bApplyText).setOnClickListener(this);
    	
		((ImageButton)findViewById(R.id.bStroke)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				v.setSelected(!v.isSelected());
    		}
		});	
    	
		lineWidthListener=new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				if(true || fromUser){
					/*dWidth = (progress/10);
	    			drawView.setWidth((dWidth+1)*5);*/
					dWidth = progress;
	    			drawView.setWidth(dWidth);
	    			Editor e = prefs.edit();
	    			e.putInt("dWidth",dWidth);
	    			e.commit();
				}
				
			}
		};
		
		((SeekBar)findViewById(R.id.seekBarLine)).setOnSeekBarChangeListener(lineWidthListener);
		((SeekBar)findViewById(R.id.ls_seekBarLine)).setOnSeekBarChangeListener(lineWidthListener);
		
		fontSizeListener=new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				
				if(fromUser){
    				fWidth = progress/10;
    				int c=40+fWidth*20;
    				drawView.setFontSize(c);
    				Editor e = prefs.edit();
    				e.putInt("fWidth",fWidth);
        			e.commit();
    				try{
    					(( TextView ) findViewById(R.id.tvTextType)).setText(String.format("%d",c));
    					(( TextView ) findViewById(R.id.ls_tvTextType)).setText(String.format("%d",c));
    				}
    				catch (Exception ex){
    					
    				}
    				
    			}
			}
		};
    	
    	
		((SeekBar)findViewById(R.id.seekBarType)).setOnSeekBarChangeListener(fontSizeListener);
		((SeekBar)findViewById(R.id.ls_seekBarType)).setOnSeekBarChangeListener(fontSizeListener);
        
        drawView = (DrawView)findViewById(R.id.painter);
        drawView.setWidth((dWidth+1)*5);
		drawView.setFontSize(40+fWidth*20);
        
		
		setBarConfig(getResources().getConfiguration().orientation);
		
        findViewById(R.id.bEditPage).setOnClickListener(this);
        findViewById(R.id.bToolColor).setOnClickListener(this);
        findViewById(R.id.bErase).setOnClickListener(this);
        findViewById(R.id.bToolShape).setOnClickListener(this);
        findViewById(R.id.bToolText).setOnClickListener(this);
        findViewById(R.id.bToolCrop).setOnClickListener(this);
        findViewById(R.id.btnBack).setOnClickListener(this);
        findViewById(R.id.bDone).setOnClickListener(this);
        findViewById(R.id.btnShare).setOnClickListener(this);
        findViewById(R.id.bSave2SD).setOnClickListener(this);
        findViewById(R.id.bSave2Nimbus).setOnClickListener(this);
        
        userMail = prefs.getString("userMail", "");
        userPass = prefs.getString("userPass", "");
        appSettings.sessionId = prefs.getString("sessionId", "");
        
        //appSettings.sessionId=sessionId;
 	    appSettings.userMail=userMail;
 	    appSettings.userPass=userPass;
        
        FileProviderCompat.setContext(getApplicationContext());
 	    storePath="";
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if(intent.hasExtra("mode")){
        	int m=intent.getIntExtra("mode", 0);
        	switch (m) {
			case 0:
				preName="Photo";
				break;
			case 1:
				preName="Document";
				defTag="Document";
				break;
			case 2:
				preName="Card";
				defTag="Document";
				break;

			default:
				break;
			}
        }
        //storePath= intent.getPackage().getClass().toString();
        if((Intent.ACTION_VIEW.equals(action) || Intent.ACTION_SEND.equals(action) || "com.onebit.nimbusnote.EDIT_PHOTO".equals(action)) && type != null){
        	if (type.startsWith("image/")) {
        		if ("com.onebit.nimbusnote.EDIT_PHOTO".equals(action)) storePath=" ";
        		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        		if (imageUri==null) imageUri=intent.getData();
        		if (imageUri!=null){
        			try{
        				InputStream is =getContentResolver().openInputStream(imageUri);
        				int orient = helper.getImageRotation(PaintActivity.this, imageUri);
        				Bitmap bmp = BitmapFactory.decodeStream(is);
        				if (bmp.getWidth()!=-1 && bmp.getHeight()!=-1)
        					drawView.setBitmap(bmp,orient);
        				is.close();
        				
        			}catch (Exception e){}
        		}
            } 
        }
        else  {
        	String act = getIntent().getExtras().getString("act");
        	if("photo".equals(act)){
        		getPhoto();
        	}else if("picture".equals(act)){
        		getPicture();
        	}
        	else {
        		String filePath = getIntent().getExtras().getString("path");
            	final boolean isTemp = getIntent().getExtras().getBoolean("temp");
            	domain =  getIntent().getExtras().getString("domain");
            	if(domain==null) domain = "";
	        	if(filePath.startsWith("http")){
	        		Bitmap bmp=helper.LoadImageFromWeb(filePath);
	        		if (bmp!=null) {
	        			drawView.setBitmap(bmp,0);
	        		}
	        	}
	        	else {
	        		showProgress(true);
	        		if(filePath.startsWith("/storage"))  filePath ="file://"+filePath;
	        		final Uri u=Uri.parse(filePath);
	        		storeFileName=getGalleryName(u);
	        		/*try{
		        		InputStream input = getContentResolver().openInputStream(u);
	    			 	final int orient = helper.getImageRotation(PaintActivity.this, u);
			 			final Bitmap b=BitmapFactory.decodeStream(input);
	    			 	drawView.setVisibility(View.INVISIBLE);
			 			drawView.setBitmap(b, orient);
			 			drawView.setVisibility(View.VISIBLE);
	        		} catch (Exception e){
	        			
	        		}*/
	        		new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							InputStream input=null;
							try{
				        		input = getContentResolver().openInputStream(u);
				        		
							} catch (Exception e){
			        			
			        		}
							if(input!=null){
				    			final int orient = helper.getImageRotation(PaintActivity.this, u);
						 		final Bitmap bmp=BitmapFactory.decodeStream(input);
						 		runOnUiThread(new Runnable() {
									public void run() {
										drawView.setVisibility(View.GONE);
										if (bmp!=null){
			        						drawView.setBitmap(bmp,orient);
			        					}
										drawView.setVisibility(View.VISIBLE);
					        			showProgress(false);
									}
								});
					 		
							}
			        		
						}
					}).start();
	        		//final File file = new File(filePath);
	        		
	        		/*if (file.exists()){
	        			{
	        				showProgress(true);
	        				new Thread(new Runnable() {
								
								@Override
								public void run() {
									// TODO Auto-generated method stub
									try{
										final int orient = helper.getOrientationFromExif(filePath);
										final Bitmap bmp  = helper.decodeSampledBitmap(filePath,isTemp);
										
										runOnUiThread(new Runnable() {
											public void run() {
												drawView.setVisibility(View.GONE);
												if (bmp!=null){
					        						drawView.setBitmap(bmp,orient);
					        					}
												drawView.setVisibility(View.VISIBLE);
							        			showProgress(false);
							        			
							        			//Toast.makeText(PaintActivity.this, "loaded", Toast.LENGTH_SHORT).show();
											}
										});
									}
									catch(Exception e){
					        			appSettings.appendLog("paint.onCreate()  "+e.getMessage());
					        		}
					        			if (isTemp) file.delete();

								}
							}).start();
	        			}
	        			
	        		}*/
	        	}
        	}
        }  
          
          
        drawView.setBackgroundColor(Color.WHITE);
        drawView.requestFocus();
        drawView.setColour(dColor);
        setPaletteColor(dColor);
        
        drawView.setSelChangeListener(new shapeSelectionListener(){

        	@Override
			public void onSelectionChanged(int shSize, int fSize, int shColor) {
				setSelectedFoot(0);
				setLandToolSelected(R.id.bEditPage_land);
				//updateColorDialog(shSize!=-1?(shSize/5)-1:dWidth, fSize!=-1?(fSize-40)/20:fWidth, shColor!=0?colorToId(shColor):dColor);
				dColor=shColor;
				ccolor = shColor;
				int sw=shSize!=-1?shSize:dWidth;
				canChange=false;
				((SeekBar)findViewById(R.id.seekBarLine)).setProgress(sw);
		    	((SeekBar)findViewById(R.id.ls_seekBarLine)).setProgress(sw);
		    	setPaletteColor(dColor);
				drawView.setColour(dColor);
				canChange=true;
			}
			
			@Override
			public void onTextChanged(String text,boolean stroke) {
				
				if(findViewById(R.id.text_field).getVisibility()!=View.VISIBLE){
				hideTools();
				
				findViewById(R.id.bStroke).setSelected(stroke);
				((EditText)findViewById(R.id.etEditorText)).setText(text);
				findViewById(R.id.text_field).setVisibility(View.VISIBLE);
				findViewById(R.id.etEditorText).requestFocus();
				((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(findViewById(R.id.etEditorText), 0);
				findViewById(R.id.bToolText).postDelayed(new Runnable() {
		            @Override
		            public void run() {
		                // TODO Auto-generated method stub
		            	setSelectedFoot(2);
		            	
		            }
		        },100);
				       
				}
			}

			@Override
			public void onTouch(MotionEvent event, int act) {
				// TODO Auto-generated method stub
				if(act==MotionEvent.ACTION_DOWN){
					if(findViewById(R.id.color_menu).getVisibility()!=View.GONE)
            		{
            			findViewById(R.id.color_menu).setVisibility(View.GONE);
            			drawView.hideCrop();
            			drawView.setCanDraw(true);
                		//drawView.startEdit();
		
                		/*if (drawView.isDrawMode()) setSelectedFoot(1);
                		else setSelectedFoot(0);*/
            		}
				}
			}
        	
        });
        
        setColorButtons(dColor);
        setSelectedFoot(1);
        updateColorDialog(dWidth, fWidth, dColor);
        drawView.startDraw();
        ImageButton bs = (ImageButton)findViewById(R.id.bToolShape);
        bs.setImageResource(R.drawable.draw_tools_06);
        drawView.setShape(6);
        
        
        //mPlanetTitles = getResources().getStringArray(R.array.lmenu_paint);
        
        /*ListView listView = (ListView) findViewById(R.id.left_drawer);
        listView.setAdapter(new DrawerMenuAdapter(this,getResources().getStringArray(R.array.lmenu_paint)));
		listView.setOnItemClickListener(this);*/
    }
    
    /*public static Bitmap getCorrectlyOrientedImage(Context context, Uri photoUri) throws IOException {
        int MAX_IMAGE_DIMENSION=2000;
    	InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        is.close();

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        if (rotatedWidth > MAX_IMAGE_DIMENSION || rotatedHeight > MAX_IMAGE_DIMENSION) {
            float widthRatio = ((float) rotatedWidth) / ((float) MAX_IMAGE_DIMENSION);
            float heightRatio = ((float) rotatedHeight) / ((float) MAX_IMAGE_DIMENSION);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        is.close();

        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        return srcBitmap;
    }
    
    public static int getOrientation(Context context, Uri photoUri) {
        
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        int result = -1;
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0);
            }
            cursor.close();
        }

        return result;
    }*/
    
    public void onButtonClick(View v){
    	drawer.closeDrawer(GravityCompat.START);
		Intent ip=new Intent();
		switch (v.getId()) {
		case R.id.lbTakePhoto:
			drawView.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					getPhoto();
				}
			}, 350);
    		
			break;
		case R.id.lbFromGallery:
			drawView.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					getPicture();
				}
			}, 350);
			
			break;
		case R.id.lbPdfAnnotate:
    		ip.setClassName("com.fvd.nimbus","com.fvd.nimbus.ChoosePDFActivity");
    		startActivity(ip);
    		finish();
			break;
		case R.id.lbWebClipper:
			ip=new Intent();
			ip.setClassName("com.fvd.nimbus","com.fvd.nimbus.BrowseActivity");
    		startActivity(ip);
    		finish();
    		break;
		case R.id.libSettings:
    		Intent inten = new Intent(getApplicationContext(), SettingsActivity.class);
        	startActivityForResult(inten,SHOW_SETTINGS);
        	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    		break;
		default:
			break;
		}
		
    }
    
    private void showLogin()
    {
    	Intent i = new Intent(getApplicationContext(), loginActivity.class);
    	i.putExtra("userMail", userMail==null?"":userMail);
    	startActivity(i);
    	//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    }
    
    void setColorButtons(int c){
    	/*switch (c) {
		case 2:
			((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_blue);
			((ImageButton)findViewById(R.id.bToolColor_land)).setImageResource(R.drawable.icon_color_blue);
			((ImageButton)findViewById(R.id.bToolColor_land1)).setImageResource(R.drawable.icon_color_blue);
			setLandColorSelected(R.id.ls_bColor1);
			break;
		case 5:
			((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_red);
			((ImageButton)findViewById(R.id.bToolColor_land)).setImageResource(R.drawable.icon_color_red);
			((ImageButton)findViewById(R.id.bToolColor_land1)).setImageResource(R.drawable.icon_color_red);
			setLandColorSelected(R.id.ls_bColor2);
			break;
		case 3:
			((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_yellow);
			((ImageButton)findViewById(R.id.bToolColor_land)).setImageResource(R.drawable.icon_color_yellow);
			((ImageButton)findViewById(R.id.bToolColor_land1)).setImageResource(R.drawable.icon_color_yellow);
			setLandColorSelected(R.id.ls_bColor3);
			break;
		case 0:
			((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_white);
			((ImageButton)findViewById(R.id.bToolColor_land)).setImageResource(R.drawable.icon_color_white);
			((ImageButton)findViewById(R.id.bToolColor_land1)).setImageResource(R.drawable.icon_color_white);
			setLandColorSelected(R.id.ls_bColor4);
			break;
		case 7:
			((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_black);
			((ImageButton)findViewById(R.id.bToolColor_land)).setImageResource(R.drawable.icon_color_black);
			((ImageButton)findViewById(R.id.bToolColor_land1)).setImageResource(R.drawable.icon_color_white);
			setLandColorSelected(R.id.ls_bColor5);
			break;

		default:
			break;
		}*/
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /*if (getResources().getInteger(R.integer.is_tablet)!=0 &&  newConfig.orientation==Configuration.ORIENTATION_LANDSCAPE){
        	findViewById(R.id.flTools).setVisibility(View.VISIBLE);
        }
        else findViewById(R.id.flTools).setVisibility(View.GONE);*/
        setBarConfig(newConfig.orientation);
    }
    
    Boolean isTabletLandscape=false;
    
    void setBarConfig(int orientation){
    	drawView.hideCrop();
		drawView.startEdit();
		hideTools();
    	if (getResources().getInteger(R.integer.is_tablet)!=0 &&  orientation==Configuration.ORIENTATION_LANDSCAPE){
        	//findViewById(R.id.flTools).setVisibility(View.VISIBLE);
    		isTabletLandscape=true;
    		findViewById(R.id.footer).setVisibility(View.GONE);
        	((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(2);
    		setLandToolSelected(R.id.bEditPage_land);
    		
        }
        else {
        	//findViewById(R.id.flTools).setVisibility(View.GONE);
        	isTabletLandscape=false;
        	((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(0);
        	findViewById(R.id.footer).setVisibility(View.VISIBLE);
        	setSelectedFoot(0);
        	
        }
    }
    
    String getPhotosPhotoLink(String uri){
    	 	        uri = uri.replace(CONTENT_PHOTOS_URI_PREFIX, "");
    	 	        String[] parts = uri.split("/");
    	 	        if (parts.length > 4) {
    	 	            String finalString = "";
    	 	
    	 	            for(int i = 3 ; i < parts.length - 2 ; i ++){
    	 	                if(!(i==parts.length - 3 && parts[i].contains("ORIGINAL"))){
	    	 	            	if(!finalString.isEmpty()){
	    	 	                    finalString +="/";
	    	 	                }
	    	 	            	finalString += parts[i];
    	 	                }
    	 	            }
    	 	            return finalString;
    	 	        }else{
    	 	            return uri;
    	 	        }
    }
   
    @Override
    protected void onResume(){
    	super.onResume();
    	//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	//overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    	if (prefs==null) prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	serverHelper.getInstance().setCallback(this,this);
    	if (appSettings.sessionId.length()==0) appSettings.sessionId = prefs.getString("sessionId", "");
    	//appSettings.sessionId=(sessionId);
    	exitOnComplete = false;
    	dColor =prefs.getInt(pColor, Color.RED);
    	drawView.setColour(dColor);
    	setPaletteColor(dColor);
    }
    
    
    
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {
                	drawView.zoom(1.25f);
                }
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (action == KeyEvent.ACTION_DOWN) {
                	drawView.zoom(0.75f);
                }
                return true;
            case KeyEvent.KEYCODE_BACK:
            	if(action ==KeyEvent.ACTION_DOWN){
            		if(drawer.isDrawerOpen(GravityCompat.START)){
            			drawer.closeDrawer(GravityCompat.START);
            			return true;
            		} else
            		if(findViewById(R.id.text_field).getVisibility()!=View.GONE)
            		{
            			findViewById(R.id.text_field).setVisibility(View.GONE);
            			drawView.hideCrop();
                		drawView.startEdit();
                		setSelectedFoot(0);
            			return true;
            		}
            		else if(findViewById(R.id.color_menu).getVisibility()!=View.GONE)
            		{
            			findViewById(R.id.color_menu).setVisibility(View.GONE);
            			drawView.hideCrop();
            			drawView.setCanDraw(true);
                		//drawView.startEdit();
		
                		/*if (drawView.isDrawMode()) setSelectedFoot(1);
                		else setSelectedFoot(0);*/
            			return true;
            		}
            		else if(findViewById(R.id.draw_tools).getVisibility()!=View.GONE)
            		{
            			findViewById(R.id.draw_tools).setVisibility(View.GONE);
            			drawView.hideCrop();
                		drawView.startEdit();
                		setSelectedFoot(0);
            			return false;
            		}else
            		if (!drawView.hideCrop()){
            			if (drawView.hasChanged() && storePath.length()==0) showDialog(0);
            			else 
            				{
            					drawView.recycle();
            					finish();
            					return true;
            				}
            		}
            		else {
            			((ImageButton)findViewById(R.id.bToolCrop)).setSelected(false);
            			drawView.startEdit();
                		setSelectedFoot(0);
            			return true;
            		}
            		
            	}
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case 0:
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.do_save_image))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.save_yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    exitOnComplete = true;
                                    screenCapture();
                                    //finish();
                                }
                            })
                    .setNeutralButton(getString(R.string.cancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            })
                    .setNegativeButton(getString(R.string.save_no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                    drawView.recycle();
                                    finish();
                                }
                            });
            
            return builder.create();
        default:
            return null;
        }
    }
    
    
    
    
    private int colorToId(int c){
    	int res=0;
    	switch (c) {
    	case Color.CYAN:
			res=2;
			break;
		case Color.RED:
			res =5;	
			break;
		case Color.GREEN:
			res=3;	
			break;
		case Color.WHITE:
			res=0;	
			break;
		case Color.BLACK:
			res=7;	
			break;
		}
    	return res;
    }
    
    private void updateColorDialog(int sw, int fw, int cid){
    	((SeekBar)findViewById(R.id.seekBarLine)).setProgress(sw);
    	((SeekBar)findViewById(R.id.ls_seekBarLine)).setProgress(sw);
		((SeekBar)findViewById(R.id.seekBarType)).setProgress(fw*10);
		((SeekBar)findViewById(R.id.ls_seekBarType)).setProgress(fw*10);
		((TextView)findViewById(R.id.tvTextType)).setText(String.format("%d",40+fw*20));
		((TextView)findViewById(R.id.ls_tvTextType)).setText(String.format("%d",40+fw*20));
		setColorButtons(cid);
		/*switch (cid){
			case 2:
				((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_blue);	
				((ImageButton)findViewById(R.id.bToolColor_land)).setImageResource(R.drawable.icon_color_blue);
				((ImageButton)findViewById(R.id.bToolColor_land1)).setImageResource(R.drawable.icon_color_blue);
				setLandColorSelected(R.id.ls_bColor1);
				break;
			case 5:
				((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_red);	
				((ImageButton)findViewById(R.id.bToolColor_land)).setImageResource(R.drawable.icon_color_red);
				((ImageButton)findViewById(R.id.bToolColor_land1)).setImageResource(R.drawable.icon_color_red);
				setLandColorSelected(R.id.ls_bColor2);
				break;
			case 3:
				((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_yellow);	
				((ImageButton)findViewById(R.id.bToolColor_land)).setImageResource(R.drawable.icon_color_yellow);
				((ImageButton)findViewById(R.id.bToolColor_land1)).setImageResource(R.drawable.icon_color_yellow);
				setLandColorSelected(R.id.ls_bColor3);
				break;
			case 0:
				((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_white);
				((ImageButton)findViewById(R.id.bToolColor_land)).setImageResource(R.drawable.icon_color_white);
				((ImageButton)findViewById(R.id.bToolColor_land1)).setImageResource(R.drawable.icon_color_white);
				setLandColorSelected(R.id.ls_bColor4);
				break;
			case 7:
				((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_black);	
				((ImageButton)findViewById(R.id.bToolColor_land)).setImageResource(R.drawable.icon_color_black);
				((ImageButton)findViewById(R.id.bToolColor_land1)).setImageResource(R.drawable.icon_color_black);
				setLandColorSelected(R.id.ls_bColor5);
				break;	
				
		}*/
    }
    
    private void showSettings()
    {
    	Intent i = new Intent(getApplicationContext(), loginActivity.class);
    	i.putExtra("userMail", userMail==null?"":userMail);
    	i.putExtra("needresult", true);
    	startActivityForResult(i, SIGN_IN);
    	//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	return true;
    }
    
    private void setSelectedFoot(int id){
    	View ib;
    	for(int i=0; i<buttons.length; i++){
    		ib = (View)findViewById(buttons[i]);
    		if(i==id) {
    			ib.setSelected(true);
    		}
    		else {
    			ib.setSelected(false);
    		}
    	}
    }
    
    void setToolSelected(int id){
    	LinearLayout ll=(LinearLayout) findViewById(R.id.flTools);
    	for (int i=0;i<ll.getChildCount();i++){
    		ll.getChildAt(i).setSelected(id==ll.getChildAt(i).getId());
    	}
    	
    }
    
    void setLandToolSelected(int id){
    	LinearLayout ll=(LinearLayout) findViewById(R.id.tool_edit_land);
    	for (int i=0;i<ll.getChildCount();i++){
    		ll.getChildAt(i).setSelected(id==ll.getChildAt(i).getId());
    	}
    	
    }
    
    void setLandColorSelected(int id){
    	/*LinearLayout ll=(LinearLayout) findViewById(R.id.color_select);
    	View v;
    	for (int i=0;i<ll.getChildCount();i++){
    		v=ll.getChildAt(i);
    		if(v instanceof ImageButton)
    			v.setSelected(id==v.getId());
    	}*/
    	
    }
    public void onToolsClick(View v){
    	//ImageButton bs = (ImageButton)findViewById(R.id.bToolShape);
    	switch (v.getId()) {
    	case R.id.ls_bDrawCancel:
    	case R.id.ls_bToolCancel:
    		if(prev==0 || v.getId()==R.id.ls_bDrawCancel){
    			((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(2);
	    		drawView.hideCrop();
	    		drawView.startEdit();
	    		hideTools();
	    		setLandToolSelected(R.id.bEditPage_land);
    		}
    		else {
    			((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(3);
    		}
    		break;
    	/*case R.id.ls_bToolCrop:
    		hideTools();
    		drawView.setShape(10); 
    		ImageButton iv = (ImageButton)findViewById(R.id.ls_bToolCrop);
    		if (iv.isSelected()) 
    			{
    				iv.setSelected(false);
    				drawView.startEdit();
    				setToolSelected(R.id.ls_bEditPage);
    			}
    		else setToolSelected(R.id.ls_bToolCrop);
    		break;
    	case R.id.ls_bToolText:
    		if(findViewById(R.id.text_field).getVisibility()!=View.VISIBLE){
    			drawView.hideCrop();
    			drawView.setShape(8); 
    			hideTools();
    			setToolSelected(R.id.ls_bToolText);
    			updateColorDialog(dWidth, fWidth, dColor);
    		}
    		else{
    			
    			findViewById(R.id.text_field).setVisibility(View.GONE);
    			drawView.hideCrop();
        		drawView.startEdit();
        		setToolSelected(R.id.ls_bEditPage);
    		}
    		break;
    		
    	case R.id.ls_bEditPage:
    		drawView.hideCrop();
    		drawView.startEdit();
    		hideTools();
    		setToolSelected(R.id.ls_bEditPage);
    		break;	
    	case R.id.ls_bToolColor:
    		if(findViewById(R.id.color_menu).getVisibility()!=View.VISIBLE){
    		drawView.hideCrop();
    		showColorPopup(findViewById(R.id.bToolColor));
    		setToolSelected(R.id.ls_bToolColor);
    		}
    		else{
    			findViewById(R.id.color_menu).setVisibility(View.GONE);
    			drawView.hideCrop();
        		drawView.startEdit();
        		setToolSelected(R.id.ls_bEditPage);
    		}
    		break;*/
    		
	    case R.id.ls_bDraw1:
			drawView.setShape(0);
			setToolSelected(R.id.ls_bDraw1);
			//((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(2);
			//bs.setImageResource(R.drawable.draw_tools_01);
			//findViewById(R.id.ls_draw_tools).setVisibility(View.GONE);
			break;
		case R.id.ls_bDraw2:
			drawView.setShape(5); 
			setToolSelected(R.id.ls_bDraw2);
			//((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(2);
			//bs.setImageResource(R.drawable.draw_tools_03);
			//findViewById(R.id.ls_draw_tools).setVisibility(View.GONE);
			break;
		case R.id.ls_bDraw3:
			drawView.setShape(3);
			//((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(2);
			setToolSelected(R.id.ls_bDraw3);
			//bs.setImageResource(R.drawable.draw_tools_02);
			//findViewById(R.id.ls_draw_tools).setVisibility(View.GONE);
			break;
		case R.id.ls_bDraw4:
			setToolSelected(R.id.ls_bDraw4);
			//((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(2);
			drawView.setShape(7);
			//bs.setImageResource(R.drawable.draw_tools_04);
			//findViewById(R.id.ls_draw_tools).setVisibility(View.GONE);
			break;
		case R.id.ls_bDraw5:
			setToolSelected(R.id.ls_bDraw5);
			drawView.setShape(6);
			//((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(2);
			//bs.setImageResource(R.drawable.draw_tools_06);
			//findViewById(R.id.ls_draw_tools).setVisibility(View.GONE);
			break;
		case R.id.ls_bDraw6:
			setToolSelected(R.id.ls_bDraw6);
			//((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(2);
			drawView.setShape(1);
			//bs.setImageResource(R.drawable.draw_tools_07);
			//findViewById(R.id.ls_draw_tools).setVisibility(View.GONE);
			break;
		case R.id.ls_bDraw8:
			setToolSelected(R.id.ls_bDraw8);
			//((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(2);
			drawView.setShape(9);
			//findViewById(R.id.ls_draw_tools).setVisibility(View.GONE);
			//bs.setImageResource(R.drawable.draw_tools_05);
			break;
    	}
    }
    
    int prev=0;
    public void onTopBarClick(View v) {
		switch (v.getId()) {
		case R.id.bUndo_land:
    		drawView.undo();
    		break;
    	case R.id.btnBack:
    		drawView.hideCrop();
    		((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(0);
    		break;
    	case R.id.bToolShape_land:
    		((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(3);
    		updateColorDialog(dWidth, fWidth, dColor);
    		drawView.setShape(0);
			setToolSelected(R.id.ls_bDraw1);
    		
    		break;
    	case R.id.bToolCrop_land:
    		hideTools();
    		drawView.setShape(10); 
    		ImageButton iv = (ImageButton)findViewById(R.id.bToolCrop_land);
    		if (iv.isSelected()) 
    			{
    				iv.setSelected(false);
    				drawView.startEdit();
    				setLandToolSelected(R.id.bEditPage_land);
    			}
    		else setLandToolSelected(R.id.bToolCrop_land);
    		break;
    	case R.id.bToolText_land:
    		if(findViewById(R.id.text_field).getVisibility()!=View.VISIBLE){
    			drawView.hideCrop();
    			drawView.setShape(8); 
    			hideTools();
    			setLandToolSelected(R.id.bToolText_land);
    			updateColorDialog(dWidth, fWidth, dColor);
    		}
    		else{
    			
    			findViewById(R.id.text_field).setVisibility(View.GONE);
    			drawView.hideCrop();
        		drawView.startEdit();
        		setLandToolSelected(R.id.bEditPage_land);
    		}
    		break;
    		
    	case R.id.bEditPage_land:
    		drawView.hideCrop();
    		drawView.startEdit();
    		hideTools();
    		setLandToolSelected(R.id.bEditPage_land);
    		break;	
    	case R.id.bToolColor_land:
    		prev=0;
    		((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(4);
    		break;
    		
    	case R.id.bToolColor_land1:
    		prev=1;
    		((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(4);
    		break;
    		
    	case R.id.bClearAll_land:
    		drawView.clear();
    		break;
    	case R.id.bTurnLeft_land:
    		drawView.deselectShapes();
    		drawView.setAngle(-90);
    		break;
    	case R.id.bTurnRight_land:
    		drawView.deselectShapes();
    		drawView.setAngle(90);
    		break;
    	case R.id.bDone_land:
    		
    		if(storePath.length()>0){
    			drawView.deselectShapes();
        		drawView.hideCrop();
        		((ImageButton)findViewById(R.id.bToolCrop)).setSelected(false);
        		drawView.startEdit();
        		setSelectedFoot(0);
        		v.postDelayed(new Runnable() {
        			@Override
        			public void run() {
        				screenCapture();
        			}
        		},200);
    		}
    		else {
    			//((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(1);
    			((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(1);
    		}
    		break;

		default:
			break;
		}
	}
    
    void setPaletteColor(int c){
    	if(paletteButton!=null) paletteButton.setColor(c);
    	if(paletteButton_land!=null) paletteButton_land.setColor(c);
    	CircleButton p=(CircleButton)findViewById(R.id.bToolColor_land1);
    	if (p!=null) p.setColor(c);
    	drawView.setCanDraw(true);
    }
    
    public void onClick(View v)
    {
    	//ImageButton b = (ImageButton)findViewById(R.id.bToolColor);
    	ImageButton bs = (ImageButton)findViewById(R.id.bToolShape);
    	int zcolor = 123;
    	switch(v.getId()){
    	case R.id.bSave2Nimbus:
    		drawView.deselectShapes();
    		drawView.hideCrop();
    		((ImageButton)findViewById(R.id.bToolCrop)).setSelected(false);
    		drawView.startEdit();
    		setSelectedFoot(0);
    		if (appSettings.sessionId.length() == 0) showSettings();
    		else {
    			v.postDelayed(new Runnable() {
        			@Override
        			public void run() {
        				getFolders();
        			}
        		},200);
    		}
    		break;
    	case R.id.bSave2SD:
    		drawView.deselectShapes();
    		drawView.hideCrop();
    		((ImageButton)findViewById(R.id.bToolCrop)).setSelected(false);
    		drawView.startEdit();
    		setSelectedFoot(0);
    		v.postDelayed(new Runnable() {
    			@Override
    			public void run() {
    				screenCapture();
    			}
    		},200);
    		break;
    	case R.id.btnBack:
    		drawView.hideCrop();
    		if(isTabletLandscape) ((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(2);
    			else  ((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(0);
    		break;
    	case R.id.bToolShape:
    		
    		if(findViewById(R.id.flTools).getVisibility()==View.VISIBLE){
    			if(findViewById(R.id.draw_tools).getVisibility()!=View.VISIBLE){
    				drawView.hideCrop();
    	    		drawView.reset();
    	    		findViewById(R.id.draw_tools).setVisibility(View.VISIBLE);
    	    		setSelectedFoot(1);
    	    		updateColorDialog(dWidth, fWidth, dColor);
    			}
    			else {
    				findViewById(R.id.draw_tools).setVisibility(View.GONE);
        			drawView.hideCrop();
            		drawView.startEdit();
            		setSelectedFoot(0);
    			}
    			return;
    		}
    		if(findViewById(R.id.draw_tools).getVisibility()!=View.VISIBLE){
    		drawView.hideCrop();
    		drawView.reset();
    		showToolsPopup(findViewById(R.id.bEditPage));
    		setSelectedFoot(1);
    		updateColorDialog(dWidth, fWidth, dColor);
    		} else{
    			findViewById(R.id.draw_tools).setVisibility(View.GONE);
    			drawView.hideCrop();
        		drawView.startEdit();
        		setSelectedFoot(0);
    		}
    		break;
    	case R.id.bToolCrop:
    		hideTools();
    		drawView.setShape(10); 
    		ImageButton iv = (ImageButton)findViewById(R.id.bToolCrop);
    		if (iv.isSelected()) 
    			{
    				iv.setSelected(false);
    				drawView.startEdit();
    				setSelectedFoot(0);
    			}
    		else setSelectedFoot(4);
    		break;
    	case R.id.bToolText:
    		if(findViewById(R.id.text_field).getVisibility()!=View.VISIBLE){
    			drawView.hideCrop();
    			drawView.setShape(8); 
    			hideTools();
    			setSelectedFoot(2);
    			updateColorDialog(dWidth, fWidth, dColor);
    		}
    		else{
    			
    			findViewById(R.id.text_field).setVisibility(View.GONE);
    			drawView.hideCrop();
        		drawView.startEdit();
        		setSelectedFoot(0);
    		}
    		break;
    		case R.id.bApplyText:
    			drawView.setText(((EditText)findViewById(R.id.etEditorText)).getText().toString(),(boolean)findViewById(R.id.bStroke).isSelected());
    			drawView.startEdit();
    			findViewById(R.id.text_field).setVisibility(View.GONE);
        		setSelectedFoot(0);
        		InputMethodManager im=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        		im.hideSoftInputFromWindow(findViewById(R.id.etEditorText).getWindowToken(), 0);
        		
    			break;
    	case R.id.bEditPage:
    		drawView.hideCrop();
    		drawView.startEdit();
    		hideTools();
    		setSelectedFoot(0);
    		break;	
    	case R.id.bToolColor:
    		if(findViewById(R.id.color_menu).getVisibility()!=View.VISIBLE){
    		drawView.hideCrop();
    		showColorPopup(findViewById(R.id.bToolColor));
    		drawView.setCanDraw(false);
    		//setSelectedFoot(3);
    		}
    		else{
    			findViewById(R.id.color_menu).setVisibility(View.GONE);
    			drawView.hideCrop();
    			drawView.setCanDraw(true);
        		//drawView.startEdit();
        		//setSelectedFoot(0);
    		}
    		break;
    	case R.id.bErase:
    		drawView.hideCrop();
    		drawView.startEdit();
    		hideTools();
    		drawView.setShape(11);
    		//findViewById(R.id.draw_tools).setVisibility(View.GONE);
    		setSelectedFoot(5);
    		break;
    	case R.id.bDraw1:
    		drawView.setShape(0);
    		bs.setImageResource(R.drawable.draw_tools_01);
    		setSelectedFoot(1);
    		findViewById(R.id.draw_tools).setVisibility(View.GONE);
    		break;
    	case R.id.bDraw2:
    		drawView.setShape(5); 
    		bs.setImageResource(R.drawable.draw_tools_03);
    		setSelectedFoot(1);
    		findViewById(R.id.draw_tools).setVisibility(View.GONE);
    		break;
    	case R.id.bDraw3:
    		drawView.setShape(3);
    		bs.setImageResource(R.drawable.draw_tools_02);
    		findViewById(R.id.draw_tools).setVisibility(View.GONE);
    		break;
    	case R.id.bDraw4:
    		drawView.setShape(7);
    		findViewById(R.id.draw_tools).setVisibility(View.GONE);
    		bs.setImageResource(R.drawable.draw_tools_04);
    		break;
    	case R.id.bDraw5:
    		drawView.setShape(6);
    		findViewById(R.id.draw_tools).setVisibility(View.GONE);
    		bs.setImageResource(R.drawable.draw_tools_06);
    		break;
    	case R.id.bDraw6:
    		drawView.setShape(1);
    		findViewById(R.id.draw_tools).setVisibility(View.GONE);
    		bs.setImageResource(R.drawable.draw_tools_07);
    		break;
    	case R.id.bDraw8:
    		drawView.setShape(9);
    		findViewById(R.id.draw_tools).setVisibility(View.GONE);
    		bs.setImageResource(R.drawable.draw_tools_05);
    		break;
    	case R.id.ls_bColor1:
    	case R.id.bColor1:
    		drawView.setColour(Color.CYAN);    		
    		setPaletteColor(Color.CYAN);
    		zcolor=Color.CYAN;
    		/*b.setImageResource(R.drawable.icon_color_blue);
    		((ImageButton)findViewById(R.id.bToolColor_land)).setImageResource(R.drawable.icon_color_blue);
    		((ImageButton)findViewById(R.id.bToolColor_land1)).setImageResource(R.drawable.icon_color_blue);*/
    		setLandColorSelected(R.id.ls_bColor1);
    		findViewById(R.id.color_menu).setVisibility(View.GONE);
    		break;
    	case R.id.ls_bColor2:
    	case R.id.bColor2:
    		drawView.setColour(Color.RED);
    		setPaletteColor(Color.RED);
    		zcolor=Color.RED;
    		/*b.setImageResource(R.drawable.icon_color_red);
    		((ImageButton)findViewById(R.id.bToolColor_land)).setImageResource(R.drawable.icon_color_red);
    		((ImageButton)findViewById(R.id.bToolColor_land1)).setImageResource(R.drawable.icon_color_red);*/
    		setLandColorSelected(R.id.ls_bColor2);
    		findViewById(R.id.color_menu).setVisibility(View.GONE);
    		break;
    	case R.id.ls_bColor3:
    	case R.id.bColor3:
    		drawView.setColour(Color.GREEN);

    		setPaletteColor(Color.GREEN);
    		zcolor=Color.GREEN;
    		/*b.setImageResource(R.drawable.icon_color_yellow);
    		((ImageButton)findViewById(R.id.bToolColor_land)).setImageResource(R.drawable.icon_color_yellow);
    		((ImageButton)findViewById(R.id.bToolColor_land1)).setImageResource(R.drawable.icon_color_yellow);*/
    		setLandColorSelected(R.id.ls_bColor3);
    		findViewById(R.id.color_menu).setVisibility(View.GONE);
    		break;
    	case R.id.ls_bColor4:
    	case R.id.bColor4:
    		drawView.setColour(Color.BLACK);
    		setPaletteColor(Color.BLACK);
    		zcolor=Color.BLACK;
    		/*b.setImageResource(R.drawable.icon_color_black);
    		((ImageButton)findViewById(R.id.bToolColor_land)).setImageResource(R.drawable.icon_color_black);
    		((ImageButton)findViewById(R.id.bToolColor_land1)).setImageResource(R.drawable.icon_color_black);*/
    		setLandColorSelected(R.id.ls_bColor4);
    		findViewById(R.id.color_menu).setVisibility(View.GONE);
    		break;
    	case R.id.ls_bColor5:
    	case R.id.bColor5:
    		openDialog(false);
    		/*drawView.setColour(7);
    		color=7;
    		b.setImageResource(R.drawable.icon_color_black);
    		((ImageButton)findViewById(R.id.bToolColor_land)).setImageResource(R.drawable.icon_color_black);
    		((ImageButton)findViewById(R.id.bToolColor_land1)).setImageResource(R.drawable.icon_color_black);
    		setLandColorSelected(R.id.ls_bColor5);*/
    		findViewById(R.id.color_menu).setVisibility(View.GONE);
    		break;
    	case R.id.bUndo:
    		drawView.undo();
    		break;
    	
    	case R.id.btnShare:
    		drawView.deselectShapes();
    		drawView.hideCrop();
    		v.postDelayed(new Runnable() {
    			@Override
    			public void run() {
    				shareCapture();	
    			}
    		},200);
    		
    		break;
    	case R.id.bDone:
    		if(storePath.length()>0){
    			drawView.deselectShapes();
        		drawView.hideCrop();
        		((ImageButton)findViewById(R.id.bToolCrop)).setSelected(false);
        		drawView.startEdit();
        		setSelectedFoot(0);
        		v.postDelayed(new Runnable() {
        			@Override
        			public void run() {
        				screenCapture();
        			}
        		},200);
    		}
    		else {
    			((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(1);
    		}
    		
    		break;
    	case R.id.bClearAll:
    		drawView.clear();
    		break;
    	case R.id.bTurnLeft:
    		drawView.deselectShapes();
    		drawView.setAngle(-90);
    		break;
    	case R.id.bTurnRight:
    		drawView.deselectShapes();
    		drawView.setAngle(90);
    		break;
    	}
    	if(zcolor!=123){
    		ccolor =zcolor;
    		dColor= zcolor;
    		Editor e = prefs.edit();
    		e.putInt(pColor,dColor);
    		e.commit();
    	}
    }
    
    
    void openDialog(boolean supportsAlpha) {
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(PaintActivity.this, ccolor, supportsAlpha, new AmbilWarnaDialog.OnAmbilWarnaListener() {
			@Override
			public void onOk(AmbilWarnaDialog dialog, int color) {
				//Toast.makeText(getApplicationContext(), "ok", Toast.LENGTH_SHORT).show();
				drawView.setColour(color);
				setPaletteColor(color);
				ccolor=color;
				dColor=color;
				if(prefs!=null){
					Editor e = prefs.edit();
		    		e.putInt(pColor,dColor);
		    		e.commit();
				}
			}

			@Override
			public void onCancel(AmbilWarnaDialog dialog) {
				//Toast.makeText(getApplicationContext(), "cancel", Toast.LENGTH_SHORT).show();
				drawView.setCanDraw(true);
			}
		});
		dialog.show();
	}
    
    public void showToolsPopup(View view) {
    	drawView.startDraw();
    	hideTools();
    	findViewById(R.id.draw_tools).setVisibility(View.VISIBLE);
    }
    
    
    public void showColorPopup(View view) {
    	
    	hideTools();
    	findViewById(R.id.color_menu).setVisibility(View.VISIBLE);

    }
    
    
public void showSettingsPopup(View view) {
    	
        
		

    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection
    	return true;
    }
    
    void setCustomBackground(DrawView v) {
    	Intent fileChooserIntent = new Intent();
    	fileChooserIntent.addCategory(Intent.CATEGORY_OPENABLE);
    	fileChooserIntent.setType("image/*");
    	fileChooserIntent.setAction(Intent.ACTION_GET_CONTENT);
    	startActivityForResult(Intent.createChooser(fileChooserIntent, "Select Picture"), 1);
    	//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    }
    
    private void sendRequest(String action, String data){
		serverHelper.getInstance().sendRequest(action, data,"");
	}
    
    String photoFileName="";
    Uri outputFileUri=null;
    public void getPhoto(){
    	try{
    		showProgress(true);
    		/*photoFileName = String.valueOf(System.currentTimeMillis())+"-tmp.jpg";	
    		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    		File file = new File(appSettings.getInstance().SavingPath, photoFileName);
    		outputFileUri = Uri.fromFile(file);
    		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
    		startActivityForResult(intent, TAKE_PHOTO);
    		//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    		overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);*/
    		photoFileName = "temp.jpg";//String.valueOf(System.currentTimeMillis())+"-tmp.jpg";	
    		Intent intent = new Intent(getApplicationContext(), com.fvd.cropper.ScannerActivity.class);
    		intent.putExtra("fname", appSettings.SavingPath+"temp.jpg"/*String.valueOf(System.currentTimeMillis())+"-tmp.jpg"*/);
    		//intent.putExtra("mode", prefs.getInt("scanMode", 1));
    		startActivityForResult(intent, TAKE_PHOTO);
    		overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    	}
    	catch (Exception e){
    		appSettings.appendLog("main:getPhoto  "+e.getMessage());
    		showProgress(false);
    	}
    	
    }
    
    public void getPicture() {
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
    
    String shot_parent = "default";
	String shot_tag = "androidclipper";
	String shot_title="";
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	ContentResolver cr;
		InputStream is;
    	if (requestCode == TAKE_PHOTO) {
    		showProgress(false);
        	if (resultCode == -1){
        		if(data!=null){
        			 try { 
         			 	Uri resultUri = data.getData();
         			 	String drawString = resultUri.getPath();
         			 	if(drawString.startsWith("/storage"))  resultUri=Uri.parse("file://"+drawString);
         			 	storeFileName="PHOTO_tmp";
      
         			 	InputStream input =  getContentResolver().openInputStream(/*Uri.parse(drawString)*/resultUri);
         			 	int orient = helper.getImageRotation(PaintActivity.this, resultUri/*Uri.parse(drawString)*/);
 			 			drawView.setVisibility(View.GONE);
 			 			drawView.recycle();
 			 			drawView.setBitmap(BitmapFactory.decodeStream(input), orient);
 			 			
 			 			drawView.setVisibility(View.VISIBLE);
 			 			drawView.forceRedraw();
         		 } 
         		 catch(Exception e){
         			 appSettings.appendLog("main:onActivityResult  "+e.getMessage()); 
         		}
        		}
        	}
        	((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(0);
          }
          else if (requestCode == TAKE_PICTURE){
        	  showProgress(false);
        	  if(resultCode == -1 && data!=null){
        		  boolean temp = false;
        		 try { 
        			 	Uri resultUri = data.getData();
        			 	String drawString = resultUri.getPath();
        			 	if(drawString.startsWith("/storage"))  resultUri=Uri.parse("file://"+drawString);
        			 	storeFileName=getGalleryName(resultUri);
        			 	//else  drawString="content:/"+drawString;
        			 	InputStream input =  getContentResolver().openInputStream(/*Uri.parse(drawString)*/resultUri);
        			 	int orient = helper.getImageRotation(PaintActivity.this, resultUri/*Uri.parse(drawString)*/);
			 			drawView.setVisibility(View.GONE);
			 			drawView.recycle();
			 			drawView.setBitmap(BitmapFactory.decodeStream(input), orient);
			 			
			 			drawView.setVisibility(View.VISIBLE);
			 			drawView.forceRedraw();
        		 } 
        		 catch(Exception e){
        			 appSettings.appendLog("main:onActivityResult  "+e.getMessage()); 
        		}
        }
        	  ((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(0);
    } else if(requestCode==GET_FOLDERS){
    	if (data!=null){
			try{ 
				DataExchange xdata=(DataExchange)data.getExtras().getSerializable("xdata");
				shot_parent = xdata.getId();
				shot_tag = xdata.getTags();
				shot_title = xdata.getTitle();
				clipData.setTitle(shot_title);
				//clipData.setTags(tag);
				if(appSettings.sessionId.length() == 0 || userPass.length()==0) showSettings();
				else {
					sendShotEx();
					clipData.setContent("");
				}
			}
			catch (Exception e){
				BugReporter.Send("BrowseAct", e.getMessage());
			}
		}
    } else if(requestCode==SIGN_IN){
    	if(resultCode == -1 && appSettings.sessionId!=""){
    		userMail=appSettings.userMail;
    		userPass=appSettings.userPass;

    		drawView.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					getFolders();
				}
			}, 350);
    		
    	}
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
        			appSettings.sessionId="";
        			//serverHelper.getInstance().setSessionId(appSettings.sessionId);
        			Editor e = prefs.edit();
        			e.putString("userMail", userMail);
            	    e.putString("userPass", "");
            	    e.putString("sessionId", appSettings.sessionId);
            	    e.commit();
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
          else 	
    if (requestCode==3){
    		if (resultCode==RESULT_OK || resultCode==RESULT_FIRST_USER){
    			userMail=data.getStringExtra("userMail");
    			userPass=data.getStringExtra("userPass");
    			if(resultCode==RESULT_OK)	sendRequest("user:auth", String.format("\"email\":\"%s\",\"password\":\"%s\"",userMail,userPass));
    			else serverHelper.getInstance().sendOldRequest("user_register", String.format("{\"action\": \"user_register\",\"email\":\"%s\",\"password\":\"%s\",\"_client_software\": \"ff_addon\"}",userMail,userPass), "");
    		}
    	}else if (requestCode==11){
    		if(appSettings.sessionId!=""){
    			//sessionId=appSettings.sessionId;
    			userPass=appSettings.userPass;
    			getFolders();
    		}
    	}
    	else
    		if (requestCode==4){
        		if (resultCode==RESULT_OK){
        			String id=data.getStringExtra("id").toString();
        			serverHelper.getInstance().shareShot(id);
        		}
        	}
        	else
    	if (resultCode == RESULT_OK)
    	{
    		drawView.setColour(dColor);
    		setPaletteColor(dColor);
    		Uri resultUri = data.getData();
	    	String drawString = resultUri.getPath();
	    	String galleryString = getGalleryPath(resultUri);
	    	
	    	if (galleryString != null)
	    	{
	    		drawString = galleryString;
	    	}
	    	// else another file manager was used
	    	else
	    	{
		    	if (drawString.contains("//"))
		    	{
		    		drawString = drawString.substring(drawString.lastIndexOf("//"));
		    	}
	    	}
	    	
	    	// set the background to the selected picture
	    	if (drawString.length() > 0)
	    	{
	    		Drawable drawBackground = Drawable.createFromPath(drawString);
	    		drawView.setBackgroundDrawable(drawBackground);
	    	}
	    	
    	}
    }
    
    
    public String getCurrDate()
    {
        String dt;
        Date now = new Date();
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
        dt=df.format(now);
        return dt;
    }
    
    void showProgress(boolean b){
    	findViewById(R.id.paintWait).setVisibility(b?View.VISIBLE:View.INVISIBLE);
    }
    
    public void screenCapture()
    {
    	try{
    		Bitmap bm = drawView.getBitmap();
			 if(bm!=null){
    		new CompressTask(0).execute(bm);
			 }
    	}
    	catch (Exception e) {
			 e.printStackTrace();
			 Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    
    public void shareCapture()
    {
    	try{
    		Bitmap bm = drawView.getBitmap();
			 if(bm!=null){
				 new CompressTask(1).execute(bm);
			 }
    	}
    	catch (Exception e) {
			 e.printStackTrace();
			 Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    void getFolders(){
    	serverHelper.getInstance().sendRequest("notes:getFolders", "","");
    }
    
    private void sendShotEx(){
    	try{
    		Bitmap bm = drawView.getBitmap();
			 if(bm!=null){
				 new CompressTask(2).execute(bm);
			 }
    	}
    	catch (Exception e) {
			 e.printStackTrace();
			 Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    private String getImagePath(){
        
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
    
   	public String getGalleryName(Uri uri) {
        	String picturePath=uri.toString();
        	if(picturePath.startsWith("file:")){
        		return helper.trimExt(picturePath.substring(picturePath.lastIndexOf("/")+1));
        	}
        	picturePath=null;
        	String[] filePathColumn = { MediaStore.Images.Media.DISPLAY_NAME };
        	try{
    	    	Cursor cursor = getContentResolver().query(uri,filePathColumn, null, null,   null);
    	    	cursor.moveToFirst();
    	    	int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
    	    	picturePath = cursor.getString(columnIndex);
    	    	cursor.close();
        	} catch (Exception e){}
        	if(picturePath!=null) picturePath=helper.trimExt(picturePath);
        	return picturePath;
    }
   	
    // used when trying to get an image path from the URI returned by the Gallery app
    public String getGalleryPath(Uri uri) {
    	String[] projection = { MediaStore.Images.Media.DATA };
    	Cursor cursor = managedQuery(uri, projection, null, null, null);
    	
    	if (cursor != null)
    	{
    		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
    		cursor.moveToFirst();
    		return cursor.getString(column_index);
    	}
    	
    	
    	return null;
    }
    
    void hideTools(){
    	findViewById(R.id.color_menu).setVisibility(View.GONE);
    	findViewById(R.id.text_field).setVisibility(View.GONE);
    	findViewById(R.id.draw_tools).setVisibility(View.GONE);
    	drawView.setCanDraw(true);
    }
    
    private void showShotSuccess(String id)
    {
    	Intent i = new Intent(getApplicationContext(), ShotSuccess.class);
    	i.putExtra("id", id);
    	startActivityForResult(i, 4);
    	//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    }
    
    private void showShareDlg(String url)
    {
    	Intent i = new Intent(getApplicationContext(), ArticleDlg.class);
    	i.putExtra("url", url);
    	startActivity(i);
    	//overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
    }
    
    String getNoteName(){
    	return String.format("Document %s", serverHelper.getDate()/*DateFormat.getDateTimeInstance().format(new Date())*/);
    }
    
    DataExchange clipData=new DataExchange();
    @SuppressLint("NewApi")
	@Override
    public void onTaskComplete(String result, String action)
    {
    		if(result.isEmpty()){
    			Toast.makeText(getApplicationContext(), "no internet connection", Toast.LENGTH_LONG).show();
    			return;
    		}
        	try{
            	JSONObject root = new JSONObject(result);
            	int error = root.getInt("errorCode");
            	if (error==0){
            		if (action.equalsIgnoreCase("user:auth")){
            			String ses = root.getJSONObject("body").getString("sessionid");
            			//appSettings.sessionId=(sessionId);
            			appSettings.storeUserData(ctx, userMail, userPass, ses,""); 
            			Toast.makeText(getApplicationContext(), "user authorized", Toast.LENGTH_LONG).show();
            			getFolders();
            		}
            		else if("screenshots:save".equals(action)){
            			
            			showShotSuccess(root.getJSONObject("body").getString("global_id"));
            		}
            		else if("notes:share".equalsIgnoreCase(action)){
            			String url=root.getString("url");
            			showShareDlg(url);
            		}
            		else if("user_register".equals(action)){
            			sendRequest("user:auth", String.format("\"email\":\"%s\",\"password\":\"%s\"",userMail,userPass));
            		}else if(action.equalsIgnoreCase("notes:getfolders")){
            			
            			//ArrayList<FolderItem>items=new ArrayList<FolderItem>();
            			//ArrayList<FolderListItem>items=new ArrayList<FolderListItem>();
            			try{
            					result = URLDecoder.decode(result,"UTF-16"); 
            					clipData.setData(result);
            	        		String cr=prefs.getString("remFolderId", "default");
            	        		if(cr==null || cr=="") cr="default";
            	        		clipData.setId(cr);
            	        		clipData.setTitle(String.format("%s %s",preName, serverHelper.getDate()));
            	        		clipData.setTags(defTag);
            	        		Intent intent = new Intent(getApplicationContext(), tagsActivity.class);
            	        		intent.putExtra("xdata", clipData);
            	        		
            	    	    	startActivityForResult(intent,GET_FOLDERS);
            	    	    	overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
            	    	    	clipData.setData("");
            	        }
            	        catch (Exception Ex){
            	        	appSettings.appendLog("prefs:onTaskComplete "+Ex.getMessage());
            	        }
            		}
            	}
            	else {
            		if(error==-6 && (action.toLowerCase().contains("upload") || action.equals("screenshots:save") )){
            			showSettings();
            		} 
            		else{
            			if(action.equalsIgnoreCase("user:auth")){
            				showSettings();
            			} else Toast.makeText(getApplicationContext(), String.format("Error: %s",serverHelper.errorMsg(error)), Toast.LENGTH_LONG).show();
            	    }
            		
            	}
            }
            catch (Exception Ex){
            }
    }
    
    private class CompressTask extends AsyncTask<Bitmap, Void, String>{
		private ProgressDialog pd;
		
		private int mode =0;
		Uri storeUri;
		String buff = "";
		public CompressTask(int imode)
	    {
	       this.mode = imode;
	    }
		
		@Override
		protected void onPreExecute(){ 
		   super.onPreExecute();
		   
		   pd = new ProgressDialog(ctx);
		   pd.setMessage(getString(R.string.please_wait));
		   pd.setCancelable(false);
		   pd.show();    
		}

		@Override
		protected String doInBackground(Bitmap... params) {
			String filename=(storeFileName==null?("IMG_"+String.valueOf(System.currentTimeMillis())):storeFileName)+(saveFormat==0?".png":".jpg");
			String sPath="";
			//storePath=" ";
			try{
			if(params[0]!=null){
				/*if(this.mode<2)*/{ 
					sPath=this.mode==1?appSettings.getSavingPath():appSettings.img_path;
					//sPath=appSettings.img_path;
					File file = new File(sPath,filename);
					storeUri=FileProviderCompat.fromFile(file);
					if(storePath.length()>0) storePath=file.getAbsolutePath();
					file.createNewFile();
					FileOutputStream ostream = new FileOutputStream(file);
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					params[0].compress(saveFormat==0?CompressFormat.PNG:CompressFormat.JPEG, 95, bos);
					
					//params[0].compress(saveFormat==0?CompressFormat.PNG:CompressFormat.JPEG, 90, ostream);
					ostream.write(bos.toByteArray());
					
					ostream.flush();
					ostream.close();
					if(this.mode!=1){
						ContentValues image = new ContentValues();
		                image.put(Images.Media.TITLE, "Nimbus");
		                image.put(Images.Media.DISPLAY_NAME, filename);
		                image.put(Images.Media.DESCRIPTION, "Nimbus Image");
		                image.put(Images.Media.DATE_ADDED, System.currentTimeMillis());
		                image.put(Images.Media.MIME_TYPE, ("image/"+(saveFormat==0?"png":"jpeg")));
		                image.put(Images.Media.ORIENTATION, 0);
		                File parent = file.getParentFile();
		                image.put(Images.ImageColumns.BUCKET_ID, parent.toString()
		                        .toLowerCase().hashCode());
		                image.put(Images.ImageColumns.BUCKET_DISPLAY_NAME, parent.getName()
		                        .toLowerCase());
		                image.put(Images.Media.SIZE, file.length());
		                image.put(Images.Media.DATA, file.getAbsolutePath());
		                Uri result = getContentResolver().insert(
		                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, image);
					}
					if(this.mode==2){
						this.buff = bos.toString("iso-8859-1");
					}
				}
				/*else {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					params[0].compress(saveFormat==0?CompressFormat.PNG:CompressFormat.JPEG, 90, bos);
	     			this.buff = bos.toString("iso-8859-1");
				}*/
				params[0].recycle();
				 
			}
		 }
			 
		 catch (Exception e) {
			 this.buff = e.getMessage();
			 this.mode= -1;
			 
			 
		}
		    return filename;
		}

		@Override
		protected void onPostExecute(String result){
		   super.onPostExecute(result);
		   drawView.saved=true;
		   
		   pd.dismiss();
		   if(this.mode==-1){
			   Toast.makeText(getApplicationContext(), this.buff, Toast.LENGTH_LONG).show();
		   }
		   else
		   if (this.mode==1){
			   Intent share = new Intent(Intent.ACTION_SEND);
			   share.setType("image/"+(saveFormat==0?"png":"jpeg"));
			   share.putExtra(Intent.EXTRA_STREAM, storeUri/*Uri.parse(String.format("file://%s/%s",sPath,result)*/);
			   startActivity(Intent.createChooser(share, "Share Image"));
		   }
		   else if(this.mode==2){
			   serverHelper.getInstance().uploadShot((domain!=null && domain!="")?String.format("Screenshot from %s", domain):shot_title,shot_parent,shot_tag,  buff);
		   }
		   else {
			   if(storePath.length()>0){
				   Intent intent = new Intent();
				   //intent.putExtra("path", storePath.charAt(0)=='/'?String.format("file:/%s", storePath):String.format("file://%s", storePath));
				   //intent.putExtra("path", String.format("file://%s", storePath));
				   intent.setData(storeUri/*Uri.parse(String.format("file://%s", storePath))*/);
				   setResult(RESULT_OK, intent);
				   overridePendingTransition(R.anim.carbon_slide_in,R.anim.carbon_slide_out);
				   finish();  
			   }
			   else Toast.makeText(getApplicationContext(), "Saved to "+appSettings.img_path, Toast.LENGTH_LONG).show();
		   }
		   //saved = true;
		   if (exitOnComplete) finish();
		}
		
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		Intent ip;
		drawer.closeDrawer(GravityCompat.START);
		switch (arg2) {
		case 0:
			
			getPhoto();
			break;
		case 1:
			getPicture();
			break;
		case 2:
			ip=new Intent();
			ip.setClassName("com.fvd.nimbus","com.fvd.nimbus.BrowseActivity");
    		startActivity(ip);
    		finish();
			break;
		case 3:
			ip=new Intent();
			ip.setClassName("com.fvd.nimbus","com.fvd.nimbus.ChoosePDFActivity");
    		startActivity(ip);
    		finish();
			break;
		default:
			break;
		}
	}
    

}
