package com.fvd.paint;

import java.util.ArrayList;
import java.util.List;

import android.R;
//import com.fvd.nimbus.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
//import android.util.Log;

public class Shape {
	protected final int max_stack=3;
	protected Paint paint = null;
	protected Paint selPaint = null;
	protected int _color = Color.RED; 
	protected boolean selected = false;
	protected int _width=1;
	protected int _progress=1;
	protected float scaleX = 1f;
	protected float scaleY = 1f;
	protected final float  scC = 0.025f;
	protected Path path=new Path();
	protected RectF rect = new RectF();
	protected Rect closeSrc = new Rect();
	//protected Rect closeDst = new Rect();
	protected RectF params = new RectF();
	protected RectF closeRect = new RectF();
	protected Context ctx;
	protected boolean visible=true;
	public static Bitmap iconClose;
	protected boolean completed =false;
	protected List<undoItem> undoStack;
	protected long uid;
	protected float p1,p2;
	protected boolean saveOnchange=false;
	//protected float[] scale= new float[9];
	protected float iScale = 1f;
	
	
	public Shape(){
		uid = System.currentTimeMillis();
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setAntiAlias(true);
		paint.setColor(_color);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(1);
		
		selPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		selPaint.setAntiAlias(true);
		selPaint.setColor(Color.MAGENTA);
		selPaint.setStyle(Paint.Style.STROKE);
		selPaint.setStrokeWidth(12);
		_progress = 20;
		_width = (20/10+1)*5;
	}
	
	public Shape(Context c, int color, int width, float sc){
		ctx = c;
		uid = System.currentTimeMillis();
		iconClose = BitmapFactory.decodeResource(c.getResources(),R.drawable.ic_delete);
		closeSrc.set(0,0,iconClose.getWidth(),iconClose.getHeight());
		iScale = sc;
		_color = color;
		//_width=width;	
		_progress = width;
		_width = (width/10+1)*5;
		
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		paint.setColor(_color);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(width/iScale);
				
		selPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		selPaint.setAntiAlias(true);
		selPaint.setColor(Color.MAGENTA);
		selPaint.setStyle(Paint.Style.STROKE);
		selPaint.setStrokeWidth((width+6)/iScale);
		undoStack=new ArrayList<undoItem>();
	}
	
	public boolean contains(float x, float y) {
		return false;
	}
	
	public void setEnd(float x, float y){
		
	}
	
	public void setColor(int _c){
		pushState();
		_color = _c;
		paint.setColor(_c);
	}
	
	public void select(boolean b){
		if (b!=selected){
			selected = b;
		}
	}
	
	public void updateRect(Rect cr, float l, float t ){
		
	}
	
	public boolean getSelected(){
		return selected;
	}
	
	public void draw(final Canvas canvas, float scale) {
		if (visible){
			
			if (selected) {
				canvas.drawPath(path, selPaint);
				closeRect.set(closeRect.left,closeRect.top,closeRect.left+iconClose.getWidth()/scale,closeRect.top+iconClose.getHeight()/scale);
				canvas.drawBitmap(iconClose, closeSrc,closeRect, paint);
			}
			canvas.drawPath(path, paint);
		}
	}
	
	public void move(float dx, float dy){
		if (saveOnchange) {
			pushState();
			saveOnchange = false;
		}
		completed = false;
	}
	
	public void scale(float dx, float dy){
		if (saveOnchange) {
			pushState();
			saveOnchange = false;
		}
		completed = false;
	}
	
	public int getStrokeWidth(){
		return _progress;
	}
	
	public int getColor(){
		return paint.getColor();
	}
	
	public int getFontSize(){
		return -1;
	}
	
	public void setLineWidth(int w){
		pushState();
		_progress=w;
		_width = (w/10+1)*5;
		paint.setStrokeWidth(_width/iScale);
		selPaint.setStrokeWidth((_width+6)/iScale);
	}
	
	public void setScale(float sc) {
		iScale = sc;
		paint.setStrokeWidth(_width/iScale);
		selPaint.setStrokeWidth((_width+6)/iScale);
	}
	
	
	public void setCompleted(){
		completed=true;
		saveOnchange=true;
	}
	
	public void pushState(){
		if (undoStack==null) return;
		if (undoStack.size()>max_stack) undoStack.remove(0);
		undoStack.add(new undoItem(paint.getColor(), paint.getStrokeWidth(), rect));
	}
	
	public boolean undo(){
		boolean b=false;
			if(undoStack!=null){
				if(undoStack.size()>0){
					undoItem item=undoStack.get(undoStack.size()-1);
					paint.setColor(item.getColor());
					paint.setStrokeWidth(item.getWidth());
					rect = item.getRect();
					params = item.getParams();
					undoStack.remove(undoStack.size()-1);
					if(!visible) visible=true;
					b=true;
				}
			} else b=true;
			return b;
	}
	
	public long getUid(){
		return uid;
	}
	
	public boolean canSave(){
		return saveOnchange;
	}
	
	public void clearStack(){
		if(undoStack!=null)	undoStack.clear();
		
	}
	
	public boolean inCloseRect(float x, float y){
		final float  d=30/iScale;
		return x>Math.min(closeRect.left, closeRect.right)-d && x<Math.max(closeRect.left, closeRect.right)+d && y>Math.min(closeRect.top, closeRect.bottom)-d && y<Math.max(closeRect.top, closeRect.bottom)+d;         
	}
	float angle=0;
	public void rotate(float a, float px, float py) {
		angle+=a;
		Matrix m = new Matrix();
		m.setRotate(angle,px,px);
		path.transform(m);
	}
}
