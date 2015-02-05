package com.fvd.nimbus;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.View.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.ViewAnimator;

import com.fvd.paint.DrawView;
import com.fvd.utils.AsyncTaskCompleteListener;
import com.fvd.utils.appSettings;
import com.fvd.utils.prefsEditListener;
import com.fvd.utils.serverHelper;
import com.fvd.utils.shapeSelectionListener;
import android.view.inputmethod.InputMethodManager;



public class Paint extends Activity implements OnClickListener, AsyncTaskCompleteListener<String, String>{
	private static final String TAG = "FingerPaint";

	DrawView drawView;
	private int dWidth=0; 
	private int fWidth =0;
	private int dColor =0;
	private String userMail = "";
	private String userPass ="";
	private String sessionId = "";
	private SharedPreferences prefs;
	private boolean saved = false;
	private int saveFormat=0;
 	private String domain="";
	private boolean exitOnComplete = false;
	Context ctx;
	int[] buttons={R.id.bEditPage,R.id.bToolShape,R.id.bToolText, R.id.bToolColor,R.id.bToolCrop};
	
    /** Called when the activity is first created. */
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
        ctx = this;
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        dWidth= prefs.getInt("dWidth", 2); 
    	fWidth =prefs.getInt("fWidth", 1);
    	dColor =prefs.getInt("dColor", 5);
    	saveFormat = Integer.parseInt(prefs.getString("saveFormat", "1"));
        serverHelper.getInstance().setCallback(this,this);
        serverHelper.getInstance().setMode(saveFormat);
        setContentView(R.layout.screen_edit);
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
    	
    	((SeekBar)findViewById(R.id.seekBarLine)).setProgress(dWidth);
		((SeekBar)findViewById(R.id.seekBarType)).setProgress(fWidth);
		
		(( TextView ) findViewById(R.id.tvTextType)).setText(String.format("%d",40+fWidth*20));

    	
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
    	
    	((SeekBar)findViewById(R.id.seekBarLine)).setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
    		@Override
    	    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    			dWidth = progress;
    			drawView.setWidth((dWidth+1)*5);
    			Editor e = prefs.edit();
    			e.putInt("dWidth",dWidth);
    			e.commit();
    	    }

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
    	});
    	
    	((SeekBar)findViewById(R.id.seekBarType)).setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
    		@Override
    	    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    			if(fromUser){
    				fWidth = progress;
    				int c=40+fWidth*20;
    				drawView.setFontSize(c);
    				Editor e = prefs.edit();
    				e.putInt("fWidth",fWidth);
        			e.commit();
    				try{
    					(( TextView ) findViewById(R.id.tvTextType)).setText(String.format("%d",c));
    				}
    				catch (Exception ex){
    					
    				}
    				
    			}
    	    }

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
    	});
        
        drawView = (DrawView)findViewById(R.id.painter);
        drawView.setWidth((dWidth+1)*5);
		drawView.setFontSize(40+fWidth*20);
        
        findViewById(R.id.bEditPage).setOnClickListener(this);
        findViewById(R.id.bToolColor).setOnClickListener(this);
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
        sessionId = prefs.getString("sessionId", "");
        
        appSettings.sessionId=sessionId;
 	    appSettings.userMail=userMail;
 	    appSettings.userPass=userPass;
        
        
        
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        
        if((Intent.ACTION_VIEW.equals(action) || Intent.ACTION_SEND.equals(action)) && type != null){
        	if (type.startsWith("image/")) {
        		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        		if (imageUri==null) imageUri=intent.getData();
        		if (imageUri!=null){
        			String url = Uri.decode(imageUri.toString()); 
        			ContentResolver cr = getContentResolver();
        			InputStream is;
        			
        			try {
        				is = cr.openInputStream(Uri.parse(url));
        				Bitmap bmp = BitmapFactory.decodeStream(is);
        				if (bmp.getWidth()!=-1 && bmp.getHeight()!=-1)
        					drawView.setBitmap(bmp,0);
        			}
        			catch (Exception e) {
        				appSettings.appendLog("paint:onCreate  "+e.getMessage());
        			}
        		}
            } 
        }
        else  {
        	String filePath = getIntent().getExtras().getString("path");
        	boolean isTemp = getIntent().getExtras().getBoolean("temp");
        	domain =  getIntent().getExtras().getString("domain");
        	if(domain==null) domain = serverHelper.getDate();
        	
        	if(filePath.contains("://")){
        		Bitmap bmp=LoadImageFromWeb(filePath);
        		if (bmp!=null) {
        			drawView.setBitmap(bmp,0);
        		}
        	}
        	else {
        		File file = new File(filePath);
        		if (file.exists()){
        			try{
        				int orient = getOrientationFromExif(filePath);
        				Bitmap bmp = decodeSampledBitmap(filePath,1000,1000);
        				if (bmp!=null){
        					drawView.setBitmap(bmp,orient);
        				}
        			}
        			catch(Exception e){
        				appSettings.appendLog("paint.onCreate()  "+e.getMessage());
        			}
        			if (isTemp) file.delete();
        		}
        	}
        }  
          
          
        drawView.setBackgroundColor(Color.WHITE);
        drawView.requestFocus();
        drawView.setColour(dColor);
        drawView.setSelChangeListener(new shapeSelectionListener(){

			@Override
			public void onSelectionChanged(int shSize, int fSize, int shColor) {
				setSelectedFoot(0);
				updateColorDialog(shSize!=-1?(shSize/5)-1:dWidth, fSize!=-1?(fSize-40)/20:fWidth, shColor!=0?colorToId(shColor):dColor);
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
        	
        });
        
        switch (dColor) {
		case 2:
			((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_blue);
			break;
		case 5:
			((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_red);
			break;
		case 3:
			((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_yellow);
			break;
		case 0:
			((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_white);
			break;
		case 7:
			((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_black);
			break;

		default:
			break;
		}
   	
    }
    
    private static int getOrientationFromExif(String imagePath) {
        int orientation = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 
                    ExifInterface.ORIENTATION_NORMAL);

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = 270;

                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = 180;

                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = 90;

                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                    orientation = 0;

                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            
        }

        return orientation;
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    	if (prefs==null) prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	serverHelper.getInstance().setCallback(this,this);
    	if (sessionId.length()==0) sessionId = prefs.getString("sessionId", "");
    	serverHelper.getInstance().setSessionId(sessionId);
    	exitOnComplete = false;
    }
    
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

        final int halfHeight = height / 2;
        final int halfWidth = width / 2;

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than the requested height and width.
        while ((halfHeight / inSampleSize) > reqHeight
                && (halfWidth / inSampleSize) > reqWidth) {
            inSampleSize *= 2;
        }
    }

    	return inSampleSize;
    }
    
    
    public static Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path,options);
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
                		drawView.startEdit();
                		setSelectedFoot(0);
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
            			if (!saved) showDialog(0);
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
    
    
    private Bitmap LoadImageFromWeb(String url){
        try{
        	String encodedurl = url.replace(" ", "%20");
        	InputStream is = (InputStream) new URL(encodedurl).getContent();
        	Bitmap d = BitmapFactory.decodeStream(is);
        	return d;
        }
        catch (Exception e) {
        	e.printStackTrace();
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
		((SeekBar)findViewById(R.id.seekBarType)).setProgress(fw);
		(( TextView )findViewById(R.id.tvTextType)).setText(String.format("%d",40+fw*20));
		switch (cid){
			case 2:
				((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_blue);	
				break;
			case 5:
				((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_red);	
				break;
			case 3:
				((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_yellow);	
				break;
			case 0:
				((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_white);	
				break;
			case 7:
				((ImageButton)findViewById(R.id.bToolColor)).setImageResource(R.drawable.icon_color_black);	
				break;	
				
		}
    }
    
    private void showSettings()
    {
    	Intent i = new Intent(getApplicationContext(), LoginDlg.class);
    	i.putExtra("userMail", userMail==null?"":userMail);
    	startActivityForResult(i, 3);
    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	return true;
    }
    
    private void setSelectedFoot(int id){
    	ImageButton ib;
    	for(int i=0; i<buttons.length; i++){
    		ib = (ImageButton)findViewById(buttons[i]);
    		if(i==id) {
    			ib.setSelected(true);
    		}
    		else {
    			ib.setSelected(false);
    		}
    	}
    }
    
    public void onClick(View v)
    {
    	ImageButton b = (ImageButton)findViewById(R.id.bToolColor);
    	ImageButton bs = (ImageButton)findViewById(R.id.bToolShape);
    	int color = -1;
    	switch(v.getId()){
    	case R.id.bSave2Nimbus:
    		drawView.deselectShapes();
    		drawView.hideCrop();
    		((ImageButton)findViewById(R.id.bToolCrop)).setSelected(false);
    		drawView.startEdit();
    		setSelectedFoot(0);
    		if (sessionId.length() == 0) showSettings();
    		else {
    			v.postDelayed(new Runnable() {
        			@Override
        			public void run() {
        				sendShot();
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
    		((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(0);
    		break;
    	case R.id.bToolShape:
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
    		setSelectedFoot(3);
    		}
    		else{
    			findViewById(R.id.color_menu).setVisibility(View.GONE);
    			drawView.hideCrop();
        		drawView.startEdit();
        		setSelectedFoot(0);
    		}
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
    	case R.id.bColor1:
    		drawView.setColour(2);
    		color=2;
    		b.setImageResource(R.drawable.icon_color_blue);
    		findViewById(R.id.color_menu).setVisibility(View.GONE);
    		break;
    	case R.id.bColor2:
    		drawView.setColour(5);
    		color=5;
    		b.setImageResource(R.drawable.icon_color_red);
    		findViewById(R.id.color_menu).setVisibility(View.GONE);
    		break;
    	case R.id.bColor3:
    		drawView.setColour(3);
    		color=3;
    		b.setImageResource(R.drawable.icon_color_yellow);
    		findViewById(R.id.color_menu).setVisibility(View.GONE);
    		break;
    	case R.id.bColor4:
    		drawView.setColour(0);
    		color=0;
    		b.setImageResource(R.drawable.icon_color_white);
    		findViewById(R.id.color_menu).setVisibility(View.GONE);
    		break;
    	case R.id.bColor5:
    		drawView.setColour(7);
    		color=7;
    		b.setImageResource(R.drawable.icon_color_black);
    		findViewById(R.id.color_menu).setVisibility(View.GONE);
    		break;
    	case R.id.bUndo:
    		drawView.undo();
    		break;
    	case R.id.bHome:
    		if (!saved) showDialog(0);
			else 
				{
					drawView.recycle();
					finish();
				}
    		
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
    		((ViewAnimator)findViewById(R.id.top_switcher)).setDisplayedChild(1);
    		
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
    	if(color!=-1){
    		dColor= color;
    		Editor e = prefs.edit();
    		e.putInt("dColor",dColor);
    		e.commit();
    	}
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
    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    }
    
    private void sendRequest(String action, String data){
		serverHelper.getInstance().sendRequest(action, data,"");
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// if statement prevents force close error when picture isn't selected
    	if (requestCode==3){
    		if (resultCode==RESULT_OK || resultCode==RESULT_FIRST_USER){
    			userMail=data.getStringExtra("userMail");
    			userPass=data.getStringExtra("userPass");
    			if(resultCode==RESULT_OK)	sendRequest("user:auth", String.format("\"email\":\"%s\",\"password\":\"%s\"",userMail,userPass));
    			else serverHelper.getInstance().sendOldRequest("user_register", String.format("{\"action\": \"user_register\",\"email\":\"%s\",\"password\":\"%s\",\"_client_software\": \"ff_addon\"}",userMail,userPass), "");
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
    
    private void sendShot(){
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
    }
    
    private void showShotSuccess(String id)
    {
    	Intent i = new Intent(getApplicationContext(), ShotSuccess.class);
    	i.putExtra("id", id);
    	startActivityForResult(i, 4);
    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    }
    
    private void showShareDlg(String url)
    {
    	Intent i = new Intent(getApplicationContext(), ArticleDlg.class);
    	i.putExtra("url", url);
    	startActivity(i);
    	overridePendingTransition( R.anim.slide_in_up, R.anim.slide_out_up );
    }
    
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
            			sessionId = root.getJSONObject("body").getString("sessionid");
            			serverHelper.getInstance().setSessionId(sessionId);
            			appSettings.storeUserData(ctx, userMail, userPass, sessionId); 
            			Toast.makeText(getApplicationContext(), "user authorized", Toast.LENGTH_LONG).show();
            			sendShot();
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
            		}
            	}
            	else Toast.makeText(getApplicationContext(), String.format("Error: %s",serverHelper.errorMsg(error)), Toast.LENGTH_LONG).show();
            }
            catch (Exception Ex){
            }
    }
    
    private class CompressTask extends AsyncTask<Bitmap, Void, String>{
		private ProgressDialog pd;
		private int mode =0;
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
			String filename=String.valueOf(System.currentTimeMillis())+"-fvd."+(saveFormat==0?"png":"jpeg");
			try{
			if(params[0]!=null){
				 
				if(this.mode<2){ 
					File file = new File(appSettings.getSavingPath(),filename);              
					file.createNewFile();
					FileOutputStream ostream = new FileOutputStream(file);
					params[0].compress(saveFormat==0?CompressFormat.PNG:CompressFormat.JPEG, 90, ostream);
					ostream.flush();
					ostream.close();
					if(this.mode==0){
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
				}
				else {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					params[0].compress(saveFormat==0?CompressFormat.PNG:CompressFormat.JPEG, 90, bos);
	     			this.buff = bos.toString("iso-8859-1");
				}
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
		   
		   
		   pd.dismiss();
		   if(this.mode==-1){
			   Toast.makeText(getApplicationContext(), this.buff, Toast.LENGTH_LONG).show();
		   }
		   else
		   if (this.mode==1){
			   Intent share = new Intent(Intent.ACTION_SEND);
			   share.setType("image/"+(saveFormat==0?"png":"jpeg"));
			   share.putExtra(Intent.EXTRA_STREAM, Uri.parse(String.format("file://%s/%s",appSettings.getSavingPath(),result)/*appSettings.getInstance(null).getSavingPath()+filename*/));
			   startActivity(Intent.createChooser(share, "Share Image"));
		   }
		   else if(this.mode==2){
			   serverHelper.getInstance().uploadShot((domain!=null && domain!="")?String.format("Screenshot from %s", domain):"Screenshot", buff);
		   }
		   else 
			   Toast.makeText(getApplicationContext(), "Saved to "+appSettings.getSavingPath(), Toast.LENGTH_LONG).show();
		   saved = true;
		   if (exitOnComplete) finish();
		}
		
	}
    

}
