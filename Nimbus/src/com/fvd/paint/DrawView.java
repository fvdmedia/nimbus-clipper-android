package com.fvd.paint;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.fvd.nimbus.R;
import com.fvd.utils.shapeSelectionListener;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
//import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.ImageButton;


public class DrawView extends ZoomView {
	public enum ShapeType
    {
        FreeHand,
        Line,
        Rectangle,
        RoundRect,
        Circle,
        Oval,
        Arrow,
        PixelMask,
        Text,
        MaskRect,
        CropRect,
        Eraser,
        none
    }
	
	public enum Mode{
		NONE,
		DRAW,
		EDIT
	}
	
	ShapeType currShape = ShapeType.FreeHand; 
	
	private static final String TAG = "DrawView";
	List<Shape> shapes = new ArrayList<Shape>();
	ArrayList<Long> undoStack = new ArrayList<Long>();
	Paint paint;
	Random gen;
	Context ctx=null;
	int wid_mode;
    int fontSize=40;
    boolean stroke = true;
    static final int CLICK = 3;
    
    float shapeScale = 1f;
    float fixTransX, fixTransY;
    float[] m;
    int touchCount=0;
    float startX, startY;
    boolean shapeSelected = false;
    PointF touch=new PointF();
    PointF drag = new PointF();
    PointF ptShape = new PointF();
    int cH, cW;
    float shiftX = 0; 
    float shiftY = 0;
    int new_col = Color.RED;
    protected float origWidth, origHeight;
    int oldMeasuredWidth, oldMeasuredHeight;
    Mode mode = Mode.NONE;
    Mode prevMode = Mode.NONE;
    int pid=0;
    boolean canDrag=false;
    boolean zooming=false;
    boolean doCreate = false;
    boolean canDraw = false;
    int maskIndex=-1;
    //ScaleGestureDetector mScaleDetector;
   
    private long currentId=0;

    private shapeSelectionListener doSelChange=null;
   
    public DrawView(Context context) {
		super(context);
		ctx = context;
		wid_mode = 5;
		setFocusable(true);
		setFocusableInTouchMode(true);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		sharedConstructing(context);
	}
	
    
    
	public DrawView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		ctx = context;
		wid_mode = 5;
		setFocusable(true);
		setFocusableInTouchMode(true);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		sharedConstructing(context);
	}
	
	public DrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
		ctx = context;
		wid_mode = 5;
		setFocusable(true);
		setFocusableInTouchMode(true);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		paint.setDither(true);
		sharedConstructing(context);
	}
	
	public void setCanDraw(boolean b){
		canDraw =b;
	}
	
	public void setSelChangeListener(shapeSelectionListener listener){
		doSelChange=listener;
	}
	// used to clear the screen
	public void clearPoints () {
		shapes.clear();
		forceRedraw();
	}
	
	public boolean saved=false;
	public boolean hasChanged(){
		//return !(maskIndex<1&&shapes.size()==1);
		return !saved && (maskIndex>0||shapes.size()>1);
	}
	
	@Override
	public boolean canZoom(){
		return mode!=Mode.DRAW;
	}
	
	public boolean isDrawMode(){
		return mode==Mode.DRAW;
	}
	
	@Override
	public void recycle() {
		try{
		clear();
			super.recycle();
		} catch (Exception e){}
	}
	public void clear(){
		hideCrop();
		while(shapes.size()>1){
			shapes.remove(1);
		}
		maskIndex=0;
		if(shapes.get(0).undo())
			super.resetCrop();
		forceRedraw();
		
	}
	/**
	 * Force view to redraw. Without this points aren't cleared until next action
	 */
	public void forceRedraw() {
		//invalidate();
		postInvalidate();
	}
	
	
	@Override
	public void setAngle(float a){
		
		super.setAngle(a);
		/*float cx=getWidth()/2;
		float cy=getHeight()/2;
		for (Shape shape : shapes) {
			shape.rotate(a, cx,cy);
		}*/
		
				
		clear();
	}
	// used to set drawing colour
	public void setColour (int col_in) {
		/*switch (col_in) {
		case 0 : {
			new_col = Color.BLACK;
			break;
		}
		case 1 : {
			new_col = Color.BLUE;
			break;
		}
		case 2 : {
			new_col = Color.CYAN;
			break;
		}
		case 3 : {
			new_col = Color.GREEN;
			break;
		}
		case 4 : {
			new_col = Color.MAGENTA;
			break;
		}
		case 5 : {
			new_col = Color.RED;
			break;
		}
		case 6 : {
			new_col = Color.YELLOW;
			break;
		}
		default : {
			new_col = col_in;
			break;
		}
	}*/
		new_col = col_in;
		if (mode==Mode.EDIT){
			boolean b=false;
			for(Shape shape:shapes){
				if (shape.getSelected()) {
					pushUndo(shape.getUid());
					shape.setColor(new_col);
					b=true;
				}
			}
			if (b) forceRedraw();
		}
	}

	// used to set drawing width
	public void setWidth (int wid_in) {
		wid_mode = wid_in;
		if (mode==Mode.EDIT){
			boolean b=false;
			for(Shape shape:shapes){
				if (shape.getSelected()) {
					//Log.v("nimbus", "setWidth");
					pushUndo(shape.getUid());
					shape.setLineWidth(wid_in);
					b=true;
				}
			}
			if (b) forceRedraw();
		}
	}
	
	void pushUndo(long uid){
		if(undoStack.size()>=10) undoStack.remove(0);
		undoStack.add(uid);
	}
	
	public boolean canUndo(){
		return undoStack.size()>0;
	}
	
	public void undo(){
		if (undoStack.size()==0) return;
		long uid = undoStack.remove(undoStack.size()-1);
		int ix=-1;
		for (int i=0; i<shapes.size(); i++){
			if (shapes.get(i).getUid()==uid){
				ix=i;
				break;
			}
		}
		if (ix==-1){
			ix=shapes.size()-1;
		}
		if (ix!=-1){
			if (shapes.get(ix).undo()){
				if(shapes.get(ix).toString().contains("CropRect"))
					super.resetCrop();
			}
			else shapes.remove(ix);
			forceRedraw();
		}
		
		if(undoStack.size()==0 && shapes.size()>0){
			for(Shape s:shapes){
				try{
					s.clearStack();
				}
				catch (Exception e){
					//Log.e("nimbus",e.getMessage());
				}
			}
		}
		
	}
	
	int getSelectedShape(){
		int ix=-1;
		for (int i=0; i<shapes.size(); i++){
			if (shapes.get(i).getUid()==currentId){
				ix=i;
				break;
			}
		}
		return ix;
	}
	
	public static Bitmap RotateBitmap(Bitmap source, float angle)
	{
	      Matrix matrix = new Matrix();
	      matrix.postRotate(angle);
	      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}
	
	public void setFontSize(int fs){
		fontSize=fs;
		if (mode==Mode.EDIT){
			boolean b=false;
			for(Shape shape:shapes){
				if (shape.getSelected() && shape.toString().contains("Text")) {
					//Log.v("nimbus", "setFontSize");
					pushUndo(shape.getUid());
					((Text)shape).setFontSize(fs);
					b=true;
				}
			}
			if (b) forceRedraw();
		}
	}
	
	public void setText(String text, boolean stroke){
		if (text.length()!=0) {
			  if (mode==Mode.DRAW){
				  Text t= new Text(ctx, ptShape.x,ptShape.y,new_col, fontSize,text,minScale);
				  t.setStroke(stroke);
				  shapes.add(t);
			  }
			  else if (mode==Mode.EDIT){
				  for (Shape shape: shapes){
					  if (shape.getUid()==currentId && shape.toString().contains("Text")){
      					((Text)shape).setText(text);
      					((Text)shape).setStroke(stroke/*((boolean)textLayout.findViewById(R.id.bStroke).isSelected())*/);
      					break;
      				}
				  }
			  }
			  forceRedraw();
		  }
	}
	
	private void sharedConstructing(Context context) {
        
        
        this.ctx = context;
        mode=Mode.EDIT;
          
        maskIndex=0;
        shapes.add(0,new CropRect(BitmapFactory.decodeResource(getResources(), R.drawable.icon_thumb)));
   }
	
	
	public boolean hideCrop(){
		
		boolean res=false;
		if( currShape == ShapeType.CropRect && !((CropRect)shapes.get(0)).isComplete()){
			((CropRect)shapes.get(0)).reset(cropRect);
			((CropRect)shapes.get(0)).hide();

			invalidate();
			currShape=ShapeType.FreeHand;
			res=true;
		}
		return res;
	}
	
	public void setShape(int index){
		mode = Mode.DRAW;
		prevMode = mode;
		if( currShape == ShapeType.CropRect && index!=10 && !((CropRect)shapes.get(0)).isComplete()){
			((CropRect)shapes.get(0)).reset(cropRect);
			((CropRect)shapes.get(0)).hide();
			forceRedraw();
		}
		switch(index){
		case 0:
			currShape = ShapeType.FreeHand; 
			break;
		case 1:
			currShape = ShapeType.Line; 
			break;
		case 2:
			currShape = ShapeType.Rectangle; 
			break;
		case 3:
			currShape = ShapeType.Circle; 
			break;
		case 4:
			currShape = ShapeType.Oval; 
			break;
		case 5:
			currShape = ShapeType.RoundRect; 
			break;
		case 6:
			currShape = ShapeType.Arrow; 
			break;
		case 7:
			currShape = ShapeType.PixelMask; 
			//mask_all = false;
			break;
		case 10:
			
			if( currShape == ShapeType.CropRect && !((CropRect)shapes.get(0)).isComplete()) 
				{
					pushUndo(shapes.get(0).getUid());	
					((CropRect)shapes.get(0)).setCompete();
					if (super.setCrop(((CropRect)shapes.get(0)).getLeft()/*-(int)lShift*/,((CropRect)shapes.get(0)).getTop()/*-(int)tShift*/,((CropRect)shapes.get(0)).getWidth(),((CropRect)shapes.get(0)).getHeight()))
					{
						forceRedraw();
						currShape=ShapeType.FreeHand;
						mode=Mode.EDIT;
					}
				}
			else {
				currShape = ShapeType.CropRect;
				((CropRect)shapes.get(0)).reset(cropRect);
			}
			forceRedraw();
			//mask_all = false;
			break;	
		case 8:
			currShape = ShapeType.Text; 
			break;	
		case 9:
			if(currShape != ShapeType.MaskRect){
				currShape = ShapeType.MaskRect;
				maskIndex++;
				Bitmap bm= Bitmap.createBitmap(background, cropRect.left, cropRect.top, cropRect.right-cropRect.left, cropRect.bottom- cropRect.top, null, true);
				shapes.add(maskIndex,new MaskRect(0,0,getWidth(), getHeight(),bm,0,0));
				pushUndo(shapes.get(maskIndex).getUid());
				forceRedraw();
			}
			break;	
		case 11:
			currShape=ShapeType.Eraser;
			break;
		}
		deselectShapes();
	}
	
	public void deselectShapes(){
		boolean b =false;
		for (Shape shape : shapes) {
			if (shape.getSelected()){
				b =true;
				shape.select(false);
			}	
		}
		if (b) invalidate();//forceRedraw();
		shapeSelected=false;
		//currentId=0;
		currentId=0;
		
	}
	
	public void startEdit(){
		mode = Mode.EDIT;
		prevMode = mode;
	}
	
	public void startDraw(){
		mode = Mode.DRAW;
		prevMode = mode;
	}
	
	public void reset(){
		mode=Mode.NONE;
		currShape=ShapeType.none;
	}
	
	@Override
	public void drawOnCanvas(Canvas canvas) {
		if (shapes.size()>1){
			if (maskIndex==0) shapes.get(0).draw(canvas, saveScale);
			for(int i=1; i<shapes.size(); i++){
				shapes.get(i).draw(canvas,saveScale);
				if (i==maskIndex) shapes.get(0).draw(canvas,saveScale);
			}
		}
		else shapes.get(0).draw(canvas,saveScale);
	}
	
	
	@Override
	public void minScaleChanged() {
		for(int i=1; i<shapes.size(); i++){
			shapes.get(i).setScale(minScale);
		}
	}
	
	
	
	
	
	boolean haveSelectedAt(float x, float y){
		boolean res=false;
		int sw= -1;
    	int fs =-1;
    	int c = 0;
		shapeSelected=false;
    	for(int i = shapes.size()-1; i>=0; i--){
    		if (!shapeSelected) {
    			shapeSelected=shapes.get(i).contains(x,y);
    			if(shapeSelected){
    				res=(currShape!=ShapeType.CropRect && currShape!=ShapeType.Eraser) ;
    				sw=shapes.get(i).getStrokeWidth();
                	fs =shapes.get(i).getFontSize();
                	c = shapes.get(i).getColor();
                	currentId = shapes.get(i).getUid();
                	//selId = currentId;
    			}
    		}
    		else shapes.get(i).select(false);
    	}
    	if (res) 
    		if(doSelChange!=null) doSelChange.onSelectionChanged(sw,fs,c);
    	return res;
	}
	
	@Override
	public boolean onTouch(MotionEvent event) {
		boolean result = false;
		if(!canDraw){
			if(doSelChange!=null) doSelChange.onTouch(event,event.getAction() & MotionEvent.ACTION_MASK);
			//if((event.getAction() & MotionEvent.ACTION_MASK)==MotionEvent.ACTION_UP) canDraw=true;
			drag.set(-1,-1);
			return true;
		}
		boolean needRedraw = false;
		float x = event.getX() / saveScale + dRect.left - lShift + cropRect.left;
        float y = event.getY() / saveScale + dRect.top - tShift + cropRect.top;
		//new_col = Color.RED;
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            	canDrag = true;
            	if (event.getPointerCount()==1) pid=event.getPointerId(0);
            	drag.set(x,y);
                if (mode==Mode.DRAW){
                	result=true;
                	ptShape.set(x,y);
                	
                	{
                	doCreate= true;
                	switch(currShape){
        			case Text:
        				doCreate=false;
        				showTextPopup(this,"",stroke);
        				break;
        			default:
        				
        				break;
        			}
                	}
                }
            	
                else if (mode==Mode.EDIT){
                	touch.set(x, y);
                	result=haveSelectedAt(x, y);
                	needRedraw = true;
                	
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
            	if (mode==Mode.EDIT){
            		int ix = getSelectedShape();
            		if (ix!=-1) shapes.get(ix).setCompleted();
            	}
            	
            	if(saveScale == minScale){ //fix that
            		
            		startX=event.getX(0) / saveScale + dRect.left - lShift + cropRect.left;
            		startY=event.getY(0) / saveScale + dRect.top - tShift + cropRect.top;
            	}
            	break;
            case MotionEvent.ACTION_MOVE:
            		if(drag.x==-1 && drag.y==-1) return true;	
            		if (mode == Mode.EDIT) {
            			final int pointerIndex = event.findPointerIndex(pid);
            			if (pointerIndex!=-1){
            				try{
            					x = event.getX(pointerIndex) / saveScale + dRect.left - lShift + cropRect.left;
            					y = event.getY(pointerIndex) / saveScale + dRect.top - tShift + cropRect.top;
            				}
            				catch (Exception e) {
            					
            				}
            				
            			float deltaX = touch.x != 0f?touch.x - x:0;
            			float deltaY = touch.y != 0f?touch.y - y:0;
            			touch.set(x, y);
            			for (Shape shape : shapes) {
            				if (currentId==shape.getUid() && shape.getSelected()){
            					pushUndo(shape.getUid());
            					if(!zooming)shape.move(deltaX, deltaY);
            					else shape.scale(shapeScale, shapeScale);
            					//invalidate();
            					needRedraw = true;
            					result=true;
            					
            					break;
            				}
            			}
            		}
            		}
            		else if(mode == Mode.DRAW){
            			needRedraw = true;
            			//result=true;
            			boolean b=!doCreate;
            			if (doCreate && Math.abs(ptShape.x-x)+Math.abs(ptShape.y-y)>10){
            				doCreate=false;
            				b=true;
            				switch(currShape){
                			case FreeHand:
                				shapes.add(new FreeHand(ctx, ptShape.x, ptShape.y,new_col, (int)(wid_mode),minScale));
                				currentId = shapes.get(shapes.size() - 1).getUid();
                				break;
                			case Eraser:
                				shapes.add(new Eraser(ctx, ptShape.x, ptShape.y,new_col, (int)(wid_mode),minScale));
                				currentId = shapes.get(shapes.size() - 1).getUid();
                				break;
                			case Line:
                				shapes.add(new Line(ctx, ptShape.x,ptShape.y,new_col, (int)(wid_mode),minScale));
                				currentId = shapes.get(shapes.size() - 1).getUid();
                				break;
                			case Arrow:
                				shapes.add(new Arrow(ctx, ptShape.x,ptShape.y,new_col, (int)(wid_mode),defScale));
                				currentId = shapes.get(shapes.size() - 1).getUid();
                				//((Arrow)shapes.get(shapes.size() - 1)).setScale(minScale);
                				break;	
                			case Rectangle:
                				shapes.add(new Rectangle(ctx, ptShape.x,ptShape.y,new_col, (int)(wid_mode),minScale));
                				currentId = shapes.get(shapes.size() - 1).getUid();
                				break;	
                			case Circle:
                				shapes.add(new Circle(ctx, ptShape.x,ptShape.y,new_col, (int)(wid_mode),minScale));
                				currentId = shapes.get(shapes.size() - 1).getUid();
                				break;
                			case Oval:
                				shapes.add(new Oval(ctx, ptShape.x,ptShape.y,new_col, (int)(wid_mode),minScale));
                				currentId = shapes.get(shapes.size() - 1).getUid();
                				break;
                			case RoundRect:
                				shapes.add(new RoundRect(ctx, ptShape.x,ptShape.y,new_col, (int)(wid_mode),minScale));
                				currentId = shapes.get(shapes.size() - 1).getUid();
                				break;
                			case PixelMask:
                				maskIndex++;
                				shapes.add(maskIndex,new PixelMask(ptShape.x,ptShape.y,new_col, (int)(wid_mode)));
                				currentId = shapes.get(maskIndex).getUid();
                				break;
                			case CropRect:
                				((CropRect)shapes.get(0)).setStart(ptShape.x,ptShape.y);
                				currentId = shapes.get(0).getUid();
                				break; 
                			case MaskRect:
                				((MaskRect)shapes.get(maskIndex)).setStart(ptShape.x, ptShape.y);
                				currentId = shapes.get(maskIndex).getUid();
                				break;
                			default:
                				break;
                			}
            			}
            			
            			if (b && shapes.size()>0){
            			
            				switch(currShape){
                			case FreeHand:
                			case Eraser:
                			case Line:
                			case Arrow:
                			case Rectangle:
                			case Circle:
                			case Oval:
                			case RoundRect:
                				(shapes.get(shapes.size() - 1)).setEnd(x,y);
                				break;
                			case MaskRect:
                				(shapes.get(maskIndex)).setEnd(x,y);
                				break;	
                			case CropRect:
                				(shapes.get(0)).setEnd(x,y);
                				break;
                			case PixelMask:
                				(shapes.get(maskIndex)).setEnd(x,y);
                				break;	
            					default:
            					break;
                			}
            		}
            		}
                break;

            case MotionEvent.ACTION_UP:
            	doCreate=true;
            	touchCount=0;
            	if(mode == Mode.DRAW && shapes.size()>0 && (currShape==ShapeType.CropRect)){
            		mode = Mode.EDIT;
            		needRedraw = true;
            		result=true;
            	}
            	else
            	if(mode == Mode.DRAW && shapes.size()>0 && (currShape==ShapeType.PixelMask)){
            		try
            		{
            			
            			int l = ((PixelMask)shapes.get(maskIndex)).getLeft();
            			int t = ((PixelMask)shapes.get(maskIndex)).getTop();
            			//Log.e("nimbus",String.format("%d, %d",l,t));
               			int w=((PixelMask)shapes.get(maskIndex)).getWidth();
               			int h=((PixelMask)shapes.get(maskIndex)).getHeight();
               			int W = cropRect.right - cropRect.left;
               			int H = cropRect.bottom - cropRect.top;
            			if(l<0){
            				w+=l;
            				l=0;
            			}
            			if(t<0){
            				h+=t;
            				t=0;
            			}
            			if(l+w>W) w = W-l;
            			if(t+h>H) h=H-t;
            			
            			Bitmap b= Bitmap.createBitmap(background, l, t, w, h, null, true);
            			((PixelMask)shapes.get(maskIndex)).setBitmap(b, 0, 0);
            			pushUndo(shapes.get(maskIndex).getUid());
            			result=true;
            			
            		}
            		catch (Exception e){
            			//Log.e("nimbus",e.getMessage());
            		}
            		//mode = Mode.EDIT;
            		needRedraw = true;
            	}
            	else if(mode==Mode.DRAW && currShape==ShapeType.MaskRect){
            		((MaskRect)shapes.get(maskIndex)).complete();
            		pushUndo(shapes.get(maskIndex).getUid());
            		mode = Mode.EDIT;
            		needRedraw = true;
            		result=true;
            	}
            	else if(mode==Mode.DRAW){
            		shapes.get(shapes.size()-1).setCompleted();
            		//Log.v("nimbus", "ACTION_UP");
            		pushUndo(currentId);
            		result=true;
            	}
            	else if(mode==Mode.EDIT){
            		int ix = getSelectedShape();
            		if (ix!=-1) {
            			shapes.get(ix).setCompleted();
            			result=true;
            		}
            	}
                shapeScale = 1f;
                
                break;

            case MotionEvent.ACTION_POINTER_UP:
            	/*startX = x;
				startY = y;	*/
            	if(mode==Mode.EDIT && shapes.size()>1){
            		
            	}
            	canDrag = false;
                break;
        }
        
        if (needRedraw) {
        	saved=false;
        	forceRedraw();
        }
        
        return result; // indicate event was handled
	}
	
	public void showTextPopup(View view, String txt, boolean stroke) {
    	
        if(doSelChange!=null){
        	doSelChange.onTextChanged(txt, stroke);
        }
    }
	@Override
	public void doScaleBegin(){
		if (!shapeSelected && mode!=Mode.DRAW){
    		prevMode = mode;
    	}
		zooming = true;
	}
	
	@Override
	public void doScaleEnd(){
		mode = prevMode;
    	zooming = false;
    	if (saveScale==1f){
    		shiftX = 0;
    		shiftY = 0;
    		forceRedraw();
    	}
	}
	
	@Override
	public boolean onZoom (ScaleGestureDetector detector) {
		boolean result=false;
		float mScaleFactor = detector.getScaleFactor();
        if (shapeSelected) {
        	shapeScale*= mScaleFactor;
        	
        	zooming = true;
        	result = true;
        }
        else if (mode!=Mode.DRAW) {
        	zooming = true;
        }
        
        return result;
	}
	

	@Override
	public void onDoubleTaped(MotionEvent ev){
		
		float x = ev.getX() / saveScale + dRect.left - lShift + cropRect.left;
        float y = ev.getY() / saveScale + dRect.top - tShift + cropRect.top;
        if(mode==Mode.EDIT){
        	for(Shape shape:shapes){
    			if (currentId == shape.getUid() && shape.toString().contains("Text")){
    				if (shape.contains(x,y)){
    					showTextPopup(this, ((Text)shape).getText(),((Text)shape).getStroke());
   					}
    				else {
    					mode=Mode.EDIT;
    					currentId = shape.getUid();
    				}
   				break;
    			}
    		}
        }
	}
	
	private void writeLog(MotionEvent e){
		int c = e.getPointerCount();
		StringBuilder sb=new StringBuilder();
		sb.append(String.format("action: %d, x: %f  y: %f  ", e.getAction(), touch.x- e.getX(),touch.y-e.getY()));
	}
}