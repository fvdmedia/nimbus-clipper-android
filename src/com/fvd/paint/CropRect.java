package com.fvd.paint;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
//import android.util.Log;

public class CropRect extends Shape{
	private float startX ,startY;
	private float endX ,endY;
	private float ls, ts;
	private int W,H;
	private boolean complete=false;
	private Rect src=new Rect();
	private RectF dst=new RectF();
	private Rect rCrop=new Rect();
	//private RectF mask=new RectF(0,0,0,0);
	private int mode=0;
	private Paint fpaint; 
	Bitmap b=null;

	
	public CropRect(Bitmap bm){
		uid = System.currentTimeMillis();
		b=bm;
		fpaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		fpaint.setAntiAlias(true);
		fpaint.setColor(Color.argb(180, 35, 35, 35));
		fpaint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.WHITE);
		paint.setStyle(Paint.Style.STROKE);
		PathEffect effects = new DashPathEffect(new float[]{7,5},1);
		paint.setPathEffect(effects);
		paint.setStrokeWidth(3);
		visible=false;
		
	}
	
	@Override
	public void select(boolean b){
		if (b!=selected){
			selected = b;
		}
	}
	
	public void setCompete(){
		if(rect.left==0 && rect.right==0){
			visible=false;
			complete=true;
			return;
		}
		/*rect.left=Math.max(rect.left, mask.left);
		if (mask.right!=0) rect.right=Math.min(rect.right, mask.right);
		rect.top=Math.max(rect.top, mask.top);
		if (mask.bottom!=0) rect.bottom=Math.min(rect.bottom, mask.bottom);
		mask.set(rect);*/
		fpaint.setColor(Color.WHITE);
		complete=true;
	}
	
	public int getWidth(){
		return (int)Math.abs(endX - startX);
	}
	
	public int getHeight(){
		return (int)Math.abs(endY - startY);
	}
	
	public int getLeft(){
		return Math.min((int)startX,(int)endX) ;
		//return (int)rect.left;
	}
	
	public int getTop(){
		return Math.min((int)startY,(int)endY);
		//return (int)rect.top;
	}
	
	public void setStart(float x, float y){
		startX = x;
		startY = y;
		endX = x;
		endY = y;
		rect.set(startX, startY, endX , endY);
		
		complete=false;
	}
	
	@Override
	public void setEnd(float x, float y){
		endX = x;
		endY = y;
		rect.set(startX<endX?startX:endX,startY<endY?startY:endY,startX>endX?startX:endX ,startY>endY?startY:endY);
		visible=true;
		complete=false;
	}
	
	@Override
	public boolean contains(float x, float y){
		mode=0;
		if (!visible||complete) return false;
		select(rect.contains(x, y));
		if (!selected){
			closeRect.set(rect.left-50,rect.top-50,rect.left+50,rect.top+50);
			if(closeRect.contains(x,y)){
				selected=true;
				mode=1;
			}
		}
		if (!selected){
			closeRect.set(rect.right-50,rect.bottom-50,rect.right+50,rect.bottom+50);
			if(closeRect.contains(x,y)){
				selected=true;
				mode=2;
			}
		}
		return selected;
	}
	
	@Override
	public void move(float dx, float dy){
		switch (mode){
		case 1: 
				startX -= dx;
				startY -= dy;
			break;
		case 2:
				endX -= dx;
				endY -= dy;
			break;
		default:
				startX -= dx;
				startY -= dy;
				endX -= dx;
				endY -= dy;
			break;
		}
		
		rect.set(startX<endX?startX:endX,startY<endY?startY:endY,startX>endX?startX:endX ,startY>endY?startY:endY);
		startX=rect.left;
		startY = rect.top;
		endX = rect.right;
		endY = rect.bottom;
	}
	
	@Override
	public void draw(final Canvas canvas, float scale) {
		if(!visible) return;
		
		if (!complete) {
			fpaint.setColor(Color.argb(180, 35, 35, 35));
			if(rect.right-rect.left>0){
				paint.setStrokeWidth(3/scale);
				path.reset();
				path.addRect(rCrop.left,rCrop.top,rCrop.right,rCrop.bottom,Path.Direction.CW);
				path.setFillType(Path.FillType.WINDING);
				path.addRect(rect,Path.Direction.CW);
				path.setFillType(Path.FillType.EVEN_ODD);
				path.close();
				canvas.drawPath(path, fpaint);
				
				src.set(0,0,b.getWidth(),b.getHeight());
				dst.set(rect.left-((b.getWidth()/2)/scale), rect.top-((b.getHeight()/2)/scale),rect.left+((b.getWidth()/2)/scale), rect.top+((b.getHeight()/2)/scale));
				canvas.drawBitmap(b,src,dst,paint);
				dst.set(rect.right-((b.getWidth()/2)/scale), rect.bottom-((b.getHeight()/2)/scale),rect.right+((b.getWidth()/2)/scale), rect.bottom+((b.getHeight()/2)/scale));
				canvas.drawBitmap(b,src,dst,paint);
				canvas.drawRect(rect, paint);
				
			}
			else {
				//src.set(0,0,W,H);
				canvas.drawRect(rCrop, fpaint);
			}
		}
	}
	
	public boolean isComplete(){
		return complete;
	}
	
	/*public void reset(int w, int h){
		complete=false;
		visible = true;
		startX = 0;
		startY = 0;
		endX = 0;
		endY = 0;
		W=w;
		H=h;
		rect.set(0,0,0,0);
		
		fpaint.setColor(Color.argb(180, 35, 35, 35));
	}*/
	
	public void reset(Rect r){
		complete=false;
		visible = true;
		startX = r.left;
		startY = r.top;
		endX = r.right;
		endY = r.bottom;
		W=r.right-r.left;
		H=r.bottom-r.top;
		rect.set(0,0,0,0);
		rCrop.set(r);
		fpaint.setColor(Color.argb(180, 35, 35, 35));
	}
	
	public void hide(){
		/*if(mask.right-mask.left==0)	visible=false;
		rect.set(mask);*/
		visible=false;
		fpaint.setColor(Color.WHITE);
		complete=true;
	}
	
	@Override
	public boolean undo(){
		visible=false;
		return true;
	}
}
