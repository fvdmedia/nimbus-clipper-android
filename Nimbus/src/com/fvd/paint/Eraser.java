package com.fvd.paint;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Eraser extends Shape {
	private float offX =0;
	private float offY =0;
	
	public Eraser(){
		super();
		
	}
	
	public Eraser(Context c,int color, int width){
		super(c,Color.WHITE, width,1f);
		
	}
	
	float xx=0;
	float yy=0;
	
	public Eraser(Context c,float x, float y, int color, int width, float sc){
		super(c,Color.WHITE, width,sc);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		
		rect.set(x, y, x, y);
		path.moveTo(x ,y);
		xx=x;
		yy=y;

	}
	
	
	@Override
	public void setEnd(float x, float y){
		
		if(x<rect.left) rect.left = x;
			else if(x>rect.right) rect.right = x;
		if(y<rect.top) rect.top = y; 
			else if(y>rect.bottom) rect.bottom=y; 
		closeRect.set(rect.right,rect.top-20/iScale,rect.right+70/iScale,rect.top+50/iScale);
		path.quadTo(xx, yy, (x + xx) / 2, (y + yy) / 2);
		
		xx=x;
		yy=y;
		
	}
	
	@Override
	public boolean contains(float x, float y){
		/*if (!visible) return false;
		if (selected) {
			if(inCloseRect(x,y)) {
				visible=false;
				return false;
			}
		}
		select(rect.contains(x, y));
		return selected;*/
		return false;
	}
	
	@Override
	public void move(float dx, float dy){
		super.move(dx, dy);
		float startX = rect.left - dx;
		float startY = rect.top - dy;
		float endX = rect.right - dx;
		float endY = rect.bottom - dy;
		rect.set(startX<endX?startX:endX,startY<endY?startY:endY,startX>endX?startX:endX ,startY>endY?startY:endY);
		closeRect.set(rect.right,rect.top-20/iScale,rect.right+70/iScale,rect.top+50/iScale);
		offX=+dx;
		offY=+dy;
		path.offset(-dx, -dy);
		
	}
	
	@Override
	public void pushState(){
		if (undoStack==null) return;
		if (undoStack.size()>max_stack) undoStack.remove(0);
		undoStack.add(new undoItem(paint.getColor(), paint.getStrokeWidth(), rect,new RectF(offX,offY,0,0)));
	}
	
	@Override
	public boolean undo(){
		float l=rect.left;
		float t =rect.top;
		boolean b=false;
		if(super.undo()){
			closeRect.set(rect.right,rect.top-20/iScale,rect.right+70/iScale,rect.top+50/iScale);
			offX=params.left;
			offY=params.top;
			path.offset(rect.left-l, rect.top-t);
			b=true;
		}
		return b;
	}

}
