package com.fvd.paint;

import android.content.Context;
import android.graphics.Path;
import android.graphics.RectF;

public class Circle extends Shape{
	private float startX ,startY;
	private float endX ,endY;
	
	public Circle(){
		super();
	}
	public Circle(float x, float y){
		super();
		startX = x;
		startY = y;
		endX = x;
		endY = y;
		rect.set(startX,startY,endX ,endY);
		path.addCircle(startX ,startY,1,Path.Direction.CW);
	}
	
	public Circle(Context c,float x, float y, int color, int width, float sc){
		super(c, color, width,sc);
		startX = x;
		startY = y;
		endX = x;
		endY = y;
		rect.set(startX,startY,endX ,endY);
		path.addCircle(startX ,startY,1,Path.Direction.CW);
	}
	
	@Override
	public void setEnd(float x, float y){
		endX = x;
		endY = y;
		float rad = (float) Math.hypot((endX-startX),(endY-startY));
		rect.set(startX-rad,startY-rad,startX+rad,startY+rad);
		
		closeRect.set(rect.left+(int)((rect.right-rect.left)*0.85),rect.top-20/iScale,rect.right+70/iScale,rect.top+50/iScale);
		path.reset();
		path.addCircle(startX ,startY, rad ,Path.Direction.CW);
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

		float rad = (float) Math.hypot((endX-startX),(endY-startY));
		float d = (float) Math.hypot((x-startX),(y-startY));
		select(Math.abs(d-rad)<40/iScale);
	
		return selected;
	}
	
	@Override
	public void move(float dx, float dy){
		super.move(dx, dy);
		startX -= dx;
		startY -= dy;
		endX -= dx;
		endY -= dy;
		float rad = (float) Math.hypot((endX-startX),(endY-startY));
		rect.set(startX-rad,startY-rad,startX+rad,startY+rad);
		
		closeRect.set(rect.left+(int)((rect.right-rect.left)*0.85),rect.top-20/iScale,rect.right+70/iScale,rect.top+50/iScale);
		path.reset();
		path.addCircle(startX ,startY,rad,Path.Direction.CW);
	}
	
	@Override
	public void scale(float dx, float dy){
		super.scale(dx, dy);
		float dw = Math.abs((rect.right - rect.left)*dx*0.5f*scC);
		float dh = Math.abs((rect.bottom - rect.top)*dy*0.5f*scC);
		if (dx>1f){
			rect.left -= dw;
			rect.right += dw;
		}
		else {
			rect.left += dw;
			rect.right -= dw;
		}
		if (dy>1f){
			rect.top -= dh;
			rect.bottom += dh;
		}
		else {
			rect.top += dh;
			rect.bottom -= dh;
		}
		endX = rect.left + (rect.right-rect.left)/2f;
		endY = rect.bottom;
		
		closeRect.set(rect.left+(int)((rect.right-rect.left)*0.85),rect.top-20/iScale,rect.right+70/iScale,rect.top+50/iScale);
		path.reset();
		path.addCircle(startX ,startY,(rect.right-rect.left)/2f,Path.Direction.CW);
		
	}
	
	@Override
	public void pushState(){
		if (undoStack==null) return;
		if (undoStack.size()>max_stack) undoStack.remove(0);
		undoStack.add(new undoItem(paint.getColor(), paint.getStrokeWidth(), new RectF(startX,startY,endX,endY)));
	}
	
	@Override
	public boolean undo(){
		boolean b=false;
		if(super.undo()){
			float rad = (float) Math.hypot((endX-startX),(endY-startY));
			startX=rect.left;
			startY = rect.top;
			endX = rect.right;
			endY = rect.bottom;
			rect.set(startX-rad,startY-rad,startX+rad,startY+rad);
			
			closeRect.set(rect.left+(int)((rect.right-rect.left)*0.85),rect.top-20/iScale,rect.right+70/iScale,rect.top+50/iScale);
			path.reset();
			path.addCircle(startX ,startY,rad,Path.Direction.CW);
			b=true;
		}
		return b;
	}
}
