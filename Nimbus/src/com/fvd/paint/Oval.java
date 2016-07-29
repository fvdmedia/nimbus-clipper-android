package com.fvd.paint;

import android.content.Context;
import android.graphics.Path;
import android.graphics.RectF;

public class Oval extends Shape{
	private float startX ,startY;
	private float endX ,endY;
	
	public Oval(Context c,float x, float y, int color, int width, float sc){
		super(c, color, width,sc);
		startX = x;
		startY = y;
		endX = x;
		endY = y;
		rect = new RectF(x,y,x,y);
		path.addOval(rect,Path.Direction.CW);
	}
	
	@Override
	public void setEnd(float x, float y){
		endX = x;
		endY = y;
		path.reset();
		float dx = Math.abs(startX-endX);
		float dy = Math.abs(startY-endY);
		rect.left = startX - dx;
		rect.top = startY - dy;
		rect.right = startX + dx;
		rect.bottom = startY + dy;
		closeRect.set(rect.right,rect.top-20/iScale,rect.right+70/iScale,rect.top+50/iScale);
		path.reset();
		path.addOval(rect,Path.Direction.CW);
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
		select(rect.contains(x, y));
		return selected;
	}
	
	@Override
	public void move(float dx, float dy){
		super.move(dx, dy);
		startX -= dx;
		startY -= dy;
		endX -= dx;
		endY -= dy;
		
		float ddx = Math.abs(startX-endX);
		float ddy = Math.abs(startY-endY);
		rect.left = startX - ddx;
		rect.top = startY - ddy;
		rect.right = startX + ddx;
		rect.bottom = startY + ddy;
		closeRect.set(rect.right,rect.top-20/iScale,rect.right+70/iScale,rect.top+50/iScale);
		path.reset();
		path.addOval(rect,Path.Direction.CW);
	}
	
	@Override
	public void scale(float dx, float dy){
		if (scaleX==dx && scaleY==dy) return;
		super.scale(dx, dy);
		scaleX=dx;
		scaleY=dy;
		float dw = Math.abs((endX - startX)*dx*0.5f*scC);
		float dh = Math.abs((endY - startY)*dy*0.5f*scC);
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
		startX = rect.left+(rect.right-rect.left)/2f;
		endX = rect.right;
		startY= rect.top+(rect.bottom-rect.top)/2f;
		endY = rect.bottom;
		closeRect.set(rect.right,rect.top-20/iScale,rect.right+70/iScale,rect.top+50/iScale);
		path.reset();
		path.addOval(rect,Path.Direction.CW);
	}
	
	@Override
	public boolean undo(){
		boolean b=false;
		if(super.undo()){
			startX = rect.left+(rect.right-rect.left)/2f;
			endX = rect.right;
			startY= rect.top+(rect.bottom-rect.top)/2f;
			endY = rect.bottom;
			closeRect.set(rect.right,rect.top-20/iScale,rect.right+70/iScale,rect.top+50/iScale);
			path.reset();
			path.addOval(rect,Path.Direction.CW);
			b=true;
		}
		return b;
	}
}
