package com.fvd.paint;

import android.content.Context;
import android.graphics.RectF;
import android.graphics.Paint;

public class Line extends Shape{
	private float startX ,startY;
	private float endX ,endY;
	
	public Line(Context c,float x, float y, int color, int width,float sc){
		super(c, color, width,sc);
		iScale = sc;
		startX = x;
		startY = y;
		endX = x;
		endY = y;
		
		paint.setStrokeWidth(width/sc);
		selPaint.setStrokeWidth((width+6)/sc);
		rect.set(startX,startY,endX ,endY);
		path.moveTo(startX ,startY);
		path.lineTo(endX ,endY);
	}
	
	@Override
	public void setEnd(float x, float y){
		endX = x;
		endY = y;
		rect.set(startX<endX?startX:endX,startY<endY?startY:endY,startX>endX?startX:endX ,startY>endY?startY:endY);
		
		setCloseRect();
		path.reset();
		path.moveTo(startX ,startY);
		path.lineTo(endX ,endY);
		
	}
	
	void setCloseRect(){
		if(startX<endX){
			closeRect.set(endX-75/iScale, endY-(startY>endY?50:-0)/iScale,endX-25 ,endY/*+50/iScale*/);
		}
		else{
			closeRect.set(endX+25/iScale, endY-(startY>endY?50:-0)/iScale,endX+75/iScale ,endY+50/iScale);
		}
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
		float xln = (float) Math.hypot((x-startX),(y-startY));
		xln += (float) Math.hypot((x-endX),(y-endY));
		float ln = (float) Math.hypot((endX-startX),(endY-startY));
		select(xln>ln-20 && xln<ln+20);
		
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
		
		setCloseRect();
		path.reset();
		path.moveTo(startX ,startY);
		path.lineTo(endX ,endY);
	}
	
	@Override
	public void scale(float dx, float dy){
		if (scaleX==dx && scaleY==dy) return;
		super.scale(dx, dy);
		scaleX=dx;
		scaleY=dy;
		float dw = Math.abs((rect.right - rect.left)*dx*0.5f*scC);
		float dh = Math.abs((rect.bottom - rect.top)*dy*0.5f*scC);
		if (dx>1f){
			if (startX<=endX){
				startX -= dw;
				endX += dw;
			}
			else {
				startX += dw;
				endX -= dw;
			}
		}
		else {
			if (startX<=endX){
				startX += dw;
				endX -= dw;
			}
			else {
				startX -= dw;
				endX += dw;
			}
		}
		if (dy>1f){
			if(startY<=endY){
				startY -= dh;
				endY += dh;
			}
			else {
				startY += dh;
				endY -= dh;
			}
		}
		else {
			if(startY<=endY){
				startY += dh;
				endY -= dh;
			}
			else {
				startY -= dh;
				endY += dh;
			}
		}
		
		rect.set(startX<endX?startX:endX,startY<endY?startY:endY,startX>endX?startX:endX ,startY>endY?startY:endY);
		
		setCloseRect();
		path.reset();
		path.moveTo(startX ,startY);
		path.lineTo(endX ,endY);
	}
	
	@Override
	public void pushState(){
		if (undoStack==null) return;
		if (undoStack.size()>max_stack) undoStack.remove(0);
		undoStack.add(new undoItem(paint.getColor(), paint.getStrokeWidth(), new RectF(startX, startY,endX,endY)));
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
			setCloseRect();
			path.reset();
			path.moveTo(startX ,startY);
			path.lineTo(endX ,endY);
			b=true;
		}
		return b;
	}
}	
