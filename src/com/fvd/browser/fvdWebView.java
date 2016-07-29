package com.fvd.browser;


import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
//import android.util.Log;
//import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings.PluginState;
import android.widget.Toast;

//import android.widget.Toast;


import com.fvd.classes.BugReporter;
import com.fvd.nimbus.R;
import com.fvd.utils.appSettings;



public class fvdWebView extends WebView implements TextSelectionJavascriptInterfaceListener, 
OnTouchListener, OnLongClickListener{
	/** The logging tag. */
	private static final String TAG = "fvdWebView";

	/** Context. */
	protected	Context	ctx;
	
	private boolean injected = false;
	
	/** The previously selected region. */
	protected Region lastSelectedRegion = null;
	
	/** The selected range. */
	protected String selectedRange = "";
	
	/** The selected text. */
	protected String selectedText = "";
	
	/** Javascript interface for catching text selection. */
	protected TextSelectionJavascriptInterface textSelectionJSInterface = null;
	
	/** Selection mode flag. */
	protected boolean inSelectionMode = false;
	
	private static SharedPreferences prefs = null;
	
	/** Flag to stop from showing context menu twice. */
	
	
	/** The current content width. */
	protected int contentWidth = 0;
	
	private float scale = 0;
	Paint fpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	/** Identifier for the selection start handle. */
	private final int SELECTION_START_HANDLE = 0;
	
	/** Identifier for the selection end handle. */
	private final int SELECTION_END_HANDLE = 1;
	
	/** Last touched selection handle. */
	private int mLastTouchedSelectionHandle = -1;
	private String defUserAgent; 
	private int count = 0;
	
	private Rect mSelectionRect = new Rect();
	private Rect selRect = new Rect();
	
	private fvdBrowerEventsListener eventsHandler = null;
	
	public boolean getInjected(){
		return this.injected;
	}
	
	public fvdWebView(Context context) {
		super(context);
		
		this.ctx = context;
		this.setup(context);
	}
	
	public fvdWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		this.ctx = context;
		this.setup(context);
		
	}

	public fvdWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.ctx = context;
		this.setup(context);
	
	}	
	
	public void setEventsHandler(fvdBrowerEventsListener handler){
		this.eventsHandler = handler;
	}
	
	public void selectVisible(){
		if(this.injected){
			this.eval("javascript:android.selectVisible();");
			//Log.e("fvdWebView","selectVisible");
		}
	}
	
	public void savePage(){
		if(this.injected){
			this.eval("javascript:android.savePage();");
			//Log.e("fvdWebView","savePage");
		}
	}
	
	public void setCanClip(boolean clip) {
		if(this.injected){
			this.eval(String.format("javascript:android.setClip(%s)",clip==true?"true":"false"));
		}
	}
	
	//*****************************************************
		//*
		//*		Touch Listeners
		//*
		//*****************************************************
		
	private boolean mScrolling = false;
	private float mScrollDiffY = 0;
	private float mLastTouchY = 0;
	private float mScrollDiffX = 0;
	private float mLastTouchX = 0;

	@Override
	protected void onDraw (Canvas canvas){
		super.onDraw(canvas);
		if(this.selRect.right-this.selRect.left>0){
			float scale = getDensityIndependentValue(this.getScale(), ctx);
			this.mSelectionRect.left = (int) (getDensityDependentValue(selRect.left, getContext()) * scale);
			this.mSelectionRect.top = (int) (getDensityDependentValue(selRect.top, getContext()) * scale);
			this.mSelectionRect.right = (int) (getDensityDependentValue(selRect.right, getContext()) * scale);
			this.mSelectionRect.bottom = (int) (getDensityDependentValue(selRect.bottom, getContext()) * scale);
			//updateDrawRect();
			if(this.mSelectionRect.left<3) this.mSelectionRect.left=3;
			canvas.drawRect(this.mSelectionRect, fpaint);
		}
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		scale = getDensityIndependentValue(this.getScale(), ctx);
		if (!this.injected){
			return false;
		}
		
		float xPoint = getDensityIndependentValue(event.getX(), ctx) / getDensityIndependentValue(this.getScale(), ctx);
		float yPoint = getDensityIndependentValue(event.getY(), ctx) / getDensityIndependentValue(this.getScale(), ctx);
		
		// TODO: Need to update this to use this.getScale() as a factor.
		
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			
			String startTouchUrl = String.format("javascript:android.startTouch(%d, %d);", (int)xPoint, getHeight());
			
			mLastTouchX = xPoint;
			mLastTouchY = yPoint;
			
			this.eval(startTouchUrl);
			eventsHandler.onTap();
			
			
		}
		else if(event.getAction() == MotionEvent.ACTION_UP){
		
			mScrollDiffX = 0;
			mScrollDiffY = 0;
			mScrolling = false;
			
		}
		else if(event.getAction() == MotionEvent.ACTION_MOVE){
			
			mScrollDiffX += (xPoint - mLastTouchX);
			mScrollDiffY += (yPoint - mLastTouchY);
			
			mLastTouchX = xPoint;
			mLastTouchY = yPoint;
			
			
			// Only account for legitimate movement.
			if(Math.abs(mScrollDiffX) > 10 || Math.abs(mScrollDiffY) > 10){
				mScrolling = true;
			}
		}
		
		// If this is in selection mode, then nothing else should handle this touch
		return false;
	}
	
	@SuppressLint("NewApi")
	public void eval(String code){
		 int currentapiVersion = android.os.Build.VERSION.SDK_INT;
		 if (currentapiVersion < Build.VERSION_CODES.KITKAT) {
             this.loadUrl(code);
             } 
		 else {
            	this.evaluateJavascript(code.replace("javascript:", ""),null); 
             }
	}
	
	@Override 
	public boolean onLongClick(View v){
		
		if (this.injected){
			try{
				this.eval("javascript:android.longTouch();");
				this.mScrolling = true;
				return true;
			}
			catch (Exception e){
				return true;
			}
		}
		else {
			Toast.makeText(ctx, getContext().getString(R.string.wait_load), Toast.LENGTH_LONG).show();
			return true;
		}
		
	}
	
	public void navigate(String url){
		this.endSelectionMode();
		this.injected = false;
		if(!"about:blank".equals(url)){
			this.loadUrl(url);
		}
	}
	
	//*****************************************************
	//*
	//*		Setup
	//*
	//*****************************************************
	
	/**
	 * Setups up the web view.
	 * @param context
	 */
	protected void setup(Context context){
		
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		try{
			count = prefs.getInt("wbcount", 0);
		}
		catch(Exception e){
			count=0;
		}
		
		this.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress)
            {
            	super.onProgressChanged(view, progress);
            	if (eventsHandler!=null) eventsHandler.onProgressChanged(progress);
            	appSettings.appendLog(String.format("onProgressChanged: %d\r\n", progress));
            }
            
            @Override
		    public void onConsoleMessage(String message, int lineNumber, String sourceID) {
            	appSettings.appendLog(String.format("onConsoleMessage: %s\r\n", message));
		        if (eventsHandler!=null) eventsHandler.onConsoleMessage(message, lineNumber, sourceID);
		        super.onConsoleMessage(message, lineNumber, sourceID);
		    }

		    @Override
		    public boolean onConsoleMessage(ConsoleMessage cm) {
		    	if (eventsHandler!=null) eventsHandler.onConsoleMessage(cm);
		        return true;
		    }
		       
		    @Override  
		    public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result){  
		    	appSettings.appendLog(String.format("onJsAlert: %s\r\n", message));
		    	if (eventsHandler!=null) eventsHandler.onJsAlert(url, message, result);
		        return true;  
		    }; 
          });
        
        this.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) 
            {
            	appSettings.appendLog(String.format("onReceivedError: %s::%s\r\n", failingUrl, description));
            	if (eventsHandler!=null) eventsHandler.onReceivedError(errorCode, description, failingUrl);
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
            	boolean b=true;
            	if (eventsHandler!=null) b=eventsHandler.getCanBrowse();
            		if (!inSelectionMode && b) navigate(url);
            	return true;
            }
            
            
            public void onPageStarted (WebView view, String url, Bitmap favicon) {
            	appSettings.appendLog(String.format("onPageStarted: %s\r\n", url));
            	if (eventsHandler!=null) eventsHandler.onPageStarted(url, favicon);
            }
          
            public void onPageFinished (WebView view, String url) {
              appSettings.appendLog(String.format("onPageFinished: %s\r\n", url));
        	  if (eventsHandler!=null) eventsHandler.onPageFinished(url);
          }
        });


		
		//
        
		
		this.setOnLongClickListener(this);
		this.setOnTouchListener(this);
		// Webview setup
		this.getSettings().setJavaScriptEnabled(true);
		this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		this.getSettings().setPluginState(PluginState.ON_DEMAND);
		this.getSettings().setSavePassword(true);
		this.getSettings().setSaveFormData(true);
		this.getSettings().setLoadWithOverviewMode(true);
		this.getSettings().setUseWideViewPort(true);
		this.getSettings().setBuiltInZoomControls(true);
		this.getSettings().setDisplayZoomControls(false);
		this.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		this.setScrollbarFadingEnabled(false);
		defUserAgent =  this.getSettings().getUserAgentString();
		// Javascript interfaces
		this.textSelectionJSInterface = new TextSelectionJavascriptInterface(context, this);		
		this.addJavascriptInterface(this.textSelectionJSInterface, this.textSelectionJSInterface.getInterfaceName());
		
		
		// Set to the empty region
		Region region = new Region();
		region.setEmpty();
		this.lastSelectedRegion = region;
		fpaint.setAntiAlias(true);
		fpaint.setColor(Color.argb(200, 255, 25, 25));
		fpaint.setStyle(Paint.Style.STROKE);
		fpaint.setStrokeWidth(6);
	}
	
	
	//*****************************************************
	//*
	//*		Selection Layer Handling
	//*
	//*****************************************************
	
	/**
	 * Creates the selection layer.
	 * 
	 * @param context
	 */
	/**z
	 * Starts selection mode on the UI thread
	 */
	public void setUserAgent(String agent){
		if (agent!=null && agent.length()>0){
			if(!agent.equals(this.getSettings().getUserAgentString())){
				this.getSettings().setUserAgentString(agent);
				this.reload();
			}
		}
		else 
			if (!defUserAgent.equals(this.getSettings().getUserAgentString())){ 
			this.getSettings().setUserAgentString(defUserAgent);
			if(this.getUrl()!=null && this.getUrl().length()>0) 
				this.reload();
		}
	}
	
	private Handler startSelectionModeHandler = new Handler(){
		
		public void handleMessage(Message m){
			invalidate();
		}
		
	};
	
	/**
	 * Starts selection mode.
	 * 
	 * @param	selectionBounds
	 */
	public void startSelectionMode(){
		
		inSelectionMode=true;
		this.startSelectionModeHandler.sendEmptyMessage(0);
		//postInvalidate();
		
	}
	
	// Ends selection mode on the UI thread
	private Handler endSelectionModeHandler = new Handler(){
		public void handleMessage(Message m){
			invalidate();
			if(getParent() != null){
				// This will throw an error if the webview is being redrawn.
				// No error handling needed, just need to stop the crash.
				try{
					if (eventsHandler!=null) eventsHandler.onEndSelection();
				}
				catch(Exception e){
					
				}
			}
			mLastTouchedSelectionHandle = -1;
			eval("javascript: android.clearSelection();");
			
		}
	};
	
	public void saveArticle(){
		eval("javascript: android.saveArticle();");
	}
	
	public void ZoomInSelection(){
		eval("javascript: android.ZoomInSelection();");
	}
	
	public void ZoomOutSelection(){
		eval("javascript: android.ZoomOutSelection();");
	}
	/**
	 * Ends selection mode.
	 */
	public void endSelectionMode(){
		inSelectionMode=false;
		this.selRect.set(0,0,0,0);
		this.endSelectionModeHandler.sendEmptyMessage(0);
	}
	
	/**
	 * Checks to see if this view is in selection mode.
	 * @return
	 */
	public boolean isInSelectionMode(){
		return (inSelectionMode);
	}
	
		//*****************************************************
	//*
	//*		Text Selection Javascript Interface Listener
	//*
	//*****************************************************
	
	
	/**
	 * Shows/updates the context menu based on the range
	 */
	public void tsjiJSError(String error){
		Log.e("error", error);
	}
	
	
	/**
	 * The user has started dragging the selection handles.
	 */
	public void tsjiStartSelectionMode(){
		
		this.startSelectionMode();
		
		
	}
	
	/**
	 * The user has stopped dragging the selection handles.
	 */
	public void tsjiEndSelectionMode(){
		this.endSelectionMode();
	}
	
	/**
	 * The selection has changed
	 * @param range
	 * @param text
	 * @param handleBounds
	 * @param menuBounds
	 * @param showHighlight
	 * @param showUnHighlight
	 */
	public void tsjiSelectionChanged(String range, String text, String handleBounds, String menuBounds){
		
		if(handleBounds==null){
			if(range!=null){
				if(range.equals("action:savePage") || range.equals("action:saveArticle") || range.equals("action:reset")){
					if (eventsHandler!=null) eventsHandler.onSelectionChanged(range, text, null);
					endSelectionMode();
				}
			}
		}
		else
		try {
			
			JSONObject menuBoundsObject = new JSONObject(handleBounds);
			selRect.left =menuBoundsObject.getInt("left");
			selRect.top = menuBoundsObject.getInt("top");
			selRect.right = menuBoundsObject.getInt("right");
			selRect.bottom = menuBoundsObject.getInt("bottom");
			
			
			this.selectedRange = range;
			this.selectedText = text;
			if(!this.isInSelectionMode()){
				this.startSelectionMode();
			}
			// This will send the menu rect
			if (eventsHandler!=null) eventsHandler.onSelectionChanged(range, text, this.selRect);
			startSelectionMode();
			
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			BugReporter.Send("tsjiSelectionChanged", e.getMessage());
			e.printStackTrace();
		}
		
		
	}
	
	
	/**
	 * Receives the content width for the page.
	 */
	public void tsjiSetContentWidth(float contentWidth){
		this.contentWidth = (int) this.getDensityDependentValue(contentWidth, ctx);
	}
	
	public void tsjiInjected(){
		this.injected = true;
	}

	
	//*****************************************************
	//*
	//*		Density Conversion
	//*
	//*****************************************************
	
	/**
	 * Returns the density dependent value of the given float
	 * @param val
	 * @param ctx
	 * @return
	 */
	public float getDensityDependentValue(float val, Context ctx){
		
		// Get display from context
		Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		// Calculate min bound based on metrics
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		
		return val * (metrics.densityDpi / 160f);
		
	}
	
public void updateDrawRect(){
		
		// Get display from context
		Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		// Calculate min bound based on metrics
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		
		float scale = this.getScale()*(metrics.densityDpi / 160f);
		
		//return val * (metrics.densityDpi / 160f);
		this.mSelectionRect.left = (int) ((selRect.left*(metrics.densityDpi / 160f)) * scale);
		this.mSelectionRect.top = (int) ((selRect.top*(metrics.densityDpi / 160f))* scale);
		this.mSelectionRect.right = (int) ((selRect.right*(metrics.densityDpi / 160f)) * scale);
		this.mSelectionRect.bottom = (int) ((selRect.bottom*(metrics.densityDpi / 160f)) * scale);
		
		
		
	}
	
	public int contentHeight()
	{
		return this.getContentHeight();
		
	}

	/**
	 * Returns the density independent value of the given float
	 * @param val
	 * @param ctx
	 * @return
	 */
	public float getDensityIndependentValue(float val, Context ctx){
		
		// Get display from context
		Display display = ((WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		
		// Calculate min bound based on metrics
		DisplayMetrics metrics = new DisplayMetrics();
		display.getMetrics(metrics);
		return val / (metrics.densityDpi / 160f);
	}
	
	private boolean mIsOverScrollEnabled = true;
    private OnScrollChangedListener mOnScrollChangedListener;
    
    public interface OnScrollChangedListener {
        void onScrollChanged(WebView who, int l, int t, int oldl, int oldt);
    }
    
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (mOnScrollChangedListener != null) {
            mOnScrollChangedListener.onScrollChanged(this, l, t, oldl, oldt);
        }
    }

    @Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY,
                                   int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(
                deltaX,
                deltaY,
                scrollX,
                scrollY,
                scrollRangeX,
                scrollRangeY,
                mIsOverScrollEnabled ? maxOverScrollX : 0,
                mIsOverScrollEnabled ? maxOverScrollY : 0,
                isTouchEvent);
    }

    // region Helper Methods
    public void setOnScrollChangedListener(OnScrollChangedListener listener) {
        mOnScrollChangedListener = listener;
    }

    public void setOverScrollEnabled(boolean enabled) {
        mIsOverScrollEnabled = enabled;
    }

    public boolean isOverScrollEnabled() {
        return mIsOverScrollEnabled;
    }
}