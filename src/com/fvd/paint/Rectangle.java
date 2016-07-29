package com.fvd.paint;

import android.content.Context;
import android.graphics.Path;
import android.graphics.RectF;


public class Rectangle extends Shape{
	private float startX ,startY;
	private float endX ,endY;

	public Rectangle(Context c,float x, float y, int color, int width, float sc){
		super(c, color, width,sc);
		startX = x;
		startY = y;
		endX = x;
		endY = y;
		rect.set(startX, startY, endX , endY);
		path.addRect(startX ,startY,endX ,endY,Path.Direction.CW);
	}
	
	@Override
	public void setEnd(float x, float y){
		endX = x;
		endY = y;
		rect.set(startX<endX?startX:endX,startY<endY?startY:endY,startX>endX?startX:endX ,startY>endY?startY:endY);
		closeRect.set(rect.right,rect.top-20/iScale,rect.right+70/iScale,rect.top+50/iScale);
		path.reset();
		path.addRect(startX ,startY,endX ,endY,Path.Direction.CW);
	}
	
	@Override
	public boolean contains(float x, float y){
		if (!visible) return false;
		if (selected) {
			if(inCloseRect(x,y)) {
				visible=false;
				return false;
			}
		}
		float d=20f/iScale;
		boolean b = false;
		RectF R=new RectF();
		R.set(rect.left-d, rect.top-d,rect.left+d, rect.bottom+d);
		b=R.contains(x, y);
		if(!b){
			R.set(rect.left-d, rect.top-d,rect.right+d, rect.top+d);
			b=R.contains(x, y);
		}
		
		if(!b){
			R.set(rect.right-d, rect.top-d,rect.right+d, rect.bottom+d);
			b=R.contains(x, y);
		}
		
		if(!b){
			R.set(rect.left-d, rect.bottom-d,rect.right+d, rect.bottom+d);
			b=R.contains(x, y);
		}
		
		select(b);		
		
		return selected;
	}
	
	@Override
	public void move(float dx, float dy){
		super.move(dx, dy);
		startX -= dx;
		startY -= dy;
		endX -= dx;
		endY -= dy;
		rect.set(startX<endX?startX:endX,startY<endY?startY:endY,startX>endX?startX:endX ,startY>endY?startY:endY);
		closeRect.set(rect.right,rect.top-20/iScale,rect.right+70/iScale,rect.top+50/iScale);
		startX=rect.left;
		startY = rect.top;
		endX = rect.right;
		endY = rect.bottom; 
		path.reset();
		path.addRect(startX ,startY,endX ,endY,Path.Direction.CW);
	}
	
	@Override
	public void scale(float dx, float dy){
		if (scaleX==dx && scaleY==dy) return;
		super.scale(dx, dy);
		scaleX=dx;
		scaleY=dy;
		float dw = Math.abs((rect.right - rect.left)*dx*0.5f*scC);
		float dh = Math.abs((rect.bottom - rect.top)*dy*0.5f*scC);
		startX=rect.left;
		startY = rect.top;
		endX = rect.right;
		endY = rect.bottom; 
		if (dx>1f){
			startX -= dw;
			endX += dw;
		}
		else {
			startX += dw;
			endX -= dw;
		}
		if (dy>1f){
			startY -= dh;
			endY += dh;
		}
		else {
			startY += dh;
			endY -= dh;
		}
		
		rect.set(startX<endX?startX:endX,startY<endY?startY:endY,startX>endX?startX:endX ,startY>endY?startY:endY);
		closeRect.set(rect.right,rect.top-20/iScale,rect.right+70/iScale,rect.top+50/iScale);
		path.reset();
		path.addRect(startX ,startY,endX ,endY,Path.Direction.CW);
	}
	
	@Override
	public boolean undo(){
		boolean b=false;
		if(super.undo()){
			startX=rect.left;
			startY = rect.top;
			endX = rect.right;
			endY = rect.bottom;
			rect.set(startX<endX?startX:endX,startY<endY?startY:endY,startX>endX?startX:endX ,startY>endY?startY:endY);
			closeRect.set(rect.right,rect.top-20/iScale,rect.right+70/iScale,rect.top+50/iScale);
			path.reset();
			path.addRect(startX ,startY,endX ,endY,Path.Direction.CW);
			b=true;
		}
		return b;
	}
	
}
