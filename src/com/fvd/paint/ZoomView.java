package com.fvd.paint;

import com.fvd.nimbus.R;
import com.fvd.utils.appSettings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.View.MeasureSpec;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

public class ZoomView extends View {

    static protected final boolean ScaleAtFocusPoint = false;
    static final int maxSize=5000;
    int deviceOrientation=0;
    protected Bitmap background;
    protected Bitmap cache;
    protected Canvas cacheCanvas;
    protected Context mContext;
    protected float mPosX;
    protected float mPosY;
    protected float mFocusX;    // these two focus variables are not needed
    protected float mFocusY;
    protected float minScale = 1f;
    protected float maxScale = 5f;
    protected float defScale = 1f;
    protected float saveScale = 1f;
    private int displayHeight;
	private int displayWidth;
    private Rect cRect = new Rect();
    protected Rect dRect = new Rect();
    protected Rect cropRect=new Rect();
    protected RectF dstRect=new RectF();
    float lShift=0;
    float tShift =0;

    float angle = 0f;
    
    
    int viewWidth = 0;
    int viewHeight = 0;

    
    protected Paint paint = new Paint();
    
    protected float mLastTouchX;
    protected float mLastTouchY;
    
protected static final int INVALID_POINTER_ID = -1;


protected int mActivePointerId = INVALID_POINTER_ID;

protected ScaleGestureDetector mScaleDetector;
protected GestureDoubleTap gestureDoubleTap;
protected GestureDetector gestureDetector;
protected boolean mSupportsPan = false;
protected boolean mSupportsZoom = true;
protected boolean mSupportsScaleAtFocus = true;


/**
 */
public ZoomView (Context context) {
    this(context, null, 0);
}

public ZoomView (Context context, AttributeSet attrs) {
    this(context, attrs, 0);
}

public ZoomView (Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mContext = context;
    //getDispGeometry(context);
    setupToDraw (context, attrs, defStyle);
    setupScaleDetector (context, attrs, defStyle);
}

/**
 * Calculate the inSampleSize to use in BitmapFactory.Options in order
 * to load a drawable resource into a bitmap of the specified size.
 */

public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {
        if (width > height) {
            inSampleSize = Math.round((float)height / (float)reqHeight);
        } else {
            inSampleSize = Math.round((float)width / (float)reqWidth);
        }
    }
    return inSampleSize;
}

/**
 * Decode a resource into a bitmap of the specified size.
 */

public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
        int reqWidth, int reqHeight) {

    // First decode with inJustDecodeBounds=true to check dimensions
    final BitmapFactory.Options options = new BitmapFactory.Options();
    options.inJustDecodeBounds = true;
    BitmapFactory.decodeResource(res, resId, options);

    // Calculate inSampleSize
    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

    // Decode bitmap with inSampleSize set
    options.inJustDecodeBounds = false;
    return BitmapFactory.decodeResource(res, resId, options);
}

/**
 * Do whatever drawing is appropriate for this view.
 * The canvas object is already set up to be drawn on. That means that all translations and scaling
 * operations have already been done.
 * 
 * @param canvas Canvas
 * @return void
 */

public void drawOnCanvas (Canvas canvas) {
    
}

public void minScaleChanged() {
	
}

public boolean onTouch (MotionEvent ev) {
    return false;
}

public boolean onZoom (ScaleGestureDetector detector) {
    return false;
}


/**
 * onDraw
 */
@Override
public void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if(cacheCanvas!=null){
    	cacheCanvas.save();
    	try{
	    	cacheCanvas.drawColor(Color.WHITE);
	    	if(background!=null) cacheCanvas.drawBitmap(background,0f,0f, paint);
	    	drawOnCanvas(cacheCanvas);
    	}
    	catch (Exception e){}
    	cacheCanvas.restore();
    }
    canvas.save();
    try{
	    canvas.drawColor(Color.DKGRAY);
	    canvas.getClipBounds(cRect);
	   	canvas.translate(mPosX, mPosY);
	   	canvas.scale(saveScale, saveScale);
	    //canvas.drawBitmap(background, cropRect, dstRect, paint);
	   	
	   	if (cache!=null) canvas.drawBitmap(cache, cropRect, dstRect, paint);
	    canvas.getClipBounds(dRect);
	    //drawOnCanvas (canvas);
    }
    catch (Exception e){}
    
    canvas.restore();
}

/**
 * Handle touch and multitouch events so panning and zooming can be supported.
 *
 */
void fixCoords(){
	int w = cropRect.right-cropRect.left;
	int h = cropRect.bottom - cropRect.top;
	if(mPosX<-(w+2*lShift)*saveScale+displayWidth) mPosX=-(w+2*lShift)*saveScale+displayWidth;
    else if (mPosX>0) mPosX=0;
    if(mPosY<-(h+2*tShift)*saveScale+displayHeight) mPosY=-(h+2*tShift)*saveScale+displayHeight;
    else if(mPosY>0) mPosY=0;

}

@Override 
public boolean onTouchEvent(MotionEvent ev) {

	mScaleDetector.onTouchEvent(ev);
	gestureDetector.onTouchEvent(ev);
    if (onTouch(ev)) return true;
    
    final int action = ev.getAction();
    switch (action & MotionEvent.ACTION_MASK) {
    case MotionEvent.ACTION_DOWN: {
        final float x = ev.getX();
        final float y = ev.getY();
        mLastTouchX = x;
        mLastTouchY = y;
        mActivePointerId = ev.getPointerId(0);
        break;
    }
        
    case MotionEvent.ACTION_MOVE: {
        final int pointerIndex = ev.findPointerIndex(mActivePointerId);
        if(pointerIndex!=-1){
        final float x = ev.getX(pointerIndex);
        final float y = ev.getY(pointerIndex);

        if(saveScale>minScale||Math.abs(x - mLastTouchX)+Math.abs(y - mLastTouchY)>10){
        		float dx=0;
        		float dy =0;
        	if (!mScaleDetector.isInProgress()) {
        		dx = x - mLastTouchX;
        		dy = y - mLastTouchY;
        	}
        	mPosX += dx;
    		mPosY += dy;
    		
        	fixCoords();
        	invalidate();
            
        }
        	mLastTouchX = x;
        	mLastTouchY = y;
        }

        break;
    }
    
    case MotionEvent.ACTION_UP: {
        mActivePointerId = INVALID_POINTER_ID;
        break;
    }
        
    case MotionEvent.ACTION_CANCEL: {
        mActivePointerId = INVALID_POINTER_ID;
        break;
    }
    case MotionEvent.ACTION_POINTER_DOWN: 
    	/*pivotX= dRect.left + 0.5f*(ev.getX(0)+ev.getX(1))/saveScale;
    	pivotY= dRect.top + 0.5f*(ev.getY(0)+ev.getY(1))/saveScale;*/
    	invalidate();
    	break;
    
    case MotionEvent.ACTION_POINTER_UP: {
    	mActivePointerId = INVALID_POINTER_ID;
       /* final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastTouchX = ev.getX(newPointerIndex);
            mLastTouchY = ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }*/
        break;
    }
    }
    
    return true;
}

/**
 * Return the resource id of the sample image.
 * 
 * @return int
 */


/**
 * This method sets up the scale detector object used by the view. It is called by the constructor.
 * 
 * @return void
 */

protected void setupScaleDetector (Context context, AttributeSet attrs, int defStyle) {
    // Create our ScaleGestureDetector
    mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    gestureDoubleTap = new GestureDoubleTap();
    gestureDetector = new GestureDetector(context, gestureDoubleTap);
}

/**
 * This method performs whatever set up is necessary to do drawing. It is called by the constructor.
 * The default implementation checks to see if both panning and zooming are supported.
 * And it also locates the sample drawable resource by calling sampleDrawableId.
 * If that method returns 0, the sample image is not set up.
 * 
 * @return void
 */

@SuppressLint("NewApi")
protected void setupToDraw (Context context, AttributeSet attrs, int defStyle) {
	
	try{
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
	}
	catch (Exception e) {

	}
		mSupportsPan = supportsPan ();
		mSupportsZoom = supportsZoom ();
		mSupportsScaleAtFocus = supportsScaleAtFocusPoint ();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
}

/**
 * superOnDraw - call the onDraw method of the superclass of PanZoomView.
 * Use this if you want to replace the onDraw defined here in PanZoomView.
 */


protected void superOnDraw(Canvas canvas) {
   // super.onDraw(canvas);
}
    
/**
 * Return true if panning is supported.
 * 
 * @return boolean
 */

public boolean supportsPan () {
    return true;
}

/**
 * Return true if scaling is done around the focus point of the pinch.
 * 
 * @return boolean
 */

public boolean supportsScaleAtFocusPoint () {
    return ScaleAtFocusPoint;
}

/**
 * Return true if pinch zooming is supported.
 * 
 * @return boolean
 */

public boolean supportsZoom () {
    return true;
}

@Override
protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	displayWidth = MeasureSpec.getSize(widthMeasureSpec);
	displayHeight = MeasureSpec.getSize(heightMeasureSpec);
    int orientation = getResources().getConfiguration().orientation;

    //if(deviceOrientation!=orientation) 
    	setupCanvas(displayWidth, displayHeight, orientation);
    setMeasuredDimension(displayWidth, displayHeight);
    
}

public void doScaleBegin(){
	
}

public void doScaleEnd(){
	
}

public Bitmap getBitmap(){
	return Bitmap.createBitmap(cache,(int)cropRect.left,(int)cropRect.top,cropRect.right-cropRect.left,cropRect.bottom-cropRect.top);
}

/**
 */
// Class definitions

/**
 * ScaleListener 
 *
 */

protected class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	@Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
    	doScaleBegin();
        return true;
    }
	
	@Override
    public void onScaleEnd(ScaleGestureDetector detector){
		doScaleEnd();
    }
	
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
    	if (onZoom(detector)) return true;
    	saveScale *= detector.getScaleFactor();
    	saveScale = Math.max(minScale, Math.min(saveScale, maxScale));
    	int newScrollX = (int)((-mPosX + detector.getFocusX()) * detector.getScaleFactor() - detector.getFocusX());
		int newScrollY = (int)((-mPosY + detector.getFocusY()) * detector.getScaleFactor() - detector.getFocusY());
		mPosX=-newScrollX;
		mPosY=-newScrollY;
		fixCoords();
        invalidate();
        return true;
    }
}

public class GestureDoubleTap extends GestureDetector.SimpleOnGestureListener {

    @Override
    public boolean onDoubleTap(MotionEvent e) {
    	onDoubleTaped(e);
       return false;
    }

}

public void onDoubleTaped(MotionEvent ev){
	
}

public void setAngle(float a){
	angle=a;
	Bitmap b=RotateBitmap(background,angle);
	setBitmap(b, 0);
	setupCanvas(displayWidth, displayHeight, deviceOrientation);
	invalidate();
}


protected void computeCropScale(int imageWidth, int imageHeight, int measuredWidth, int measuredHeight) {
	float wRatio = (float) imageWidth / (float) measuredWidth;
	float hRatio = (float) imageHeight / (float) measuredHeight;
	
	float fitScaleHorizontal = (float) measuredWidth / (float) imageWidth;
	float fitScaleVertical = (float) measuredHeight / (float) imageHeight;
	
	if(wRatio > hRatio) {
		saveScale = fitScaleHorizontal;
	}
	else {
		saveScale = fitScaleVertical;
	}
	  minScale = saveScale;
}

protected void setupCanvas(int measuredWidth, int measuredHeight, int orientation) {

	if(deviceOrientation != orientation) {
		deviceOrientation = orientation;
	}

	if(background != null) {
		int bWidth = cropRect.right-cropRect.left;
    	int bHeight = cropRect.bottom - cropRect.top;
    	int bW = 0;
		int bH = 0;
		if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
			float ratio = (float) bWidth / (float) bHeight;
			float dispratio = (float)measuredWidth / (float)measuredHeight;
			if(dispratio>=ratio){
				bH = measuredHeight;
				bW = (int)(bH*ratio);
			}
			else {
				bW = measuredWidth;
				bH = (int)(bW/ratio);
			}
		}
		else {
			
			float ratio = (float) bHeight / (float) bWidth;
			float dispratio = (float)measuredHeight /(float) measuredWidth;
			if(dispratio>=ratio){
				bW = measuredWidth;
				bH = (int)(bW*ratio);
			}
			else {
				bH = measuredHeight;
				bW = (int)(bH/ratio);
			}
		}
				
			minScale = Math.min((float)bW/bWidth, (float)bH/bHeight);
			if(defScale==-1) defScale = minScale; 
			saveScale = minScale;
			lShift = (measuredWidth - bW)* 0.5f/minScale;
			tShift = (measuredHeight - bH)* 0.5f/minScale;
			dstRect.set(lShift,tShift,lShift+bWidth, tShift + bHeight);
			mPosX=0;
			mPosY=0;
		
		
		minScaleChanged();
	}
}

public void setBitmap(Bitmap b, int ra){
	float scale = 1f;
	boolean rotate = ra!=0;
	int w = b.getWidth();
	int h = b.getHeight();
	
	if(w>=h && w>maxSize){
		scale =  (float)maxSize/w;
		h=Math.round(h*maxSize/w);
		w=maxSize;
	}
	else if(h>w && h>maxSize){
		scale = (float)maxSize/h;
		w=w*maxSize/h;
		h=maxSize;
	}
	
	
	
	if(scale!=1 || rotate){
		background = PrepareBitmap(b, scale, (float)ra/*rotate?90.0f:0f*/);// rotate?RotateBitmap(b, 90.0f):b;
		b.recycle();
	}
	else background = b;
	cropRect.set(0,0,background.getWidth(),background.getHeight());
	saveScale = -1;
	defScale = -1;
	mPosX=0;
	mPosY=0;
	 
	if (cache!=null) cache.recycle();
	cache=Bitmap.createBitmap(background.getWidth(), background.getHeight(), background.getConfig());
	cacheCanvas= new Canvas(cache);
	return;
	
}

public void recycle() {
	if(background!=null) background.recycle();
	if(cache!=null) cache.recycle();
	cacheCanvas=null;
}

public boolean setCrop(int l, int t, int w, int h){
	/*l+=cropRect.left;
	t+=cropRect.top;
	if(l<0){w+=l;l=0;}
	if(t<0){h+=t;t=0;}
	w=Math.max(0, Math.min(w,background.getWidth()));
	h=Math.max(0,Math.min(h,background.getHeight()));*/
	if(l<cropRect.left) l=cropRect.left;
	if(t<cropRect.top) t=cropRect.top;
	if(l+w>cropRect.right) w=cropRect.right-l;
	if(t+h>cropRect.bottom) h=cropRect.bottom-t;
	if(h*w==0) return false;
	cropRect.set(l,t,l+w,t+h);
	mPosX=0;
	mPosY=0;
	setupCanvas(displayWidth, displayHeight, deviceOrientation);
	invalidate();
	return true;
}

public void resetCrop(){
	/*cropRect.set(0,0,0,0);
	setCrop(0, 0, background.getWidth(), background.getHeight());*/
	cropRect.set(0,0,background.getWidth(), background.getHeight());
	mPosX=0;
	mPosY=0;
	setupCanvas(displayWidth, displayHeight, deviceOrientation);
	invalidate();
}

public void zoom(float factor) {
	saveScale *= factor;
	saveScale = Math.max(minScale, Math.min(saveScale, maxScale));
	int newScrollX = (int)((-mPosX + viewWidth/2f) * factor - viewWidth/2f);
	int newScrollY = (int)((-mPosY + viewHeight/2f) * factor- viewHeight/2f);
	mPosX=-newScrollX;
	mPosY=-newScrollY;
	fixCoords();
    invalidate();
}

public static Bitmap RotateBitmap(Bitmap source, float angle)
{
      Matrix matrix = new Matrix();
      matrix.postRotate(angle);
      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
}

public static Bitmap ResizeBitmap(Bitmap source, float scale)
{
      Matrix matrix = new Matrix();
      matrix.postScale(scale, scale);
      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
}

public static Bitmap PrepareBitmap(Bitmap source, float scale, float angle)
{
	Matrix matrix = new Matrix();
    if(scale!=1) matrix.postScale(scale, scale);
    if(angle!=0) matrix.postRotate(angle);
    return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	
}
}
