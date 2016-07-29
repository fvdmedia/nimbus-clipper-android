package com.fvd.paint;

import android.graphics.RectF;

public class undoItem {
	private int color;
	private float width;
	private RectF rect,params;
	
	
	public undoItem(int c, float w, RectF r){
		color=c;
		width=w;
		rect=new RectF(r);
		params=new RectF(0,0,0,0);

	}
	
	public undoItem(int c, float w, RectF r, RectF r2){
		color=c;
		width=w;
		rect=new RectF(r);
		params=new RectF(r2);
	}
	
	public int getColor(){
		return color;
	}
	
	public float getWidth(){
		return width;
	}
	
	public float getLeft(){
		return rect.left;
	}
	
	public float getTop(){
		return rect.top;
	}
	
	public float getRight(){
		return rect.right;
	}
	
	public float getBottom(){
		return rect.bottom;
	}
	
	public RectF getRect(){
		return rect;
	}
	
	public RectF getParams(){
		return params;
	}
	
}
